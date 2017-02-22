package se.redmind.utils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeModules {

    private static final Logger log = LoggerFactory.getLogger(NodeModules.class);

    public static String path() {
        String path = null;
        if (TestHome.get() != null) {
            path = path(TestHome.get());
        }
        if (path == null) {
            path = path(System.getProperty("user.dir"));
        }
        if (path == null) {
            log.warn("didn't find any node_modules folder ...");
        }
        return path;
    }

    public static String path(String basePath) {
        File file = new File(basePath);
        int size = file.getAbsolutePath().split(File.separator).length;
        log.debug("Searching for node_modules in " + file.getAbsolutePath() + " and its parents");
        for (int i = size - 1; i > 0; i--) {
            String absolutePath = file.toPath().getRoot() + file.toPath().subpath(0, i).toString();
            String testPath = absolutePath + File.separator + "node_modules";
            if (new File(testPath).exists()) {
                log.debug("found node_modules folder in " + testPath);
                return testPath;
            }
        }
        return null;
    }

}
