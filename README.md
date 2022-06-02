# DiscordUtils
DiscordUtils plugin for Minecraft. This plugin allows you to host your own Discord bot on your Minecraft server and get a lot of handy utilities!

## Social:
* Discord: https://discord.gg/47txjnVtz7 **(If you encounter any problems with my plugin and want to receive a free technical support - you can join the plugin's Discord server and create a ticket)**
* Donation link: https://paypal.me/mirrerror **(all your support will be very appreciated and will give me much more will to update the plugin)**

## Testing:
You have the possibility to test the plugin without installing it on your server. You can join our server and test our plugin if you want to. IP: **discordutils.pacenode.org**.

## Features:
* Link your Discord account with your in-game account;
* 2Factor authentication using Discord;
* 2FA sessions (can be disabled);
* Possibility to give rewards for players for being in voice channels;
* Other bot commands (you can check your server's current online and other);
* Integration with an permissions plugin for auto role assigning and removing in your Discord server;
* Customizing messages;
* Automatic update checker (can be disabled);
* Invites in voice channels in-game;
* Nickname synchronization;
* Animated bot's activity;
* PlaceholderAPI integration;
* Possibility to create a virtual console on your Discord server (you can execute commands as a console sender there and see the full console log) (commands can be executed only by the admins);
* Possibility to log some events from Minecraft server to your Discord server (join, quit, death, chat);
* Possibility to notify the verified players about their mentions in Discord.

## Bot commands list:
### Discord:
* `!link` — link your Discord account with your in-game account;
* `!online` — check your current server online;
* `!stats` — check your stats (based on PAPI placeholders);
* `!sudo` — execute a server command as a console sender (admins only);
* `!embed` — send an embed message (admins only).

### Server:
* `/du help` — command help (permission - `discordutils.discordutils.help`);
* `/du link` — link your Discord account with your in-game account (permission - `discordutils.discordutils.link`);
* `/du unlink` — unlink your Discord account from your in-game account (permission - `discordutils.discordutils.unlink`);
* `/du reload` — reload the configuration files (permission - `discordutils.discordutils.reload`);
* `/du twofactor` — enable/disable 2FA (permission - `discordutils.discordutils.twofactor`);
* `/du sendtodiscord` — send an embed message to the Discord server (permission - `discordutils.discordutils.sendtodiscord`);
* `/du voiceinvite` — invite all online players for conversation in the voice channel (permission - `discordutils.discordutils.voiceinvite`);
* `/du getdiscord` — get verified player's Discord (permission - `discordutils.discordutils.getdiscord`).

## Plugin's Support:
* LuckPerms;
* Vault (supports multiple plugins to integrate with).
* DiscordSRV (Use `%discord_id%` to create a link between the minecraft player and Discord User during verification)

## Install plugin:

### Video: https://youtu.be/5XgrHoyLyPs

### Guide:
0. Stop server.
1. Download [jar file](https://www.spigotmc.org/resources/discordutils-discord-bot-for-your-minecraft-server.97433)
2. Open `plugins` folder in server.
3. Drop jar file in folder.
4. Run server and edit config.

## Build plugin's (Maven)
1. `apt instal maven`
2. `git clone https://github.com/mirrerror/DiscordUtils`
3. `cd DiscordUtils`
4. `mvn compile`

___

Project distributed under **Apache License 2.0**
