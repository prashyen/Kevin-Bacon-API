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

public class AddRelationship implements HttpHandler, AutoCloseable {


    private static Driver driver;

    public AddRelationship(Driver driverIn){
        driver = driverIn;
    }

    public void handle(HttpExchange r) throws IOException {
        try {
           if (r.getRequestMethod().equals("PUT")) {
                handlePost(r);
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

    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            Record result;
            String aName = null, mName = null;
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String mID = deserialized.getString("movieId");
            String aID = deserialized.getString("actorId");
            System.out.println(mID+"  "+aID);
            Map<String,Object> params = new HashMap<String,Object>();
            params.put( "aID", aID );
            params.put( "mID", mID );
            try(Session CREATEsession = driver.session()) {
                String query = "MATCH (a:Actor{actorId:{aID}}),(m:Movie{movieId:{mID}}) MERGE (a)-[:ACTED_IN]-(m) RETURN a.name, m.name";
                StatementResult statementResult = CREATEsession.run(query, params);
                while (statementResult.hasNext()) {
                    result = statementResult.next();
                    Map<String, Object> data = result.asMap();
                    aName = (String) data.get("a.name");
                    mName = (String) data.get("m.name");
                }
                if(aName != null && mName != null){
                    r.sendResponseHeaders(200, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }else{
                    r.sendResponseHeaders(400, -1);
                    OutputStream os = r.getResponseBody();
                    os.write(-1);
                    os.close();
                }
            }
        }catch (JSONException e){
            r.sendResponseHeaders(400, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
        catch (Exception e){
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    @Override
    public void close() throws Exception
    {
        driver.close();
    }
}
