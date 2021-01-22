CREATE TABLE IF NOT EXISTS `players` (
  `id` /**!PRIMARY_KEY*/,
  `uuid` VARCHAR(255) NOT NULL UNIQUE,
  `name` VARCHAR(255) NOT NULL,
  `last_online_time` DATETIME NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `players_uuid_idx` ON players(`uuid`);
CREATE INDEX IF NOT EXISTS `players_name_idx` ON players(`name`);

CREATE TABLE IF NOT EXISTS `perms` (
  `id` /**!PRIMARY_KEY*/,
  `player_id` INTEGER UNSIGNED NOT NULL,
  `password_hash` VARCHAR(255),
  `temporary_pass` BOOLEAN NOT NULL,
  `salt` VARCHAR(255),
  `roll_back` BOOLEAN NOT NULL,
  `delete` BOOLEAN NOT NULL,
  FOREIGN KEY (`player_id`) REFERENCES players(`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `perms_player_id_idx` ON `perms`(`player_id`);

CREATE TABLE IF NOT EXISTS `registry` (
  `id` /**!SMALL_PRIMARY_KEY*/,
  `name` VARCHAR(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `registry_name_idx` ON registry(`name`);


CREATE TABLE IF NOT EXISTS `placements` (
  `id` /**!PRIMARY_KEY*/,
  `date` DATETIME NOT NULL,
  `player_id` INTEGER UNSIGNED NOT NULL,
  `type` SMALLINT UNSIGNED NOT NULL,
  `x` INTEGER NOT NULL,
  `y` TINYINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  `placed` BOOLEAN NOT NULL,
  `dimension_id` SMALLINT UNSIGNED,
  `state` TEXT DEFAULT NULL,
  FOREIGN KEY (`player_id`) REFERENCES players(`id`),
  FOREIGN KEY (`dimension_id`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `placements_x_idx` ON placements(`x`);
CREATE INDEX IF NOT EXISTS `placements_y_idx` ON placements(`y`);
CREATE INDEX IF NOT EXISTS `placements_z_idx` ON placements(`z`);
CREATE INDEX IF NOT EXISTS `placements_date_idx` ON placements(`date`);
CREATE INDEX IF NOT EXISTS `placements_player_id_idx` ON placements(`player_id`);
CREATE INDEX IF NOT EXISTS `placements_type_idx` ON placements(`type`);

CREATE TABLE IF NOT EXISTS `containers` (
  `id` /**!PRIMARY_KEY*/,
  `uuid` VARCHAR(255) NOT NULL,
  `last_access` DATETIME NOT NULL,
  `item_type` SMALLINT UNSIGNED NOT NULL,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  `first_player_id` INTEGER UNSIGNED NOT NULL,
  `last_player_id` INTEGER UNSIGNED NOT NULL,
  `dimension_id` SMALLINT UNSIGNED,
  FOREIGN KEY (`first_player_id`) REFERENCES players(`id`),
  FOREIGN KEY (`last_player_id`) REFERENCES players(`id`),
  FOREIGN KEY (`item_type`) REFERENCES registry(`id`),
  FOREIGN KEY (`dimension_id`) REFERENCES registry(`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `containers_uuid_idx` ON containers(`uuid`);
CREATE INDEX IF NOT EXISTS `containers_last_access_idx` ON containers(`last_access`);
CREATE INDEX IF NOT EXISTS `containers_x_idx` ON containers(x);
CREATE INDEX IF NOT EXISTS `containers_y_idx` ON containers(y);
CREATE INDEX IF NOT EXISTS `containers_z_idx` ON containers(z);
CREATE INDEX IF NOT EXISTS `containers_first_player_id_idx` ON containers(`first_player_id`);
CREATE INDEX IF NOT EXISTS `containers_last_player_id_idx` ON containers(`last_player_id`);
CREATE INDEX IF NOT EXISTS `containers_item_type_idx` ON containers(`item_type`);
CREATE INDEX IF NOT EXISTS `containers_dimension_id_idx` ON containers(`dimension_id`);

CREATE TABLE IF NOT EXISTS `container_transactions` (
  `id` /**!BIG_PRIMARY_KEY*/,
  `player_id` INTEGER UNSIGNED NOT NULL,
  `container_id` INTEGER UNSIGNED NOT NULL,
  `date` DATETIME NOT NULL,
  `item_type` SMALLINT UNSIGNED NOT NULL,
  `item_count` INTEGER NOT NULL,
  `item_data` TEXT DEFAULT NULL,
  FOREIGN KEY (`player_id`) REFERENCES players(`id`),
  FOREIGN KEY (`container_id`) REFERENCES containers(`id`),
  FOREIGN KEY (`item_type`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `container_tx_date_idx` ON container_transactions(`date`);
CREATE INDEX IF NOT EXISTS `container_tx_player_id_idx` ON container_transactions(`player_id`);
CREATE INDEX IF NOT EXISTS `container_tx_container_id_idx` ON container_transactions(`container_id`);
CREATE INDEX IF NOT EXISTS `container_tx_item_type_idx` ON container_transactions(`item_type`);

CREATE TABLE IF NOT EXISTS `killed_entities` (
  `id` /**!PRIMARY_KEY*/,
  `name` VARCHAR(100) NOT NULL,
  `source` VARCHAR(100) NOT NULL,
  `killer_id` INTEGER UNSIGNED,
  `dimension_id` SMALLINT UNSIGNED,
  `date` DATETIME NOT NULL,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  FOREIGN KEY (`killer_id`) REFERENCES players(`id`),
  FOREIGN KEY (`dimension_id`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `killed_entities_x_idx` ON killed_entities(`x`);
CREATE INDEX IF NOT EXISTS `killed_entities_y_idx` ON killed_entities(`y`);
CREATE INDEX IF NOT EXISTS `killed_entities_z_idx` ON killed_entities(`z`);
CREATE INDEX IF NOT EXISTS `killed_entities_date_idx` ON killed_entities(`date`);
CREATE INDEX IF NOT EXISTS `killed_entities_killer_id_idx` ON killed_entities(`killer_id`);
CREATE INDEX IF NOT EXISTS `killed_entities_source_idx` ON killed_entities(`source`);
CREATE INDEX IF NOT EXISTS `killed_entities_name_idx` ON killed_entities(`name`);
CREATE INDEX IF NOT EXISTS `killed_entities_dimension_id_idx` ON killed_entities(`dimension_id`);


CREATE TABLE IF NOT EXISTS `mob_grief` (
  `id` /**!PRIMARY_KEY*/,
  `date` DATETIME NOT NULL,
  `entity_type` SMALLINT UNSIGNED NOT NULL,
  `target` INTEGER UNSIGNED,
  `dimension_id` SMALLINT UNSIGNED,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  FOREIGN KEY (`target`) REFERENCES players(`id`),
  FOREIGN KEY (`entity_type`) REFERENCES registry(`id`),
  FOREIGN KEY (`dimension_id`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `mob_grief_x_idx` ON mob_grief(`x`);
CREATE INDEX IF NOT EXISTS `mob_grief_y_idx` ON mob_grief(`y`);
CREATE INDEX IF NOT EXISTS `mob_grief_z_idx` ON mob_grief(`z`);
CREATE INDEX IF NOT EXISTS `mob_grief_date_idx` ON mob_grief(`date`);
CREATE INDEX IF NOT EXISTS `mob_grief_target_idx` ON mob_grief(`target`);
CREATE INDEX IF NOT EXISTS `mob_grief_entity_type_idx` ON mob_grief(`entity_type`);
CREATE INDEX IF NOT EXISTS `mob_grief_dimension_id_idx` ON mob_grief(`dimension_id`);


