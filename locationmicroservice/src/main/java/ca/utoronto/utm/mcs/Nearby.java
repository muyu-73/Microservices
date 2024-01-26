package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Nearby extends Endpoint {
    
    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String params[] = r.getRequestURI().toString().split("/");
            if (params.length != 4 || params[3].isEmpty() || !params[3].contains("radius=")
                    || params[3].split("\\?").length != 2) {
                this.sendStatus(r, 400);
                return;
            }
            String userUid = params[3].split("\\?")[0];
            int radius = Integer.parseInt(getParamMap(r.getRequestURI().getQuery()).get("radius"));
            Result location = this.dao.getUserLocationByUid(userUid);
            if (!location.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            Result drivers = this.dao.getDriverWithinRadius(userUid, radius);
            if (!drivers.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }
            JSONObject result = new JSONObject();
            JSONObject data = new JSONObject();
            while (drivers.hasNext()) {
                Record driver = drivers.next();
                String uid = driver.get(0).get("uid").asString();
                double longti = driver.get(0).get("longitude").asDouble();
                double lat = driver.get(0).get("latitude").asDouble();
                String street = driver.get(0).get("street").asString();
                JSONObject driverInfo = new JSONObject();
                driverInfo.put("longitude", longti);
                driverInfo.put("latitude", lat);
                driverInfo.put("street", street);
                data.put(uid, driverInfo);
            }
            result.put("status", "OK");
            result.put("data", data);
            this.sendResponse(r, result, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
