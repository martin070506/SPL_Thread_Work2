package memory;

import parser.OutputWriter;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        /// store vector data and its orientation

        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        /// return element at index (read-locked)

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

        readLock();
        try {
            return vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        /// return vector orientation

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

        writeLock();
        if (orientation == VectorOrientation.ROW_MAJOR)
            orientation = VectorOrientation.COLUMN_MAJOR;
        else
            orientation = VectorOrientation.ROW_MAJOR;
        writeUnlock();
    }

    public void add(SharedVector other) {
        /// add two vectors

        int thisOBJ = System.identityHashCode(this);
        int matrixOBJ = System.identityHashCode(other);
        // TODO: Delete Comment
        if (thisOBJ <= matrixOBJ) { // THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
            this.readLock();
            other.readLock();
        } else {
            other.readLock();
            this.readLock();
        }
        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("Vector Lengths don't match");
            if (this.orientation != other.orientation)
                throw new IllegalArgumentException("Vectors Orientation don't match");

            for (int i = 0; i < vector.length; i++)
                vector[i] += other.vector[i];
        } finally {
            // TODO: Delete Comment
            if (thisOBJ <= matrixOBJ) { // THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
                other.readLock();
                this.readLock();
            }
            else {
                this.readLock();
                other.readLock();
            }
        }
    }

    public void negate()
    {
        /// negate vector

        writeLock();
        for (int i = 0; i < vector.length; i++)
            vector[i] *= -1;
        writeUnlock();
    }

    public double dot(SharedVector other) {
        int thisOBJ = System.identityHashCode(this);
        int otherOBJ = System.identityHashCode(other);

        if (thisOBJ <= otherOBJ) {
            this.readLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.readLock();
        }

        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("Vectors Length Mismatch");
            if (this.orientation == other.orientation)
                throw new IllegalArgumentException("Vectors Orientation Mismatch");

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
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


    public void vecMatMul(SharedMatrix matrix) {
        /// compute row-vector × matrix

        this.writeLock();
        acquireAllVectorReadLocks(matrix);
        // TODO: Delete Comment
        // IMPORTANT
        // in this method we assume we get a vector by columns, in the LAE class before sending to here, we will LoadColumnMajor
        try {
            // TODO: Delete Comment
            // HERE WE Split into cases where we are given row matrix(we will load column) or a already given a column matrix

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
