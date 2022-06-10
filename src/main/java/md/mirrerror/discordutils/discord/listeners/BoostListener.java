package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public class BoostListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdateBoostTime(@NotNull GuildMemberUpdateBoostTimeEvent event) {
        OffsetDateTime newTime = event.getNewTimeBoosted();
        OffsetDateTime oldTime = event.getOldTimeBoosted();
        if(newTime == null || oldTime == null) {
            Main.getInstance().getLogger().severe("An error occurred while handling the GuildMemberUpdateBoostTimeEvent! Please, contact the developer!");
            return;
        }

        if(newTime.isAfter(oldTime)) {
            Member member = event.getEntity();
            User user = member.getUser();
            if(DiscordUtils.isVerified(user)) return;

            OfflinePlayer offlinePlayer = DiscordUtils.getOfflinePlayer(user);
            Main.getInstance().getConfigManager().getBotSettings().getStringList("CommandsAfterServerBoosting").forEach(command -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", offlinePlayer.getName()).replace("%user%", user.getAsTag()));
            });

        }
    }

}
