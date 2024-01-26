package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param _id
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {
        //sanitized input
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 3 || params[2].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String tripId = params[2].split("\\?")[0];

        String body = Utils.convert(r.getRequestBody());
        JSONObject obj = new JSONObject(body);
        String[] fields = {"distance", "endTime", "timeElapsed", "totalCost"};
        Class<?>[] fieldClasses = {double.class, Integer.class, String.class, double.class};
        if (!this.validateFields(obj, fields, fieldClasses)) {
            sendStatus(r, 400);
            return;
        }

        double distance = obj.getDouble("distance");
        int endTime = obj.getInt("endTime");
        String timeElapsed = obj.getString("timeElapsed");
        double totalCost = obj.getDouble("totalCost");
        if (distance < 0 || totalCost < 0) {
            this.sendStatus(r, 400);
            return;
        }

        //get discount, if not set it to 0
        double discount = 0;
        String[] dis = {"discount"};
        Class<?>[] disClass = {double.class};
        if (this.validateFields(obj, dis, disClass)) {
            discount = obj.getDouble("discount");
        }
        double driverPayout = totalCost * 0.65;
        String[] payout = {"driverPayout"};
        Class<?>[] payoutClass = {double.class};
        if (this.validateFields(obj, payout, payoutClass)) {
            driverPayout = obj.getDouble("driverPayout");
        }

        try {
            Boolean updated = this.dao.updateTrip(tripId, distance, endTime, timeElapsed, discount, totalCost,
                    driverPayout);
            if (updated) {
                this.sendStatus(r, 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
