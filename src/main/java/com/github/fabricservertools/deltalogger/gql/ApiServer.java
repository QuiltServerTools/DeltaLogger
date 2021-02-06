package com.github.fabricservertools.deltalogger.gql;

import com.github.fabricservertools.deltalogger.ModInit;
import com.github.fabricservertools.deltalogger.gql.handlers.AuthHandlers;
import com.github.fabricservertools.deltalogger.gql.handlers.GraphqlHandler;

import io.javalin.Javalin;
// import io.javalin.http.staticfiles.Location;

public class ApiServer {
  public static void start() {
    String staticDirectory = "/data/deltalogger/http";
    int port;
    try {
      port = Integer.parseInt(ModInit.CONFIG.getProperty("webapp_port", "8080"));
    } catch (Error e) {
      throw new Error("webapp_port config not a valid number");
    }
    Javalin app = Javalin.create(config -> {
      // if (System.getProperty("develop").equals("true")) {
      //   System.out.println("SERVING EXTERNAL STATIC");
      //   config.addStaticFiles("../webui/dist", Location.EXTERNAL);
      // } else {
      // }
      config
        .addStaticFiles(staticDirectory)
        .addSinglePageRoot("/", staticDirectory + "/index.html");
      
      config.showJavalinBanner = false;

      String devProp = System.getProperty("develop");
      if (devProp != null && devProp.equals("true")) {
        config.enableCorsForAllOrigins();
      }
    }).start(port);

    app
      .post("/auth", AuthHandlers.provideJwtHandler)
      .before("/auth/change-pass", AuthHandlers.validateHandler)
      .post("/auth/change-pass", AuthHandlers.changePassHandler)
      .before("/graphql", AuthHandlers.validateHandler)
      .post("/graphql", new GraphqlHandler());
  }
}
