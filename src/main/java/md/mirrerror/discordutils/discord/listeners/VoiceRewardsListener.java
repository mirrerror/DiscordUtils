package md.mirrerror.discordutils.discord.listeners;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class VoiceRewardsListener extends ListenerAdapter {

    private Map<Long, BukkitTask> rewardTimers = new HashMap<>();
    private static final Map<Long, Long> voiceTime = new HashMap<>();

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if(!Main.getInstance().getConfigManager().getBotSettings().getBoolean("GuildVoiceRewards.Enabled")) return;
        if(BotController.getRewardBlacklistedVoiceChannels().contains(event.getChannelJoined().getIdLong())) return;

        Member member = event.getMember();

        if(!DiscordUtils.isVerified(member.getUser())) return;

        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            GuildVoiceState voiceState = member.getVoiceState();
            if(voiceState == null) return;
            if(voiceState.isSelfDeafened() || voiceState.isSelfMuted()) return;
            if(event.getChannelJoined() == null) return;
            if(event.getChannelJoined().getMembers().size() < Main.getInstance().getConfigManager().getBotSettings().getInt("GuildVoiceRewards.MinMembers")) return;

            long id = member.getIdLong();

            if(voiceTime.containsKey(id)) voiceTime.put(id, voiceTime.get(id)+1L);
            else voiceTime.put(id, 1L);

            long minTime = Main.getInstance().getConfigManager().getBotSettings().getLong("GuildVoiceRewards.Time");
            long time = voiceTime.get(id);

            if(time >= minTime) {
                String command = Main.getInstance().getConfigManager().getBotSettings().getString("GuildVoiceRewards.Reward")
                        .replace("%player%", DiscordUtils.getOfflinePlayer(member.getUser()).getName());
                Bukkit.getScheduler().callSyncMethod(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                voiceTime.put(id, 0L);
            }
        }, 0L, 20L);

        rewardTimers.put(member.getIdLong(), bukkitTask);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(!Main.getInstance().getConfigManager().getBotSettings().getBoolean("GuildVoiceRewards.Enabled")) return;
        if(BotController.getRewardBlacklistedVoiceChannels().contains(event.getChannelLeft().getIdLong())) return;

        Member member = event.getMember();
        long memberId = member.getIdLong();

        if(!DiscordUtils.isVerified(member.getUser())) return;
        if(!voiceTime.containsKey(memberId)) return;

        if(rewardTimers.containsKey(memberId)) rewardTimers.get(memberId).cancel();
        rewardTimers.remove(memberId);
        voiceTime.remove(memberId);
    }

}
