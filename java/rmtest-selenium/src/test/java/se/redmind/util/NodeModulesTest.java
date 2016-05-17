package se.redmind.util;

import static org.junit.Assert.*;

import java.io.File;
import java.util.UUID;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import se.redmind.utils.NodeModules;
import se.redmind.utils.TestHome;

public class NodeModulesTest {

    private static final File file = new File("/tmp/" + UUID.randomUUID() + "/node_modules/");
    private static final File noNodeModules = new File("/tmp/" + UUID.randomUUID() + "/no_node/");

    @BeforeClass
    public static void before() {
        file.mkdirs();
        file.deleteOnExit();
        noNodeModules.mkdirs();
        noNodeModules.deleteOnExit();
    }

    @Test
    public void test_modulePath() {
        Assume.assumeTrue(file.exists());
        if (!TestHome.isWindows()) {
            String path = NodeModules.path(file.getAbsolutePath());
            assertEquals(file.getAbsolutePath(), path);
        }
    }

    @Test
    public void no_modulesPath() {
        Assume.assumeTrue(noNodeModules.exists());
        if (!TestHome.isWindows()) {
            String noModule = NodeModules.path(noNodeModules.getAbsolutePath());
            assertNull(noModule);
        }
    }

}
