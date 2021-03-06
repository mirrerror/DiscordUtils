package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class Help implements SubCommand {
    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        Message.HELP.getStringList().forEach(sender::sendMessage);
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.help";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("h");
    }
}
