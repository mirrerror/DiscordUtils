package md.mirrerror.discordutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    public abstract void onCommand(CommandSender sender, Command command, String label, String[] args);
    public abstract String getName();
    public abstract String getPermission();

}
