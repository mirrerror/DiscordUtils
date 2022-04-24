package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.*;

public class BotController {

    private static Map<Long, String> groupRoles;
    private static List<Long> adminRoles;

    private static JDA jda;
    private String botPrefix = Main.getInstance().getConfigManager().getConfig().getString("Discord.BotPrefix");

    private static final List<GatewayIntent> gatewayIntents = new ArrayList<>();

    private static final Map<String, User> linkCodes = new HashMap<>();
    private static final Map<Player, String> twoFactorPlayers = new HashMap<>();
    private static final Map<String, Integer> twoFactorAttempts = new HashMap<>(); // ip, attempts
    private static final Map<UUID, TwoFactorSession> sessions = new HashMap<>();
    private static final Map<User, LocalDateTime> voiceTime = new HashMap<>();
    private static final Map<Player, Message> unlinkPlayers = new HashMap<>();

    private static final List<Long> rewardBlacklistedVoiceChannels = new ArrayList<>();

    private static TextChannel serverActivityLoggingTextChannel;

    public static void setupBot(String token) {
        try {
            setupGatewayIntents();

            jda = JDABuilder.create(gatewayIntents)
                            .setMemberCachePolicy(MemberCachePolicy.ALL)
                            .addEventListeners(new EventListener())
                            .setAutoReconnect(true)
                            .setToken(token)
                            .setContextEnabled(false)
                            .setBulkDeleteSplittingEnabled(false)
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
            setupRewardBlacklistedVoiceChannels();
            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.Activities.Enabled")) {
                setupActivityChanger();
            }
            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.DelayedRolesCheck.Enabled") && Main.getPermissionsPlugin() != Main.PermissionsPlugin.NONE) {
                DiscordUtils.setupDelayedRolesCheck();
            }
            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.DelayedNamesCheck.Enabled")) {
                DiscordUtils.setupDelayedNamesCheck();
            }

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.ServerActivityLogging.Enabled")) {
                serverActivityLoggingTextChannel = jda.getTextChannelById(Main.getInstance().getConfigManager().getConfig().getLong("Discord.ServerActivityLogging.ChannelId"));
            }

            Main.getInstance().getLogger().info("Bot has been successfully loaded.");
        } catch (LoginException | InterruptedException e) {
            Main.getInstance().getLogger().severe("Something went wrong while setting up the bot!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    public static void setupGroupRoles() {
        groupRoles = new HashMap<>();
        for(String s : Main.getInstance().getConfigManager().getConfig().getConfigurationSection("Discord.GroupRoles").getKeys(false)) {
            groupRoles.put(Long.parseLong(s), Main.getInstance().getConfigManager().getConfig().getString("Discord.GroupRoles." + s));
        }
    }

    public static void setupAdminRoles() {
        adminRoles = Main.getInstance().getConfigManager().getConfig().getLongList("Discord.AdminRoles");
    }

    public static void setupRewardBlacklistedVoiceChannels() {
        adminRoles = Main.getInstance().getConfigManager().getConfig().getLongList("Discord.GuildVoiceRewards.BlacklistedChannels");
    }

    public static void setupActivityChanger() {
        if(Main.getInstance().getActivityManager().getBotActivities().size() <= 1) return;

        long updateDelay = Main.getInstance().getConfigManager().getConfig().getLong("Discord.Activities.UpdateDelay");
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
            Activity activity = Main.getInstance().getActivityManager().nextActivity();
            jda.getPresence().setActivity(Activity.of(activity.getType(), Main.getInstance().getPapiManager().setPlaceholders(null, activity.getName())));
        }, 0L, updateDelay*20L);
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

    public static Map<User, LocalDateTime> getVoiceTime() {
        return voiceTime;
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
}
