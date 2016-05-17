package se.redmind.rmtest.testdroid;

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testdroid.api.model.APIDevice;
import io.appium.java_client.AppiumDriver;
import se.redmind.rmtest.TestDroidDriverWrapper;
import se.redmind.rmtest.config.TestDroidConfiguration;
import se.redmind.rmtest.runners.FilterDrivers;
import se.redmind.rmtest.runners.WebDriverRunner;

@RunWith(WebDriverRunner.class)
@FilterDrivers(types = AppiumDriver.class)
public class ScreenShotExample {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TestDroidDriverWrapper wrapper;

    public ScreenShotExample(TestDroidDriverWrapper wrapper) {
        this.wrapper = wrapper;
        this.wrapper.addPreConfiguration(() -> {
            Optional<APIDevice> potentialDevice = wrapper.getFirstNonLockedDevice(APIDevice.OsType.ANDROID, 21);
            if (potentialDevice.isPresent()) {
                APIDevice device = potentialDevice.get();
                logger.info("found testdroid device: " + device.getDisplayName());
                TestDroidConfiguration configuration = wrapper.getConfiguration();
                String fileUUID = TestDroidDriverWrapper.uploadFile(configuration.appPath, configuration.serverUrl + "/upload", configuration.username, configuration.password);

                wrapper.getCapability().setCapability("platformName", "Android");
                wrapper.getCapability().setCapability("testdroid_target", "Android");
                wrapper.getCapability().setCapability("deviceName", "Android Device");
                wrapper.getCapability().setCapability("testdroid_project", "LocalAppium");
                wrapper.getCapability().setCapability("testdroid_testrun", "Android Run 1");
                wrapper.getCapability().setCapability("testdroid_device", device.getDisplayName()); // Freemium device
                wrapper.getCapability().setCapability("testdroid_app", fileUUID); //to use existing app using "latest" as fileUUID

                logger.info(wrapper.getCapability().toString());
                logger.info("Creating Appium session, this may take couple minutes..");
            } else {
                logger.error("didn't find any device matching our criterias ...");
            }
        });
    }

    @Test
    public void mainPageTest() throws IOException, InterruptedException {
        wrapper.takeScreenshot("start");
    }
}
