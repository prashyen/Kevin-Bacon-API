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
    public void handle(HttpExchange r) {
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
            String id = deserialized.getString("movieId");
            String a = "";
            if (id != null) {
                List<String> actors = new ArrayList<String>();
                String idin = null;
                String name = null;
                try (Session MATCHsession = driver.session()) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("id", id);
                    Record result;
                    String querycheck = "MATCH (movie:Movie { movieId: {id} })<-[:ACTED_IN]-(actor) Return movie.name, movie.movieId, actor.actorId";
                    StatementResult srcheck = MATCHsession.run(querycheck, params);
                    ResultSummary ds = srcheck.summary();
                    while (srcheck.hasNext()) {
                        result = srcheck.next();
                        Map<String, Object> data = result.asMap();
                        name = (String) data.get("movie.name");
                        idin = (String) data.get("movie.movieId");
                        actors.add((String) data.get("actor.actorId"));
                    }
                }

                if (idin != null) {

                    a = "{\n     \"movieId\": \"" + idin + "\",\n     \"name\": \"" + name + "\",\n     ";
                    a = a + "\"actors\": [\n";
                    for (String actor : actors) {
                        a = a + "          " + actor + ",\n";
                    }
                    a = a + "     ]\n}";
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
