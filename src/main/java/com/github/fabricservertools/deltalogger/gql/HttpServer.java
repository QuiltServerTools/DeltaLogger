package com.github.fabricservertools.deltalogger.gql;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Resources;

public class HttpServer {
  private static GraphQL graphQL;

  public static void buildSchema() throws IOException {
    // String schema = "type Query{hello: String}";
    URL url = HttpServer.class.getResource("/data/watchtower/schema.graphql");
    String schema = Resources.toString(url, StandardCharsets.UTF_8);

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(
      typeDefinitionRegistry,
      GQLWiring.getWiring()
    );

    GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
    graphQL = build;
  }

  public static void handleGQLRequest(HttpServerExchange exchange) {
    exchange.getRequestReceiver().receiveFullString((ex, data) -> {
      try {
        Map<String, Object> payload = JsonKit.toMap(data);

        String query = (String) payload.get("query");
        String operationname = (String) payload.get("operationName");
        Map<String, Object> variables = (Map<String, Object>) payload.get("variables");

        if (ex.getRequestMethod().equalToString("POST")) {
          query = (String) payload.get("query");
          operationname = (String) payload.get("operationName");
          variables = (Map<String, Object>) payload.get("variables");
        } else {
          Map<String, String> queryParams = new HashMap<>();
          ex.getQueryParameters().forEach((k, v) -> queryParams.put(k, v.getFirst()));

          query = queryParams.get("query");
          operationname = queryParams.get("operationName");
          variables = queryParams.get("variables") != null
            ? JsonKit.toMap(queryParams.get("variables")) : null;
        }

        ExecutionInput input = ExecutionInput.newExecutionInput()
            .query(query)
            .variables(variables)
            .operationName(operationname)
            .build();

        ExecutionResult executionResult = graphQL.execute(input);
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        ex.getResponseSender().send(JsonKit.toJson(executionResult.toSpecification()));
      } catch (Exception e) {
        e.printStackTrace();
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        ex.getResponseSender().send(e.getMessage());
      }
    }, (ex, exception) -> {
      exception.printStackTrace();
      ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
      ex.getResponseSender().send(exception.getMessage());
    });
  }

  public static void start() {
    Undertow server = Undertow.builder()
      .addHttpListener(8080, "localhost")
      /*
        // REST API path
        .addPrefixPath("/api", Handlers.routing()
            .get("/customers", exchange -> {...})
            .delete("/customers/{customerId}", exchange -> {...})
            .setFallbackHandler(exchange -> {...}))

        // Redirect root path to /static to serve the index.html by default
        .addExactPath("/", Handlers.redirect("/static"))

        // Serve all static files from a folder
        .addPrefixPath("/static", new ResourceHandler(
            new PathResourceManager(Paths.get("/path/to/www/"), 100))
            .setWelcomeFiles("index.html"))
      */
      .setHandler(Handlers.path().addPrefixPath("/graphql", HttpServer::handleGQLRequest))
      .build();

    try {
      buildSchema();
    } catch (IOException e) {
      e.printStackTrace();
    }
    server.start();
  }
}
