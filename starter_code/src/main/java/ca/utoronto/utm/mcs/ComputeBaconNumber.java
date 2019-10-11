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

public class ComputeBaconNumber implements HttpHandler {
    private Driver driver;
    private String baconId;
    public ComputeBaconNumber(Driver driverIn, String id){
        driver = driverIn;
        baconId = id;
    }


    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }else{
                throw new Exception();
            }
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String id = deserialized.getString("actorId");
            String jsonResult = "";
            if (id != null) {
                if(!baconId.equals(id) && baconId != null) {
                    List<String> actors = new ArrayList<String>();
                    String pathlen = null;
                    try (Session matchSession = driver.session()) {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("id", id);
                        params.put("baconId", baconId);
                        Record result;
                        String query = "MATCH  (actor:Actor {actorId: {id}}), (bacon:Actor {actorId: {baconId}}), path = shortestPath((actor)-[*]-(bacon)) RETURN length(path)";
                        StatementResult statementResult = matchSession.run(query, params);
                        while (statementResult.hasNext()) {
                            result = statementResult.next();
                            Map<String, Object> data = result.asMap();
                            Long len = ((long) data.get("length(path)")) / 2;
                            pathlen = len.toString();
                        }

                        if (pathlen != null) {
                            jsonResult = "{\n     \"baconNumber\": " + pathlen + "\n}";
                            String response = jsonResult;
                            r.sendResponseHeaders(200, response.length());
                            OutputStream os = r.getResponseBody();
                            os.write(response.getBytes());
                            os.close();
                        }else{
                            r.sendResponseHeaders(404, -1);
                            OutputStream os = r.getResponseBody();
                            os.write(-1);
                            os.close();
                        }
                    }
                }else if(baconId.equals((id))){
                    jsonResult = "{\n     \"baconNumber\": " + 0 + "\n}";String response = jsonResult;
                    r.sendResponseHeaders(200, response.length());
                    OutputStream os = r.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }else{
                    r.sendResponseHeaders(404, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }
            }

        } catch (JSONException e){
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
}
