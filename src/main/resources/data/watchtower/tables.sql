CREATE TABLE IF NOT EXISTS `players` (
  `id` /**!PRIMARY_KEY*/,
  `uuid` varchar(255) NOT NULL UNIQUE,
  `name` varchar(255) NOT NULL,
  `lastonline` DATETIME NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `players_uuid_idx` ON players(`uuid`);
CREATE INDEX IF NOT EXISTS `players_name_idx` ON players(`name`);


CREATE TABLE IF NOT EXISTS `registry` (
  `id` /**!SMALL_PRIMARY_KEY*/,
  `name` varchar(255) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS `registry_name_idx` ON registry(`name`);


CREATE TABLE IF NOT EXISTS `placements` (
  `id` /**!PRIMARY_KEY*/,
  `date` DATETIME NOT NULL,
  `playerid` INTEGER UNSIGNED NOT NULL,
  `type` SMALLINT UNSIGNED NOT NULL,
  `x` INTEGER NOT NULL,
  `y` TINYINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  `placed` BOOLEAN NOT NULL,
  `dimensionid` SMALLINT UNSIGNED,
  FOREIGN KEY (`playerid`) REFERENCES players(`id`),
  FOREIGN KEY (`dimensionid`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `placements_x_idx` ON placements(`x`);
CREATE INDEX IF NOT EXISTS `placements_y_idx` ON placements(`y`);
CREATE INDEX IF NOT EXISTS `placements_z_idx` ON placements(`z`);
CREATE INDEX IF NOT EXISTS `placements_date_idx` ON placements(`date`);
CREATE INDEX IF NOT EXISTS `placements_playerid_idx` ON placements(`playerid`);
CREATE INDEX IF NOT EXISTS `placements_type_idx` ON placements(`type`);

CREATE TABLE IF NOT EXISTS `containers` (
  `id` /**!PRIMARY_KEY*/,
  `uuid` varchar(255) NOT NULL,
  `lastaccess` DATETIME NOT NULL,
  `itemtype` SMALLINT UNSIGNED NOT NULL,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  `firstplayer` INTEGER UNSIGNED NOT NULL,
  `lastplayer` INTEGER UNSIGNED NOT NULL,
  `dimensionid` SMALLINT UNSIGNED,
  FOREIGN KEY (`firstplayer`) REFERENCES players(`id`),
  FOREIGN KEY (`lastplayer`) REFERENCES players(`id`),
  FOREIGN KEY (`itemtype`) REFERENCES registry(`id`),
  FOREIGN KEY (`dimensionid`) REFERENCES registry(`id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `containers_uuid_idx` ON containers(`uuid`);
CREATE INDEX IF NOT EXISTS `containers_lastaccess_idx` ON containers(`lastaccess`);
CREATE INDEX IF NOT EXISTS `containers_x_idx` ON containers(x);
CREATE INDEX IF NOT EXISTS `containers_y_idx` ON containers(y);
CREATE INDEX IF NOT EXISTS `containers_z_idx` ON containers(z);
CREATE INDEX IF NOT EXISTS `containers_firstplayer_idx` ON containers(`firstplayer`);
CREATE INDEX IF NOT EXISTS `containers_lastplayer_idx` ON containers(`lastplayer`);
CREATE INDEX IF NOT EXISTS `containers_itemtype_idx` ON containers(`itemtype`);
CREATE INDEX IF NOT EXISTS `containers_dimensionid_idx` ON containers(`dimensionid`);

CREATE TABLE IF NOT EXISTS `container_transactions` (
  `id` /**!BIG_PRIMARY_KEY*/,
  `playerid` INTEGER UNSIGNED NOT NULL,
  `containerid` INTEGER UNSIGNED NOT NULL,
  `date` DATETIME NOT NULL,
  `itemtype` SMALLINT UNSIGNED NOT NULL,
  `itemcount` INTEGER NOT NULL,
  `itemdata` MEDIUMBLOB DEFAULT NULL,
  FOREIGN KEY (`playerid`) REFERENCES players(`id`),
  FOREIGN KEY (`containerid`) REFERENCES containers(`id`),
  FOREIGN KEY (`itemtype`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `container_tx_date_idx` ON container_transactions(`date`);
CREATE INDEX IF NOT EXISTS `container_tx_playerid_idx` ON container_transactions(`playerid`);
CREATE INDEX IF NOT EXISTS `container_tx_containerid_idx` ON container_transactions(`containerid`);
CREATE INDEX IF NOT EXISTS `container_tx_itemtype_idx` ON container_transactions(`itemtype`);


CREATE TABLE IF NOT EXISTS `killed_entities` (
  `id` /**!PRIMARY_KEY*/,
  `name` VARCHAR(100) NOT NULL,
  `source` VARCHAR(100) NOT NULL,
  `killerid` INTEGER UNSIGNED,
  `dimensionid` SMALLINT UNSIGNED,
  `date` DATETIME NOT NULL,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  FOREIGN KEY (`killerid`) REFERENCES players(`id`),
  FOREIGN KEY (`dimensionid`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `killed_entities_x_idx` ON killed_entities(`x`);
CREATE INDEX IF NOT EXISTS `killed_entities_y_idx` ON killed_entities(`y`);
CREATE INDEX IF NOT EXISTS `killed_entities_z_idx` ON killed_entities(`z`);
CREATE INDEX IF NOT EXISTS `killed_entities_date_idx` ON killed_entities(`date`);
CREATE INDEX IF NOT EXISTS `killed_entities_killerid_idx` ON killed_entities(`killerid`);
CREATE INDEX IF NOT EXISTS `killed_entities_source_idx` ON killed_entities(`source`);
CREATE INDEX IF NOT EXISTS `killed_entities_name_idx` ON killed_entities(`name`);
CREATE INDEX IF NOT EXISTS `killed_entities_dimensionid_idx` ON killed_entities(`dimensionid`);


CREATE TABLE IF NOT EXISTS `mob_grief` (
  `id` /**!PRIMARY_KEY*/,
  `date` DATETIME NOT NULL,
  `entity_type` SMALLINT UNSIGNED NOT NULL,
  `target` INTEGER UNSIGNED,
  `dimensionid` SMALLINT UNSIGNED,
  `x` INTEGER NOT NULL,
  `y` SMALLINT UNSIGNED NOT NULL,
  `z` INTEGER NOT NULL,
  FOREIGN KEY (`target`) REFERENCES players(`id`),
  FOREIGN KEY (`entity_type`) REFERENCES registry(`id`),
  FOREIGN KEY (`dimensionid`) REFERENCES registry(`id`)
);
CREATE INDEX IF NOT EXISTS `mob_grief_x_idx` ON mob_grief(`x`);
CREATE INDEX IF NOT EXISTS `mob_grief_y_idx` ON mob_grief(`y`);
CREATE INDEX IF NOT EXISTS `mob_grief_z_idx` ON mob_grief(`z`);
CREATE INDEX IF NOT EXISTS `mob_grief_date_idx` ON mob_grief(`date`);
CREATE INDEX IF NOT EXISTS `mob_grief_target_idx` ON mob_grief(`target`);
CREATE INDEX IF NOT EXISTS `mob_grief_entity_type_idx` ON mob_grief(`entity_type`);
CREATE INDEX IF NOT EXISTS `mob_grief_dimensionid_idx` ON mob_grief(`dimensionid`);


