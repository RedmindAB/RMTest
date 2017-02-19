package se.redmind.utils;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHome {

    private TestHome() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHome.class);
    private static String testHome;

    @SuppressWarnings("CallToPrintStackTrace")
    public static String get() {
        if (testHome == null) {
            if (isWindows()) {
                testHomeWindows();
            } else {
                testHomePosix();
            }
        }
        return testHome;
    }

    private static void testHomePosix() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("We are on a POSIX system");
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getenv("HOME") + "/.RmTest")))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("TESTHOME=")) {
                    testHome = line.split("=")[1];
                }
            }
        } catch (IOException e) {
            // no legacy config, it's okay
        }
    }

    private static void testHomeWindows() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("We are on windows");
        }
        testHome = System.getenv("TESTHOME");
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static boolean isWindows() {
        return getOsName().startsWith("Windows");
    }

}
