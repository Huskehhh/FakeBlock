# FakeBlock [![Build Status](https://travis-ci.com/Huskehhh/FakeBlock.svg?branch=master)](https://travis-ci.com/Huskehhh/FakeBlock)

Have you ever wanted to make things appear differently to different groups of players? Have you ever wanted to have hidden paths that only certain players could see? Have you ever wanted to change how the world appeared to players at different times? Now you can!

This versatile plugin allows you to create fake blocks and walls that exist for some players and do not exist for others!

These fake blocks are indistinguishable from real blocks and prevent players from walking through them, or interacting with them.

Every wall has its own permission and giving a player the permission for the wall completely removes it for that player. The wall does not exist for players who have its permission.

## Use cases

This plugin has many potential uses in a variety of different circumstances and especially for RPG servers.
````
1. Creating hidden VIP areas on your server.
2. Creating hidden Staff areas.
3. Hidden passages and hallways that are only accessible by certain players or ranks.
4. Control the progress of players along RPG questlines by selectively opening and closing different areas with permissions.
5. Shops and other buildings with iron doors/flat walls that are open only for certain players who have the permission.
6. Castle and Fort gates that can be opened or closed depending on the permissions of the player at that time.
7. Buildings that look different and have different blocks at different times.
8. Buildings can be "repaired" or "damaged" along the progress of certain questlines by fixing or creating holes in its walls by removing and adding permissions to players.
9. Edges of portals like nether portals etc can be made to appear different to players using fake blocks.
 ````
Any variable blocks that you want to look different for players at different times can be done.

## [Download](https://ci.husk.pro/job/FakeBlock/)
## [Spigot Resource Page](https://www.spigotmc.org/resources/fakeblock.12830/)

### Compiling from source
You must have [Apache Maven](http://maven.apache.org) installed to compile.

to compile use the following command:

```xml
mvn clean package
```

Artifacts can be found in their respective file target folders