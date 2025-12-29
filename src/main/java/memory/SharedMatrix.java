package memory;

import parser.OutputWriter;

import java.util.Arrays;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        /// initialize empty matrix

        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        /// construct matrix as row-major SharedVectors
        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++)
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
    }

    public void loadRowMajor(double[][] matrix) {
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }

        SharedVector[] oldVectors = vectors;
        acquireAllVectorWriteLocks(oldVectors);
        try {
            vectors = newVectors;
        } finally {
            releaseAllVectorWriteLocks(oldVectors);
        }
    }


    public void loadColumnMajor(double[][] matrix) {
        /// replace internal data with new column-major matrix

        SharedVector[] newVectors = new SharedVector[matrix[0].length];

        for (int i = 0; i < matrix[0].length; i++) {
            double[] columnArr = new double[matrix.length];
            for (int j = 0; j < matrix.length; j++)
                columnArr[j] = matrix[j][i];

            newVectors[i] = new SharedVector(columnArr, VectorOrientation.COLUMN_MAJOR);
        }
        SharedVector[] oldVectors = vectors;
        acquireAllVectorWriteLocks(oldVectors);
        try {
            vectors = newVectors;
        }
        finally {
            releaseAllVectorWriteLocks(oldVectors);
        }
    }


    public double[][] readRowMajor()  {
        /// return matrix contents as a row-major double[][]
        acquireAllVectorReadLocks(vectors);
        try {
            if (!isValidVector())
                return new double[0][0];
            double[][] matrix = new double[vectors.length][vectors[0].length()];
            for (int i = 0; i < vectors.length; i++)
                for (int j = 0; j < vectors[0].length(); j++)
                    matrix[i][j] = vectors[i].get(j);
            return matrix;
        }
        finally {
            releaseAllVectorReadLocks(vectors);
        }

    }

    public SharedVector get(int index) {
        /// return vector at index
            return vectors[index];
    }

    public int length() {
        /// return number of stored vectors
        if (!isValidVector())
            return 0;

        return vectors.length;
    }



    public VectorOrientation getOrientation() {
        /// return orientation
        if (!isValidVector())
            try {
                OutputWriter.write("Cannot Get orienation of an empty Matrix", "out.json");
                throw  new IllegalStateException("Cannot Get orienation of an empty Matrix");
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        /// acquire read lock for each vector

        for (SharedVector vec : vecs)
            vec.readLock();
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        /// release read locks

        for (SharedVector vec : vecs)
            vec.readUnlock();
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        /// acquire write lock for each vector

        for (SharedVector vec : vecs)
            vec.writeLock();
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        /// release write locks

        for (SharedVector vec : vecs)
            vec.writeUnlock();
    }

    private boolean isValidVector() {

        return (vectors != null && vectors.length != 0 && vectors[0] != null);
    }
}
