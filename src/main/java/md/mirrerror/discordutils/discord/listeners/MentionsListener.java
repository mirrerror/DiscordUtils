package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MentionsListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot() || event.isWebhookMessage()) return;

        for(Member member : event.getMessage().getMentionedMembers()) {
            User user = member.getUser();
            if(!DiscordUtils.isVerified(user)) continue;

            Player player = DiscordUtils.getPlayer(user);
            if(player == null) continue;

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.NotifyAboutMentions.Title.Enabled")) {
                int fadeIn = Main.getInstance().getConfigManager().getConfig().getInt("Discord.NotifyAboutMentions.Title.FadeIn");
                int stay = Main.getInstance().getConfigManager().getConfig().getInt("Discord.NotifyAboutMentions.Title.Stay");
                int fadeOut = Main.getInstance().getConfigManager().getConfig().getInt("Discord.NotifyAboutMentions.Title.FadeOut");
                String title = ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getConfig().getString("Discord.NotifyAboutMentions.Title.Title"));
                String subtitle = ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getConfig().getString("Discord.NotifyAboutMentions.Title.Subtitle"));

                player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            }

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.NotifyAboutMentions.Message.Enabled")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfigManager().getConfig().getString("Discord.NotifyAboutMentions.Message.Text")));
            }

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.NotifyAboutMentions.Sound.Enabled")) {
                String soundType = Main.getInstance().getConfigManager().getConfig().getString("Discord.NotifyAboutMentions.Sound.Type");
                float volume = (float) Main.getInstance().getConfigManager().getConfig().getDouble("Discord.NotifyAboutMentions.Sound.Volume");
                float pitch = (float) Main.getInstance().getConfigManager().getConfig().getDouble("Discord.NotifyAboutMentions.Sound.Pitch");
                player.playSound(player.getLocation(), Sound.valueOf(soundType.toUpperCase()), volume, pitch);
            }

        }
    }

}
