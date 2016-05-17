package se.redmind.rmtest.selenium.livestream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import se.redmind.rmtest.config.Configuration;

/**
 * This class had the goal to me communication between RMTest and RMReport able. However, this implementation did not go as planned. On the RMReport side the
 * behavior is flaky as a noggenfogger, so for the time being the RMReportConnection class will be commented out until the RMReport side is fixed.
 *
 * @author gustavholfve
 */
public class LiveStreamListener extends RunListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private volatile RmTestResultBuilder resBuilder;
    //    private final RmReportConnection rmrConnection;
    private volatile HashSet<String> finishedTests;
    private final boolean parentRunner;
    private final List<LiveStreamListener> listeners;
    private volatile HashMap<String, Long> testStartTimes;
    private volatile static boolean isSaved = false;

    public LiveStreamListener() {
        resBuilder = new RmTestResultBuilder();
        finishedTests = new HashSet<>();
        parentRunner = true;
        listeners = new ArrayList<>();
//        rmrConnection = new RmReportConnection();
        this.testStartTimes = new HashMap<>();
//        addShutdownHook(rmrConnection);
    }

    private LiveStreamListener(RmTestResultBuilder resBuilder, RmReportConnection connection) {
        this.resBuilder = resBuilder;
        this.finishedTests = new HashSet<>();
        this.parentRunner = false;
        this.listeners = new ArrayList<>();
//        this.rmrConnection = connection;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        initSuite(description, 0);
//        rmrConnection.connect();
//        rmrConnection.sendMessage("suite", resBuilder.build());
        super.testRunStarted(description);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        resBuilder.addTest(description.getDisplayName(), description);
        if (parentRunner) {
//            rmrConnection.sendMessage("testStart", id);
            testStartTimes.put(description.getDisplayName(), System.currentTimeMillis());
        }
        super.testStarted(description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        String displayName = description.getDisplayName();
        resBuilder.addFinishedTest(description.getDisplayName());
        finishedTests.add(displayName);
        if (parentRunner) {
            double runTime = (double) (System.currentTimeMillis() - testStartTimes.get(displayName)) / 1000;
            resBuilder.addRunTime(displayName, runTime);
//            rmrConnection.sendMessage("test", resBuilder.getTest(description.getDisplayName()));
        }
        super.testFinished(description);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        String description = failure.getDescription().getDisplayName();
        resBuilder.addAssumptionFailure(description, failure);
        super.testAssumptionFailure(failure);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        String description = failure.getDescription().getDisplayName();
        resBuilder.addTestFailure(description, failure);
        super.testFailure(failure);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        resBuilder.addIgnoredTest(description.getDisplayName());
//        rmrConnection.sendMessage("test", resBuilder.getTest(description.getDisplayName()));
        super.testIgnored(description);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        if (parentRunner) {
            resBuilder.setResult(result);
            JsonObject results = resBuilder.build();
//            rmrConnection.sendSuiteFinished();
//            rmrConnection.sendClose();
//            if(rmrConnection.isConnected())rmrConnection.close();
            saveReport();
            super.testRunFinished(result);
        }
    }

    private void addShutdownHook(RmReportConnection con) {
        Runtime.getRuntime().addShutdownHook(new Thread(new LiveTestShutdownHook(con)));
    }

    private synchronized void saveReport() {
        if (!isSaved) {
            String suitename = resBuilder.getSuiteName();
            String timestamp = resBuilder.getTimestamp();
            String savePath = Configuration.current().jsonReportSavePath;

            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            JsonObject build = resBuilder.build();
            JsonReportOrganizer jsonReportOrganizer = new JsonReportOrganizer(build);
            String filename = suitename + "-" + timestamp + ".json";
            try {
                String concatFilename = savePath + "/" + filename;
                try (PrintWriter writer = new PrintWriter(concatFilename, "UTF-8")) {
                    writer.print(new GsonBuilder().setPrettyPrinting().create().toJson(jsonReportOrganizer.build()));
                    isSaved = true;
                    logger.info("Saved report as Json to: " + concatFilename);
                }
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void initSuite(Description desc, int level) {
        level++;
        String suitename = System.getProperty("rmt.live.suitename");
        if (suitename != null && level == 1) {
            resBuilder.setSuiteName(suitename);
        } else if (level == 1 && desc.isSuite()) {
            resBuilder.setSuiteName(desc.getClassName());
        }
        if (desc.getClassName().startsWith("Feature: ")) {
            resBuilder.setCurrentFeature(desc.getClassName());
        }
        if (resBuilder.isScenario(desc)) {
            String scenarioName = resBuilder.getScenarioName(desc);
            resBuilder.setCurrentScenario(scenarioName.substring(0, scenarioName.lastIndexOf("[")).trim());
        }
        if (desc.isTest()) {
            resBuilder.addTest(desc.getDisplayName(), desc);
        }
        ArrayList<Description> children = desc.getChildren();
        for (Description description : children) {
            initSuite(description, level);
        }
    }

    public LiveStreamListener getSubListener() {
        LiveStreamListener subListener = new LiveStreamListener(resBuilder, null); // this need to be set to the connection in this class.
        listeners.add(subListener);
        return subListener;
    }

}
