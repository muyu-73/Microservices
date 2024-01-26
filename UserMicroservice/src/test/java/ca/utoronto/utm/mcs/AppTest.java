package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
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
    @Test
    public void userLoginPass() throws IOException, InterruptedException, JSONException {
        JSONObject step = new JSONObject()
                .put("name", "testUser1")
                .put("email", "test1@gmail.com")
                .put("password", "xxxxx");
        sendRequest("/user/register", "POST",
                step.toString());

        JSONObject confirmReq = new JSONObject()
                .put("email", "test1@gmail.com")
                .put("password", "xxxxx");

        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());

    }

    @Test
    public void userLoginFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("email", "test@gmail.com");

        HttpResponse<String> response = sendRequest("/user/register", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
    }

    @Test
    public void userRegisterPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("name", "testUser3")
                .put("email", "test3@gmail.com")
                .put("password", "xxxxx");

        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void userRegisterFail() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject()
                .put("email", "no@gmail.com");

        HttpResponse<String> response = sendRequest("/user/login", "POST",
                confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());

    }
}
