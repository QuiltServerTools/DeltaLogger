# DISCLAIMER

This project is still under heavy development and is not recommended for use yet.
# DeltaLogger

Block and chest/container inventory tracking tool for fabric that can be configured to use MySQL or SQLite.

## Setup

DeltaLogger supports two types of database: MySQL and SQLite
Place the mod jar in the mods folder of your server directory and launch your fabric server once to make it generate a `config/deltalogger.properties`. Or make a file called `deltalogger.properties` in the `config` folder of your server directory. Put the following as the file content:

```
# DeltaLogger configuration

# Whether you want to use an SQLite database. Put false for MySQL.
use_sqlite=true

##! MYSQL ONLY BELOW. If you are using SQLite these  are safe to ignore. !##

# Name of the MySQL database to use. If you do not have one, then do
# "CREATE DATABASE yourdbname" from a MySQL client first.
database=

# MySQL port
port=
# MySQL host/url
host=
# MySQL username
username=
# MySQL password
password=

# MySQL SSL configuration. You can leave this as false but the database
# connection will not be encrypted. If you are using MySQL across the open internet
# then you should strongly consider properly enabling SSL
useSSL=false
requireSSL=false
```
By default, SQLite is enabled. This means that your logs will be saved to a database found in `world/deltalogger.sqlite`. If you want to use MySQL, fill out the additional properties with the relevant information to access your database.

This mod also requires the fabric api mod, which you can find [here](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

## In Game Commands

- `/dl resetpass` get a temporary password for the web interface.
- `/dl inspect` turns diamond swords into an inspect tool. Whack a block or container to see recent interactions with the target.
- `/dl inspect <pos>` Shows database records for the block position provided.
- `/dl search <args>` Builds a database query with the parameters specified
- `/dl sql (block|transaction) <query>` Runs a query on the database with the SQL specified

## Support

Support can be obtained on the discord found [here](https://discord.gg/UxHnDWr)

## License

Licensed under AGPL with additional linking permission. See LICENSE file.

## Build

Windows:
`gradlew.bat build`
Linux/macOS
`./gradlew build`

The mod jars are then found in `build/libs`

## Contribute

Feel free to fork and open a pull request with features you add. We do suggest asking on the discord about a feature before spending loads on time on it!
