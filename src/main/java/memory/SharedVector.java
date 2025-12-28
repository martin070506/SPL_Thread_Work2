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
            {
                OutputWriter.write("Index Out Of Bound", "out.json");
                throw new IndexOutOfBoundsException("Index Out Of Bound");
            }

            return vector[index];
        } catch (Exception ignored) {}
        finally {
            readUnlock();
        }
        return 0.0;
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
                OutputWriter.write("Vectors Length Mismatch","out.json");
                throw new IllegalArgumentException("Vectors Length Mismatch");
            }
            if (this.orientation == other.orientation) {
                OutputWriter.write("Vectors Orientation Mismatch","out.json");
                throw new IllegalArgumentException("Vectors Orientation Mismatch");
            }

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;

        } catch (IOException e) {
            throw new RuntimeException(e);
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
            try {
                OutputWriter.write(e.getMessage(),"out.json");
            }
            catch (IOException ioe) {
                throw new RuntimeException(e);
            }
            throw e;

        }
    }

    private double UnsafeDot(SharedVector other){
        try {
            if (this.vector.length != other.vector.length) {
                OutputWriter.write("Vectors Length Mismatch", "out.json");
                throw new IllegalArgumentException("Vectors Length Mismatch");
            }
            if (this.orientation == other.orientation) {
                OutputWriter.write("Vectors Orientation Mismatch", "out.json");
                throw new IllegalArgumentException("Vectors Orientation Mismatch");
            }

            double sum = 0;
            for (int i = 0; i < vector.length; i++)
                sum += vector[i] * other.vector[i];

            return sum;
        }
        catch (IOException e) {
           throw new RuntimeException(e);
        }
    }
}
