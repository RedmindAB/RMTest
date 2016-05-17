package se.redmind.rmtest.selenium.framework;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import se.redmind.rmtest.config.*;
import se.redmind.rmtest.selenium.livestream.LiveStreamListener;

public class RmSuite extends Suite {

    private LiveStreamListener liveStreamListener;

    public RmSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
    }

    @Override
    public void run(RunNotifier notifier) {
        if (Configuration.current().drivers.stream().anyMatch(driver -> driver instanceof GridConfiguration && driver.as(GridConfiguration.class).enableLiveStream)) {
            liveStreamListener = new LiveStreamListener();
            notifier.addListener(liveStreamListener);
        }
        notifier.fireTestRunStarted(getDescription());
        super.run(notifier);
    }

    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        if (liveStreamListener != null) {
            notifier.addListener(liveStreamListener.getSubListener());
        }
        super.runChild(runner, notifier);
    }
}
