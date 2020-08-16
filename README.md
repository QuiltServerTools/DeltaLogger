# WatchTower

Block and chest/container inventory tracking tool for fabric modloader that uses MySQL as data storage.

## Setup

Place the mod jar in the mods folder of your server directory and launch your fabric server once to make it generate a `config/watchtower.properties`. Or make a file called `watchtower.properties` in the `config` folder of your server directory. Put the following as the file content:

```
# Config for WatchTower

# MySQL database password
password=

# MySQL Database name
database=

# MySQL port
port=3306

# MySQL hostname/ip address
host=localhost

# MySQL username
username=mc

# SSL configuration. You can leave this as false but the database connection will not be encrypted.
useSSL=false
requireSSL=false
```

This mod also requires the fabric api mod, which you can find [here](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

## In Game Commands

- `/wt tool` turns diamond swords into an inspect tool. Whack a block or container to see recent interactions with the target.

## Support

I developed this for my own server and offer no unpaid support for users. I am open to pull requests to add more features.

There is no UI for queries beyond the simple `/wt tool` command because I can use SQL to query for data. If you'd like something more comprehensive please open an issue and hope that someone will add it. Try offering a [bounty](https://www.bountysource.com/) for it.

## Example SQL queries

Query to check all container transactions of EXAMPLE_PLAYER_NAME
```sql
SELECT date, P.name, I.name, itemcount, C.x, C.z, PC.name, CT.name
FROM container_transactions
INNER JOIN players as P on playerid=P.id
INNER JOIN registry as I on itemtype=I.id
INNER JOIN containers as C on containerid=C.id
INNER JOIN players as PC on C.firstplayer=PC.id
INNER JOIN registry as CT on CT.id=C.itemtype
WHERE P.name="EXAMPLE_PLAYER_NAME"
ORDER BY date DESC;
```

Query to check all placements in x coordinate between 60 and 100, z between 130 and 170, in the overworld dimension. For block removals use `placed = 0`
```sql
SELECT P.name, PC.date, R.name, PC.placed, DR.name, PC.x, PC.y, PC.z
FROM placements as PC
INNER JOIN players as P ON PC.playerid = P.id
INNER JOIN registry as R ON PC.type = R.id
INNER JOIN registry as DR ON PC.dimensionid = DR.id
WHERE x > 60 AND x < 100 AND z > 130 AND z < 170 AND DR.name = "minecraft:overworld" AND placed = 1
ORDER BY date DESC;
```

Query to check player's mined diamond ores
```sql
SELECT P.name, PC.date, R.name, PC.placed, DR.name, PC.x, PC.y, PC.z
FROM placements as PC
INNER JOIN players as P ON PC.playerid = P.id
INNER JOIN registry as R ON PC.type = R.id
INNER JOIN registry as DR ON PC.dimensionid = DR.id
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

## License

Licensed under AGPL with additional linking permission. See LICENSE file.