package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    private static final Object tieLock = new Object();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        /// store vector data and its orientation

        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        /// return element at index (read-locked)

        // We ReadLock because we're only reading an index of the vector here.
        // So if another thread wants to read , it is allowed.
        readLock();

        try {
            if (index >= vector.length)
                throw new IndexOutOfBoundsException("Index out of bounds: " + index);

            return vector[index];
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException(e.getMessage());
        } finally {
            readUnlock();
        }
    }

    public int length() {
        /// return vector length

        // We ReadLock because we're only getting the length of the vector here.
        // So if another thread wants to read , it is allowed.
        readLock();

        try {
            return vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        /// return vector orientation

        // We ReadLock because we're only getting the orientation of the vector here.
        // So if another thread wants to read , it is allowed.
        readLock();

        try {
            return orientation;
        } finally {
            readUnlock();
        }
    }

    public void writeLock() {
        /// acquire write lock

        lock.writeLock().lock();
    }

    public void writeUnlock() {
        /// release write lock

        lock.writeLock().unlock();
    }

    public void readLock() {
        /// acquire read lock

        lock.readLock().lock();
    }

    public void readUnlock() {
        /// release read lock

        lock.readLock().unlock();
    }

    public void transpose() {
        /// transpose vector

        // We WriteLock because we EDIT (transpose) data here, so we do not want another thread to
        // either read while we are changing things, so that other thread gets incorrect / corrupt
        // data, and we also don't want anyone else writing while we are writing,
        // and overwriting what we wrote
        writeLock();

        if (orientation == VectorOrientation.ROW_MAJOR)
            orientation = VectorOrientation.COLUMN_MAJOR;
        else
            orientation = VectorOrientation.ROW_MAJOR;
        writeUnlock();
    }

    public void add(SharedVector other) {
        /// add two vectors

        // In here the locking mechanism locks 2 objects.
        // The matrix, we always lock / unlock in the same order, so no deadlock occurs.
        // In the case of an extreme case (- identity hashcode is equal) we added a synchronized
        // Block so there's a tieBreaker.

        // We use a WriteLock on this, because he is the one we edit (write to)
        // And ReadLock on other because we only read from him.
        // Same explanation as all other paragraphs.

        int thisOBJ = System.identityHashCode(this);
        int matrixOBJ = System.identityHashCode(other);
        boolean tieFlag = false;

        if (thisOBJ < matrixOBJ) {
            this.writeLock();
            other.readLock();
        } else if (thisOBJ > matrixOBJ) {
            other.readLock();
            this.writeLock();
        } else
            tieFlag = true;

        try {
            if (tieFlag)
                synchronized (tieLock) {
                    this.writeLock();
                    other.readLock();

                    performAdd(other);
                }
            else
                performAdd(other);
        } finally {
            if (thisOBJ <= matrixOBJ) {
                other.readUnlock();
                this.writeUnlock();
            } else {
                this.writeUnlock();
                other.readUnlock();
            }
        }
    }

    private void performAdd(SharedVector other) {
        if (this.vector.length != other.vector.length)
            throw new IllegalArgumentException("Vector Lengths don't match");
        if (this.orientation != other.orientation)
            throw new IllegalArgumentException("Vectors Orientation don't match");

        for (int i = 0; i < vector.length; i++)
            vector[i] += other.vector[i];
    }

    public void negate()
    {
        /// negate vector

        // WriteLock, so no one overwrites the thread while its working / reads incorrect / invalid
        // / corrupt data while another thread is writing this data.
        writeLock();

        for (int i = 0; i < vector.length; i++)
            vector[i] *= -1;
        writeUnlock();
    }

    public double dot(SharedVector other) {

        // Again, as said - we handle the equalness case (IHC equalness case) and lock / unlock
        // In same order so no deadlock happens.
        // In here we only use read locks because we do not edit out object, but only read from it.

        int thisOBJ = System.identityHashCode(this);
        int otherOBJ = System.identityHashCode(other);
        boolean tieFlag = false;

        if (thisOBJ < otherOBJ) {
            this.readLock();
            other.readLock();
        }
        else if (thisOBJ > otherOBJ) {
            other.readLock();
            this.readLock();
        } else
            tieFlag = true;

        try {
            double sum;
            if (tieFlag)
                synchronized (tieLock) {
                    this.readLock();
                    other.readLock();
                    sum = performDot(other);
                }
            else
                sum = performDot(other);

            return sum;

        } finally {
            if (thisOBJ <= otherOBJ) {
                other.readUnlock();
                this.readUnlock();
            } else {
                this.readUnlock();
                other.readUnlock();
            }
        }
    }

    private double performDot(SharedVector other) {
        if (this.vector.length != other.vector.length)
            throw new IllegalArgumentException("Vectors Length Mismatch");
        if (this.orientation == other.orientation)
            throw new IllegalArgumentException("Vectors Orientation Mismatch");

        double sum = 0;
        for (int i = 0; i < vector.length; i++)
            sum += vector[i] * other.vector[i];

        return sum;
    }


    public void vecMatMul(SharedMatrix matrix) {
        /// compute row-vector × matrix

        // In here we locked vector write, so no one corrupts it, because we edit the object itself,
        // And also read from it, so we don't want any other threads accessing while its being
        // Worked on, and we lock matrix to reading, because we only read from it so other threads
        // Can read from the same matrix.

        // e.g. when we multiply matrices, we split it into vecMatMul tasks so the right matrix can
        // Be read by everyone, but only 1 thread accesses each left side vector at once.

        this.writeLock();
        acquireAllVectorReadLocks(matrix);

        try {
            if (orientation != VectorOrientation.ROW_MAJOR)
                throw new IllegalArgumentException("Vector Orientation Should Be Row Major");
            if (matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR)
                throw new IllegalArgumentException("Matrix Should Be Column Major");
            if (this.length() != matrix.get(0).length())
                throw new IllegalArgumentException("Matrix Length Mismatch");

            double[] newVector = new double[matrix.length()];
            for (int i = 0; i < matrix.length(); i++)
                newVector[i] = UnsafeDot(matrix.get(i));

            vector = newVector;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            releaseAllVectorReadLocks(matrix);
            this.writeUnlock();
        }
    }

    private void acquireAllVectorReadLocks(SharedMatrix matrix) {
        for (int i = 0; i < matrix.length(); i++)
            matrix.get(i).readLock();
    }

    private void releaseAllVectorReadLocks(SharedMatrix matrix) {
        for (int i = 0; i < matrix.length(); i++)
            matrix.get(i).readUnlock();
    }

    private double UnsafeDot(SharedVector other){
        if (this.vector.length != other.vector.length)
            throw new IllegalArgumentException("Vectors Length Mismatch");
        if (this.orientation == other.orientation)
            throw new IllegalArgumentException("Right Vector Should Be Column-Major");

        double sum = 0;
        for (int i = 0; i < vector.length; i++)
            sum += vector[i] * other.vector[i];

        return sum;
    }
}
