package md.mirrerror.discordutils.config;

import md.mirrerror.discordutils.Main;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Message {

    PREFIX,
    INSUFFICIENT_PERMISSIONS,
    ACCOUNT_SUCCESSFULLY_LINKED,
    INVALID_LINK_CODE,
    INVALID_TWOFACTOR_CODE,
    TWOFACTOR_REJECTED,
    ACCOUNT_ALREADY_VERIFIED,
    DISCORDUTILS_LINK_USAGE,
    SENDER_IS_NOT_A_PLAYER,
    CONFIG_FILES_RELOADED,
    ACCOUNT_IS_NOT_VERIFIED,
    ENABLED,
    DISABLED,
    DISCORDUTILS_TWOFACTOR_SUCCESSFUL,
    TWOFACTOR_NEEDED,
    TWOFACTOR_AUTHORIZED,
    TWOFACTOR_CODE_MESSAGE,
    TWOFACTOR_REACTION_MESSAGE,
    VERIFICATION_MESSAGE,
    VERIFICATION_CODE_MESSAGE,
    UNKNOWN_SUBCOMMAND,
    LINK_ALREADY_INITIATED,
    ONLINE,
    COMMAND_EXECUTED,
    DISCORD_SUDO_USAGE,
    ERROR,
    INFORMATION,
    SUCCESSFULLY,
    HELP;

    public String getText() {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfigManager().getLang().getString(String.valueOf(this)));
    }

    public String getText(boolean addPrefix) {
        if(addPrefix) return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfigManager().getLang().getString(String.valueOf(PREFIX)) + " "
                + ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfigManager().getLang().getString(String.valueOf(this))));
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfigManager().getLang().getString(String.valueOf(this)));
    }

    public List<String> getStringList() {
        List<String> stringList = new ArrayList<>();
        Main.getInstance().getConfigManager().getLang().getStringList(String.valueOf(this)).forEach(s -> stringList.add(ChatColor.translateAlternateColorCodes('&', s)));
        return stringList;
    }

    public List<String> getStringList(boolean addPrefix) {
        List<String> stringList = new ArrayList<>();
        if(addPrefix)
            Main.getInstance().getConfigManager().getLang().getStringList(String.valueOf(this)).forEach(s -> stringList.add(ChatColor.translateAlternateColorCodes('&',
                    Main.getInstance().getConfigManager().getLang().getString(String.valueOf(PREFIX)) + " " + s)));
        else Main.getInstance().getConfigManager().getLang().getStringList(String.valueOf(this)).forEach(s -> stringList.add(ChatColor.translateAlternateColorCodes('&', s)));
        return stringList;
    }

/*    public static void sendMessage(Player player, Message message, boolean addPrefix) {
        try {
            message.getStringList(addPrefix).forEach(player::sendMessage);
            return;
        } catch (Exception ignored) {}
        player.sendMessage(message.getText(addPrefix));
    }

    public static void sendMessage(Player player, Message message) {
        try {
            message.getStringList().forEach(player::sendMessage);
            return;
        } catch (Exception ignored) {}
        player.sendMessage(message.getText());
    }

    public static void sendMessage(CommandSender commandSender, Message message, boolean addPrefix) {
        try {
            message.getStringList(addPrefix).forEach(commandSender::sendMessage);
            return;
        } catch (Exception ignored) {}
        commandSender.sendMessage(message.getText(addPrefix));
    }

    public static void sendMessage(CommandSender commandSender, Message message) {
        try {
            message.getStringList().forEach(commandSender::sendMessage);
            return;
        } catch (Exception ignored) {}
        commandSender.sendMessage(message.getText());
    }*/
}
