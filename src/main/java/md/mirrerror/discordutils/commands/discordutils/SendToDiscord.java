package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.EmbedManager;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.lang.reflect.Field;

public class SendToDiscord implements SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.MessagesChannel.Enabled")) {
            sender.sendMessage(Message.COMMAND_DISABLED.getText(true));
            return;
        }
        if(args.length < 3) {
            sender.sendMessage(Message.DISCORDUTILS_SENDTODISCORD_USAGE.getText(true));
            return;
        }
        long channelId = Main.getInstance().getConfigManager().getConfig().getLong("Discord.MessagesChannel.Id");
        if(channelId <= 0) {
            Main.getInstance().getLogger().severe("You have set an invalid id for the messages TextChannel (id: " + channelId + "). Check your config.yml.");
            return;
        }
        BotController.getJda().getGuilds().forEach(guild -> {
            TextChannel textChannel = guild.getTextChannelById(channelId);
            if(textChannel == null) {
                Main.getInstance().getLogger().severe("The plugin is trying to send a message to a null TextChannel (id: " + channelId + "). Check your config.yml.");
                return;
            }
            EmbedManager embedManager = new EmbedManager();
            String text = "";
            for(int i = 2; i < args.length; i++) text += args[i] + " ";
            text = text.trim();

            Color color;
            try {
                Field field = Class.forName("java.awt.Color").getField(args[1].toUpperCase());
                color = (Color) field.get(null);
            } catch (Exception e) {
                color = null;
            }

            if(color == null) {
                sender.sendMessage(Message.INVALID_COLOR_VALUE.getText(true));
                return;
            }

            textChannel.sendMessageEmbeds(embedManager.embed(args[0], text, color, Message.SENDTODISCORD_SENT_BY.getText().replace("%sender%", sender.getName()))).queue();
        });
    }

    @Override
    public String getName() {
        return "sendtodiscord";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.sendtodiscord";
    }
}
