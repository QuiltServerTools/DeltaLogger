package com.github.fabricservertools.deltalogger.gql.handlers;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.fabricservertools.deltalogger.gql.GQLWiring;
import com.google.common.io.Resources;

public class GraphqlHandler implements Handler {
  private static GraphQL graphQL;

  private static class UserGQLContext {
    public String userId;
    public String userName;
    public UserGQLContext(String userId, String userName) {
      this.userId = userId;
      this.userName = userName;
    }
  }

  public GraphqlHandler() {
    try {
      buildSchema();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void buildSchema() throws IOException {
    // String schema = "type Query{hello: String}";
    URL url = GraphqlHandler.class.getResource("/data/deltalogger/schema.graphql");
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

  static class GQLRequest {
    public String query;
    public String operationName;
    public Map<String, Object> variables;
  }
  @Override
  public void handle(Context ctx) throws Exception {
    GQLRequest gqlreq = ctx.bodyValidator(GQLRequest.class).get();

    // } else {
    //   Map<String, String> queryParams = new HashMap<>();
    //   ex.getQueryParameters().forEach((k, v) -> queryParams.put(k, v.getFirst()));

    //   query = queryParams.get("query");
    //   operationname = queryParams.get("operationName");
    //   variables = queryParams.get("variables") != null
    //     ? JsonKit.toMap(queryParams.get("variables")) : null;
    // }
    DecodedJWT djwt = ctx.attribute("token");
    UserGQLContext context = new UserGQLContext(
      djwt.getClaim("user_id").asString(),
      djwt.getClaim("user_name").asString()
    );

    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query(gqlreq.query)
        .variables(gqlreq.variables)
        .operationName(gqlreq.operationName)
        .context(context)
        .build();

    ExecutionResult executionResult = graphQL.execute(input);
    ctx.status(200).json(executionResult.toSpecification());
  }
}
