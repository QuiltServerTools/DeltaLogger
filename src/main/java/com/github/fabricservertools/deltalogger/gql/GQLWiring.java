package com.github.fabricservertools.deltalogger.gql;

import java.util.UUID;
import java.util.stream.Collectors;

import com.github.fabricservertools.deltalogger.dao.DAO;

import graphql.schema.idl.RuntimeWiring;

public class GQLWiring {
  public static RuntimeWiring getWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .type("Query", builder -> builder
          .dataFetcher("players", dfe -> DAO.player
              .getPlayers())
          .dataFetcher("playerById", dfe -> DAO.player
            .getPlayerById(dfe.getArgument("id")))
          .dataFetcher("playerByUUID", dfe -> DAO.player
            .getPlayerByUUID(UUID.fromString(dfe.getArgument("uuid"))))
          .dataFetcher("placements", dfe -> DAO.block
            .getPlacements())
          .dataFetcher("transactions", dfe -> DAO.transaction
            .getTransactions())
        )
        .build();
  }
}
