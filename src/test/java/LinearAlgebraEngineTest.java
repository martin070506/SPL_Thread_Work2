import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;
    private int numThreads = 4;

    @BeforeEach
    void setUp() {
        engine = new LinearAlgebraEngine(numThreads);
    }

    @Test
    void testSimpleAddition() {
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        double[][] expected = {{6.0, 8.0}, {10.0, 12.0}};

        ComputationNode resultNode = runOperation(ComputationNodeType.ADD, dataA, dataB);

        assertNotNull(resultNode.getMatrix());
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    void testMatrixMultiplication() {
        double[][] dataA = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        double[][] dataB = {
                {7.0, 8.0},
                {9.0, 1.0},
                {2.0, 3.0}
        };

        double[][] expected = {
                {31.0, 19.0},
                {85.0, 55.0}
        };

        ComputationNode resultNode = runOperation(ComputationNodeType.MULTIPLY, dataA, dataB);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    void testTranspose() {
        double[][] data = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        }; // 2x3

        double[][] expected = {
                {1.0, 4.0},
                {2.0, 5.0},
                {3.0, 6.0}
        }; // 3x2

        LinkedList<ComputationNode> nodeList = new LinkedList<>();
        nodeList.add(new ComputationNode(data));
        ComputationNode root = new ComputationNode(ComputationNodeType.TRANSPOSE, nodeList);

        ComputationNode resultNode = engine.run(root);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    void testNegate() {
        double[][] data = {
                {1.0, -2.0},
                {0.0, 5.5}
        };
        double[][] expected = {
                {-1.0, 2.0},
                {0.0, -5.5}
        };

        LinkedList<ComputationNode> nodeList = new LinkedList<>();
        nodeList.add(new ComputationNode(data));
        ComputationNode root = new ComputationNode(ComputationNodeType.NEGATE, nodeList);

        ComputationNode resultNode = engine.run(root);
        assertMatrixEquals(expected, resultNode.getMatrix());
    }

    @Test
    void testComplexTreeExecution() {
        double[][] A = {{1, 0}, {0, 1}};
        double[][] B = {{1, 2}, {3, 4}};
        double[][] C = {{2, 0}, {0, 2}};
        double[][] expected = {{4.0, 4.0}, {6.0, 10.0}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);
        ComputationNode nodeC = new ComputationNode(C);

        LinkedList<ComputationNode> addChildren = new LinkedList<>();
        addChildren.add(nodeA);
        addChildren.add(nodeB);
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, addChildren);

        LinkedList<ComputationNode> multChildren = new LinkedList<>();
        multChildren.add(addNode);
        multChildren.add(nodeC);
        ComputationNode rootMult = new ComputationNode(ComputationNodeType.MULTIPLY, multChildren);

        ComputationNode result = engine.run(rootMult);
        assertMatrixEquals(expected, result.getMatrix());
    }

    @Test
    void testAssociativeAddition() {
        double[][] A = {{1}};
        double[][] B = {{2}};
        double[][] C = {{3}};
        double[][] expected = {{6}};

        LinkedList<ComputationNode> children = new LinkedList<>();
        children.add(new ComputationNode(A));
        children.add(new ComputationNode(B));
        children.add(new ComputationNode(C));

        ComputationNode rootAdd = new ComputationNode(ComputationNodeType.ADD, children);

        ComputationNode result = engine.run(rootAdd);
        assertMatrixEquals(expected, result.getMatrix());
    }

    @Test
    void testDimensionMismatchMultiply() {
        double[][] A = {{1, 2}};
        double[][] B = {{1, 2}};

        assertThrows(RuntimeException.class, () -> {
            runOperation(ComputationNodeType.MULTIPLY, A, B);
        });
    }

    @Test
    void testDimensionMismatchAdd() {
        double[][] A = {{1, 2}};
        double[][] B = {{1, 2, 3}};

        assertThrows(Exception.class, () -> {
            runOperation(ComputationNodeType.ADD, A, B);
        });
    }

    private ComputationNode runOperation(ComputationNodeType type, double[][] matA, double[][] matB) {

        LinkedList<ComputationNode> nodeList = new LinkedList<>();
        nodeList.add(new ComputationNode(matA));
        nodeList.add(new ComputationNode(matB));
        ComputationNode root = new ComputationNode(type, nodeList);

        return engine.run(root);
    }

    private void assertMatrixEquals(double[][] expected, double[][] actual) {

        assertEquals(expected.length, actual.length, "Rows mismatch");
        assertEquals(expected[0].length, actual[0].length, "Cols mismatch");

        for (int i = 0; i < expected.length; i++)
            for (int j = 0; j < expected[0].length; j++)
                assertEquals(expected[i][j], actual[i][j], 0.001,
                        "Mismatch at [" + i + "][" + j + "]");

    }
}