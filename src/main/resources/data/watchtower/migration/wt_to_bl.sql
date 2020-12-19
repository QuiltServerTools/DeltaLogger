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
