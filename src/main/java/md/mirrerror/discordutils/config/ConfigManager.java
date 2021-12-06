package md.mirrerror.discordutils.config;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.BotController;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private File configFile;
    private FileConfiguration config;
    private File dataFile;
    private FileConfiguration dataConfig;
    private File langFile;
    private FileConfiguration langConfig;

    public ConfigManager() {
        initializeConfigFiles();
    }

    public void initializeConfigFiles() {
        configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
        if(!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Main.getInstance().saveResource(configFile.getName(), false);
            Main.getInstance().getLogger().info(Message.PREFIX.getText() + "Config file '" + configFile.getName() + "' has been successfully created.");
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
        }
    }

    public void saveConfigFiles() {
        try {
            config.save(configFile);
            dataConfig.save(dataFile);
            langConfig.save(langFile);
            Main.getInstance().getLogger().info("Successfully saved the config files.");
        } catch (IOException e) {
            Main.getInstance().getLogger().severe("Something went wrong while saving the config files!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    public void reloadConfigFiles() {
        config = new YamlConfiguration();
        dataConfig = new YamlConfiguration();
        langConfig = new YamlConfiguration();
        try {
            config.load(configFile);
            dataConfig.load(dataFile);
            langConfig.load(langFile);
            BotController.setupAdminRoles();
            BotController.setupGroupRoles();
            Main.getInstance().getLogger().info("Successfully reloaded the config files.");
        } catch (IOException | InvalidConfigurationException e) {
            Main.getInstance().getLogger().severe("Something went wrong while loading the config files!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getData() {
        return dataConfig;
    }

    public FileConfiguration getLang() {
        return langConfig;
    }
}
