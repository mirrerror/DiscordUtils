package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedManager {
    public MessageEmbed errorEmbed(String text) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(Message.ERROR.getText());
        embedBuilder.setColor(Color.RED);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter("Bot by mirrerror#4790");
        return embedBuilder.build();
    }

    public MessageEmbed successfulEmbed(String text) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(Message.SUCCESSFULLY.getText());
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter("Bot by mirrerror#4790");
        return embedBuilder.build();
    }

    public MessageEmbed infoEmbed(String text) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(Message.INFORMATION.getText());
        embedBuilder.setColor(Color.YELLOW);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter("Bot by mirrerror#4790");
        return embedBuilder.build();
    }

    public MessageEmbed embed(String title, String text, Color color) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setColor(color);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter("Bot by mirrerror#4790");
        return embedBuilder.build();
    }
}
