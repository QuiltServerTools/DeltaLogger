# DISCLAIMER

This project is still under heavy development and is not recommended for use yet.
# DeltaLogger

Block and chest/container inventory tracking tool for fabric that can be configured to use MySQL or SQLite.

## Setup

DeltaLogger supports two types of database: MySQL and SQLite
Place the mod jar in the mods folder of your server directory and launch your fabric server once to make it generate a `config/deltalogger.properties`. Or make a file called `deltalogger.properties` in the `config` folder of your server directory. Put the following as the file content:

```
# Config for DeltaLogger

# Use SQLite
use_sqlite=true
# MySQL database password (Optional if using SQLite)
password=

# MySQL Database name (Optional if using SQLite)
database=

# MySQL port (Optional if using SQLite)
port=3306

# MySQL hostname/ip address (Optional if using SQLite)
host=localhost

# MySQL username (Optional if using SQLite)
username=mc

# SSL configuration. You can leave this as false but the database connection will not be encrypted. (Optional if using SQLite)
useSSL=false
requireSSL=false
```
By default, SQLite is enabled. This means that your logs will be saved to a database found in `world/dl.db`. If you want to use MySQL, fill out the additional properties with the relevant information to access your database.

This mod also requires the fabric api mod, which you can find [here](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

## In Game Commands

- `/dl inspect` turns diamond swords into an inspect tool. Whack a block or container to see recent interactions with the target.
- `/dl inspect <pos>` will show database records for the block position provided.
- `/dl search <args>` Builds a database query with the parameters specified (Not working)

## Support

Support can be obtained on the discord found [here](https://discord.gg/UxHnDWr)
## License

Licensed under AGPL with additional linking permission. See LICENSE file.