package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        /// create executor with given thread count

        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        /// resolve the computation tree step by step until the final matrix is produced

        try {
            loadAndCompute(computationRoot);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) throws Exception {
        /// load operand matrices
        /// create compute tasks & submit tasks to executor

        // TODO: Check if The new Variable is Legit
        AtomicReference<Throwable> taskError = new java.util.concurrent.atomic.AtomicReference<>(null);

        while (node != null && node.findResolvable() != null) {
            List<Runnable> tasks = List.of();
            ComputationNode resolvablePointer = node.findResolvable(); // A+B+C+D

            while (resolvablePointer.getChildren().size() > 2) {
                resolvablePointer.associativeNesting();
                resolvablePointer = resolvablePointer.findResolvable();
                // This ensures resolvable pointer has exactly 2 children, because having 1 child is impossible and 0 children means its a MATRIX
                // and if the node is a matrix, then node.resolvable is null and the while block would stop
            }

            leftMatrix = new SharedMatrix(resolvablePointer.getChildren().get(0).getMatrix());
            rightMatrix = null;

            if (resolvablePointer.getChildren().size() == 2)
                rightMatrix = new SharedMatrix(resolvablePointer.getChildren().get(1).getMatrix());

            switch (resolvablePointer.getNodeType()) {
                case ADD: {
                    System.out.println("ADD");
                    tasks = createAddTasks();
                    break;
                }
                case MULTIPLY: {
                    System.out.println("MULTIPLY");
                    tasks = createMultiplyTasks();
                    break;
                }
                case NEGATE: {
                    System.out.println("NEGATE");
                    tasks = createNegateTasks();
                    break;
                }
                case TRANSPOSE: {
                    System.out.println("TRANSPOSE");
                    tasks = createTransposeTasks();
                    break;
                }
            }

            executor.submitAll(tasks);
            resolvablePointer.resolve(leftMatrix.readRowMajor());
        }

        System.out.println(getWorkerReport());
        executor.shutdown();
    }

    public List<Runnable> createAddTasks() {
        /// return tasks that perform row-wise addition

        if (leftMatrix.getOrientation() != rightMatrix.getOrientation())
            transpose(rightMatrix);
        if (leftMatrix.length() != rightMatrix.length())
            throw new IllegalArgumentException("Matrix Length Mismatch");

        List<Runnable> tasks = new ArrayList<>(leftMatrix.length());
        for (int i = 0; i < leftMatrix.length(); i++) {
            int finalI = i;
            tasks.add(() -> {
                leftMatrix.get(finalI).add(rightMatrix.get(finalI));
            });
        }

        return tasks;
    }


    public List<Runnable> createMultiplyTasks() {
        /// return tasks that perform row × matrix multiplication
        // TODO we most likely get matrices as ROW-MAJOR , but our multiply only works on columns, and a simple transpose doesnt help
        // TODO so we need to somehow lad th matrix to columns, maybe by load column major or something
        // TODO hust get the matrix and load column major , thats it
        try {
            if (leftMatrix.getOrientation() != VectorOrientation.ROW_MAJOR)
                transpose(leftMatrix);
            if (leftMatrix.getOrientation() != VectorOrientation.COLUMN_MAJOR)
                transpose(rightMatrix);
            if (leftMatrix.get(0).length() != rightMatrix.get(0).length())
                throw new IllegalArgumentException("Matrix Length Mismatch");
        } catch (Exception e) {
            executor.shutdown();
            throw new RuntimeException(e.getMessage());
        }


        List<Runnable> tasks = new ArrayList<>(leftMatrix.length());
        for (int i = 0; i < leftMatrix.length(); i++) {
            int finalI = i;
            tasks.add(() -> {
                leftMatrix.get(finalI).vecMatMul(rightMatrix);
            });
        }

        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        /// return tasks that negate rows

        List<Runnable> tasks = new ArrayList<>(leftMatrix.length());
        for (int i = 0; i < leftMatrix.length(); i++) {
            int finalI = i;
            tasks.add(() -> {
                leftMatrix.get(finalI).negate();
            });
        }

        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        /// return tasks that transpose rows

        List<Runnable> tasks = new ArrayList<>(1);
        tasks.add(() -> {
            transpose(leftMatrix);
        });

        return tasks;
    }

    public String getWorkerReport() {
        /// return summary of worker activity

        return executor.getWorkerReport();
    }

    private void transpose(SharedMatrix Matrix) {

        if (Matrix.getOrientation() == VectorOrientation.ROW_MAJOR)
            Matrix.loadColumnMajor(Matrix.readRowMajor());
        else
            Matrix.loadRowMajor(Matrix.readRowMajor());
    }
}
