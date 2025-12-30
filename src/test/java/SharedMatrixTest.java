import memory.SharedMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import memory.SharedVector;
import memory.VectorOrientation;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {


    SharedMatrix sharedMatrix1, sharedMatrix2, sharedMatrix3, sharedMatrix4;
    double[][] matrix1, matrix2, matrix3, matrix4;

    @BeforeEach
    public void setUp() {
        // Two 2x2 matrices
        matrix1 = new double[][] {
                {1.0, 2.0},
                {3.0, 4.0}
        };
        sharedMatrix1 = new SharedMatrix();

        matrix2 = new double[][] {
                {5.0, 6.0},
                {7.0, 8.0}
        };
        sharedMatrix2 = new SharedMatrix();

        // One 3x3 matrix
        matrix3 = new double[][] {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        };
        sharedMatrix3 = new SharedMatrix();

        // One 4x4 matrix
        matrix4 = new double[][] {
                {1.0, 2.0, 3.0, 4.0},
                {5.0, 6.0, 7.0, 8.0},
                {9.0, 10.0, 11.0, 12.0},
                {13.0, 14.0, 15.0, 16.0}
        };
        sharedMatrix4 = new SharedMatrix();
    }


    @Test
    public void LoadColumnMajorTest(){

        sharedMatrix3=new SharedMatrix(matrix3);
        try {
            sharedMatrix3.loadColumnMajor(sharedMatrix3.readRowMajor());
            double[][] arr=sharedMatrix3.readRowMajor();

            double [][]result = new double[][] {
                    {1.0, 4.0,7.0},
                    {2.0, 5.0,8.0},
                    {3.0, 6.0,9.0},
            };
            assertEquals(arr.length,result.length);
            assertEquals(arr[0].length,result[0].length);
            for(int i=0;i<arr.length;i++){
                for(int j=0;j<arr[0].length;j++){
                    System.out.print(arr[i][j] +", ");

                }
                System.out.println();
            }
            System.out.println("---------------");
            for(int i=0;i<result.length;i++){
                for(int j=0;j<result[0].length;j++){
                    System.out.print(result[i][j] +", ");

                }
                System.out.println();
            }
            assertArrayEquals(result,arr);


        }

        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void LoadRowMajorTest(){

        sharedMatrix4.loadRowMajor(matrix4);
        try {
            double[][] arr = sharedMatrix4.readRowMajor();

            double[][] result = new double[][] {
                    {1.0, 2.0, 3.0, 4.0},
                    {5.0, 6.0, 7.0, 8.0},
                    {9.0, 10.0, 11.0, 12.0},
                    {13.0, 14.0, 15.0, 16.0}
            };

            assertEquals(arr.length, result.length);
            assertEquals(arr[0].length, result[0].length);

            for(int i = 0; i < arr.length; i++){
                for(int j = 0; j < arr[0].length; j++){
                    System.out.print(arr[i][j] + ", ");
                }
                System.out.println();
            }
            System.out.println("---------------");

            for(int i = 0; i < result.length; i++){
                for(int j = 0; j < result[0].length; j++){
                    System.out.print(result[i][j] + ", ");
                }
                System.out.println();
            }

            assertArrayEquals(result, arr);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Test public void readRowMajor(){
        try{
            sharedMatrix1.loadRowMajor(matrix1);
            assertArrayEquals(sharedMatrix1.readRowMajor(),matrix1);
            sharedMatrix1.loadColumnMajor(matrix1);
            assertNotEquals(sharedMatrix1.readRowMajor(),matrix1);
        }
         catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void testConstructorWithMatrix() {
        // Test that constructor properly creates row-major SharedVectors
        SharedMatrix matrix = new SharedMatrix(matrix3);

        try {
            assertEquals(3, matrix.length());
            assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());

            // Verify first row
            SharedVector firstRow = matrix.get(0);
            assertEquals(1.0, firstRow.get(0),0);
            assertEquals(2.0, firstRow.get(1),0);
            assertEquals(3.0, firstRow.get(2),0);

            // Verify second row
            SharedVector secondRow = matrix.get(1);
            assertEquals(4.0, secondRow.get(0),0);
            assertEquals(5.0, secondRow.get(1),0);
            assertEquals(6.0, secondRow.get(2),0);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Constructor test failed");
        }
    }

    @Test
    public void testGetMethod() {
        // Test retrieving vectors at specific indices
        sharedMatrix2.loadRowMajor(matrix2);

        try {
            SharedVector row0 = sharedMatrix2.get(0);
            assertNotNull(row0);
            assertEquals(5.0, row0.get(0),0);
            assertEquals(6.0, row0.get(1),0);

            SharedVector row1 = sharedMatrix2.get(1);
            assertNotNull(row1);
            assertEquals(7.0, row1.get(0),0);
            assertEquals(8.0, row1.get(1),0);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Get method test failed");
        }
    }

    @Test
    public void testLengthMethod() {
        // Test length returns correct number of vectors
        assertEquals(0, sharedMatrix1.length());

        sharedMatrix1.loadRowMajor(matrix1);
        assertEquals(2, sharedMatrix1.length());

        sharedMatrix3.loadColumnMajor(matrix3);
        assertEquals(3, sharedMatrix3.length());

        sharedMatrix4.loadRowMajor(matrix4);
        assertEquals(4, sharedMatrix4.length());
    }

    @Test
    public void testGetOrientationRowMajor() {
        // Test orientation for row-major loaded matrix
        sharedMatrix1.loadRowMajor(matrix1);
        assertEquals(VectorOrientation.ROW_MAJOR, sharedMatrix1.getOrientation());

        sharedMatrix4.loadRowMajor(matrix4);
        assertEquals(VectorOrientation.ROW_MAJOR, sharedMatrix4.getOrientation());
    }

    @Test
    public void testGetOrientationColumnMajor() {
        // Test orientation for column-major loaded matrix
        sharedMatrix3.loadColumnMajor(matrix3);
        assertEquals(VectorOrientation.COLUMN_MAJOR, sharedMatrix3.getOrientation());

        sharedMatrix2.loadColumnMajor(matrix2);
        assertEquals(VectorOrientation.COLUMN_MAJOR, sharedMatrix2.getOrientation());
    }

    @Test
    public void testEmptyMatrixConstructor() {
        // Test default constructor creates empty matrix
        SharedMatrix emptyMatrix = new SharedMatrix();
        assertEquals(0, emptyMatrix.length());
    }

    @Test
    public void testEmptyMatrixReadRowMajor() {
        System.out.println("=== Testing Empty Matrix Read ===");
        SharedMatrix emptyMatrix = new SharedMatrix();
        double[][] result = emptyMatrix.readRowMajor();

        System.out.println("Result length: " + result.length);
        assertNotNull(result);
        assertEquals(0, result.length);
        System.out.println("✓ Empty matrix test passed\n");
    }

    @Test
    public void testLoadEmptyMatrix() {
        System.out.println("=== Testing Load Empty Matrix ===");
        double[][] emptyMatrix = new double[0][0];
        sharedMatrix1.loadRowMajor(emptyMatrix);

        System.out.println("Matrix length after load: " + sharedMatrix1.length());
        assertEquals(0, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Read result length: " + result.length);
        assertEquals(0, result.length);
        System.out.println("✓ Load empty matrix test passed\n");
    }

    @Test
    public void testGetOrientationEmptyMatrix() {
        System.out.println("=== Testing Get Orientation on Empty Matrix ===");
        SharedMatrix emptyMatrix = new SharedMatrix();

        System.out.println("Attempting to get orientation (should throw exception)...");
        assertThrows(IllegalStateException.class, () -> {
            emptyMatrix.getOrientation();
        });
        System.out.println("✓ Exception thrown as expected\n");
    }

    @Test
    public void testSingleElementMatrix() {
        System.out.println("=== Testing Single Element Matrix (1x1) ===");
        double[][] singleElement = new double[][]{{42.0}};
        sharedMatrix1.loadRowMajor(singleElement);

        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(1, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result dimensions: " + result.length + "x" + result[0].length);
        System.out.println("Result value: " + result[0][0]);

        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals(42.0, result[0][0], 0.0);
        System.out.println("✓ Single element test passed\n");
    }

    @Test
    public void testSingleRowMatrix() {
        System.out.println("=== Testing Single Row Matrix (1xN) ===");
        double[][] singleRow = new double[][]{{1.0, 2.0, 3.0, 4.0, 5.0}};
        sharedMatrix1.loadRowMajor(singleRow);

        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(1, sharedMatrix1.length());

        SharedVector vec = sharedMatrix1.get(0);
        System.out.println("Vector length: " + vec.length());
        System.out.println("Orientation: " + sharedMatrix1.getOrientation());

        assertEquals(5, vec.length());
        assertEquals(VectorOrientation.ROW_MAJOR, sharedMatrix1.getOrientation());
        System.out.println("✓ Single row test passed\n");
    }

    @Test
    public void testSingleColumnMatrix() {
        System.out.println("=== Testing Single Column Matrix (Nx1) ===");
        double[][] singleColumn = new double[][]{
                {1.0},
                {2.0},
                {3.0},
                {4.0}
        };
        sharedMatrix1.loadRowMajor(singleColumn);

        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(4, sharedMatrix1.length());

        SharedVector vec = sharedMatrix1.get(0);
        System.out.println("First vector length: " + vec.length());
        assertEquals(1, vec.length());
        System.out.println("✓ Single column test passed\n");
    }

    @Test
    public void testLoadColumnMajorSingleColumn() {
        System.out.println("=== Testing Load Column Major Single Column ===");
        double[][] singleColumn = new double[][]{
                {1.0},
                {2.0},
                {3.0}
        };
        System.out.println("Input: 3 rows x 1 column");
        sharedMatrix1.loadColumnMajor(singleColumn);

        System.out.println("Matrix length after load: " + sharedMatrix1.length());
        System.out.println("Orientation: " + sharedMatrix1.getOrientation());
        assertEquals(1, sharedMatrix1.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, sharedMatrix1.getOrientation());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result dimensions: " + result.length + "x" + result[0].length);
        System.out.println("Result values: [" + result[0][0] + ", " + result[0][1] + ", " + result[0][2] + "]");

        assertEquals(1, result.length);
        assertEquals(3, result[0].length);
        assertEquals(1.0, result[0][0], 0.0);
        assertEquals(2.0, result[0][1], 0.0);
        assertEquals(3.0, result[0][2], 0.0);
        System.out.println("✓ Column major single column test passed\n");
    }

    @Test
    public void testRectangularMatrix2x5() {
        System.out.println("=== Testing Rectangular Matrix (2x5) ===");
        double[][] rect = new double[][]{
                {1.0, 2.0, 3.0, 4.0, 5.0},
                {6.0, 7.0, 8.0, 9.0, 10.0}
        };
        System.out.println("Input: 2 rows x 5 columns");
        sharedMatrix1.loadRowMajor(rect);

        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(2, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result dimensions: " + result.length + "x" + result[0].length);
        assertArrayEquals(rect, result);
        System.out.println("✓ Rectangular 2x5 test passed\n");
    }

    @Test
    public void testRectangularMatrix5x2() {
        System.out.println("=== Testing Rectangular Matrix (5x2) ===");
        double[][] rect = new double[][]{
                {1.0, 2.0},
                {3.0, 4.0},
                {5.0, 6.0},
                {7.0, 8.0},
                {9.0, 10.0}
        };
        System.out.println("Input: 5 rows x 2 columns");
        sharedMatrix1.loadRowMajor(rect);

        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(5, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result dimensions: " + result.length + "x" + result[0].length);
        assertArrayEquals(rect, result);
        System.out.println("✓ Rectangular 5x2 test passed\n");
    }

    @Test
    public void testLoadColumnMajorRectangular() {
        System.out.println("=== Testing Load Column Major Rectangular (2x3) ===");
        double[][] rect = new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        System.out.println("Input matrix:");
        for (int i = 0; i < rect.length; i++) {
            System.out.println(Arrays.toString(rect[i]));
        }

        sharedMatrix1.loadColumnMajor(rect);

        System.out.println("Matrix length after load: " + sharedMatrix1.length());
        System.out.println("Orientation: " + sharedMatrix1.getOrientation());
        assertEquals(3, sharedMatrix1.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, sharedMatrix1.getOrientation());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result matrix (should be transposed):");
        for (int i = 0; i < result.length; i++) {
            System.out.println(Arrays.toString(result[i]));
        }

        double[][] expected = new double[][]{
                {1.0, 4.0},
                {2.0, 5.0},
                {3.0, 6.0}
        };
        assertArrayEquals(expected, result);
        System.out.println("✓ Column major rectangular test passed\n");
    }

    @Test
    public void testMultipleLoadsOverwrite() {
        System.out.println("=== Testing Multiple Loads Overwrite ===");

        sharedMatrix1.loadRowMajor(matrix1);
        System.out.println("After first load (matrix1): length = " + sharedMatrix1.length());
        assertEquals(2, sharedMatrix1.length());

        sharedMatrix1.loadRowMajor(matrix3);
        System.out.println("After second load (matrix3): length = " + sharedMatrix1.length());
        assertEquals(3, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Final matrix matches matrix3: " + Arrays.deepEquals(result, matrix3));
        assertArrayEquals(matrix3, result);
        System.out.println("✓ Multiple loads test passed\n");
    }

    @Test
    public void testSwitchBetweenOrientations() {
        System.out.println("=== Testing Switch Between Orientations ===");

        sharedMatrix1.loadRowMajor(matrix1);
        System.out.println("After loadRowMajor: " + sharedMatrix1.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, sharedMatrix1.getOrientation());

        sharedMatrix1.loadColumnMajor(matrix1);
        System.out.println("After loadColumnMajor: " + sharedMatrix1.getOrientation());
        assertEquals(VectorOrientation.COLUMN_MAJOR, sharedMatrix1.getOrientation());

        sharedMatrix1.loadRowMajor(matrix1);
        System.out.println("After loadRowMajor again: " + sharedMatrix1.getOrientation());
        assertEquals(VectorOrientation.ROW_MAJOR, sharedMatrix1.getOrientation());
        System.out.println("✓ Orientation switch test passed\n");
    }

    @Test
    public void testLargeMatrix() {
        System.out.println("=== Testing Large Matrix (10x10) ===");
        double[][] large = new double[10][10];

        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                large[i][j] = i * 10 + j;

        System.out.println("Loading 10x10 matrix...");
        sharedMatrix1.loadRowMajor(large);
        System.out.println("Matrix length: " + sharedMatrix1.length());
        assertEquals(10, sharedMatrix1.length());

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("Result dimensions: " + result.length + "x" + result[0].length);
        System.out.println("First element: " + result[0][0] + ", Last element: " + result[9][9]);
        assertArrayEquals(large, result);
        System.out.println("✓ Large matrix test passed\n");
    }

    @Test
    public void testGetOutOfBounds() {
        System.out.println("=== Testing Get Out of Bounds ===");
        sharedMatrix1.loadRowMajor(matrix1);
        System.out.println("Matrix length: " + sharedMatrix1.length());

        System.out.println("Attempting to get index 5 (should throw exception)...");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            sharedMatrix1.get(5);
        });
        System.out.println("✓ Exception thrown for index 5");

        System.out.println("Attempting to get index -1 (should throw exception)...");
        assertThrows(IndexOutOfBoundsException.class, () -> {
            sharedMatrix1.get(-1);
        });
        System.out.println("✓ Exception thrown for index -1");
        System.out.println("✓ Out of bounds test passed\n");
    }

    @Test
    public void testZeroMatrix() {
        System.out.println("=== Testing Zero Matrix ===");
        double[][] zeros = new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0}
        };

        sharedMatrix1.loadRowMajor(zeros);
        System.out.println("Loaded 3x3 zero matrix");

        double[][] result = sharedMatrix1.readRowMajor();
        System.out.println("All zeros preserved: " + Arrays.deepEquals(result, zeros));
        assertArrayEquals(zeros, result);
        System.out.println("✓ Zero matrix test passed\n");
    }

    @Test
    public void testNegativeValues() {
        System.out.println("=== Testing Negative Values ===");
        double[][] negative = new double[][]{
                {-1.0, -2.0},
                {-3.0, -4.0}
        };

        System.out.println("Input matrix:");
        for (int i = 0; i < negative.length; i++) {
            System.out.println(Arrays.toString(negative[i]));
        }

        sharedMatrix1.loadRowMajor(negative);
        double[][] result = sharedMatrix1.readRowMajor();

        System.out.println("Result matrix:");
        for (int i = 0; i < result.length; i++) {
            System.out.println(Arrays.toString(result[i]));
        }

        assertArrayEquals(negative, result);
        System.out.println("✓ Negative values test passed\n");
    }

    @Test
    public void testMixedSignValues() {
        System.out.println("=== Testing Mixed Sign Values ===");
        double[][] mixed = new double[][]{
                {1.0, -2.0, 3.0},
                {-4.0, 5.0, -6.0},
                {7.0, -8.0, 9.0}
        };

        System.out.println("Input matrix:");
        for (int i = 0; i < mixed.length; i++) {
            System.out.println(Arrays.toString(mixed[i]));
        }

        sharedMatrix1.loadRowMajor(mixed);
        double[][] result = sharedMatrix1.readRowMajor();

        System.out.println("Result matrix:");
        for (int i = 0; i < result.length; i++) {
            System.out.println(Arrays.toString(result[i]));
        }

        assertArrayEquals(mixed, result);
        System.out.println("✓ Mixed sign values test passed\n");
    }

    @Test
    public void testVeryLargeValues() {
        System.out.println("=== Testing Very Large Values ===");
        double[][] large = new double[][]{
                {1e10, 2e10},
                {3e10, 4e10}
        };

        System.out.println("Input values: 1e10, 2e10, 3e10, 4e10");
        sharedMatrix1.loadRowMajor(large);
        double[][] result = sharedMatrix1.readRowMajor();

        System.out.println("Result values: " + result[0][0] + ", " + result[0][1] + ", " +
                result[1][0] + ", " + result[1][1]);
        assertArrayEquals(large, result);
        System.out.println("✓ Very large values test passed\n");
    }

    @Test
    public void testVerySmallValues() {
        System.out.println("=== Testing Very Small Values ===");
        double[][] small = new double[][]{
                {1e-10, 2e-10},
                {3e-10, 4e-10}
        };

        System.out.println("Input values: 1e-10, 2e-10, 3e-10, 4e-10");
        sharedMatrix1.loadRowMajor(small);
        double[][] result = sharedMatrix1.readRowMajor();

        System.out.println("Result values: " + result[0][0] + ", " + result[0][1] + ", " +
                result[1][0] + ", " + result[1][1]);
        assertArrayEquals(small, result);
        System.out.println("✓ Very small values test passed\n");
    }




}
