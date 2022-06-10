package md.mirrerror.discordutils.config.customconfigs;

import org.bukkit.configuration.ConfigurationSection;

public class DataConfig extends CustomConfig {
    public DataConfig(String fileName) {
        super(fileName);
    }

    @Override
    public void initializeFields() {
        getFileConfiguration().addDefault("DiscordLink", new ConfigurationSection[0]);
        getFileConfiguration().options().copyDefaults(true);
        getFileConfiguration().options().copyHeader(true);
        saveConfigFile();
    }
}
