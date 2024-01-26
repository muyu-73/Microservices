package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        //sanitized input
        String body = Utils.convert(r.getRequestBody());
        JSONObject obj = new JSONObject(body);
        String[] fields = {"driver", "passenger", "startTime"};
        Class<?>[] fieldClasses = {String.class, String.class, Integer.class};
        if (!this.validateFields(obj, fields, fieldClasses)) {
            sendStatus(r, 400);
            return;
        }

        try {
            String driver = obj.getString("driver");
            String passenger = obj.getString("passenger");
            int startTime = obj.getInt("startTime");
            if (driver.trim().equals("") || passenger.trim().equals("")) {
                this.sendStatus(r, 400);
                return;
            }

            String id = this.dao.confirmTrip(driver, passenger, startTime);

            if (id.length() <= 0) {
                this.sendStatus(r, 500);
                return;
            }

            JSONObject result = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("_id", id);
            result.put("data", data);

            this.sendResponse(r, result, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }

    }
}
