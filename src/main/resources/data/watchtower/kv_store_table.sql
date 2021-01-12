/** KV-STORE for misc configuration things */
CREATE TABLE IF NOT EXISTS `kv_store` (
  `id` /**!PRIMARY_KEY*/,
  `key` VARCHAR(255) NOT NULL UNIQUE,
  `value` VARCHAR(255)
);
CREATE UNIQUE INDEX IF NOT EXISTS `kv_store_key` ON kv_store(`key`);