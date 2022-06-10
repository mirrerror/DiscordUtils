package md.mirrerror.discordutils.config;

import md.mirrerror.discordutils.config.customconfigs.*;
import md.mirrerror.discordutils.discord.BotController;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    public ConfigManager() {
        initializeConfigFiles();
    }

    private CustomConfig config;
    private CustomConfig botSettings;
    private CustomConfig data;
    private CustomConfig lang;

    public void initializeConfigFiles() {
    /*    configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Main.getInstance().saveResource(configFile.getName(), false);
            Main.getInstance().getLogger().info("Config file '" + configFile.getName() + "' has been successfully created.");
        }
        dataFile = new File(Main.getInstance().getDataFolder(), "data.yml");
        if(!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            Main.getInstance().saveResource(dataFile.getName(), false);
            Main.getInstance().getLogger().info("Config file '" + dataFile.getName() + "' has been successfully created.");
        }
        langFile = new File(Main.getInstance().getDataFolder(), "lang.yml");
        if(!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            Main.getInstance().saveResource(langFile.getName(), false);
            Main.getInstance().getLogger().info("Config file '" + langFile.getName() + "' has been successfully created.");
        }

        config = new YamlConfiguration();
        dataConfig = new YamlConfiguration();
        langConfig = new YamlConfiguration();
        try {
            config.load(configFile);
            dataConfig.load(dataFile);
            langConfig.load(langFile);
        } catch (IOException | InvalidConfigurationException e) {
            Main.getInstance().getLogger().severe("Something went wrong while initializing the config files!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }*/

        config = new MainConfig("config.yml");
        botSettings = new BotSettingsConfig("bot_settings.yml");
        data = new DataConfig("data.yml");
        lang = new LangConfig("lang.yml");
    }

    public void saveConfigFiles() {
        config.saveConfigFile();
        botSettings.saveConfigFile();
        data.saveConfigFile();
        lang.saveConfigFile();
    }

    public void reloadConfigFiles() {
        config.reloadConfigFile();
        botSettings.reloadConfigFile();
        data.reloadConfigFile();
        lang.reloadConfigFile();
        BotController.setupAdminRoles();
        BotController.setupGroupRoles();
    }

    public FileConfiguration getConfig() {
        return config.getFileConfiguration();
    }

    public FileConfiguration getBotSettings() {
        return botSettings.getFileConfiguration();
    }

    public FileConfiguration getData() {
        return data.getFileConfiguration();
    }

    public FileConfiguration getLang() {
        return lang.getFileConfiguration();
    }

}
