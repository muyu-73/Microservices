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

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException,JSONException{
        // TODO
        //sanitized input
        String body = Utils.convert(r.getRequestBody());
        JSONObject obj = new JSONObject(body);
        String[] fields = {"uid", "radius"};
        Class<?>[] fieldClasses = {String.class, Integer.class};
        if (!this.validateFields(obj, fields, fieldClasses)) {
            sendStatus(r, 400);
            return;
        }

        String uid = obj.getString("uid");
        int radius = obj.getInt("radius");
        if (uid.trim().equals("") || radius <= 0) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String apiUrl = "http://locationmicroservice:8000";
            String endpoint = String.format("/location/nearbyDriver/%s?radius=%d",
                    uid, radius);
            HttpResponse<String> response = this.sendRequest(endpoint, "GET", "", apiUrl);

            JSONObject res = new JSONObject(response.body());
            //send the corresponding response
            if (response.statusCode() == 200) {
                //covert json obj to list
                JSONObject data = res.getJSONObject("data");
                Iterator uids = data.keys();
                JSONArray driverUids = new JSONArray();
                while(uids.hasNext()) {
                    String key = (String) uids.next();
                    driverUids.put(key);
                }
                JSONObject result = new JSONObject();
                result.put("data", driverUids);

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
