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

public class AddRelationship implements HttpHandler {


    private static Driver driver;


    public AddRelationship(Driver driverIn){
        driver = driverIn;
    }
    public void handle(HttpExchange r) {
        try {
           if (r.getRequestMethod().equals("POST")) {
                handlePost(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePost(HttpExchange r) throws IOException, JSONException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String mID = deserialized.getString("movieId");
            String aID = deserialized.getString("actorId");
            System.out.println(mID+"  "+aID);
            addRelationship(mID, aID, driver);
            String response = "";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        catch (IOException e){
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

    private static void addRelationship(String mID, String aID, Driver driver)
    {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put( "aID", aID );
        params.put( "mID", mID );
        Session CREATEsession = driver.session();
        String query ="MATCH (a:Actor{actorId:{aID}}),(m:Movie{movieId:{mID}}) MERGE (a)-[:ACTED_IN]-(m) RETURN a.name";
        StatementResult sr = CREATEsession.run( query, params);
        System.out.print(sr.summary());
    }
}
