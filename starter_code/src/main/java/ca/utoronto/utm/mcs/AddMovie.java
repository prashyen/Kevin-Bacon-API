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

    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePost(r);
            }else{
                throw new Exception();
            }
        }catch (Exception e) {
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String name = deserialized.getString("name");
            String id = deserialized.getString("movieId");
            addMovie(name, id, driver);
            r.sendResponseHeaders(200, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
        catch (JSONException e){
            r.sendResponseHeaders(400, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }catch (Exception e){
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    private static void addMovie(String name, String id, Driver driver) throws Exception {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("id", id);
            Session createSession = driver.session();
            String query = "MERGE (a:movie { id: {id} }) ON MATCH SET a.name = {name} ON CREATE SET a.name = {name} RETURN a";
            StatementResult result = createSession.run(query, params);
        }catch(Exception e){
            throw new Exception();
        }
    }
}
