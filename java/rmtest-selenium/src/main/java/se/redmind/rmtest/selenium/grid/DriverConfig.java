package se.redmind.rmtest.selenium.grid;

import org.openqa.selenium.remote.DesiredCapabilities;

public interface DriverConfig {

    boolean eval(DesiredCapabilities capabilities, String description);

    void config(DesiredCapabilities capabilities);

}
