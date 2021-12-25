package md.mirrerror.discordutils;

import md.mirrerror.discordutils.commands.CommandManager;
import md.mirrerror.discordutils.commands.SubCommand;
import md.mirrerror.discordutils.commands.discordutils.*;
import md.mirrerror.discordutils.config.ConfigManager;
import md.mirrerror.discordutils.database.DatabaseManager;
import md.mirrerror.discordutils.database.MySQLManager;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.events.Events;
import md.mirrerror.discordutils.integrations.permissions.LuckPermsIntegration;
import md.mirrerror.discordutils.integrations.permissions.PermissionsIntegration;
import md.mirrerror.discordutils.integrations.permissions.VaultIntegration;
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
        getLogger().info("Configuration files successfully loaded.");
        BotController.setupBot(configManager.getConfig().getString("Discord.BotToken"));
        checkOutForPermissionsPlugin();
        if(permissionsPlugin != PermissionsPlugin.NONE) getLogger().info("Successfully integrated with " + permissionsPlugin.name() + ".");
        else getLogger().info("You chose no permission plugin or it is not supported. Disabling this feature.");
        registerCommands();
        getLogger().info("Commands successfully loaded.");
        Bukkit.getPluginManager().registerEvents(new Events(), this);
        getLogger().info("Events successfully loaded.");
        setupTwoFactorType();
        getLogger().info("2FA successfully loaded.");
        setupDatabaseType();
        getLogger().info("DatabaseManager successfully loaded.");
        setupMetrics();
        getLogger().info("Metrics successfully loaded.");
        if(configManager.getConfig().getBoolean("CheckForUpdates")) {
            getLogger().info("Checking for updates...");
            UpdateChecker.checkForUpdates();
            //UpdateChecker.downloadUpdate();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //Bukkit.getScheduler().getPendingTasks().forEach(BukkitTask::cancel);
        //BotController.getJda().shutdownNow();
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
        String type = configManager.getConfig().getString("Discord.2FAType").toUpperCase();
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
        List<SubCommand> subCommands = new ArrayList<>();
        subCommands.add(new Link());
        subCommands.add(new Reload());
        subCommands.add(new TwoFactor());
        subCommands.add(new Help());
        subCommands.add(new SendToDiscord());
        subCommands.add(new VoiceInvite());
        subCommands.add(new Unlink());
        commandManager.registerCommand("discordutils", subCommands);
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

    public static TwoFactorType getTwoFactorType() {
        return twoFactorType;
    }

    public static DatabaseType getDatabaseType() {
        return databaseType;
    }
}
