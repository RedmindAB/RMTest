package se.redmind.rmtest.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import se.redmind.rmtest.appium.AppiumTest;
import se.redmind.rmtest.runners.Parallelize;
import se.redmind.rmtest.runners.ParallelizedSuite;
import se.redmind.rmtest.testdroid.ScreenShotExample;

/**
 * @author Jeremy Comte
 */
@RunWith(ParallelizedSuite.class)
@Parallelize(threads = 16)
@Suite.SuiteClasses({
    se.redmind.rmtest.cucumber.GoogleExample.class,
    se.redmind.rmtest.selenium.GoogleExample.class,
    AppiumTest.class,
    ScreenShotExample.class
})
public class SuiteTest {
}
