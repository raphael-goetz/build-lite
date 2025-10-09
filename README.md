# Build-Lite

> **ATTENTION:** This plugin manipulates the server extensively. This plugin is only made for better building experience and shouldn't be used for other things.

But what does it do?
- Let you manage worlds & save world specific data
- Manipulates the physics of worlds
- Manages a users permissions to build (adds a build-mode)
- Manipulates many Minecraft-Events for better building experience
- Build Utilities like Banners...

# Specs
## Supported Minecraft-Version: 1.21.5
## Current Java-Version: 21
## Developed for Paper

# Permissions

```
betterbuild.player.back
```
The permission to use the /back command, to get to your last known location.

```
betterbuild.player.mode
```
The permission to use the /build command.

```
betterbuild.player.fly
```
The permission to update your fly-speed.

```
betterbuild.world.physics
```
The permission to toggle the physics of a world.

```
betterbuild.world.create
```
The permission to create a new world.

```
betterbuild.world.delete
```
The permission to delete a world.

```
betterbuild.world.spawn
```
The permission to change the spawn of a world.
```
betterbuild.world.permission
```

# Commands

```
/back
```
By using the command /back, the user will be teleported to his last known location.

```
/fly <0 - 10>
```
By using the command /fly the flying-speed will be updated.

```
/physics
```
By using the command /physics the physics of the world where the command has been executed will be toggled.

```
/world delete <name>
```
By using the command /world delete the world by the given name will be deleted.

```
/world create <name> <category>
```
By using the command /world create a new world by the given name will be created. When a category is given, it will be automatically sorted by the given category.

```
/world spawn
```
By using the command /world spawn the current spawn of the world will be updated to the location at which the command is executed.

# Events

## Blocks
- Physics are manipulated. Turing them on/off with the /physics toggle command
- Breaking, placing and interacting is only possible on entering the build-mode
- Events that destroy/manipulate blocks on default Minecraft behavior are not permitted to execute

## Entity/Player
- Events that destroy/manipulate entities on default Minecraft behavior are not permitted to execute
- Player teleportation is only possible when the player has the permission required to enter the world
- The player is the only entity on the server that is permitted to interact with the worlds
- Dropping items is only possible when sneaking

# Menus
- World-Overview (Menu to manage worlds)
- Player-Overview (Menu to visit all online-players)
- Banner-Creation (Menu to create banners)