DELETE FROM killed_entities;
ALTER TABLE killed_entities ADD COLUMN `entity_type` SMALLINT UNSIGNED NOT NULL;