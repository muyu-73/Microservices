package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class Passenger extends Endpoint {

    private MongoCursor<Document> trpis;

    /**
     * GET /trip/passenger/:uid
     * @param uid
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException,JSONException{
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        String uid = params[3].split("\\?")[0];


        try {
            MongoCursor<Document> trips = this.dao.getPassengerTrips(uid);

            if (!trips.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            JSONObject result = new JSONObject();
            JSONObject data = new JSONObject();
            JSONArray info = new JSONArray();

            while (trips.hasNext()) {
                Document trip = trips.next();
                JSONObject tripObj = new JSONObject();
                tripObj.put("_id", trip.get("_id"))
                        .put("distance", trip.get("distance"))
                        .put("totalCost", trip.get("totalCost"))
                        .put("discount", trip.get("discount"))
                        .put("startTime", trip.get("startTime"))
                        .put("endTime", trip.get("endTime"))
                        .put("timeElapsed", trip.get("timeElapsed"))
                        .put("driver", trip.get("driver"));
                info.put(tripObj);
            }
            
            data.put("trips", info);
            result.put("data", data);
            this.sendResponse(r, result, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
