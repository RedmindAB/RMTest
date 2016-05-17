package se.redmind.rmtest.selenium;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Strings;
import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.rmtest.runners.Parallelize;
import se.redmind.rmtest.runners.WebDriverRunner;
import se.redmind.rmtest.runners.WebDriverRunnerOptions;
import se.redmind.utils.Try;

import static org.junit.Assert.assertTrue;

@RunWith(WebDriverRunner.class)
@WebDriverRunnerOptions(reuseDriver = true, parallelize = @Parallelize)
public class GoogleExample {

    protected final WebDriverWrapper<?> wrapper;

    public GoogleExample(WebDriverWrapper<?> driverWrapper) {
        this.wrapper = driverWrapper;
    }

    @Test
    public void testGoogle() throws Exception {
        wrapper.getDriver().get("http://www.google.se");

        String pageTitle = Try.toGet(() -> wrapper.getDriver().getTitle())
            .until(value -> !Strings.isNullOrEmpty(value))
            .delayRetriesBy(500)
            .nTimes(10);

        assertTrue(pageTitle.startsWith("Goo"));
    }

}
