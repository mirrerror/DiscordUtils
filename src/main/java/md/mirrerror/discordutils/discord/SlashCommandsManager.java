package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlashCommandsManager extends ListenerAdapter {

    public SlashCommandsManager() {
        //registerCommands();
        BotController.getJda().addEventListener(this);
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String commandName = event.getName();
        EmbedManager embedManager = new EmbedManager();
        String[] args = event.getCommandPath().split("/");

        switch (commandName) {
            case "link": {
                event.deferReply().queue();
                if(DiscordUtils.isVerified(event.getUser())) {
                    event.replyEmbeds(embedManager.errorEmbed(Message.ACCOUNT_ALREADY_VERIFIED.getText())).queue();
                    return;
                }
                if(BotController.getLinkCodes().containsValue(event.getUser())) {
                    event.replyEmbeds(embedManager.errorEmbed(Message.LINK_ALREADY_INITIATED.getText())).queue();
                    return;
                }
                String code = "";
                byte[] secureRandomSeed = new SecureRandom().generateSeed(20);
                for(byte b : secureRandomSeed) code += b;
                code = code.replace("-", "");

                final String FINAL_CODE = code;
                event.getUser().openPrivateChannel().submit()
                        .thenCompose(channel -> event.replyEmbeds(embedManager.infoEmbed(Message.VERIFICATION_CODE_MESSAGE.getText().replace("%code%", FINAL_CODE))).submit())
                        .whenComplete((msg, error) -> {
                            if(error == null) {
                                event.getHook().sendMessageEmbeds(embedManager.successfulEmbed(Message.VERIFICATION_MESSAGE.getText())).queue();
                                BotController.getLinkCodes().put(FINAL_CODE, event.getUser());
                                return;
                            }
                            event.getHook().sendMessageEmbeds(embedManager.errorEmbed(Message.CAN_NOT_SEND_MESSAGE.getText())).queue();
                        });
                break;
            }
            case "online": {
                event.deferReply().queue();
                event.replyEmbeds(embedManager.infoEmbed(Message.ONLINE.getText().replace("%online%", "" + Bukkit.getOnlinePlayers().size()))).queue();
                break;
            }
            case "sudo": {
                event.deferReply().queue();
                if(!DiscordUtils.isAdmin(event.getUser())) {
                    event.getHook().sendMessageEmbeds(embedManager.errorEmbed(Message.INSUFFICIENT_PERMISSIONS.getText())).queue();
                    return;
                }
                if(args.length < 2) {
                    event.getHook().sendMessageEmbeds(embedManager.infoEmbed(Message.DISCORD_SUDO_USAGE.getText())).queue();
                    return;
                }
                String command = "";
                for(int i = 1; i < args.length; i++) command += args[i] + " ";
                command = command.trim();

                final String FINAL_COMMAND = command;
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), FINAL_COMMAND));
                event.replyEmbeds(embedManager.successfulEmbed(Message.COMMAND_EXECUTED.getText())).queue();
                break;
            }
            case "embed": {
                event.deferReply().queue();
                if(!DiscordUtils.isAdmin(event.getUser())) {
                    event.getHook().sendMessageEmbeds(embedManager.errorEmbed(Message.INSUFFICIENT_PERMISSIONS.getText())).queue();
                    return;
                }
                if(args.length < 4) {
                    event.getHook().sendMessageEmbeds(embedManager.infoEmbed(Message.DISCORD_EMBED_USAGE.getText())).queue();
                    return;
                }
                String text = "";
                for(int i = 3; i < args.length; i++) text += args[i] + " ";
                text = text.trim();

                Color color;
                try {
                    Field field = Class.forName("java.awt.Color").getField(args[2].toUpperCase());
                    color = (Color) field.get(null);
                } catch (Exception e) {
                    color = null;
                }

                if(color == null) {
                    event.getHook().sendMessageEmbeds(embedManager.errorEmbed(Message.INVALID_COLOR_VALUE.getText())).queue();
                    return;
                }

                event.replyEmbeds(embedManager.embed(args[1], text, color, Message.EMBED_SENT_BY.getText().replace("%sender%",
                        event.getUser().getAsTag()))).queue();
            }
        }
    }

    /*private void registerCommands() {
        AtomicBoolean check = new AtomicBoolean(false);
        Main.getInstance().getConfigManager().getSlashCommands().getConfigurationSection("SlashCommands").getKeys(false).forEach(command -> {
            BotController.getJda().retrieveCommands().complete().forEach(cmd -> {
                if(cmd.getName().equals(command)) check.set(true);
            });
            if(check.get()) BotController.getJda().upsertCommand(command, getCommandDescription(command)).queue();
            check.set(false);
        });
    }

    private String getCommandDescription(String commandName) {
        return Main.getInstance().getConfigManager().getSlashCommands().getString("SlashCommands." + commandName + ".Description");
    }*/

}
