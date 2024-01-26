package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject obj = new JSONObject(body);
            String[] fields = {"name", "email", "password"};
            Class<?>[] fieldClasses = {String.class, String.class, String.class};
            if (!this.validateFields(obj, fields, fieldClasses)) {
                sendStatus(r, 400);
                return;
            }

            String name = obj.getString("name");
            String email = obj.getString("email");
            String password = obj.getString("password");
            if (name.trim().equals("") || email.trim().equals("") || password.trim().equals("")) {
                this.sendStatus(r, 400);
                return;
            }
            ResultSet emailExist = this.dao.getUserByAuth(email, password);
            if (emailExist.next()) {
                this.sendStatus(r, 400);
                return;
            }
            this.dao.register(name, email, password);
            this.sendStatus(r, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }


    }
}
