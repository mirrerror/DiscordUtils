package md.mirrerror.discordutils;

import md.mirrerror.discordutils.commands.CommandManager;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.commands.discordutils.*;
import md.mirrerror.discordutils.commands.discordutilsadmin.ForceUnlink;
import md.mirrerror.discordutils.commands.discordutilsadmin.Reload;
import md.mirrerror.discordutils.config.ConfigManager;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.database.MySQLManager;
import md.mirrerror.discordutils.discord.ActivityManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.events.Events;
import md.mirrerror.discordutils.events.ServerActivityLoggerHandler;
import md.mirrerror.discordutils.utils.integrations.permissions.LuckPermsIntegration;
import md.mirrerror.discordutils.utils.integrations.permissions.PermissionsIntegration;
import md.mirrerror.discordutils.utils.integrations.permissions.VaultIntegration;
import md.mirrerror.discordutils.utils.integrations.placeholders.PAPIManager;
import md.mirrerror.discordutils.metrics.Metrics;
import md.mirrerror.discordutils.utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Main extends JavaPlugin {

    private static Main instance;

    private ConfigManager configManager;
    private ActivityManager activityManager;
    private PAPIManager papiManager;
    private static PermissionsPlugin permissionsPlugin;
    private static TwoFactorType twoFactorType;
    private static DatabaseType databaseType;
    private static final int PLUGIN_ID = 13243; // metrics

    public enum PermissionsPlugin {
        NONE, LUCK_PERMS, VAULT;

        public PermissionsIntegration getPermissionsIntegration() {
            switch (this) {
                case LUCK_PERMS: return new LuckPermsIntegration();
                case VAULT: return new VaultIntegration();
                default: return null;
            }
        }
    }

    public enum DatabaseType {
        NONE, MYSQL;

        public DatabaseManager getDatabaseManager() {
            switch (this) {
                case MYSQL: return new MySQLManager();
                default: return null;
            }
        }
    }

    public enum TwoFactorType {
        CODE, REACTION
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        configManager = new ConfigManager();
        getLogger().info("Configuration files have been successfully loaded.");
        papiManager = new PAPIManager();
        activityManager = new ActivityManager();
        getLogger().info("ActivityManager has been successfully enabled.");
        if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("AsyncBotLoading")) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> BotController.setupBot(configManager.getBotSettings().getString("BotToken")));
        } else {
            BotController.setupBot(configManager.getBotSettings().getString("BotToken"));
        }
        checkOutForPermissionsPlugin();
        if(permissionsPlugin != PermissionsPlugin.NONE) getLogger().info("Successfully integrated with " + permissionsPlugin.name() + ".");
        else getLogger().info("You chose no permission plugin or it is not supported. Disabling this feature...");
        registerCommands();
        getLogger().info("Commands have been successfully loaded.");
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("ServerActivityLogging.Enabled")) {
            Bukkit.getPluginManager().registerEvents(new ServerActivityLoggerHandler(), this);
        }
        getLogger().info("Events have been successfully loaded.");
        setupTwoFactorType();
        getLogger().info("2FA has been successfully loaded.");
        setupDatabaseType();
        getLogger().info("DatabaseManager has been successfully loaded.");
        setupMetrics();
        getLogger().info("Metrics has been successfully loaded.");
        if(configManager.getConfig().getBoolean("CheckForUpdates")) {
            getLogger().info("Checking for updates...");
            UpdateChecker.checkForUpdates();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        BotController.getJda().shutdownNow();
    }

    private void checkOutForPermissionsPlugin() {
        switch(configManager.getConfig().getString("PermissionsPlugin").toUpperCase()) {
            case "LUCKPERMS": {
                permissionsPlugin = PermissionsPlugin.LUCK_PERMS;
                break;
            }
            case "VAULT": {
                permissionsPlugin = PermissionsPlugin.VAULT;
                break;
            }
            default: {
                permissionsPlugin = PermissionsPlugin.NONE;
                break;
            }
        }
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, PLUGIN_ID);
        metrics.addCustomChart(new Metrics.MultiLineChart("players_and_servers", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            valueMap.put("servers", 1);
            valueMap.put("players", Bukkit.getOnlinePlayers().size());
            return valueMap;
        }));
    }

    private void setupTwoFactorType() {
        String type = configManager.getBotSettings().getString("2FAType").toUpperCase();
        switch (type) {
            case "CODE":
            case "REACTION": {
                twoFactorType = TwoFactorType.valueOf(type);
                break;
            }
            default: {
                twoFactorType = TwoFactorType.REACTION;
                break;
            }
        }
    }

    private void setupDatabaseType() {
        String type = configManager.getConfig().getString("Database.Type").toUpperCase();
        switch (type) {
            case "MYSQL": {
                databaseType = DatabaseType.valueOf(type);
                break;
            }
            default: {
                databaseType = DatabaseType.NONE;
                break;
            }
        }
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager();

        List<SubCommand> discordUtilsSubCommands = new ArrayList<>();
        discordUtilsSubCommands.add(new Link());
        discordUtilsSubCommands.add(new TwoFactor());
        discordUtilsSubCommands.add(new Help());
        discordUtilsSubCommands.add(new SendToDiscord());
        discordUtilsSubCommands.add(new VoiceInvite());
        discordUtilsSubCommands.add(new Unlink());
        discordUtilsSubCommands.add(new GetDiscord());
        commandManager.registerCommand("discordutils", discordUtilsSubCommands);

        List<SubCommand> discordUtilsAdminSubCommands = new ArrayList<>();
        discordUtilsAdminSubCommands.add(new Reload());
        discordUtilsAdminSubCommands.add(new ForceUnlink());
        commandManager.registerCommand("discordutilsadmin", discordUtilsAdminSubCommands);
    }

    public static Main getInstance() {
        return instance;
    }

    public static PermissionsPlugin getPermissionsPlugin() {
        return permissionsPlugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ActivityManager getActivityManager() {
        return activityManager;
    }

    public PAPIManager getPapiManager() {
        return papiManager;
    }

    public static TwoFactorType getTwoFactorType() {
        return twoFactorType;
    }

    public static DatabaseType getDatabaseType() {
        return databaseType;
    }
}
