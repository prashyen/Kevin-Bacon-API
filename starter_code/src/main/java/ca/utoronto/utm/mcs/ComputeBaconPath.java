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
                List<Object> actor = GetActor.GetActor("Kevin Bacon", "name");
                String baconId = (String) actor.get(1);
                List<String> actors = new ArrayList<String>();
                Session actorSession = driver.session();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", id);
                params.put("baconId", baconId);
                Record result;
                Object[] actorArray = null;
                String actorQuery = "MATCH  (actor:Actor {actorId: {id}}), (bacon:Actor {actorId: {baconId}}), path = shortestPath((actor)-[*]-(bacon)) RETURN EXTRACT (n in NODES(path)|n.actorId)";
                StatementResult actorStatementResult = actorSession.run(actorQuery, params);
                while (actorStatementResult.hasNext()) {
                    result = actorStatementResult.next();
                    Map<String, Object> data = result.asMap();
                    actorArray =  ((Collection) data.get("EXTRACT (n in NODES(path)|n.actorId)")).toArray();
                }

                if (actorArray != null) {
                    actorArray = removeNullElements(actorArray);
                    System.out.println();
                } else {
                    r.sendResponseHeaders(404, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }
                Session movieSession = driver.session();
                Object[] movieArray = null;
                String movieQuery = "MATCH (actor:Actor {actorId: {id}}), (bacon:Actor {actorId: {baconId}}), path = shortestPath((actor)-[*]-(bacon)) RETURN EXTRACT (n in NODES(path)|n.movieId)";
                StatementResult movieStatementResult = movieSession.run(movieQuery, params);
                while(movieStatementResult.hasNext()){
                    result = movieStatementResult.next();
                    Map<String, Object> data = result.asMap();
                    movieArray = ((Collection) data.get("EXTRACT (n in NODES(path)|n.movieId)")).toArray();
                }

                if (movieArray != null) {
                    movieArray = removeNullElements(movieArray);
                    System.out.println(movieArray[0]);
                } else {
                    r.sendResponseHeaders(404, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }


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

    public Object[] removeNullElements(Object[] array){
        // store the length of the array
        int len = array.length;
        ArrayList<Object> temp = new ArrayList<Object>();
        for (int i = 0; i < len; i++){
            if (array[i] != null){
                temp.add(array[i]);
            }
        }
        Object[] ret = temp.toArray();
       return ret;
    }

    public String generateBaconPathResponse(Object[] actorArray, Object[] movieArray){
        return "";
    }
}
