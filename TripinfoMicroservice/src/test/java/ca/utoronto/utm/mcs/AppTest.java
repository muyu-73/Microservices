package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public static void setUp() throws IOException, InterruptedException, JSONException {
        JSONObject passenger = new JSONObject()
                .put("uid", "pass")
                .put("is_driver", false);
        JSONObject driver = new JSONObject()
                .put("uid", "driver")
                .put("is_driver", true);

        JSONObject dundas = new JSONObject()
                .put("roadName", "Dundas")
                .put("hasTraffic", true);
        JSONObject queens = new JSONObject()
                .put("roadName", "Queens")
                .put("hasTraffic", true);
        JSONObject dundasLocation = new JSONObject()
                .put("longitude", 98.0358)
                .put("latitude", 62.0057)
                .put("street", "Dundas");

        JSONObject queensLocation = new JSONObject()
                .put("longitude", 98.3832)
                .put("latitude",  63.6532)
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
        sendRequest("/location/pass", "PATCH", dundasLocation.toString());
        sendRequest("/location/driver", "PATCH", queensLocation.toString());
        sendRequest("/location/hasRoute", "POST", route.toString());

    }

    @Test
    public void tripRequestPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("uid", "pass")
                .put("radius", 300);

        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());

        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", new JSONArray().put("driver"));
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void tripRequestFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("uid", "pass");
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("driver", "driver1")
                .put("passenger", "pass1")
                .put("startTime", 1615855949);

        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        assertEquals("OK", new JSONObject(confirmRes.body()).getString("status"));
        assertEquals(String.class, new JSONObject(confirmRes.body()).getJSONObject("data").get("_id").getClass());
    }

    @Test
    public void tripConfirmFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("driver", "driver1")
                .put("passenger", "pass1");

        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void patchTripPass() throws JSONException, IOException, InterruptedException {
        JSONObject step = new JSONObject()
                .put("driver", "driver2")
                .put("passenger", "pass2")
                .put("startTime", 1615865949);;
        HttpResponse<String> stepRes = sendRequest("/trip/confirm", "POST", step.toString());
        String id = new JSONObject(stepRes.body()).getJSONObject("data").getString("_id");
        JSONObject confirmReq = new JSONObject()
                .put("distance", 30)
                .put("endTime", 1615855949)
                .put("timeElapsed", "00:15:00")
                .put("totalCost", 65);
        String endpoint = String.format("/trip/%s", id);
        HttpResponse<String> confirmRes = sendRequest(endpoint, "PATCH", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void patchTripFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("distance", 30)
                .put("endTime", 1615855949)
                .put("timeElapsed", "00:15:00")
                .put("totalCost", 65);
        HttpResponse<String> confirmRes = sendRequest("/trip/", "PATCH", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void tripsForPassengerPass() throws JSONException, IOException, InterruptedException {
        JSONObject step1 = new JSONObject()
                .put("driver", "driver3")
                .put("passenger", "pass3")
                .put("startTime", 1615955941);
        HttpResponse<String> step1Res = sendRequest("/trip/confirm", "POST", step1.toString());
        String id = new JSONObject(step1Res.body()).getJSONObject("data").getString("_id");
        JSONObject step2 = new JSONObject()
                .put("distance", 30)
                .put("endTime", 1615955949)
                .put("timeElapsed", "00:15:00")
                .put("totalCost", 65);
        String stepEndpoint = String.format("/trip/%s", id);
        sendRequest(stepEndpoint, "PATCH", step2.toString());

        HttpResponse<String> confirmRes = sendRequest("/trip/passenger/pass3",
                "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject trip = new JSONObject()
                .put("_id", id)
                .put("distance", 30)
                .put("totalCost", 65)
                .put("discount", 0)
                .put("startTime", 1615955941)
                .put("endTime",1615955949)
                .put("timeElapsed", "00:15:00")
                .put("driver", "driver3");
        JSONArray expectedTrips = new JSONArray()
                .put(trip);
        JSONObject expectedData = new JSONObject()
                .put("trips", expectedTrips);
        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", expectedData);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void tripsForPassengerFail() throws IOException, InterruptedException {
        HttpResponse<String> confirmRes = sendRequest("/trip/passenger/",
                "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void tripsForDriverPass() throws JSONException, IOException, InterruptedException {
        JSONObject step1 = new JSONObject()
                .put("driver", "driver4")
                .put("passenger", "pass4")
                .put("startTime", 1615955946);
        HttpResponse<String> step1Res = sendRequest("/trip/confirm", "POST", step1.toString());
        String id = new JSONObject(step1Res.body()).getJSONObject("data").getString("_id");
        JSONObject step2 = new JSONObject()
                .put("distance", 30)
                .put("endTime", 1615955949)
                .put("timeElapsed", "00:15:00")
                .put("totalCost", 65);
        String stepEndpoint = String.format("/trip/%s", id);
        sendRequest(stepEndpoint, "PATCH", step2.toString());

        HttpResponse<String> confirmRes = sendRequest("/trip/driver/driver4",
                "GET", "");
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject trip = new JSONObject()
                .put("_id", id)
                .put("distance", 30)
                .put("driverPayout", 42.25)
                .put("startTime", 1615955946)
                .put("endTime",1615955949)
                .put("timeElapsed", "00:15:00")
                .put("passenger", "pass4");
        JSONArray expectedTrips = new JSONArray()
                .put(trip);
        JSONObject expectedData = new JSONObject()
                .put("trips", expectedTrips);
        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", expectedData);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void tripsForDriverFail() throws IOException, InterruptedException {
        HttpResponse<String> confirmRes = sendRequest("/trip/driver",
                "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }

    @Test
    public void driverTimePass() throws IOException, InterruptedException, JSONException {
        JSONObject step = new JSONObject()
                .put("driver", "driver")
                .put("passenger", "pass")
                .put("startTime", 1615855949);

        HttpResponse<String> stepRes = sendRequest("/trip/confirm", "POST",
                step.toString());
        String id = new JSONObject(stepRes.body()).getJSONObject("data").getString("_id");
        HttpResponse<String> confirmRes = sendRequest("/trip/driverTime/" + id, "GET", "");

        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
        JSONObject expectedData = new JSONObject()
                .put("arrival_time", 10);
        JSONObject expectedBody = new JSONObject()
                .put("status", "OK")
                .put("data", expectedData);
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(String.valueOf(expectedBody)), mapper.readTree(confirmRes.body()));
    }

    @Test
    public void driverTimeFail() throws IOException, InterruptedException {
        HttpResponse<String> confirmRes = sendRequest("/trip/driver/",
                "GET", "");
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, confirmRes.statusCode());
    }
}
