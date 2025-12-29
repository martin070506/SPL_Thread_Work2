

import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import parser.OutputWriter;
import spl.lae.LinearAlgebraEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    double[] Vec1, Vec2, Vec3, Vec4, Vec5;
    double[][] Matrix1;
    SharedVector sVec1, sVec2, sVec3, sVec4, sVec5;
    SharedMatrix sMatrix1;
    @BeforeEach
    void beforeEach() {
        Vec1 = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
        sVec1 = new SharedVector(Vec1, VectorOrientation.ROW_MAJOR);
        Vec2 = new double[]{2.0, 3.0, 4.0, 5.0, 6.0};
        sVec2 = new SharedVector(Vec2, VectorOrientation.ROW_MAJOR);
        Vec3 = new double[]{4.0, 6.0, 1.0, 52.0, 1.5};
        sVec3 = new SharedVector(Vec3, VectorOrientation.COLUMN_MAJOR);
        Vec4 = new double[]{-1.0, -2.0, -3.0, -4.0, -5.0};
        sVec4 = new SharedVector(Vec4, VectorOrientation.ROW_MAJOR);
        Vec5 = new double[]{2.0, 3.0, 4.0, 5.0, 6.0, 7.0};
        sVec5 = new SharedVector(Vec5, VectorOrientation.ROW_MAJOR);
        Matrix1 = new double[][]{{2.0,2.0,2.0,3.0,3.0}, {4.0,4.0,4.0,4.0,5.0},{6.0,6.0,6.0,7.0,7.0}};

        // [2,2,2,3,3]
        // [4,4,4,4,5]
        // [6,6,6,7,7]
        // Transposed ->
        // [2,4,6]
        // [2,4,6]
        // [2,4,6]
        // [3,4,7]
        // [3,5,7]

        sMatrix1 = new SharedMatrix(Matrix1);
    }

    @Test
    void VectorAdditionTest1() throws Exception {
        double[] result = new double[]{3.0, 5.0, 7.0, 9.0, 11.0};
        sVec1.add(sVec2);
        boolean flag = true;
        for (int i = 0; i < sVec1.length(); i++)
            if (sVec1.get(i) != result[i])
                flag = false;

        assertTrue(flag);
    }

    @Test
    void vectorGetTest1() {
        boolean flag = true;

        try {
            for (int i = 0; i < sVec1.length(); i++)
                if (sVec1.get(i) != Vec1[i])
                    flag = false;
        }
        catch (Exception ignored) {
            flag = false;
        }

        assertTrue(flag);
    }

    @Test
    void vectorGetTest2() {
        boolean flag = true;

        try {
            sVec1.get(10);
            flag = false;
        }
        catch (Exception ignored) {}

        assertTrue(flag);
    }

    @Test
    void VectorOrientationTest() throws Exception {
        boolean flag = true;
        if (sVec1.getOrientation() != VectorOrientation.ROW_MAJOR)
            flag = false;
        if (sVec3.getOrientation() != VectorOrientation.COLUMN_MAJOR)
            flag = false;

        assertTrue(flag);
    }

    @Test
    void VectorLengthTest()
    {
       assertTrue(Vec1.length == sVec1.length());
       assertTrue(Vec2.length == sVec2.length());
       assertTrue(Vec3.length == sVec3.length());
    }

    @Test
    void LengthAfterMultiplicationTest() throws Exception {
        for (int i = 0; i < sMatrix1.length(); i++)
        {
            sMatrix1.get(i).transpose();
        }
        sVec1.vecMatMul(sMatrix1);
        assertEquals(sVec1.length(),3);
    }

    @Test void MultiplyTest1() throws Exception {
        double []result = new double[]{39,65,99};
        for (int i = 0; i < sMatrix1.length(); i++)
        {
            sMatrix1.get(i).transpose();
        }
        sVec1.vecMatMul(sMatrix1);
        assertEquals(sVec1.length(),3);
        for(int i=0;i<sVec1.length();i++)
        {
            System.out.println("Expected: " + result[i] + " Got: " +  sVec1.get(i));
            assertEquals(sVec1.get(i),result[i],0);
        }
    }

    @Test void MultiplyTest2() throws Exception {
        try {
            sVec1.vecMatMul(sMatrix1);
            assertNotEquals(sVec1,false);
        }
        catch (Exception ignored) {
            System.out.println("was here");
            assertEquals(true,true);
        }

    }
    @Test void MultiplyTest3() throws Exception {
        try {
            double[] result = new double[]{46,46,46,55,57};
            sMatrix1.loadColumnMajor(sMatrix1.readRowMajor());
            SharedVector sVec5=new SharedVector(new double[]{4.0,2.0,5.0},VectorOrientation.ROW_MAJOR);
            sVec5.vecMatMul(sMatrix1);
            assertEquals(true,true);
            assertEquals(sVec5.length(),5);
            for (int i=0;i<sVec5.length();i++){
                System.out.println("Expected: " + result[i] + " Got: " +  sVec5.get(i));
            }
        }
        catch (Exception ignored) {
            System.out.println("was here");
            assertEquals(true,false);
        }

    }



    @Test
    void VectorTransposeTest() {
        boolean flag = true;

        sVec1.transpose();
        sVec3.transpose();

        if (sVec1.getOrientation() != VectorOrientation.COLUMN_MAJOR)
            flag = false;
        if (sVec3.getOrientation() != VectorOrientation.ROW_MAJOR)
            flag = false;

        assertTrue(flag);
    }

    @Test
    void VectorNegateTest() {
        boolean flag = true;

        sVec1.negate();

        for (int i = 0; i < Vec1.length; i++)
            if (Vec1[i] != Vec4[i])
                flag = false;

        assertTrue(flag);
    }

    @Test
    void VectorDotTest1() throws Exception {
        boolean flag = true;

        if (sVec1.dot(sVec3) != 234.5)
            flag = false;

        assertTrue(flag);
    }

    @Test
    void VectorDotTest2() throws Exception {
        boolean flag = true;

        try {
            sVec1.dot(sVec5);
            flag = false;
        }
        catch (Exception ignored) {}

        assertTrue(flag);
    }




}