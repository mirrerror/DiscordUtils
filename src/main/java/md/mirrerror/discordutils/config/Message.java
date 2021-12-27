package md.mirrerror.discordutils.config;

import md.mirrerror.discordutils.Main;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public enum Message {

    PREFIX,
    INSUFFICIENT_PERMISSIONS,
    ACCOUNT_SUCCESSFULLY_LINKED,
    ACCOUNT_UNLINK_REQUEST_SENT,
    ACCOUNT_SUCCESSFULLY_UNLINKED,
    ACCOUNT_UNLINK_CONFIRMATION,
    ACCOUNT_UNLINK_CANCELLED,
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
    VERIFICATION_NEEDED,
    TWOFACTOR_AUTHORIZED,
    TWOFACTOR_CODE_MESSAGE,
    TWOFACTOR_REACTION_MESSAGE,
    TWOFACTOR_DISABLED_REMINDER,
    VERIFICATION_MESSAGE,
    VERIFICATION_CODE_MESSAGE,
    CAN_NOT_SEND_MESSAGE,
    UNKNOWN_SUBCOMMAND,
    LINK_ALREADY_INITIATED,
    DISCORDUTILS_SENDTODISCORD_USAGE,
    SENDTODISCORD_SENT_BY,
    COMMAND_DISABLED,
    INVALID_COLOR_VALUE,
    ONLINE,
    COMMAND_EXECUTED,
    DISCORD_SUDO_USAGE,
    DISCORD_EMBED_USAGE,
    VOICE_INVITE_SENT,
    VOICE_INVITE,
    VOICE_INVITE_HOVER,
    SENDER_IS_NOT_IN_A_VOICE_CHANNEL,
    EMBED_SENT_BY,
    ERROR,
    INFORMATION,
    SUCCESSFULLY,
    EMBED_FOOTER,
    HELP;

    public String getText() {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfigManager().getLang().getString(String.valueOf(this)).replace("\\n", "\n"));
    }

    public String getText(boolean addPrefix) {
        if(addPrefix) return PREFIX.getText() + " " + this.getText();
        return this.getText();
    }

    public List<String> getStringList() {
        List<String> stringList = new ArrayList<>();
        Main.getInstance().getConfigManager().getLang().getStringList(String.valueOf(this)).forEach(s -> stringList.add(ChatColor.translateAlternateColorCodes('&', s).replace("\\n", "\n")));
        return stringList;
    }

    public List<String> getStringList(boolean addPrefix) {
        List<String> stringList = new ArrayList<>();
        if(addPrefix) {
            for(String s : Main.getInstance().getConfigManager().getLang().getStringList(String.valueOf(this))) {
                stringList.add(ChatColor.translateAlternateColorCodes('&', PREFIX.getText() + " " + s.replace("\\n", "\n")));
            }
        } else return this.getStringList();
        return stringList;
    }

}
