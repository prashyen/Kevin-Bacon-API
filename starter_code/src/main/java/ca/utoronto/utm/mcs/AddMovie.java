package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMovie implements HttpHandler {
    private static Driver driver;
    public AddMovie(Driver driverIn){
        driver = driverIn;
    }
    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("POST")) {
                handlePost(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String name = deserialized.getString("name");
            String id = deserialized.getString("movieId");
            addMovie(name, id, driver);
            String response = "";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        catch (IOException e){
            String response = "";
            r.sendResponseHeaders(500, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }catch (JSONException e){
            String response = "";
            r.sendResponseHeaders(400, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static void addMovie(String name, String id, Driver driver)
    {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put( "x", name );
        params.put( "id", id );
        Session CREATEsession = driver.session();
        String query ="MERGE (m:Movie { name: {x} , movieId: {id}}) RETURN m";
        StatementResult sr = CREATEsession.run( query, params);
    }
}
