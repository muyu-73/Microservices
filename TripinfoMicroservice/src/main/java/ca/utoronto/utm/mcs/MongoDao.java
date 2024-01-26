package ca.utoronto.utm.mcs;

import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.types.ObjectId;
import org.bson.Document;

import java.sql.DriverManager;

public class MongoDao {
	
	public MongoCollection<Document> collection;
	private final String username = "root";
	private final String password = "123456";
	Dotenv dotenv = Dotenv.load();
	String addr = dotenv.get("MONGODB_ADDR");
	private final String uriDb = String.format("mongodb://%s:%s@%s:27017", username, password, addr);
	private final String dbName = "trips";
	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection.
        // Use Dotenv like in the DAOs of the other microservices.
		try {
			MongoClient mongoClient = MongoClients.create(this.uriDb);
			MongoDatabase database = mongoClient.getDatabase(this.dbName);
			this.collection = database.getCollection(this.dbName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// *** implement database operations here *** //
	public String confirmTrip(String driverUid, String passengerUid, Integer startTime) {
		Document query = new Document()
				.append("driver", driverUid)
				.append("passenger", passengerUid)
				.append("startTime", startTime);
		this.collection.insertOne(query);
		System.out.println(query.getObjectId("_id"));
		return String.valueOf(query.getObjectId("_id"));
	}

	public Boolean updateTrip(String tripId, double distance, Integer endTime, String timeElapsed, double discount,
							 double totalCost, double driverPayout) {
		try {
			Document query = new Document().append("_id", new ObjectId(tripId));
			return (this.collection.updateOne(query, Updates.combine(
					Updates.set("distance", distance),
					Updates.set("endTime", endTime),
					Updates.set("timeElapsed", timeElapsed),
					Updates.set("discount", discount),
					Updates.set("totalCost", totalCost),
					Updates.set("driverPayout", driverPayout))).getModifiedCount() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public MongoCursor<Document> getPassengerTrips(String passenger) {
		Document query = new Document().append("passenger", passenger);
		return this.collection.find(query).cursor();
	}

	public MongoCursor<Document> getDriverTrips(String passenger) {
		Document query = new Document().append("driver", passenger);
		return this.collection.find(query).cursor();
	}

	public MongoCursor<Document> getTripByUid(String uid) {
		Document query = new Document().append("_id", new ObjectId(uid));
		return this.collection.find(query).cursor();
	}
}
