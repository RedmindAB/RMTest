package se.redmind.rmtest.cucumber.rest;

import static spark.Spark.*;

import java.util.HashMap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import se.redmind.utils.Fields;
import spark.Spark;
import spark.webserver.JettySparkServer;

public class SparkServer {

    public void init() {
        init();
    }
    
    private HashMap<String, JsonElement> db = new HashMap<String, JsonElement>();

    Gson gson = new Gson();
	public int localPort;

    public SparkServer initServices() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    	Spark.port(0);
        //root
        get("/", (req, res) -> "hello");
        //status
        get("/status/:status", (req, res) -> {
            int code = Integer.valueOf(req.params("status"));
            res.status(code);
            return "";
        });

        //json
        get("/json", (req, res) -> gson.fromJson(req.body(), JsonElement.class));
        get("/param", (req, res) -> {
            JsonObject json = new JsonObject();
            req.queryParams().forEach(key -> json.addProperty(key, req.queryParams(key)));
            return json;

        });
        get("/cookie/:cookie", (req, res) -> {
        	JsonObject json = new JsonObject();
        	json.addProperty(req.params("cookie"), req.cookie(req.params("cookie")));
        	return json;
        });
        get("/header/:header", (req,res) -> {
        	String headerToValidate = req.params("header");
			String headerValue = req.headers(headerToValidate);
        	res.header(headerToValidate, headerValue);
        	return new JsonObject();
        });
        //db
        get("/db/:id", (req,res) -> db.get(req.params("id")));
        delete("/db/:id", (req, res) -> {
        	db.remove(req.params("id"));
        	return true;
        });
        patch("/db/:id", (req,res) -> {
        	db.put(req.params("id"), gson.fromJson(req.body(), JsonElement.class));
        	return true; 
        });
        post("/db/:id", (req,res) -> {
        	db.put(req.params("id"), gson.fromJson(req.body(), JsonElement.class));
        	return true;
        });
        put("/db/:id", (req,res) -> {
        	db.put(req.params("id"), gson.fromJson(req.body(), JsonElement.class));
        	return true;
        });

        //Filters
        after("/*", (req, res) -> res.header("Content-Type", "application/json"));
        
        awaitInitialization();
        Spark.awaitInitialization();
        JettySparkServer sparkServer = Fields.getValue(Spark.getInstance(), "server");
        Server jettyServer = Fields.getValue(sparkServer, "server");
        ServerConnector connector = (ServerConnector) jettyServer.getConnectors()[0];
        localPort = connector.getLocalPort();
        return this;
    }
    
    public int getLocalPort(){
    	return localPort;
    }
}
