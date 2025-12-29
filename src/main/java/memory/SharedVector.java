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
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        finally {
            readUnlock();
        }
    }

    public int length() {
        /// return vector length

        readLock();
        try {
            return vector.length;
        }
        finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        /// return vector orientation

        readLock();
        try {
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
        if (thisOBJ <= matrixOBJ) { //THIS IS TO ALWAYS LOCK/UNLOCK IN THE SAME ORDER
            this.readLock();
            other.readLock();
        }
        else {
            other.readLock();
            this.readLock();
        }
        try {
            if (this.vector.length != other.vector.length)
                throw new IllegalArgumentException("Vector lengths don't match");
            if (this.orientation != other.orientation)
                throw new IllegalArgumentException("Vectors Orientation Dismatch");

            for (int i = 0; i < vector.length; i++)
                vector[i] += other.vector[i];
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        finally {
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
        try {
            for (int i = 0; i < vector.length; i++)
                vector[i] *= -1;
        }
        finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        int thisOBJ = System.identityHashCode(this);
        int otherOBJ = System.identityHashCode(other);

        if (thisOBJ <= otherOBJ) {
            this.readLock();
            other.readLock();
        } else {
            other.readLock();
            this.readLock();
        }

        try {
            if (this.vector.length != other.vector.length) {
                throw new IllegalArgumentException("Vectors Length Mismatch");
            }
            if (this.orientation == other.orientation) {
                throw new IllegalArgumentException("Vectors Orientation Mismatch");
            }

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
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
        // IMPORTANT
        // in this method we assume we get a vector by columns, in the LAE class before sending to here, we will LoadColumnMajor
        try {
            if (orientation != VectorOrientation.ROW_MAJOR){
                throw new IllegalArgumentException("Vector Orientation Should Be ROWMAJOR");
            }
            // HERE WE Split into cases where we are given row matrix(we will load column) or a already given a column matrix
            if (matrix.getOrientation() == VectorOrientation.ROW_MAJOR){
                throw new IllegalArgumentException("Matrix Should Be Column Major");
            }

            else if (matrix.getOrientation() != VectorOrientation.ROW_MAJOR && matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR) {
                throw new IllegalArgumentException("Matrix Does Not Have Orientation");
            }

            acquireAllVectorReadLocks(matrix);
            try {
                handleVecMatMulWithColumnMatrix(matrix);
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            finally {
                releaseAllVectorReadLocks(matrix);
            }

        }
        catch (Exception e) {
                throw new RuntimeException(e.getMessage());
        }
        finally {
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

    private void handleVecMatMulWithColumnMatrix(SharedMatrix matrix) {

        ///Here We know we get a column displayed matrix
        try {

            if (this.length() != matrix.get(0).length())
                throw new IllegalArgumentException("Matrix Length Mismatch");
           double[] vector = new double[matrix.length()];
           for (int i = 0; i < vector.length; i++)
               vector[i] = UnsafeDot(matrix.get(i));

           this.vector = vector;
        }
        catch (Exception e) {
                throw new RuntimeException(e.getMessage());
        }
    }

    private double UnsafeDot(SharedVector other){
        try {
            if (this.vector.length != other.vector.length) {
                throw new IllegalArgumentException("Vectors Length Mismatch");
            }
            if (this.orientation == other.orientation) {
                throw new IllegalArgumentException("Matrix Should Be Column Major");
            }

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
