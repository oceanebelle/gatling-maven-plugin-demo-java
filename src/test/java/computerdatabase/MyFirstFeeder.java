package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

public class MyFirstFeeder extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    // JSON File has 3 entries and a gameid and name property.
    FeederBuilder feeder = jsonFile("test.json");

    // comma separated file
    FeederBuilder csvFeeder = csv("foo.csv").circular();
    // tabulation separated file
    FeederBuilder tsvFeeder = tsv("foo.tsv").circular();
    // semicolon separated file
    FeederBuilder ssvFeeder = ssv("foo.ssv").circular();
    // custom separated file
    FeederBuilder custom = separatedValues("foo.tsv", '#').circular();

    ChainBuilder fetchVideo = feed(feeder)
            .exec(http("Get Video id=#{gameid} #{name}")
                    .get("/videogame/#{gameid}")
                    .check(status().is(200)));

    ScenarioBuilder visitOne = scenario("Visit One").exec(fetchVideo);

    {
        setUp(visitOne.injectOpen(atOnceUsers(5))).protocols(httpProtocol);

    }
}
