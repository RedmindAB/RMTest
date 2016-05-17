package se.redmind.rmtest.selenium.framework;

import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author petost
 */
public enum Environments {

    S4("chrome", "25", Platform.ANDROID),
    NEXUS7("chrome", "25", Platform.ANDROID),
    MAC_CHROME("chrome", "27", Platform.MAC);

    private final DesiredCapabilities mCapabilities;
    private final String mBrowserName;
    private final String mVersion;
    private final Platform mPlatform;

    /**
     * @param pBrowserName
     * @param pVersion
     * @param pPlatform
     */
    Environments(final String pBrowserName, final String pVersion, final Platform pPlatform) {
        this.mBrowserName = pBrowserName;
        this.mVersion = pVersion;
        this.mPlatform = pPlatform;
        mCapabilities = new DesiredCapabilities();
    }

    /**
     * @return
     */
    public DesiredCapabilities capabilities() {
        mCapabilities.setBrowserName(mBrowserName);
        mCapabilities.setVersion(mVersion);
        mCapabilities.setPlatform(mPlatform);
        return this.mCapabilities;
    }

    /**
     * @return
     */
    public String browserName() {
        return mBrowserName;
    }
}
