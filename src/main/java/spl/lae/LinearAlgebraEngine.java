package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        /// create executor with given thread count

        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        /// resolve computation tree step by step until final matrix is produced

        return computationRoot.findResolvable();
    }

    public void loadAndCompute(ComputationNode node) throws Exception {
        /// load operand matrices
        /// create compute tasks & submit tasks to executor

        while (node != null && node.findResolvable() != null) {

            List<Runnable> tasks = List.of();
            ComputationNode resolvablePointer=node.findResolvable();
            resolvablePointer.associativeNesting(); // so i make sure each node only has 2 leaves like said it turns A+B+C to (A+B)+C

            leftMatrix = new SharedMatrix(resolvablePointer.getChildren().get(0).getMatrix());
            try {
                rightMatrix = new SharedMatrix(resolvablePointer.getChildren().get(1).getMatrix());
            } catch (Exception ignored) {}

            switch (resolvablePointer.getNodeType())
            {
                case ADD:
                {
                    tasks = createAddTasks();
                    break;
                }
                case MULTIPLY:
                {
                    tasks = createMultiplyTasks();
                    break;
                }
                case NEGATE:
                {
                    tasks = createNegateTasks();
                    break;
                }
                case TRANSPOSE:
                {
                    tasks = createTransposeTasks();
                    break;
                }
            }

            executor.submitAll(tasks);
            resolvablePointer.resolve(leftMatrix.readRowMajor());
        }
    }


    public List<Runnable> createAddTasks() {
        /// return tasks that perform row-wise addition

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

        List<Runnable> tasks = new ArrayList<>(leftMatrix.length());
        for (int i = 0; i < leftMatrix.length(); i++) {
            int finalI = i;
            tasks.add(() -> {
                leftMatrix.get(finalI).transpose();
            });
        }

        return tasks;
    }

    public String getWorkerReport() {
        /// return summary of worker activity

        return executor.getWorkerReport();
    }
}
