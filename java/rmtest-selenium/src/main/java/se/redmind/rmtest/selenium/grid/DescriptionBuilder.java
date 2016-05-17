package se.redmind.rmtest.selenium.grid;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.base.Strings;

public final class DescriptionBuilder {

    private DescriptionBuilder() {
    }

    /**
     * @param capabilities
     * @return
     */
    public static String buildDescriptionFromCapabilities(DesiredCapabilities capabilities) {
        String os = getOS(capabilities);
        String osVersion = getOsVersion(capabilities);
        String device = getDevice(capabilities);
        String browser = getBrowser(capabilities);
        String browserVersion = "UNKNOWN";
        return os + "_" + osVersion + "_" + device + "_" + browser + "_" + browserVersion;
    }

    public static String getBrowser(DesiredCapabilities capability) {
        String browser;
        if (isCapabilitySet("browserName", capability)) {
            browser = getSafeCapability("browserName", capability);
        } else if (isCapabilitySet("appPackage", capability)) {
            browser = getSafeCapability("appPackage", capability);
        } else {
            browser = "UNKNOWN";
        }
        return browser;
    }

    public static String getDevice(DesiredCapabilities capability) {
        String device;
        if (isCapabilitySet("deviceName", capability)) {
            device = getSafeCapability("deviceName", capability);
        } else {
            device = "UNKNOWN";
        }
        return device;
    }

    public static String getOsVersion(DesiredCapabilities capability) {
        String osVer;
        if (isCapabilitySet("platformVersion", capability)) {
            osVer = getSafeCapability("platformVersion", capability);
        } else {
            osVer = "UNKNOWN";
        }
        return osVer;
    }

    public static String getOS(DesiredCapabilities capability) {
        String os;
        if (isCapabilitySet("rmOsName", capability)) {
            os = getSafeCapability("rmOsName", capability);
        } else if (isCapabilitySet("platformName", capability)) {
            os = getSafeCapability("platformName", capability);
        } else if (isCapabilitySet("osname", capability)) {
            os = getSafeCapability("osname", capability);
        } else if (isCapabilitySet("platform", capability)) {
            os = getSafeCapability("platform", capability);
        } else {
            os = "UNKNOWN";
        }
        return os;
    }

    public static Boolean isCapabilitySet(String capName, DesiredCapabilities currentCapability) {
        return !Strings.isNullOrEmpty(getSafeCapability(capName, currentCapability));
    }

    public static String getSafeCapability(String capName, DesiredCapabilities currentCapability) {
        if (currentCapability.getCapability(capName) instanceof Platform) {
            return currentCapability.getCapability(capName).toString();
        } else {
            return (String) currentCapability.getCapability(capName);
        }

    }

}
