package ca.utoronto.utm.mcs;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;

public class App 
{
    //add better name variables to all classes
    //use transactions within session
    //add try catch for drivers, sessions and transactions
    //catch various errors pointed out in a1
    //finish bacon path
    //close driver

    static int PORT = 8080;
    private static String baconId = "nm0000102";
    public static void main(String[] args) throws IOException, JSONException
    {
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            Driver  driver = GraphDatabase.driver("bolt://localhost:7687/", AuthTokens.basic( "neo4j", "password" ) );
            server.createContext("/api/v1/addActor", new AddActor(driver));
            server.createContext("/api/v1/addMovie", new AddMovie(driver));
            server.createContext("/api/v1/addRelationship", new AddRelationship(driver));
            server.createContext("/api/v1/getActor", new GetActor(driver));
            server.createContext("/api/v1/getMovie", new GetMovie(driver));
            server.createContext("/api/v1/hasRelationship", new HasRelationship(driver));
            server.createContext("/api/v1/computeBaconNumber", new ComputeBaconNumber(driver, baconId));
            server.createContext("/api/v1/computeBaconPath", new ComputeBaconPath(driver));
            server.start();
            System.out.printf("Server started on port %d...\n", PORT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
