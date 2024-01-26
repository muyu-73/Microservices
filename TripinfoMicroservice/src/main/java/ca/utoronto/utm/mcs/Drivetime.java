package ca.utoronto.utm.mcs;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * other microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO

        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String tripId = params[3].split("\\?")[0];

        try {
            MongoCursor<Document> trip = this.dao.getTripByUid(tripId);
            if (!trip.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            Document doc = trip.next();

            String driverUid = doc.getString("driver");
            String passengerUid = doc.getString("passenger");

            String apiUrl = "http://locationmicroservice:8000";
            String endpoint = String.format("/location/navigation/%s?passengerUid=%s",
                    driverUid, passengerUid);
            HttpResponse<String> response = this.sendRequest(endpoint, "GET", "", apiUrl);

            JSONObject res = new JSONObject(response.body());
            if (response.statusCode() == 200) {
                //covert json obj to list
                int arrTime = res.getJSONObject("data").getInt("total_time");

                JSONObject result = new JSONObject();
                JSONObject data = new JSONObject();

                data.put("arrival_time", arrTime);
                result.put("data", data);

                this.sendResponse(r, result, 200);
            } else {
                this.sendStatus(r, response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
