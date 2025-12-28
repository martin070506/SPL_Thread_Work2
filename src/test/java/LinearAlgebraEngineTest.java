import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import memory.SharedMatrix;
import spl.lae.LinearAlgebraEngine;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;
    private int numThreads = 4;

    @BeforeEach
    void setUp() {
        // אתחול המנוע לפני כל טסט
        engine = new LinearAlgebraEngine(numThreads);
    }

    @Test
    void testInitialization() {
        assertNotNull(engine, "Engine should be initialized successfully");
        assertNotNull(engine.getWorkerReport());
    }

    @Test
    void testSimpleAddition() {
        // 1. Arrange: הכנת הנתונים (מטריצות בגודל 2x2)
        // A = [[1, 2], [3, 4]]
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrixA = new SharedMatrix(dataA);

        // B = [[5, 6], [7, 8]]
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        SharedMatrix matrixB = new SharedMatrix(dataB);

        LinkedList<ComputationNode> nodeList = new LinkedList<>();
        nodeList.add(new ComputationNode(dataA));
        nodeList.add(new ComputationNode(dataB));
        ComputationNode rootAdd = new ComputationNode(ComputationNodeType.ADD, nodeList);

        // 2. Act: הרצת המנוע
        ComputationNode resultNode = engine.run(rootAdd);

        // 3. Assert: בדיקת התוצאה הצפויה
        // Expected = [[6, 8], [10, 12]]
        assertNotNull(resultNode.getMatrix(), "Result matrix should not be null");
        double[][] resultData = resultNode.getMatrix(); // הנחה שיש מתודה כזו

        assertEquals(6.0, resultData[0][0], 0.001);
        assertEquals(8.0, resultData[0][1], 0.001);
        assertEquals(10.0, resultData[1][0], 0.001);
        assertEquals(12.0, resultData[1][1], 0.001);
    }
}

