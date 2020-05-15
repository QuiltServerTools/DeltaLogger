CREATE TABLE IF NOT EXISTS `players` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `lastonline` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `name` (`name`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `registry` (
  `id` smallint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `placements` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `playerid` int unsigned NOT NULL,
  `type` smallint unsigned NOT NULL,
  `x` int NOT NULL,
  `y` tinyint unsigned NOT NULL,
  `z` int NOT NULL,
  `placed` boolean NOT NULL,
  `dimensionid`smallint unsigned,
  PRIMARY KEY (`id`),
  KEY `coords` (`x`,`z`,`y`),
  KEY `date` (`date`),
  KEY (`playerid`),
  KEY (`type`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `containers` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `uuid` varchar(255) NOT NULL,
  `lastaccess` datetime NOT NULL,
  `itemtype` smallint unsigned NOT NULL,
  `x` int NOT NULL,
  `y` smallint(5) unsigned NOT NULL,
  `z` int NOT NULL,
  `firstplayer` int unsigned NOT NULL,
  `lastplayer` int unsigned NOT NULL,
  `dimensionid`smallint unsigned,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`uuid`),
  KEY `lastaccess` (`lastaccess`),
  KEY `coords` (`x`,`z`,`y`),
  KEY (`firstplayer`),
  KEY (`lastplayer`),
  KEY (`itemtype`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `container_transactions` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `playerid` int unsigned NOT NULL,
  `containerid` int unsigned NOT NULL,
  `date` datetime NOT NULL,
  `itemtype` smallint unsigned NOT NULL,
  `itemcount` int NOT NULL,
  `itemdata` mediumblob DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `date` (`date`),
  KEY (`playerid`),
  KEY (`containerid`),
  KEY (`itemtype`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `killed_entities` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `source` VARCHAR(100) NOT NULL,
  `killerid` int unsigned,
  `date` datetime NOT NULL,
  `x` int NOT NULL,
  `y` smallint(5) unsigned NOT NULL,
  `z` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `coords` (`x`,`z`,`y`),
  KEY `date` (`date`),
  KEY (`killerid`),
  KEY (`source`),
  KEY (`name`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `mob_grief` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `entity_type` smallint unsigned NOT NULL,
  `target` int unsigned,
  `x` int NOT NULL,
  `y` smallint(5) unsigned NOT NULL,
  `z` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `coords` (`x`,`z`,`y`),
  KEY `date` (`date`),
  KEY (`target`),
  KEY (`entity_type`)
) ENGINE=InnoDB;

