package md.mirrerror.discordutils.discord;

import md.mirrerror.discordutils.config.Message;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedManager {
    private final EmbedBuilder embedBuilder = new EmbedBuilder();
    private final String FOOTER = Message.EMBED_FOOTER.getText();

    public MessageEmbed errorEmbed(String text) {
        embedBuilder.setTitle(Message.ERROR.getText());
        embedBuilder.setColor(Color.RED);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter(FOOTER);
        return embedBuilder.build();
    }

    public MessageEmbed successfulEmbed(String text) {
        embedBuilder.setTitle(Message.SUCCESSFULLY.getText());
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter(FOOTER);
        return embedBuilder.build();
    }

    public MessageEmbed infoEmbed(String text) {
        embedBuilder.setTitle(Message.INFORMATION.getText());
        embedBuilder.setColor(Color.YELLOW);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter(FOOTER);
        return embedBuilder.build();
    }

    public MessageEmbed embed(String title, String text, Color color) {
        embedBuilder.setTitle(title);
        embedBuilder.setColor(color);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter(FOOTER);
        return embedBuilder.build();
    }

    public MessageEmbed embed(String title, String text, Color color, String footer) {
        embedBuilder.setTitle(title);
        embedBuilder.setColor(color);
        embedBuilder.setDescription(text);
        embedBuilder.setFooter(footer + " / " + FOOTER);
        return embedBuilder.build();
    }
}
