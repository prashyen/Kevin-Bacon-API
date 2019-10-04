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

public class ComputeBaconPath implements HttpHandler {
    private Driver driver;

    public ComputeBaconPath(Driver driverIn){
        driver = driverIn;
    }

    @Override
    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String id = deserialized.getString("actorId");
            String a = "";
            if (id != null) {
                List<Object> actor = GetActor.GetActor("Kevin Bacon", "name");
                String baconId = (String) actor.get(1);
                List<String> actors = new ArrayList<String>();
                String pathlen = null;
                try (Session MATCHsession = driver.session()) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", id);
                    params.put("baconId", baconId);
                    Record result;
                    String querycheck = "MATCH  (actor:Actor {actorId: {id}}), (bacon:Actor {actorId: {baconId}}), path = shortestPath((actor)-[*]-(bacon)) RETURN path";
                    StatementResult srcheck = MATCHsession.run(querycheck, params);
                    ResultSummary ds = srcheck.summary();
                    while (srcheck.hasNext()) {
                        result = srcheck.next();
                        Map<String, Object> data = result.asMap();
                        System.out.println(43);
                    }

                    if (pathlen != null) {
                        a = "{\n     \"baconNumber\": "+pathlen+"\n}";
                    }
                }
            }
            String response = a;
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e){
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
}