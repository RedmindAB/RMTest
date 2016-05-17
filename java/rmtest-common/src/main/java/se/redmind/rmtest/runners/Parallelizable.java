package se.redmind.rmtest.runners;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.TestClass;
import org.slf4j.LoggerFactory;

/**
 * @author Jeremy Comte
 */
public interface Parallelizable {

    default void parallelize() {
        parallelize(getTestClass().getJavaClass().getAnnotation(Parallelize.class));
    }

    default void parallelize(Parallelize parallelize) {
        int nThreads = 1;
        String threads = System.getProperty("junit.parallel.threads");
        if (threads != null && threads.matches("[0-9]+")) {
            nThreads = Integer.parseInt(threads);
        } else if (parallelize != null && parallelize.drivers()) {
            nThreads = parallelize.threads() > -1 ? parallelize.threads() : (Runtime.getRuntime().availableProcessors() / 2) + 1;
        }
        if (nThreads > 1) {
            LoggerFactory.getLogger(this.getClass()).info("will run " + nThreads + " test" + (nThreads > 1 ? "s" : "") + " in parallel");
            setScheduler(new Scheduler(nThreads));
        }
    }

    TestClass getTestClass();

    void setScheduler(RunnerScheduler scheduler);

    public static class Scheduler implements RunnerScheduler {

        private final ExecutorService executor;

        public Scheduler(int nThreads) {
            executor = Executors.newFixedThreadPool(nThreads);
        }

        @Override
        public void finished() {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException exc) {
                throw new RuntimeException(exc);
            }
        }

        @Override
        public void schedule(Runnable runnable) {
            executor.submit(runnable);
        }
    }

}
