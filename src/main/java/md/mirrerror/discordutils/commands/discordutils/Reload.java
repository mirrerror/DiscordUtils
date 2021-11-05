package md.mirrerror.discordutils.commands.discordutils;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Reload extends SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        Main.getInstance().getConfigManager().reloadConfigFiles();
        sender.sendMessage(Message.CONFIG_FILES_RELOADED.getText(true));
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutils.reload";
    }
}
