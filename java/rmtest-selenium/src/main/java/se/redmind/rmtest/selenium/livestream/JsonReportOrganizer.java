package se.redmind.rmtest.selenium.livestream;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class JsonReportOrganizer {

    private JsonObject build;
    private int regularTestCnt = 0;
    private List<JsonElement> gherkinScenarios;
    private List<JsonElement> regularTests;
    private HashMap<String, ArrayList<JsonObject>> gherkinMap;

    public JsonReportOrganizer(JsonObject build) {
        this.build = build;
        gherkinScenarios = new ArrayList<>();
        regularTests = new ArrayList<>();
        gherkinMap = new HashMap<>();
        JsonArray tests = sortArrayById(build.get("tests").getAsJsonArray());
        for (JsonElement entry : tests) {
            JsonObject test = entry.getAsJsonObject();
            if (test.get("isGherkin").getAsBoolean()) {
                gherkinScenarios.add(test);
            } else {
                regularTests.add(test);
                regularTestCnt++;
            }
        }
        populateGherkinMap();
        build.addProperty("totalTests", getTestCount());
        build.add("tests", parseToGherkinFormat());
    }

    public JsonObject build() {
        return build;
    }

    private JsonArray parseToGherkinFormat() {
        JsonArray gherkin = new JsonArray();
        double runTime;

        for (ArrayList<JsonObject> jsonMapObjects : gherkinMap.values()) {
            runTime = 0.0;
            JsonObject testScenario = new JsonObject();
            JsonArray stepObjects = new JsonArray();
            JsonObject step = new JsonObject();
            stepObjects.add(step);
            for (int i = 0; i < jsonMapObjects.size(); i++) {
                JsonObject object = jsonMapObjects.get(i);
                boolean stepFailed = testNotPassed(object);
                if (i == 0) {
                    testScenario = object;
                }
                if (stepFailed) {
                    testScenario.addProperty("result", "failure");
                    if (object.get("failureMessage") != null) {
                        testScenario.addProperty("failureMessage", object.get("failureMessage").getAsString());
                    }
                }
                step.addProperty(String.valueOf(i + 1), object.get("method").getAsString() +
                        (stepFailed ? getResultString(object) : ""));
                runTime += object.get("runTime").getAsDouble();
            }
            testScenario.addProperty("runTime", runTime);
            testScenario.addProperty("method", testScenario.get("testclass").getAsString());
            testScenario.addProperty("testclass", testScenario.get("feature").getAsString());
            testScenario.add("steps", stepObjects);
            gherkin.add(testScenario);
        }
        /* Also add the regular tests */
        regularTests.forEach(gherkin::add);
        return gherkin;
    }

    private String getResultString(JsonObject object) {
        if (object.get("result").getAsString().equals("failure")) {
            return "@ThisStepFailed@";
        } else {
            return "@ThisStepSkipped@";
        }
    }

    private boolean testNotPassed(JsonObject object) {
        return !object.get("result").getAsString().equals("passed");
    }

    private int populateGherkinMap() {
        for (JsonElement element : gherkinScenarios) {
            String key = getKey(element);
            if (gherkinMap.containsKey(key)) {
                ArrayList<JsonObject> steps = gherkinMap.get(key);
                steps.add((JsonObject) element);
                gherkinMap.put(key, steps);
            } else {
                ArrayList<JsonObject> steps = new ArrayList<>();
                steps.add((JsonObject) element);
                gherkinMap.put(key, steps);
            }
        }
        return gherkinMap.size();
    }

    private String getKey(JsonElement element) {
        String feature = element.getAsJsonObject().get("feature").getAsString();
        String scenario = element.getAsJsonObject().get("testclass").getAsString();
        String deviceInfo = element.getAsJsonObject().get("deviceInfo").toString();
        return feature + ", " + scenario + ", " + deviceInfo;
    }

    private JsonArray sortArrayById(JsonArray tests) {
        JsonArray sortedArray = new JsonArray();
        List<JsonElement> jsonElements = new ArrayList<>();
        for (JsonElement element : tests) {
            jsonElements.add(element);
        }
        Collections.sort(jsonElements, (o1, o2) -> {
            Integer firstId = o1.getAsJsonObject().get("id").getAsInt();
            Integer secondId = o2.getAsJsonObject().get("id").getAsInt();
            return firstId.compareTo(secondId);
        });
        jsonElements.forEach(sortedArray::add);
        return sortedArray;
    }

    public int getTestCount() {
        return regularTestCnt + getGherkinCount();
    }

    private int getGherkinCount() {
        return gherkinMap.size();
    }

    public List<JsonElement> getGherkinScenarios() {
        return gherkinScenarios;
    }

    public List<JsonElement> getRegularTests() {
        return regularTests;
    }

    public HashMap<String, ArrayList<JsonObject>> getGherkinMap() {
        return gherkinMap;
    }
}
