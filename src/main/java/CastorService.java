import castor.wrappers.LearningResult;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

public class CastorService {

    public static void main(String[] args) {
        //http://localhost:4567/
//		CorsFilter.apply();
        enableCORS("*", "*", "*");

        get("/hello", (req, res) -> {
            Gson gson = new Gson();
            return gson.toJson("Hello world");
        });

        get("/hello/:name", (req, res) -> {
            Gson gson = new Gson();
            return gson.toJson("Hello, " + req.params(":name"));

            //return "Hello, "+ req.params(":name");
        });

        get("/learn/:dataset/:query/:method/:sps/:pos/:neg", (request, response) -> {
            CastorClient cl = new CastorClient();
            response.type("application/json");
            String dataset = request.params(":dataset");
            String method = request.params(":method");
            String sps = request.params(":method");
            String query = request.params(":query");
            boolean pos =  Boolean.parseBoolean(request.params(":pos"));
            boolean neg =  Boolean.parseBoolean(request.params(":neg"));

            boolean createSPs = false;
            if (sps != null)
                createSPs = true;
            LearningResult result = cl.learn(dataset, method, query, createSPs, pos, neg);
            Gson gson = new Gson();
            return gson.toJson(result);
        });

        get("/learn/:dataset/:query/:method/:sps", (request, response) -> {
            CastorClient cl = new CastorClient();
            response.type("application/json");
            String dataset = request.params(":dataset");
            String method = request.params(":method");
            String sps = request.params(":sps");
            String query = request.params(":query");
            boolean createSPs = false;
            if (sps != null)
                createSPs = true;
            LearningResult result = cl.learn(dataset, method, query, createSPs, false, false);
            Gson gson = new Gson();
            return gson.toJson(result);
        });

        get("/learn/:dataset/:method/:sps", (request, response) -> {
            CastorClient cl = new CastorClient();
            response.type("application/json");
            String dataset = request.params(":dataset");
            String method = request.params(":method");
            String sps = request.params(":sps");
            boolean createSPs = false;
            if (sps != null)
                createSPs = true;
            LearningResult result = cl.learn(dataset, method, null, createSPs, false, false);
            Gson gson = new Gson();
            return gson.toJson(result);
        });

        get("/learn/:dataset/:method", (request, response) -> {
            CastorClient cl = new CastorClient();
            response.type("application/json");
            String dataset = request.params(":dataset");
            String method = request.params(":method");

            LearningResult result = cl.learn(dataset, method, null, false, false, false);
            Gson gson = new Gson();
            return gson.toJson(result);
        });

        //http://localhost:4567/learn/uwcse
        get("/learn/:dataset", (request, response) -> {
            CastorClient cl = new CastorClient();
            response.type("application/json");
            String dataset = request.params(":dataset");

            LearningResult result = cl.learn(dataset, "automode", null, false, false, false);
            Gson gson = new Gson();
            return gson.toJson(result);
        });


        post("/save/files", (request, response) -> {
            System.out.println("Post request to save file ");
            response.type("application/json");
            ExamplesFile examplesFile = new Gson().fromJson(request.body(), ExamplesFile.class);

            System.out.println("Examples Path :: " + examplesFile.filePath);

            Reader inputString = new StringReader(examplesFile.examples);
            BufferedReader br = new BufferedReader(inputString);
            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = br.readLine()) != null) {
                line = line.trim().replaceAll("\\s+", ""); // remove leading and trailing whitespace
                if (!line.equals("")) // don't write out blank lines
                {
                    stringBuilder.append(line+"\n");
                }
            }

            Files.write(Paths.get(examplesFile.filePath), stringBuilder.toString().getBytes());

            return new Gson()
                    .toJson("success");
        });
    }


    private static void enableCORS(final String origin, final String methods, final String headers) {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
            // Note: this may or may not be necessary in your particular application
            response.type("application/json");
        });
    }


}
