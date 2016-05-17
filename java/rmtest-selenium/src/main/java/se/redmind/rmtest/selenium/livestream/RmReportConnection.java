package se.redmind.rmtest.selenium.livestream;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import se.redmind.rmtest.config.Configuration;

public class RmReportConnection {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String rmrLiveAddress;
    private final int rmrLivePort;
    private Socket socket;
    private PrintWriter out;
    private boolean isConnected;

    public RmReportConnection() {
        Configuration config = Configuration.current();
        this.rmrLiveAddress = config.rmReportIP;
        this.rmrLivePort = config.rmReportLivePort;
    }

    public boolean connect() {
        logger.info("Connecting to RMReport...");
        try {
            socket = new Socket(rmrLiveAddress, rmrLivePort);
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Could not connect to RMReport with: " + rmrLiveAddress + ":" + rmrLivePort);
            logger.error("RMReport might not be online or the config is not correct...");
            return false;
        }
        logger.info("Connection etablished.");
        isConnected = true;
        return true;
    }

    public boolean close() {
        logger.info("Closing connection to RMReport...");
        try {
            socket.close();
            out.close();
            logger.info("Connection closed");
        } catch (NullPointerException | IOException e) {
            logger.error("Could not close socket...", e);
            return false;
        }
        return true;
    }

    public synchronized void sendMessage(String type, JsonObject message) {
        send(type + "@" + new Gson().toJson(message));
    }

    public synchronized void sendMessage(String message) {
        send("message@" + message);
    }

    public synchronized void sendMessage(String type, String message) {
        send(type + "@" + message);
    }

    public synchronized void sendSuiteFinished() {
        send("!suiteFinished@");
    }

    public synchronized void sendClose() {
        send("!close@");
    }

    private synchronized void send(String message) {
        try {
            out.println(message);
            out.flush();
        } catch (NullPointerException e) {
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

}
