package md.mirrerror.discordutils.commands;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private static Map<String, List<SubCommand>> commands = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(commands.get(command.getName()) != null) {
            int newLength = 0;
            if(args.length >= 1) newLength = args.length-1;
            String[] newArgs = new String[newLength];
            for (int i = 1, j = 0; i < args.length && j < args.length; i++, j++) {
                newArgs[j] = args[i];
            }
            boolean hasSubCommand = false;
            if(args.length >= 1) for(SubCommand subCommand : commands.get(command.getName())) {
                if(subCommand.getName().equals(args[0])) {
                    hasSubCommand = true;
                    if(sender.hasPermission(subCommand.getPermission())) {
                        subCommand.onCommand(sender, command, label, newArgs);
                    } else {
                        sender.sendMessage(Message.INSUFFICIENT_PERMISSIONS.getText(true));
                    }
                }
            }
            if(!hasSubCommand) sender.sendMessage(Message.UNKNOWN_SUBCOMMAND.getText(true));
        }
        return true;
    }

    public void registerCommand(String command, List<SubCommand> subCommands) {
        commands.put(command, subCommands);
        Main.getInstance().getCommand(command).setExecutor(this);
    }

    public void registerSubCommand(String command, SubCommand subCommand) {
        if(commands.get(command) == null) return;
        List<SubCommand> subCommands = commands.get(command);
        subCommands.add(subCommand);
        commands.put(command, subCommands);
    }

    public static Map<String, List<SubCommand>> getCommands() {
        return commands;
    }
}
