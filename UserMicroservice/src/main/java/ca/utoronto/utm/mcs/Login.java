package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        //sanitized input
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject obj = new JSONObject(body);
            String[] fields = {"email", "password"};
            Class<?>[] fieldClasses = {String.class, String.class};
            if (!this.validateFields(obj, fields, fieldClasses)) {
                sendStatus(r, 400);
                return;
            }

            String email = obj.getString("email");
            String password = obj.getString("password");
            if (email.trim().equals("") || password.trim().equals("")) {
                this.sendStatus(r, 400);
                return;
            }

            ResultSet emailExist = this.dao.getUserByEmail(email);
            if (!emailExist.next()) {
                this.sendStatus(r, 404);
                return;
            }

            ResultSet passwordMatch = this.dao.getUserByAuth(email, password);
            if (!passwordMatch.next()) {
                this.sendStatus(r, 401);
                return;
            }

            this.sendStatus(r, 200);

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
    }
}
