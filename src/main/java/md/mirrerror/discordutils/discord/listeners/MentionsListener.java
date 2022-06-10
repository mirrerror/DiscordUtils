package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MentionsListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.isWebhookMessage()) return;
        if(!event.isFromGuild()) return;

        for(Member member : event.getMessage().getMentions().getMembers()) {
            User user = member.getUser();
            if(!DiscordUtils.isVerified(user)) continue;

            Player player = DiscordUtils.getPlayer(user);
            if(player == null) continue;

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("NotifyAboutMentions.Title.Enabled")) {
                int fadeIn = Main.getInstance().getConfigManager().getBotSettings().getInt("NotifyAboutMentions.Title.FadeIn");
                int stay = Main.getInstance().getConfigManager().getBotSettings().getInt("NotifyAboutMentions.Title.Stay");
                int fadeOut = Main.getInstance().getConfigManager().getBotSettings().getInt("NotifyAboutMentions.Title.FadeOut");
                String title = ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getBotSettings().getString("NotifyAboutMentions.Title.Title"));
                String subtitle = ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getBotSettings().getString("NotifyAboutMentions.Title.Subtitle"));

                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("NotifyAboutMentions.Message.Enabled")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getBotSettings().getString("NotifyAboutMentions.Message.Text")));
            }

            if(Main.getInstance().getConfigManager().getBotSettings().getBoolean("NotifyAboutMentions.Sound.Enabled")) {
                String soundType = Main.getInstance().getConfigManager().getBotSettings().getString("NotifyAboutMentions.Sound.Type");
                float volume = (float) Main.getInstance().getConfigManager().getBotSettings().getDouble("NotifyAboutMentions.Sound.Volume");
                float pitch = (float) Main.getInstance().getConfigManager().getBotSettings().getDouble("NotifyAboutMentions.Sound.Pitch");
                player.playSound(player.getLocation(), Sound.valueOf(soundType.toUpperCase()), volume, pitch);
            }

        }
    }

}
