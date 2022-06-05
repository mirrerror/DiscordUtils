package md.mirrerror.discordutils.commands.discordutilsadmin;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.config.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class Reload implements SubCommand {

    @Override
    public void onCommand(CommandSender sender, Command command, String label, String[] args) {
        Main.getInstance().getConfigManager().reloadConfigFiles();
        Message.CONFIG_FILES_RELOADED.getFormattedText(true).forEach(sender::sendMessage);
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "discordutils.discordutilsadmin.reload";
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("rl");
        aliases.add("rel");
        return aliases;
    }
}
