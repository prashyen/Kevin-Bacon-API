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

public class GetMovie implements HttpHandler {
    private static Driver driver;

    public GetMovie(Driver driverIn){
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

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String id = deserialized.getString("movieId");
            Record result;
            if (id != null) {
                List<String> actors = new ArrayList<String>();
                String idin = null;
                String name = null;
                try (Session matchSession = driver.session()) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", id);
                    String query = "MATCH (movie:Movie { movieId: {id} })<-[:ACTED_IN]-(actor) Return movie.name, movie.movieId, actor.actorId";
                    StatementResult statementResult = matchSession.run(query, params);
                    while (statementResult.hasNext()) {
                        result = statementResult.next();
                        Map<String, Object> data = result.asMap();
                        name = (String) data.get("movie.name");
                        idin = (String) data.get("movie.movieId");
                        actors.add((String) data.get("actor.actorId"));
                    }
                }
                if (idin != null) {

                    String jsonResult = "{\n     \"movieId\": \"" + idin + "\",\n     \"name\": \"" + name + "\",\n     ";
                    jsonResult = jsonResult + "\"actors\": [\n";
                    for (String actor : actors) {
                        jsonResult = jsonResult + "          " + actor + ",\n";
                    }
                    jsonResult = jsonResult + "     ]\n}";

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
