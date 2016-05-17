package se.redmind.rmtest.selenium.grid;

import java.util.ArrayList;
import java.util.Map;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class HubNodesStatus {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private JsonArray nodesAsJson;
    private ArrayList<RegistrationRequest> nodesAsRegReqs = new ArrayList<>();

    public HubNodesStatus(String pHost, int pPort) {
        try {
            nodesAsJson = NodeInfoFromHub.main(pHost, pPort).getAsJsonArray("FreeProxies");
            RegistrationRequest currentNode;
            for (int i = 0; i < nodesAsJson.size(); i++) {
                currentNode = getRegRequest(nodesAsJson.get(i).getAsJsonObject());
                nodesAsRegReqs.add(currentNode);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static RegistrationRequest getRegRequest(JsonObject node) {
        RegistrationRequest request = new RegistrationRequest();

        JsonObject config = node.get("configuration").getAsJsonObject();
        Map<String, Object> configuration = new JsonToBeanConverter().convert(Map.class, config);
        // For backward compatibility numbers should be converted to integers
        configuration.keySet().forEach(key -> {
            Object value = configuration.get(key);
            if (value instanceof Long) {
                configuration.put(key, ((Long) value).intValue());
            }
        });
        request.setConfiguration(configuration);

        JsonArray capabilities = node.get("capabilities").getAsJsonArray();

        for (int i = 0; i < capabilities.size(); i++) {
            DesiredCapabilities cap = new JsonToBeanConverter().convert(DesiredCapabilities.class, capabilities.get(i));
            request.addDesiredCapability(cap);
        }

        return request;
    }

    public ArrayList<RegistrationRequest> getNodesAsRegReqs() {
        return nodesAsRegReqs;
    }
}
