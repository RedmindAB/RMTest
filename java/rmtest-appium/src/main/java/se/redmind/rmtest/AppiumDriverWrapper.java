package se.redmind.rmtest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import io.appium.java_client.AppiumDriver;
import se.redmind.rmtest.config.AppiumConfiguration;

public class AppiumDriverWrapper extends WebDriverWrapper<AppiumDriver<WebElement>> {

    private static final AtomicInteger SCREENSHOT_COUNTER = new AtomicInteger();
    private final AppiumConfiguration configuration;

    public AppiumDriverWrapper(AppiumConfiguration configuration, DesiredCapabilities capabilities, String description, Function<DesiredCapabilities, AppiumDriver<WebElement>> function) {
        super(capabilities, description, function);
        this.configuration = configuration;
    }

    public AppiumConfiguration getConfiguration() {
        return configuration;
    }

    public static String uploadFile(String appPath, String serverURL, String username, String password) throws IOException {
        HttpHeaders headers = new HttpHeaders();

        if (username != null) {
            headers.setBasicAuthentication(username, password);
        }

        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(request -> {
            request.setParser(new JsonObjectParser(new JacksonFactory()));
            request.setHeaders(headers);
        });

        MultipartContent multipartContent = new MultipartContent();
        FileContent fileContent = new FileContent("application/octet-stream", new File(appPath));

        MultipartContent.Part filePart = new MultipartContent.Part(new HttpHeaders()
            .set("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileContent.getFile().getName() + "\""), fileContent);
        multipartContent.addPart(filePart);

        HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(serverURL), multipartContent);

        HttpResponse response = request.execute();

        LoggerFactory.getLogger(AppiumDriverWrapper.class).info("response:" + response.parseAsString());

        AppiumResponse appiumResponse = request.execute().parseAs(AppiumResponse.class);

        LoggerFactory.getLogger(AppiumDriverWrapper.class).info("File id:" + appiumResponse.uploadStatus.fileInfo.file);

        return appiumResponse.uploadStatus.fileInfo.file;
    }

    public void takeScreenshot(String screenshotName) {
        String fullFileName = System.getProperty("user.dir") + "/Screenshots/" + getScreenshotsCounter() + "_" + screenshotName + ".png";
        screenshot(fullFileName);
    }

    private File screenshot(String name) {
        logger.info("Taking screenshot...");
        File scrFile = getDriver().getScreenshotAs(OutputType.FILE);
        try {
            File testScreenshot = new File(name);
            FileUtils.copyFile(scrFile, testScreenshot);
            logger.info("Screenshot saved as " + testScreenshot.getAbsolutePath());
            return testScreenshot;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private String getScreenshotsCounter() {
        int currentCounter = SCREENSHOT_COUNTER.incrementAndGet();
        if (currentCounter < 10) {
            return "0" + currentCounter;
        } else {
            return String.valueOf(currentCounter);
        }
    }

    public static class AppiumResponse {

        Integer status;

        @Key("sessionId")
        String sessionId;

        @Key("value")
        UploadStatus uploadStatus;

    }

    public static class UploadedFile {

        @Key("file")
        String file;
    }

    public static class UploadStatus {

        @Key("message")
        String message;

        @Key("uploadCount")
        Integer uploadCount;

        @Key("expiresIn")
        Integer expiresIn;

        @Key("uploads")
        UploadedFile fileInfo;
    }
}
