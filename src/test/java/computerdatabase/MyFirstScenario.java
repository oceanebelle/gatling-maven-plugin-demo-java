package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.redis.S;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class MyFirstScenario extends Simulation {

    // Define the HTTP configuration and base URL
    HttpProtocolBuilder httpProtocol = http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");
    
    ChainBuilder fetchAllVideos = exec(
        http("Get All videos").get("/videogame")
            .check(
                status().is(200),
                jsonPath("$[*].id").findRandom().saveAs("gameid"),
                jsonPath("$[*].id").findAll().saveAs("gameidlist"))
    ).pause(1);            
    
    ChainBuilder fetchVideo = exec(MyFirstScenario::logGameId).exec(
        http("Get Video").get("/videogame/#{gameid}")
            .check(status().is(200)));

    ChainBuilder fetchEveryVideo = exec(MyFirstScenario::logSessionVariable)
        .foreach("#{gameidlist}", "gameid").on(fetchVideo);
    
    ScenarioBuilder visitOne = scenario("Visit one")
        .exec(fetchAllVideos, fetchVideo);

    ScenarioBuilder visitAll = scenario("Visit all")
        .exec(fetchAllVideos, fetchEveryVideo);
    
    {
        setUp(
            // define populations and work models
            visitOne.injectOpen(rampUsers(10).during(10)),
            visitAll.injectOpen(rampUsers(1).during(10))
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().max().lt(150),
            global().allRequests().percent().gte(99.0));
    }


    static Session logSessionVariable(Session session) {
        System.out.println(session.getString("gameidlist"));
        return (session);
    }

    static Session logGameId(Session session) {
        System.out.println(session.getString("gameid"));
        return (session);
    }

}
