package se.redmind.rmtest;

import java.util.*;

import org.openqa.grid.common.GridRole;
import org.openqa.grid.selenium.GridLauncher;

import com.google.common.collect.Lists;
import se.redmind.rmtest.config.ChromeConfiguration;
import se.redmind.rmtest.selenium.grid.servlets.GridQueryServlet;

/**
 * @author Jeremy Comte
 */
public class Main {

    static {
        if (System.getProperty("logback.configurationFile") == null) {
            System.setProperty("logback.configurationFile", "logback.xml");
        }
    }

    public static void main(String[] args) throws Exception {
        GridRole gridRole = GridRole.find(args);
        switch (gridRole) {
            case NOT_GRID:
            case NODE:
                if (System.getProperty(ChromeConfiguration.CHROMEDRIVER_SYSTEM_PROPERTY) == null) {
                    ChromeConfiguration.setChromePath();
                }
                GridLauncher.main(args);
                break;
            case HUB:
                List<String> argList = Lists.newArrayList(args);
                argList.add("-servlets");
                argList.add(GridQueryServlet.class.getCanonicalName());
                GridLauncher.main(argList.toArray(new String[0]));
                break;
        }
    }
}
