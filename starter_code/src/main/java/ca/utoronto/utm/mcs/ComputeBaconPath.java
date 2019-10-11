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
import java.util.*;

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
                String baconId = "nm0000102";
                List<String> actors = new ArrayList<String>();
                Session actorSession = driver.session();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id);
                params.put("baconId", baconId);
                Record result;
                Object[] actorArray = null;
                String actorQuery = "MATCH  (actor:actor {id: {id}}), (bacon:actor {id: {baconId}}), path = shortestPath((actor)-[*]-(bacon)) RETURN EXTRACT (n in NODES(path)|n.id)";
                StatementResult actorStatementResult = actorSession.run(actorQuery, params);
                while (actorStatementResult.hasNext()) {
                    result = actorStatementResult.next();
                    Map<String, Object> data = result.asMap();
                    actorArray =  ((Collection) data.get("EXTRACT (n in NODES(path)|n.id)")).toArray();
                }

                if (actorArray == null) {
                    r.sendResponseHeaders(404, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }
                jsonResult = generateBaconPathResponse(actorArray);
            }
            String response = jsonResult;
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }catch (JSONException e){
            r.sendResponseHeaders(400, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        } catch (Exception e){
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    public String generateBaconPathResponse(Object[] actorArray){
        int baconNumber = actorArray.length/2;
        String response = "{" +
                "\"baconNumber\":" + Integer.toString(baconNumber) +
                ",\"baconPath\":";
        String baconPath = "[";
        for (int i = 0; i < actorArray.length - 2; i += 2){
            baconPath += "{\"actorId\":\"" + actorArray[i] + "\",\"movieId\":\"" + actorArray[i+1] + "\"},";
        }
        // add last node
        baconPath += "{\"actorId\":\"nm0000102\",\"movieId\":\"" + actorArray[actorArray.length - 2] + "\"}]}";

        return response + baconPath;
    }
}
