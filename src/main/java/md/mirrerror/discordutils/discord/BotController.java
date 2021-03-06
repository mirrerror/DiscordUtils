package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.listeners.EventListener;
import md.mirrerror.discordutils.discord.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.util.*;

public class BotController {

    private static Map<Long, String> groupRoles;
    private static List<Long> adminRoles;

    private static JDA jda;
    private String botPrefix = Main.getInstance().getConfigManager().getBotSettings().getString("BotPrefix");

    private static final List<GatewayIntent> gatewayIntents = new ArrayList<>();

    private static final Map<String, User> linkCodes = new HashMap<>();
    private static final Map<Player, String> twoFactorPlayers = new HashMap<>();
    private static final Map<String, Integer> twoFactorAttempts = new HashMap<>(); // ip, attempts
    private static final Map<UUID, TwoFactorSession> sessions = new HashMap<>();
    private static final Map<Player, Message> unlinkPlayers = new HashMap<>();

    private static List<Long> rewardBlacklistedVoiceChannels = new ArrayList<>();
    private static List<String> virtualConsoleBlacklistedCommands = new ArrayList<>();

    private static TextChannel serverActivityLoggingTextChannel;
    private static TextChannel consoleLoggingTextChannel;

    public static void setupBot(String token) {
        try {
            setupGatewayIntents();

            jda = JDABuilder.create(gatewayIntents)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .addEventListeners(new EventListener())
                            .addEventListeners(new BotCommandsListener())
                            .addEventListeners(new BoostListener())
                            .setAutoReconnect(true)
                            .setToken(token)
                            .setContextEnabled(false)
                            .setBulkDeleteSplittingEnabled(false)
                            .setStatus(OnlineStatus.fromKey(Main.getInstance().getConfigManager().getBotSettings().getString("OnlineStatus")))
                            .build();
            jda.awaitReady();

            for (Guild guild : jda.getGuilds()) {
                guild.retrieveOwner(true).queue();
                guild.loadMembers().onSuccess(members -> {
                    Main.getInstance().getLogger().info("Successfully loaded " + members.size() + " members in guild " + guild.getName() + ".");
                }).onError(error -> {
                    Main.getInstance().getLogger().severe("Failed to load members of the guild " + guild.getName() + "!");
                }).get();
            }

            setupGroupRoles();
            setupAdminRoles();

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("GuildVoiceRewards.Enabled")) {
                rewardBlacklistedVoiceChannels = Main.getInstance().getConfigManager().getBotSettings().getLongList("GuildVoiceRewards.BlacklistedChannels");
                jda.addEventListener(new VoiceRewardsListener());
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("Activities.Enabled")) {
                setupActivityChanger();
            }
            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("RolesSynchronization.Enabled") && Main.getPermissionsPlugin() != Main.PermissionsPlugin.NONE) {
                DiscordUtils.setupDelayedRolesCheck();
            } else {
                Main.getInstance().getLogger().warning("Roles synchronization has been disabled due to the lacking of the specified permissions plugin or disabling this option in the config.");
            }
            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("NamesSynchronization.Enabled")) {
                DiscordUtils.setupDelayedNamesCheck();
            } else {
                Main.getInstance().getLogger().warning("Names synchronization has been disabled due to disabling this option in the config.");
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("ServerActivityLogging.Enabled")) {
                serverActivityLoggingTextChannel = jda.getTextChannelById(Main.getInstance().getConfigManager().getBotSettings().getLong("ServerActivityLogging.ChannelId"));
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("Console.Enabled")) {
                virtualConsoleBlacklistedCommands = Main.getInstance().getConfigManager().getBotSettings().getStringList("Console.BlacklistedCommands");
                consoleLoggingTextChannel = jda.getTextChannelById(Main.getInstance().getConfigManager().getBotSettings().getLong("Console.ChannelId"));
                if(consoleLoggingTextChannel == null) {
                    Main.getInstance().getLogger().severe("Failed to setup the virtual console, because you've set a wrong channel id for it. Check your config.yml.");
                    return;
                }

                if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("Console.ClearOnEveryInit")) {
                    TextChannel textChannel = consoleLoggingTextChannel.createCopy().complete();
                    consoleLoggingTextChannel.delete().queue();
                    consoleLoggingTextChannel = textChannel;

                    Main.getInstance().getConfigManager().getBotSettings().set("Console.ChannelId", consoleLoggingTextChannel.getIdLong());
                    Main.getInstance().getConfigManager().saveConfigFiles();
                }

                ConsoleLoggingManager consoleLoggingManager = new ConsoleLoggingManager();
                consoleLoggingManager.initialize();
                jda.addEventListener(new ConsoleCommandsListener());
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("NotifyAboutMentions.Enabled")) {
                jda.addEventListener(new MentionsListener());
            }

            Main.getInstance().getLogger().info("Bot has been successfully loaded.");
        } catch (LoginException | InterruptedException e) {
            Main.getInstance().getLogger().severe("Something went wrong while setting up the bot!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    public static void setupGroupRoles() {
        groupRoles = new HashMap<>();
        for(String s : Main.getInstance().getConfigManager().getBotSettings().getConfigurationSection("GroupRoles").getKeys(false)) {
            groupRoles.put(Long.parseLong(s), Main.getInstance().getConfigManager().getBotSettings().getString("GroupRoles." + s));
        }
    }

    public static void setupAdminRoles() {
        adminRoles = Main.getInstance().getConfigManager().getBotSettings().getLongList("AdminRoles");
    }

    public static void setupActivityChanger() {
        if(Main.getInstance().getActivityManager().getBotActivities().size() == 1) {

            Activity activity = Main.getInstance().getActivityManager().nextActivity();
            jda.getPresence().setActivity(Activity.of(activity.getType(), Main.getInstance().getPapiManager().setPlaceholders(null, activity.getName())));

        } else {

            long updateDelay = Main.getInstance().getConfigManager().getBotSettings().getLong("Activities.UpdateDelay");
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
                Activity activity = Main.getInstance().getActivityManager().nextActivity();
                jda.getPresence().setActivity(Activity.of(activity.getType(), Main.getInstance().getPapiManager().setPlaceholders(null, activity.getName())));
            }, 0L, updateDelay*20L);

        }
    }

    public static void setupGatewayIntents() {
        gatewayIntents.add(GatewayIntent.GUILD_MEMBERS);
        gatewayIntents.add(GatewayIntent.GUILD_EMOJIS);
        gatewayIntents.add(GatewayIntent.GUILD_INVITES);
        gatewayIntents.add(GatewayIntent.GUILD_VOICE_STATES);
        gatewayIntents.add(GatewayIntent.GUILD_PRESENCES);
        gatewayIntents.add(GatewayIntent.GUILD_MESSAGES);
        gatewayIntents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        gatewayIntents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
        gatewayIntents.add(GatewayIntent.DIRECT_MESSAGES);
        gatewayIntents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        gatewayIntents.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
    }

    public static Map<Long, String> getGroupRoles() {
        return groupRoles;
    }

    public static JDA getJda() {
        return jda;
    }

    public String getBotPrefix() {
        return botPrefix;
    }

    public static Map<String, User> getLinkCodes() {
        return linkCodes;
    }

    public static Map<Player, String> getTwoFactorPlayers() {
        return twoFactorPlayers;
    }

    public static List<Long> getAdminRoles() {
        return adminRoles;
    }

    public static Map<UUID, TwoFactorSession> getSessions() {
        return sessions;
    }

    public static List<Long> getRewardBlacklistedVoiceChannels() {
        return rewardBlacklistedVoiceChannels;
    }

    public static Map<Player, Message> getUnlinkPlayers() {
        return unlinkPlayers;
    }

    public static Map<String, Integer> getTwoFactorAttempts() {
        return twoFactorAttempts;
    }

    public static TextChannel getServerActivityLoggingTextChannel() {
        return serverActivityLoggingTextChannel;
    }

    public static TextChannel getConsoleLoggingTextChannel() {
        return consoleLoggingTextChannel;
    }

    public static List<String> getVirtualConsoleBlacklistedCommands() {
        return virtualConsoleBlacklistedCommands;
    }
}
