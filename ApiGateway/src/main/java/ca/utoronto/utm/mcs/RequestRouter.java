package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response
import java.util.HashMap;

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */
	public HashMap<Integer, String> errorMap;

	public RequestRouter() {
		errorMap = new HashMap<>();
		errorMap.put(200, "OK");
		errorMap.put(400, "BAD REQUEST");
		errorMap.put(401, "UNAUTHORIZED");
		errorMap.put(404, "NOT FOUND");
		errorMap.put(405, "METHOD NOT ALLOWED");
		errorMap.put(409, "CONFLICT");
		errorMap.put(500, "INTERNAL SERVER ERROR");
	}

	public void writeOutputStream(HttpExchange r, String response) throws IOException {
		OutputStream os = r.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
		String response = obj.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

	public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
		JSONObject res = new JSONObject();
		res.put("status", errorMap.get(statusCode));
		String response = res.toString();
		r.sendResponseHeaders(statusCode, response.length());
		this.writeOutputStream(r, response);
	}

	public HttpResponse<String> sendRequest(String endpoint, String method, String reqBody, String apiURL) throws
			InterruptedException, IOException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(apiURL + endpoint))
				.method(method, HttpRequest.BodyPublishers.ofString(reqBody))
				.build();

		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
        // TODO
		String uri = r.getRequestURI().toString();
		String method = r.getRequestMethod();
		String body = Utils.convert(r.getRequestBody());
		String[] params = uri.split("/");

		String microservice = params[1];
		if (params[1].isEmpty()) {
			try {
				this.sendStatus(r, 400);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
			return;
		}

		String apiUrl = "";
		switch (microservice) {
			case "location":
				apiUrl = "http://locationmicroservice:8000";
				break;
			case "trip":
				apiUrl = "http://tripinfomicroservice:8000";
				break;
			case "user":
				apiUrl = "http://usermicroservice:8000";
				break;
			default:
				try {
					this.sendStatus(r, 405);
					return;
				} catch (JSONException e) {
					throw new RuntimeException(e);
				}
		}

		try {
			HttpResponse<String> response = this.sendRequest(uri, method, body, apiUrl);
			JSONObject res = new JSONObject(response.body());
			int status = response.statusCode();
			if (status == 200) {
				this.sendResponse(r, res, status);
			} else {
				this.sendStatus(r, status);
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				this.sendStatus(r, 500);
			} catch (JSONException ex) {
				throw new RuntimeException(ex);
			}
		}


	}
}
