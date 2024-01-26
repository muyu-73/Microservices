package ca.utoronto.utm.mcs;

import java.io.IOException;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.types.Node;

public class Navigation extends Endpoint {
    
    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // TODO
        try {
            String params[] = r.getRequestURI().toString().split("/");
            if (params.length != 4 || params[3].isEmpty() || !params[3].contains("passengerUid=")
                    || params[3].split("\\?").length != 2) {
                this.sendStatus(r, 400);
                return;
            }

            String driverUid = params[3].split("\\?")[0];
            String passengerUid = getParamMap(r.getRequestURI().getQuery()).get("passengerUid");
            if (driverUid.trim().equals("") || passengerUid.trim().equals("")) {
                this.sendStatus(r, 400);
                return;
            }
            Result driver = this.dao.getUserByUid(driverUid);
            if (!driver.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }
            Record driverRecord = driver.next();
            if (!driverRecord.get(0).get("is_driver").asBoolean()) {
                this.sendStatus(r, 404);
                return;
            }
            String driverLocation = driverRecord.get(0).get("street").asString();
            Result passenger = this.dao.getUserByUid(passengerUid);
            String passengerLocation = passenger.next().get(0).get("street").asString();

            Result shortestPath = this.dao.getMinTimeRoad(driverLocation, passengerLocation);

            JSONObject result = new JSONObject();
            if (!shortestPath.hasNext()) {
                this.sendStatus(r, 404);
                return;
            }

            Record roadsData = shortestPath.next();
            int total_time = (int)roadsData.get("total_time").asDouble();

            JSONArray route = new JSONArray();
            for (int i = 0; i < roadsData.get("roads").size(); i++) {
                JSONObject location = new JSONObject();
                Node locationNode = roadsData.get("roads").get(i).asNode();
                String street = locationNode.get("name").asString();
                boolean has_traffic = locationNode.get("has_traffic").asBoolean();
                int time = (int)roadsData.get("costs").get(i).asDouble();

                location.put("street", street);
                location.put("has_traffic", has_traffic);
                location.put("time", time);

                route.put(location);
            }

            JSONObject data = new JSONObject()
                    .put("total_time", total_time)
                    .put("route", route);

            result.put("status", "OK");
            result.put("data", data);
            this.sendResponse(r, result, 200);

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }

    }
}
