package cucumber.runtime;

import java.util.LinkedList;

/**
 * the version from cucumber would throw a nullpointer if the stopwatch is called twice
 *
 * this one will stack and unstack the duration nicely
 *
 * @author Jeremy Comte
 */
public interface StopWatch {

    void start();

    /**
     * @return nanoseconds since start
     */
    long stop();

    StopWatch SYSTEM = new StopWatch() {
        private final ThreadLocal<LinkedList<Long>> start = ThreadLocal.withInitial(LinkedList::new);

        @Override
        public void start() {
            start.get().add(System.nanoTime());
        }

        @Override
        public long stop() {
            return System.nanoTime() - start.get().removeLast();
        }
    };

    public static class Stub implements StopWatch {

        private final long duration;

        public Stub(long duration) {
            this.duration = duration;
        }

        @Override
        public void start() {
        }

        @Override
        public long stop() {
            return duration;
        }
    }
}
