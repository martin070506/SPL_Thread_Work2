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

    public double get(int index) throws Exception {
        /// return element at index (read-locked)

        readLock();
        try {
            if (index >= vector.length)
            {
                OutputWriter.write("Index Out Of Bound", "out.json");
                throw new IndexOutOfBoundsException("Index Out Of Bound");
            }

            return vector[index];
        }
        finally {
            readUnlock();
        }
    }

    public int length() {
        /// return vector length

        readLock();
        try{
            return vector.length;
        }
        finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        /// return vector orientation

        readLock();
        try{
            return orientation;
        }
        finally {
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
        try {
            if (orientation == VectorOrientation.ROW_MAJOR)
                    orientation = VectorOrientation.COLUMN_MAJOR;
            else
                orientation = VectorOrientation.ROW_MAJOR;
        }
        finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        /// add two vectors

        int thisOBJ = System.identityHashCode(this);
        int matrixOBJ = System.identityHashCode(other);
        if (thisOBJ <= matrixOBJ) {///THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
            this.readLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.readLock();
        }
        try {
            if (this.vector.length != other.vector.length)
                OutputWriter.write("Vectors Length Dismatch", "out.json");
            if (this.orientation != other.orientation)
                OutputWriter.write("Vectors Orientation Dismatch", "out.json");

            for (int i = 0; i < vector.length; i++)
                vector[i] += other.vector[i];
        } catch (IOException ignored) {}

        finally {
            if (thisOBJ <= matrixOBJ) {///THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
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
        try {
            for (int i = 0; i < vector.length; i++)
                vector[i] *= -1;
        }
        finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        /// compute dot product (row · column)

        int thisOBJ = System.identityHashCode(this);
        int matrixOBJ = System.identityHashCode(other);
        if (thisOBJ <= matrixOBJ) {///THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
            this.readLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.readLock();
        }

        try {
            if (this.vector.length != other.vector.length)
                OutputWriter.write("Vectors Length Dismatch", "out.json");
            if (this.orientation == other.orientation)
                OutputWriter.write("Vectors Orientation Dismatch", "out.json");

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;
        } catch (IOException ignored) {}
        finally { // THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
            if (thisOBJ <= matrixOBJ) {
                other.readUnlock();
                this.readUnlock();
            }
            else {
                this.readUnlock();
                other.readUnlock();
            }
        }
        return 0;
    }

    public void vecMatMul(SharedMatrix matrix) {
        /// compute row-vector × matrix

        int thisOBJ = System.identityHashCode(this);
        int matrixOBJ = System.identityHashCode(matrix);
        if (thisOBJ <= matrixOBJ) {
            this.writeLock();
            acquireAllVectorReadLocks(matrix);
        }
        else {
            acquireAllVectorReadLocks(matrix);
            this.writeLock();
        }

        try {
            if (orientation != VectorOrientation.ROW_MAJOR)
                OutputWriter.write("Vector Orientation Dismatch", "out.json");
            if (matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR)
                OutputWriter.write("Matrix Orientation Dismatch", "out.json");


            double[] newVector = new double[matrix.length()];

            for (int i = 0; i < matrix.length(); i++) {
                double sum = 0;
                sum = NoLockDot(matrix.get(i));

                newVector[i] = sum;
            }

            vector = new double[matrix.length()];
            System.arraycopy(newVector, 0, vector, 0, vector.length);
        } catch (IOException ignored) {}
        finally {
            if (thisOBJ <= matrixOBJ) {
                releaseAllVectorReadLocks(matrix);
                this.writeUnlock();
            }
            else {
                this.writeUnlock();
                releaseAllVectorReadLocks(matrix);
            }
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

    private double NoLockDot(SharedVector other)
    {
        /// compute dot product (row · column)

        try {
            if (this.vector.length != other.vector.length)
                OutputWriter.write("Vectors Length Dismatch", "out.json");
            if (this.orientation == other.orientation)
                OutputWriter.write("Vectors Orientation Dismatch", "out.json");
        } catch (IOException ignored) {}

        double sum = 0;
        for (int i = 0; i < vector.length; i++)
            sum += vector[i] * other.vector[i];

        return sum;
    }
}
