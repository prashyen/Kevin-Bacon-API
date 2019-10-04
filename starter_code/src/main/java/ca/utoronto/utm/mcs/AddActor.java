package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

public class AddActor implements HttpHandler
{
    private static Driver driver;

    public AddActor(Driver driverIn){
        driver = driverIn;
    }
    public void handle(HttpExchange r) throws IOException {
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
           String name = deserialized.getString("name");
           String id = deserialized.getString("actorId");
           addActor(name, id, driver);
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

    private static void addActor(String name, String id, Driver driver)
    {
        Map<String,Object> params = new HashMap<String,Object>();
        params.put( "x", name );
        params.put( "id", id );
        Session CREATEsession = driver.session();
        String query ="MERGE (a:Actor { name:{x} , actorId: {id} })\n" +
                "RETURN a";
        StatementResult sr = CREATEsession.run( query, params);
    }
}
