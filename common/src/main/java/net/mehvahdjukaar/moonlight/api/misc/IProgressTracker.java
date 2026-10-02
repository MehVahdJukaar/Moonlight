package net.mehvahdjukaar.moonlight.api.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

//copy on write, fast reads. nonsensical thread safe stuff below
public interface IProgressTracker {
    Task subtask(int totalSteps);

    interface Task extends IProgressTracker {
        void step();
    }

    static Tree createTree(int totalSteps) {
        return new Tree(totalSteps);
    }

    final class Tree implements Task {
        private volatile List<Tree> subtasks = List.of();

        private final int totalSteps;
        private final AtomicInteger completedSteps = new AtomicInteger(0);

        public Tree(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        @Override
        public Task subtask(int totalSteps) {
            Tree child = new Tree(totalSteps);
            synchronized (this) {
                List<Tree> next = new ArrayList<>(subtasks);
                next.add(child);
                subtasks = Collections.unmodifiableList(next);
            }
            return child;
        }

        @Override
        public void step() {
            int prev, next;
            do {
                prev = completedSteps.get();
                if (prev >= totalSteps) return;
                next = prev + 1;
            } while (!completedSteps.compareAndSet(prev, next));
        }

        public float getProgress() {
            if (totalSteps == 0) return 1.0f;

            List<Tree> snapshot = this.subtasks;
            int localCompleted = this.completedSteps.get();

            if (snapshot.isEmpty()) {
                return (float) localCompleted / (float) totalSteps;
            } else {
                float sum = 0.0f;
                for (Tree sub : snapshot) {
                    sum += sub.getProgress();
                }
                return sum / (float) snapshot.size();
            }
        }

        public int countLeaves() {
            List<Tree> snapshot = this.subtasks;
            if (snapshot.isEmpty()) {
                return 1;
            } else {
                int sum = 0;
                for (Tree sub : snapshot) {
                    sum += sub.countLeaves();
                }
                return sum;
            }
        }
    }
}
