package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
    }

    public SharedMatrix(double[][] matrix) {
        /// construct matrix as row-major SharedVectors
        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++)
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
    }

    public void loadRowMajor(double[][] matrix) {
        /// replace internal data with new row-major matrix

        for (int i = 0; i < vectors.length; i++)
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
    }

    public void loadColumnMajor(double[][] matrix) {
        /// replace internal data with new column-major matrix

        for (int i = 0; i < vectors.length; i++)
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
    }

    public double[][] readRowMajor() throws Exception {
        /// return matrix contents as a row-major double[][]

        double[][] matrix = new double[vectors.length][];
        for (int i = 0; i < vectors.length; i++)
            for (int j = 0; j < vectors.length; j++)
                matrix[i][j] = vectors[i].get(j);

        return matrix;
    }

    public SharedVector get(int index) {
        /// return vector at index

        return vectors[index];
    }

    public int length() {
        /// return number of stored vectors

        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        /// return orientation

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
}
