# DeltaLogger

![build](https://img.shields.io/github/workflow/status/fabricservertools/DeltaLogger/build)
[![discord](https://img.shields.io/discord/764543203772334100?label=discord)](https://discord.gg/jydqZzkyEa)
![stable](https://img.shields.io/github/v/release/fabricservertools/DeltaLogger?label=latest%20release)
![Modrinth downloads](https://img.shields.io/badge/dynamic/json?color=brightgreen&label=Modrinth%20downloads&query=downloads&url=https%3A%2F%2Fapi.modrinth.com%2Fapi%2Fv1%2Fmod%2FWgFOx7Xi)
![GitHub downloads](https://img.shields.io/github/downloads/fabricservertools/DeltaLogger/total?label=GitHub%20downloads&color=blueviolet)

Block and chest/container inventory tracking tool for fabric that can be configured to use MySQL or SQLite with a fast and responsive web UI.
[Join the DeltaLogger discord](https://discord.gg/UxHnDWr)

## Setup

DeltaLogger supports two types of database: MySQL and SQLite
Place the mod jar in the mods folder of your server directory and launch your fabric server once to make it generate a `config/deltalogger.properties`. Or make a file called `deltalogger.properties` in the `config` folder of your server directory. Put the following as the file content:

```
# DeltaLogger configuration

# Whether you want to use an SQLite database. Put false for MySQL.
use_sqlite=true

# Port to use for the web app. Navigate to http://serverhostname:port to see web app
webapp_port=

# Server domain url or hostname. Used for giving links to the web app
webapp_host=

##! MYSQL ONLY BELOW. If you are using SQLite these are safe to ignore. !##

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
By default, SQLite is enabled. This means that your logs will be saved to a database found in `world/deltalogger.sqlite`. If you want to use MySQL, fill out the additional properties with the relevant information to access your database and set `use_sqlite` to `false`.

This mod also requires the fabric api mod, which you can find [here](https://www.curseforge.com/minecraft/mc-mods/fabric-api).

## In Game Commands

- `/dl resetpass` get a temporary password for the web interface.
- `/dl inspect` Whack a block or container to see recent interactions with the target.
- `/dl inspect <pos>` Shows database records for the block position provided.
- `/dl search <args>` Builds a database query with the parameters specified
- `/dl sql (block|transaction) <query>` Runs a query on the database with the SQL specified. Only available in a development environment

## Support

Support can be obtained on the discord found [here](https://discord.gg/UxHnDWr)

## License

Licensed under AGPL with additional linking permission. See LICENSE file.

## Issues

If you find a bug, please open an issue on the [issues page](https://github.com/fabricservertools/DeltaLogger/issues). Please ask on the discord if you have a support issue which is not a bug

### Issue resolving

Issues which are not valid will be closed as invalid. We will fix remaining issues once we decide the time is right. Generally, major issues will be fixed first.

## Build

You will need a JDK installed. We suggest AdoptOpenJDK 8, which you can download [from their website](https://adoptopenjdk.net/releases.html?variant=openjdk8&jvmVariant=hotspot). From there, just download the code, open a terminal in the location of the code and run the following command:

Windows:
`gradlew.bat build`

Linux/macOS
`./gradlew build`

The mod jars are then found in `build/libs`. For almost all uses, you want the jar without `-dev` or `-sources` on the end

## Contribute

Feel free to fork and open a pull request with features you add. We do suggest asking on the discord about a feature before spending loads on time on it! Ping `@yitzy` if you want to have access to the development channel

### Translations

Please see `translations.md` for information about contributing translations
