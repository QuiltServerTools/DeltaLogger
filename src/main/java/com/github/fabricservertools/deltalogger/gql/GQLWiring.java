package com.github.fabricservertools.deltalogger.gql;

import java.util.List;
import java.util.UUID;

import com.github.fabricservertools.deltalogger.dao.DAO;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.RuntimeWiring;

public class GQLWiring {
  private static final Validators validators = new Validators();
  public static class ValidationError implements GraphQLError {
    private String message;

    public ValidationError(String message) {
      this.message = message;
    }

    @Override
    public String getMessage() {
      return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
      return null;
    }

    @Override
    public ErrorClassification getErrorType() {
      return ErrorType.ValidationError;
    }
  }

  private static DataFetcherResult<Object> dataResult(Object data) {
    return DataFetcherResult.newResult().data(data).build();
  }

  private static DataFetcherResult<Object> errResult(String message) {
    return DataFetcherResult.newResult().error(new ValidationError(message)).build();
  }

  private static <T> T getArgOrElse(DataFetchingEnvironment dfe, String key, T elseValue) {
    T attempt = dfe.getArgument(key);
    if (attempt == null) {
      attempt = elseValue;
    }
    return attempt;
  }

  public static RuntimeWiring getWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", builder -> builder
          .dataFetcher("players", dfe -> validators
            .validatePagination(
              getArgOrElse(dfe, "offset", 0),
              getArgOrElse(dfe, "limit", 10),
              100
            )
            .map(tup ->
              dataResult(DAO.player.getPlayers(tup._1, tup._2))
            )
            .getOrElseGet(GQLWiring::errResult)
          )
          .dataFetcher("playerById", dfe -> DAO.player
            .getPlayerById(dfe.getArgument("id")))
          .dataFetcher("playerByUUID", dfe -> DAO.player
            .getPlayerByUUID(UUID.fromString(dfe.getArgument("uuid"))))
          .dataFetcher("placements", dfe -> validators
            .validatePagination(
              getArgOrElse(dfe, "offset", 0),
              getArgOrElse(dfe, "limit", 10), // FIXME change to applicative validation?
              100
            )
            .map(tup ->
              dataResult(DAO.block.getLatestPlacements(tup._1, tup._2))
            )
            .getOrElseGet(GQLWiring::errResult)
          )
          .dataFetcher("transactions", dfe -> validators
            .validatePagination(
              getArgOrElse(dfe, "offset", 0),
              getArgOrElse(dfe, "limit", 10), // FIXME change to applicative validation?
              100
            )
            .map(tup ->
              dataResult(DAO.transaction.getLatestTransactions(tup._1, tup._2))
            )
            .getOrElseGet(GQLWiring::errResult)
          )
        )
        .build();
  }
}
