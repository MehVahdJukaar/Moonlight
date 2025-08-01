package net.mehvahdjukaar.moonlight.api.misc;

import java.util.ArrayList;
import java.util.List;

public interface ITaskProgress {
    Scope subtask(int totalSteps);

    interface Scope extends ITaskProgress {
        void step();
    }

    static Tree createTree(int totalSteps) {
        return new Tree(totalSteps);
    }

    class Tree implements ITaskProgress.Scope {
        private final List<Tree> subtasks = new ArrayList<>();

        private final int totalSteps;
        private int completedSteps = 0;

        public Tree(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        @Override
        public synchronized ITaskProgress.Scope subtask(int totalSteps) {
            Tree subtask = new Tree(totalSteps);
            subtasks.add(subtask);
            return subtask;
        }

        @Override
        public synchronized void step() {
            if (completedSteps < totalSteps) {
                completedSteps++;
            }
        }

        public float getProgress() {
            if (totalSteps == 0) return 1.0f;

            if (subtasks.isEmpty()) {
                return (float) completedSteps / totalSteps;
            } else {
                float sum = 0.0f;
                for (Tree sub : subtasks) {
                    sum += sub.getProgress();
                }
                return sum / subtasks.size();
            }
        }

        public int countLeaves() {
            if (subtasks.isEmpty()) {
                return 1; // this is a leaf
            } else {
                int sum = 0;
                for (Tree sub : subtasks) {
                    sum += sub.countLeaves();
                }
                return sum;
            }
        }
    }
}
