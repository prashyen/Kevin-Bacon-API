package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

public class GetActor implements HttpHandler
{
    private static Driver driver;

    public GetActor(Driver driverIn){
        driver = driverIn;
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
    public static List<Object> GetActor(String idorName, String GetActorBy) {
        List<String> movies = new ArrayList<String>();
        String idin = null;
        String name = null;
        if (idorName != null) {
            try (Session matchSession = driver.session()) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", idorName);
                Record result;
                String querycheck = "MATCH (actor { "+GetActorBy+": {id} })-[:ACTED_IN]->(movie) RETURN actor.name, actor.id, movie.id";
                StatementResult statementResult = matchSession.run(querycheck, params);
                while (statementResult.hasNext()) {
                    result = statementResult.next();
                    Map<String, Object> data = result.asMap();
                    name = (String) data.get("actor.name");
                    idin = (String) data.get("actor.id");
                    movies.add((String) data.get("movie.id"));
                }
            }
        }

        return Arrays.asList(name, idin, movies);
    }
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String result = "";
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String id = deserialized.getString("actorId");
            List<Object> actor = GetActor(id, "actorId");
            String name = (String) actor.get(0);
            String idin = (String) actor.get(1);
            List<String> movies = (List<String>) actor.get(2);
            if (idin != null) {

                result = "{\n     \"actorId\": \"" + idin + "\",\n     \"name\": \"" + name + "\",\n     ";
                result = result + "\"movies\": [\n";
                for (String movie : movies) {
                    result = result + "          \"" + movie + "\",\n";
                }
                result = result + "     ]\n}";

                String response = result;
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
