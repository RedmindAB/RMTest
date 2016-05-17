package se.redmind.rmtest;

import java.util.Optional;
import java.util.function.Function;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.testdroid.api.*;
import com.testdroid.api.model.APIDevice;
import io.appium.java_client.AppiumDriver;
import se.redmind.rmtest.config.TestDroidConfiguration;

/**
 * @author Jeremy Comte
 */
public class TestDroidDriverWrapper extends AppiumDriverWrapper {

    private final APIClient client;

    public TestDroidDriverWrapper(TestDroidConfiguration configuration, APIClient client, DesiredCapabilities capabilities, String description, Function<DesiredCapabilities, AppiumDriver<WebElement>> function) {
        super(configuration, capabilities, description, function);
        this.client = client;
    }

    @Override
    public TestDroidConfiguration getConfiguration() {
        return (TestDroidConfiguration) super.getConfiguration();
    }

    public Optional<APIDevice> getFirstNonLockedDevice(APIDevice.OsType osType, int requiredApiLevel) throws APIException {
        APIListResource<APIDevice> devicesResource = client.getDevices(new APIDeviceQueryBuilder().offset(0).limit(getConfiguration().maxDevices).search("")
            .sort(APIDevice.class, new APISort.SortItem(APISort.Column.SOFTWARE_API_LEVEL, APISort.Type.DESC)));
        for (APIDevice device : devicesResource.getEntity().getData()) {
            if (!device.isLocked()) {
                if (device.getOsType().equals(osType)) {
                    if (device.getSoftwareVersion().getApiLevel() >= requiredApiLevel) {
                        return Optional.of(device);
                    }
                }
            }
        }

        return Optional.empty();
    }

}
