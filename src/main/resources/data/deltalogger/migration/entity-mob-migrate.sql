DROP TABLE killed_entities;
CREATE TABLE `killed_entities`
(
    `id` INTEGER PRIMARY KEY,
    `source`       VARCHAR(100)      NOT NULL,
    `killer_id`    INTEGER UNSIGNED,
    `dimension_id` SMALLINT UNSIGNED,
    `date`         DATETIME          NOT NULL,
    `x`            INTEGER           NOT NULL,
    `y`            SMALLINT UNSIGNED NOT NULL,
    `z`            INTEGER           NOT NULL,
    `entity_type`  SMALLINT UNSIGNED NOT NULL,
    `entity_nbt` TEXT DEFAULT NULL,
    FOREIGN KEY (`killer_id`) REFERENCES players (`id`),
    FOREIGN KEY (`dimension_id`) REFERENCES registry (`id`)
);
CREATE INDEX IF NOT EXISTS `killed_entities_x_idx` ON killed_entities(`x`);
CREATE INDEX IF NOT EXISTS `killed_entities_y_idx` ON killed_entities(`y`);
CREATE INDEX IF NOT EXISTS `killed_entities_z_idx` ON killed_entities(`z`);
CREATE INDEX IF NOT EXISTS `killed_entities_date_idx` ON killed_entities(`date`);
CREATE INDEX IF NOT EXISTS `killed_entities_killer_id_idx` ON killed_entities(`killer_id`);
CREATE INDEX IF NOT EXISTS `killed_entities_source_idx` ON killed_entities(`source`);
CREATE INDEX IF NOT EXISTS `killed_entities_dimension_id_idx` ON killed_entities(`dimension_id`);