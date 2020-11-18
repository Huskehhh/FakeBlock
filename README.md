# FakeBlock

![Java CI with Maven](https://github.com/Huskehhh/FakeBlock/workflows/Java%20CI%20with%20Maven/badge.svg)
![drovah CI](https://ci.husk.pro/FakeBlock/badge)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a3a1e78960f243a79d3c585e9d09819f)](https://app.codacy.com/manual/Huskehhh/FakeBlock?utm_source=github.com&utm_medium=referral&utm_content=Huskehhh/FakeBlock&utm_campaign=Badge_Grade_Dashboard)

This versatile plugin allows you to create fake blocks that exist for some players and do not exist for others!

These fake blocks are indistinguishable from real blocks and prevent players from walking through them, or interacting
with them.

## Installation

First of all, download and install [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

Once that's complete, download and install FakeBlock

- Latest for 1.16.2+
- Intermediate for 1.13 -> 1.16.1
- Legacy for 1.7 -> 1.12

## Creation

To create a fake block selection, use the command

``/fakeblock create <name>`` obviously replacing <name> with your desired name. (Be careful, large selections will cause
lag. Ideally set this up without players online)

The selection requires you to create a cuboid selection (similar to WorldEdit, select the two diagonal corners). Once
this is complete, the plugin will replace the selection with AIR, and instead send "fake" blocks.

Players WITH the permission
``fakeblock.<name>`` will be sent these "fake" blocks. This is different to v1, where it is inversed.

## Deletion

To delete a wall (and restore it's previous state), use ``/fakeblock delete <name>`` (Be careful, large selections will
cause lag. Ideally set this up without players online)

## Suggestions

If you have any suggestions or criticism, please report it on the GitHub page.

Optionally depends on [LuckPerms](https://luckperms.net/) to update wall visibility on permission change.

## Note: Version 1 configurations are NOT compatible with version 2

## [Download latest build(s)](https://ci.husk.pro/)

## [Spigot Resource Page](https://www.spigotmc.org/resources/fakeblock.12830/)

## Compiling from source

This project now uses Gradle!

```./gradlew build``` for Unix based systems

```gradlew.bat build``` for Windows

Artifacts can be found in their respective build/libs/ folder!

## Maven repo

If you are planning to hook into FakeBlock, you will need to add the repository, as well as the dependency.

For example:

```xml
<repository>
    <id>husk</id>
    <url>https://maven.husk.pro/repository/maven-public/</url>
</repository>
```

```xml
<dependency>
    <groupId>pro.husk</groupId>
    <artifactId>FakeBlock-latest</artifactId>
    <version>2.0.3-SNAPSHOT</version>
</dependency>
```

Once that's complete, you can work with the WallObject relevant to your target version.

Example of creating both persistent and non persistent wall

```java
// Persistent example... the rest is done for us!
LatestMaterialWall latestMaterialWallPersistent = new LatestMaterialWall("some_persistent_wall", location1, location2);

// Non-persistent example
LatestMaterialWall latestMaterialWall = new LatestMaterialWall("some_non_persistent_wall");

// For example, build map of Location -> FakeBlockData.. This example uses the world data, however, you might want to load from a schematic or something.
HashMap<Location, FakeBlockData> fakeBlockDataHashMap = new HashMap<>();
latestMaterialWall.loadBlocksInBetween().forEach(location -> fakeBlockDataHashMap.put(location, new FakeBlockData(location.getBlock().getBlockData())));

// Finally create the non persistent wall
latestMaterialWall.createNonPersistentWall(fakeBlockDataHashMap, location1, location2);
```

If you have any further questions feel free to reach out, or create an issue and I'll try help to the best of my ability!