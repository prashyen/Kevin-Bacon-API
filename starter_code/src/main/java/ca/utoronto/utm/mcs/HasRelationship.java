package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HasRelationship implements HttpHandler {


    private static Driver driver;


    public HasRelationship(Driver driverIn){
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
        String exists = null;
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String actorId = deserialized.getString("actorId");
            String movieId = deserialized.getString("movieId");
            if (actorId != null && movieId != null) {
                Map<String,Object> params = new HashMap<String,Object>();
                params.put( "aid", actorId );
                params.put( "mid", movieId );
                try (Session MATCHsession = driver.session())
                {
                    Record result;
                    String query ="MATCH  (p:Actor {actorId: {aid}}), (b:Movie {movieId: {mid}}) RETURN EXISTS( (p)-[:ACTED_IN]-(b) )";
                    StatementResult statementResult = MATCHsession.run( query, params);
                    while(statementResult.hasNext()){
                        result= statementResult.next();
                        Map<String,Object> data = result.asMap();
                        exists = data.get("EXISTS( (p)-[:ACTED_IN]-(b) )").toString();
                        System.out.print((exists));
                    }
                }
                if (exists != null) {
                    String response = "{\n     \"actorId\": \"" + actorId + "\",\n     \"movieId\": \"" + movieId + "\",\n     \"hasRelationship\": "+exists+"\n}";
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
