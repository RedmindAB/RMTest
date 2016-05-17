package se.redmind.rmtest.selenium.grid.servlets;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openqa.grid.internal.ProxySet;
import org.openqa.grid.internal.Registry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.web.servlet.RegistryBasedServlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GridQueryServlet extends RegistryBasedServlet {

    private static final long serialVersionUID = -1975392591408983229L;

    public GridQueryServlet() {
        this(null);
    }

    public GridQueryServlet(Registry registry) {
        super(registry);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);

    }

    protected void process(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);
        JsonObject res;

        res = getResponse();
        response.getWriter().print(res);
        response.getWriter().close();

    }

    private JsonObject getResponse() throws IOException {
        JsonObject requestJSON = new JsonObject();
        ProxySet proxies = this.getRegistry().getAllProxies();
        Iterator<RemoteProxy> iterator = proxies.iterator();
        JsonArray busyProxies = new JsonArray();
        JsonArray freeProxies = new JsonArray();
        while (iterator.hasNext()) {
            RemoteProxy eachProxy = iterator.next();
            if (eachProxy.isBusy()) {
                busyProxies.add(eachProxy.getOriginalRegistrationRequest().getAssociatedJSON());
            } else {
                freeProxies.add(eachProxy.getOriginalRegistrationRequest().getAssociatedJSON());
            }
        }
        requestJSON.add("BusyProxies", busyProxies);
        requestJSON.add("FreeProxies", freeProxies);

        return requestJSON;
    }

}
