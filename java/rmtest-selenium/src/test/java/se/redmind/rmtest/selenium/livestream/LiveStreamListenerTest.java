package se.redmind.rmtest.selenium.livestream;

import org.junit.Test;
import org.junit.runner.Description;

/**
 * Created by victormattsson on 2016-02-02.
 */
public class LiveStreamListenerTest {

    LiveStreamListener listener = new LiveStreamListener();

    @Test
    public void test() throws Exception {
        listener.testRunStarted(Description.createTestDescription(LiveStreamListenerTest.class, "test"));
    }

}
