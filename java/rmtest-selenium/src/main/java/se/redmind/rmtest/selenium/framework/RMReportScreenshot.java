package se.redmind.rmtest.selenium.framework;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.redmind.rmtest.WebDriverWrapper;
import se.redmind.utils.StackTraceInfo;
import se.redmind.utils.TestHome;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class RMReportScreenshot {

    private static final Logger LOGGER = LoggerFactory.getLogger(RMReportScreenshot.class);
    private static final int MAX_LONG_SIDE = 1920;
    private static final HashMap<String, Integer> FILENAME_NUMBERS = new HashMap<>();
    private String screenShotFormat;
    private final WebDriverWrapper<?> driverWrapper;

    public RMReportScreenshot(WebDriverWrapper<?> driverWrapper, String screenShotFormat) {
        this.driverWrapper = driverWrapper;
        this.screenShotFormat = screenShotFormat;
        if(screenShotFormat.equals(null) || screenShotFormat.isEmpty()){
            screenShotFormat="png";
        }
    }

    public RMReportScreenshot(WebDriverWrapper<?> driverWrapper) {
        this(driverWrapper, "png");
    }


    /**
     * this method should be called directly from a test-method, the filename will have the name of the invoked class and method inside it. if more than one
     * screenshot is taken in the same method make sure that the screenshot is unique for each screenshot.
     *
     * @param prefix - optional, description of the screenshot can be null or empty. if there are two or more screenshots taken in the same method without a
     * prefix the oldest file will be over written
     */
    public void takeScreenshot(String prefix) {
        String className = StackTraceInfo.getInvokingClassName();
        String methodName = StackTraceInfo.getInvokingMethodName();
        takeScreenshot(className, methodName, prefix);
    }

    /**
     * USE WITH CAUTION!
     *
     * This method should be an alternative to the takeScreenshot method if needed, examples of use is Navigation classes. its important that the class and
     * method name is the same as they are stored in RMReport.
     *
     * @param className - name of the testclass.
     * @param methodName - name of the test method that was invoked.
     * @param prefix - optional, description of the screenshot can be null or empty. if there are two or more screenshots taken in the same method without a
     * prefix the oldest file will be over written
     */
    public void takeScreenshot(String className, String methodName, String prefix) {
        File scrFile = ((TakesScreenshot) driverWrapper.getDriver()).getScreenshotAs(OutputType.FILE);
        BufferedImage image = fileToImage(scrFile);
        if (isResizeNecessary(image)) {
            image = resizeImage(image);
        }
        String filename = getFileName(className, methodName, prefix);
        if (filename == null) {
            return;
        }
        saveImage(image, filename);
    }

    private void saveImage(BufferedImage image, String filename) {
        try {
            ImageIO.write(image, screenShotFormat, new File(filename));
        } catch (IOException e) {
            LOGGER.error("Image: " + image + " filename: " + filename, e);
        }
    }

    private String getFileName(String className, String methodName, String prefix) {
        String timestamp = System.getProperty("rmt.timestamp");
        if (timestamp == null) {
            LOGGER.warn("no rmt.timestamp property given, skipping screenshot ...");
            return null;
        }
        timestamp = timestamp.replace("-", "");
        String description = driverWrapper.getDescription();
        String filename = className + "." + methodName + "-" + timestamp + "[" + description + "]." + screenShotFormat;
        int screenshotNumber = getPrefixNumber(filename);
        if (prefix != null && prefix.length() > 0) {
            filename = prefix + "-_-" + filename;
        }
        filename = getSavePath(timestamp) + screenshotNumber + "-" + filename;
        return filename;
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        BufferedImage resizedImage = null;
        try {
            int type = getType(originalImage);
            resizedImage = resizeImageWithHint(originalImage, type);
        } catch (Exception e) {
            LOGGER.error("Could not resize screenshot image!", e);
        }
        return resizedImage;
    }

    private BufferedImage fileToImage(File scrFile) {
        try {
            return ImageIO.read(scrFile);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private BufferedImage resizeImageWithHint(BufferedImage originalImage, int type) {

        BufferedImage resizedImage = getResizedBufferedImage(originalImage, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        return resizedImage;
    }

    private BufferedImage getResizedBufferedImage(BufferedImage originalImage, int type) {
        int height = originalImage.getHeight();
        int width = originalImage.getWidth();
        float factor = 0;
        if (height > width) {
            factor = (float) MAX_LONG_SIDE / height;
            height = MAX_LONG_SIDE;
            width = (int) (width * factor);
        } else {
            factor = (float) MAX_LONG_SIDE / width;
            width = MAX_LONG_SIDE;
            height = (int) (height * factor);
        }
        return new BufferedImage(width, height, type);
    }

    private boolean isResizeNecessary(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        if (width > height) {
            return width > MAX_LONG_SIDE;
        } else {
            return height > MAX_LONG_SIDE;
        }
    }

    private int getType(BufferedImage originalImage) {
        return originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
    }

    private String getSavePath(String timestamp) {
        String path = TestHome.get() + "/RMR-Screenshots/" + timestamp + "/";
        File file = new File(path);
        file.mkdirs();
        return path;
    }

    public static synchronized int getPrefixNumber(String filename) {
        Integer number = FILENAME_NUMBERS.get(filename);
        if (number == null) {
            number = 0;
        }
        number++;
        FILENAME_NUMBERS.put(filename, number);
        return number;
    }
}
