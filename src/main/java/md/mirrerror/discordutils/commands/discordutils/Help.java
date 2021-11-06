package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Help extends SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        Message.sendMessage(sender, Message.HELP);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.help";
    }
}
