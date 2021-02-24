package com.github.fabricservertools.deltalogger.gql;

import com.github.fabricservertools.deltalogger.gql.handlers.AuthHandlers;
import com.github.fabricservertools.deltalogger.gql.handlers.GraphqlHandler;
import io.javalin.Javalin;
// import io.javalin.http.staticfiles.Location;

public class ApiServer {
	private static final String STATIC_DIRECTORY = "/data/deltalogger/http";
	private final Javalin app;

	public ApiServer() {
		this.app = Javalin.create(config -> {
			// if (System.getProperty("develop").equals("true")) {
			//   System.out.println("SERVING EXTERNAL STATIC");
			//   config.addStaticFiles("../webui/dist", Location.EXTERNAL);
			// } else {
			// }
			config
					.addStaticFiles(STATIC_DIRECTORY)
					.addSinglePageRoot("/", STATIC_DIRECTORY + "/index.html");

			config.showJavalinBanner = false;

			String devProp = System.getProperty("develop");
			if (devProp != null && devProp.equals("true")) {
				config.enableCorsForAllOrigins();
			}
		});

		app
				.post("/auth", AuthHandlers.provideJwtHandler)
				.before("/auth/change-pass", AuthHandlers.validateHandler)
				.post("/auth/change-pass", AuthHandlers.changePassHandler)
				.before("/graphql", AuthHandlers.validateHandler)
				.post("/graphql", new GraphqlHandler());
	}

	public void start(int port) {
		app.start(port);
	}

	public void stop() {
		app.stop();
	}
}
