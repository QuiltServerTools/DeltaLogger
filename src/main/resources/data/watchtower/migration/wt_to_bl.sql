/* RENAMING */
ALTER TABLE players CHANGE lastonline last_online_time DATETIME;
ALTER TABLE placements CHANGE playerid player_id INTEGER UNSIGNED;
ALTER TABLE placements CHANGE dimensionid dimension_id SMALLINT UNSIGNED;
ALTER TABLE containers CHANGE lastaccess last_access DATETIME;
ALTER TABLE containers CHANGE itemtype item_type SMALLINT UNSIGNED;
ALTER TABLE containers CHANGE firstplayer first_player_id INTEGER UNSIGNED;
ALTER TABLE containers CHANGE lastplayer last_player_id INTEGER UNSIGNED;
ALTER TABLE containers CHANGE dimensionid dimension_id SMALLINT UNSIGNED;
ALTER TABLE container_transactions CHANGE playerid player_id INTEGER UNSIGNED;
ALTER TABLE container_transactions CHANGE containerid container_id INTEGER UNSIGNED;
ALTER TABLE container_transactions CHANGE itemtype item_type SMALLINT UNSIGNED;
ALTER TABLE container_transactions CHANGE itemcount item_count INTEGER NOT NULL;
ALTER TABLE container_transactions CHANGE itemdata item_data MEDIUMBLOB DEFAULT NULL;
ALTER TABLE killed_entities CHANGE killerid killer_id INTEGER UNSIGNED;
ALTER TABLE killed_entities ADD COLUMN dimension_id SMALLINT AFTER killer_id;
/* ALTER TABLE mob_grief CHANGE entitytype entity_type SMALLINT UNSIGNED; */
ALTER TABLE mob_grief ADD COLUMN dimension_id SMALLINT AFTER `target`;

/* DROP OLD INDICES */
ALTER TABLE placements DROP INDEX `date`;
ALTER TABLE placements DROP INDEX `type`;
ALTER TABLE placements DROP INDEX `playerid`;
ALTER TABLE placements DROP INDEX `coords`;
ALTER TABLE containers DROP INDEX `lastaccess`;
ALTER TABLE containers DROP INDEX `coords`;
ALTER TABLE containers DROP INDEX `firstplayer`;
ALTER TABLE containers DROP INDEX `lastplayer`;
ALTER TABLE containers DROP INDEX `itemtype`;
ALTER TABLE container_transactions DROP INDEX `date` ;
ALTER TABLE container_transactions DROP INDEX `playerid`;
ALTER TABLE container_transactions DROP INDEX `containerid`;
ALTER TABLE container_transactions DROP INDEX `itemtype`;
ALTER TABLE killed_entities DROP INDEX `coords`;
ALTER TABLE killed_entities DROP INDEX `killerid`;
ALTER TABLE killed_entities DROP INDEX `source`;
ALTER TABLE killed_entities DROP INDEX `name`;
ALTER TABLE killed_entities DROP INDEX `date`;
ALTER TABLE kv_store DROP INDEX `key`;
ALTER TABLE mob_grief DROP INDEX `coords`;
ALTER TABLE mob_grief DROP INDEX `date`;
ALTER TABLE mob_grief DROP INDEX `target`;
ALTER TABLE mob_grief DROP INDEX `entity_type`;
ALTER TABLE players DROP INDEX `name`;
ALTER TABLE players DROP INDEX `uuid`;
ALTER TABLE registry DROP INDEX `name`;

/* CHANGE COLUMNS */
ALTER TABLE placements ADD `state` TEXT; 
ALTER TABLE container_transactions DROP COLUMN item_data; 
ALTER TABLE container_transactions ADD item_data TEXT; 
