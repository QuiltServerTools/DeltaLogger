package com.github.fabricservertools.deltalogger.dao;

import org.jdbi.v3.core.Jdbi;

public class DAO {
	public static AuthDAO auth;
	public static BlockDAO block;
	public static ContainerDAO container;
	public static EntityDAO entity;
	public static PlayerDAO player;
	public static RegistryDAO registry;
	public static TransactionDAO transaction;

	public static void register(Jdbi jdbi) {
		auth = new AuthDAO(jdbi);
		block = new BlockDAO(jdbi);
		container = new ContainerDAO(jdbi);
		entity = new EntityDAO(jdbi);
		player = new PlayerDAO(jdbi);
		registry = new RegistryDAO(jdbi);
		transaction = new TransactionDAO(jdbi);
	}
}
