package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Please write your tests in this class. 
 */
 
public class AppTest {
    final static String API_URL = "http://0.0.0.0:8004";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody) throws
            InterruptedException, IOException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException, JSONException, JSONException {
        JSONObject passenger = new JSONObject()
                .put("uid", "pass1")
                .put("is_driver", false);
        JSONObject driver = new JSONObject()
                .put("uid", "driver1")
                .put("is_driver", true);

        JSONObject dundas = new JSONObject()
                .put("roadName", "Dundas")
                .put("hasTraffic", true);
        JSONObject queens = new JSONObject()
                .put("roadName", "Queens")
                .put("hasTraffic", true);
        JSONObject dundasLocation = new JSONObject()
                .put("longitude", 79.0358)
                .put("latitude", 42.0057)
                .put("street", "Dundas");

        JSONObject queensLocation = new JSONObject()
                .put("longitude", 79.3832)
                .put("latitude",  43.6532)
                .put("street", "Queens");

        JSONObject route = new JSONObject()
                .put("roadName1", "Queens")
                .put("roadName2", "Dundas")
                .put("hasTraffic", false)
                .put("time", 10);

        sendRequest("/location/road", "PUT", dundas.toString());
        sendRequest("/location/road", "PUT", queens.toString());
        sendRequest("/location/user", "PUT", passenger.toString());
        sendRequest("/location/user", "PUT", driver.toString());
        sendRequest("/location/pass1", "PATCH", dundasLocation.toString());
        sendRequest("/location/driver1", "PATCH", queensLocation.toString());
        sendRequest("/location/hasRoute", "POST", route.toString());
    }
    @Test
    public void getNearbyDriverPass() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/nearbyDriver/%s?radius=%d", "pass1", 300);
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject expectedDriver = new JSONObject()
                .put("longitude", 79.3832)
                .put("latitude",  43.6532)
                .put("street", "Queens");
        JSONObject expectedData = new JSONObject()
                .put("driver1", expectedDriver);
        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", expectedData);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void getNearbyDriverFail () throws IOException, InterruptedException {
        String endpoint = String.format("/location/nearbyDriver/%s", "pass1");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void getNavigationPass() throws IOException, InterruptedException, JSONException {
        String endpoint = String.format("/location/navigation/%s?passengerUid=%s", "driver1", "pass1");
        HttpResponse<String> confirmRes = sendRequest(endpoint, "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject dundas = new JSONObject()
                .put("street", "Dundas")
                .put("has_traffic", true)
                .put("time", 10);
        JSONObject queens = new JSONObject()
                .put("street", "Queens")
                .put("has_traffic", true)
                .put("time", 0);
        JSONArray expectedRoutes = new JSONArray();
        expectedRoutes.put(queens);
        expectedRoutes.put(dundas);

        JSONObject expectedData = new JSONObject()
                .put("total_time", 10)
                .put("route", expectedRoutes);
        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", expectedData);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void getNavigationFail() throws IOException, InterruptedException {
        String url = String.format("/location/navigation/%s", "driver");
        HttpResponse<String> response = sendRequest(url, "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }


}
