DROP TABLE killed_entities;
CREATE TABLE `killed_entities`
(
    `id` /**!PRIMARY_KEY*/,
    `name`         VARCHAR(100)      NOT NULL,
    `source`       VARCHAR(100)      NOT NULL,
    `killer_id`    INTEGER UNSIGNED,
    `dimension_id` SMALLINT UNSIGNED,
    `date`         DATETIME          NOT NULL,
    `x`            INTEGER           NOT NULL,
    `y`            SMALLINT UNSIGNED NOT NULL,
    `z`            INTEGER           NOT NULL,
    `entity_type`  SMALLINT UNSIGNED NOT NULL,
    FOREIGN KEY (`killer_id`) REFERENCES players (`id`),
    FOREIGN KEY (`dimension_id`) REFERENCES registry (`id`)
);