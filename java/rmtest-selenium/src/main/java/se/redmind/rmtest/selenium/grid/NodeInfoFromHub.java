package se.redmind.rmtest.selenium.grid;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpRequest;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class NodeInfoFromHub {

    public static JsonObject main(String pHost, int pPort) throws IOException {
        URL proxyApi = new URL("http://" + pHost + ":" + pPort + "/grid/admin/GridQueryServlet");
        HttpClient client = HttpClientBuilder.create().build();
        BasicHttpRequest r = new BasicHttpRequest("GET", proxyApi.toExternalForm());
        HttpHost host = new HttpHost(pHost, pPort);
        HttpResponse response = client.execute(host, r);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        JsonObject o = extractObject(response);
        return o;

    }

    private static JsonObject extractObject(HttpResponse resp) throws IOException {
        StringBuilder s = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                s.append(line);
            }
        }
        return new Gson().fromJson(s.toString(), JsonObject.class);
    }
}
