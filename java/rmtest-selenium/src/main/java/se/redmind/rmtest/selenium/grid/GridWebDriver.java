package se.redmind.rmtest.selenium.grid;

import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author Jeremy Comte
 */
public class GridWebDriver extends RemoteWebDriver {

    public GridWebDriver(URL remoteAddress, Capabilities desiredCapabilities) {
        super(remoteAddress, desiredCapabilities);
    }

}
