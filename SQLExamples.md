# Example SQL queries

Query to check all container transactions of EXAMPLE_PLAYER_NAME
```sql
SELECT date, P.name, I.name, item_count, C.x, C.z, PC.name, CT.name
FROM container_transactions
INNER JOIN players as P on player_id=P.id
INNER JOIN registry as I on item_type=I.id
INNER JOIN containers as C on container_id=C.id
INNER JOIN players as PC on C.first_player_id=PC.id
INNER JOIN registry as CT on CT.id=C.item_type
WHERE P.name="EXAMPLE_PLAYER_NAME"
ORDER BY date DESC;
```

Query to check all placements in x coordinate between 60 and 100, z between 130 and 170, in the overworld dimension. For block removals use `placed = 0`
```sql
SELECT P.name, PC.date, R.name, PC.placed, DR.name, PC.x, PC.y, PC.z
FROM placements as PC
INNER JOIN players as P ON PC.player_id = P.id
INNER JOIN registry as R ON PC.type = R.id
INNER JOIN registry as DR ON PC.dimension_id = DR.id
WHERE x > 60 AND x < 100 AND z > 130 AND z < 170 AND DR.name = "minecraft:overworld" AND placed = 1
ORDER BY date DESC;
```

Query to check player's mined diamond ores
```sql
SELECT P.name, PC.date, R.name, PC.placed, DR.name, PC.x, PC.y, PC.z
FROM placements as PC
INNER JOIN players as P ON PC.player_id = P.id
INNER JOIN registry as R ON PC.type = R.id
INNER JOIN registry as DR ON PC.dimension_id = DR.id
WHERE R.name = "minecraft:diamond_ore" AND P.name="EXAMPLE_PLAYER_NAME" AND placed = 0
ORDER BY date DESC;
```

Check for mob explosion griefing and which players the mobs targetted
```sql
SELECT date, M.name, P.name, x, y, z
FROM mob_grief
INNER JOIN players as P on P.id = target
INNER JOIN registry as M on M.id = entity_type
ORDER BY date DESC;
```
