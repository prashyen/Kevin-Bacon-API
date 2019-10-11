package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.neo4j.driver.v1.*;

public class AddActor implements HttpHandler
{
    private static Driver driver;

    public AddActor(Driver driverIn){
        driver = driverIn;
    }

    public void handle(HttpExchange r) throws IOException {
        try {
            System.out.println(r.getRequestMethod());
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

    public void handlePost(HttpExchange r) throws IOException {
        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);
            String name = deserialized.getString("name");
            String id = deserialized.getString("actorId");
            System.out.println(name+" "+id);
            addActor(name, id, driver);
            r.sendResponseHeaders(200, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
            OutputStream os = r.getResponseBody();
            os.write(-1);
            os.close();
        }
    }

    private static void addActor(String name, String id, Driver driver) throws Exception {
        try(Session creatSession = driver.session()){
            Map<String,Object> params = new HashMap<String,Object>();
            params.put( "name", name );
            params.put( "actorId", id );
            String query = "MERGE (a:Actor { actorId: {actorId} }) ON MATCH SET a.name = {name} ON CREATE SET a.name = {name} RETURN a";
            StatementResult result = creatSession.run( query, params);
        }catch(Exception e){
            throw new Exception();
        }
    }
}
