package com.github.fabricservertools.deltalogger.gql;

import com.github.fabricservertools.deltalogger.gql.handlers.AuthHandlers;
import com.github.fabricservertools.deltalogger.gql.handlers.GraphqlHandler;

import io.javalin.Javalin;
// import io.javalin.http.staticfiles.Location;

public class ApiServer {
  public static void start() {
    String staticDirectory = "/data/watchtower/http";

    Javalin app = Javalin.create(config -> {
      // if (System.getProperty("develop").equals("true")) {
      //   System.out.println("SERVING EXTERNAL STATIC");
      //   config.addStaticFiles("../webui/dist", Location.EXTERNAL);
      // } else {
      // }
      config
        .addStaticFiles(staticDirectory)
        .addSinglePageRoot("/", staticDirectory + "/index.html");

      if (System.getProperty("develop").equals("true")) {
        config.enableCorsForAllOrigins();
      }
    }).start(8080);

    app
      .get("/hello", ctx -> ctx.result("Hello World"))
      .post("/auth", AuthHandlers.provideJwtHandler)
      .before("/change-pass", AuthHandlers.validateHandler)
      .post("/change-pass", AuthHandlers.changePassHandler)
      .before("/graphql", AuthHandlers.validateHandler)
      .post("/graphql", new GraphqlHandler());
  }
}
