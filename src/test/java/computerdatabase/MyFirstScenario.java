package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class MyFirstScenario extends Simulation {

    // Define the HTTP configuration and base URL
    HttpProtocolBuilder httpProtocol = http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    // A call chain that fetches a random id and save to session
    ChainBuilder fetchAllVideos = exec(
        http("Get All videos").get("/videogame")
            .check(status().is(200))            
            .check(jsonPath("$[*].id").findRandom().saveAs("gameid"))
        ).pause(1);
            
    // A call chain that fetches given an id from the user session and checks the http response
    ChainBuilder fetchVideo = exec(
        http("Get Video").get("/videogame/#{gameid}")
            .check(responseTimeInMillis().lte(50))
            .check(status().is(200)));

    // A Scenario that executes two chains sequentially
    ScenarioBuilder scenario = scenario("Drill down Videos")
        .exec(fetchAllVideos, fetchVideo);

    // Define the simulation definition including user load.
    // Attach the httpProtol to all scenarios in this simulation
    {
        setUp(
            scenario.injectOpen(rampUsers(10).during(10))
        ).protocols(httpProtocol);
    }

}
