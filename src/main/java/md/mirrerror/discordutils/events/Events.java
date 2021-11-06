package md.mirrerror.discordutils.events;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import md.mirrerror.discordutils.integrations.permissions.PermissionsIntegration;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkRoles(player);
        if(DiscordUtils.hasTwoFactor(player)) {
            String code = "" + (ThreadLocalRandom.current().nextLong(899999999)+100000000); // TODO: replace
            EmbedManager embedManager = new EmbedManager();
            DiscordUtils.getDiscordUser(player).openPrivateChannel().complete().sendMessageEmbeds(embedManager.infoEmbed(Message.TWOFACTOR_CODE_MESSAGE.getText().replaceAll("%code%", code))).queue();
            BotController.getTwoFactorPlayers().put(player, code);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        checkRoles(player);
        BotController.getTwoFactorPlayers().remove(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            if(message.replaceAll(" ", "").equals(BotController.getTwoFactorPlayers().get(player))) {
                BotController.getTwoFactorPlayers().remove(player);
                player.sendMessage(Message.TWOFACTOR_AUTHORIZED.getText(true));
            } else {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.kickPlayer(Message.INVALID_TWOFACTOR_CODE.getText()));
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        checkTwoFactor((Player) event.getEntity(), event);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onBreakItem(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            player.getInventory().addItem(event.getBrokenItem());
            player.sendMessage(Message.TWOFACTOR_NEEDED.getText(true));
        }
    }

    @EventHandler
    public void onDamageItem(PlayerItemDamageEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onHeldItem(PlayerItemHeldEvent event) {
        checkTwoFactor(event.getPlayer(), event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        checkTwoFactor((Player) event.getWhoClicked(), event);
    }

    private void checkRoles(Player player) {
        PermissionsIntegration permissionsIntegration = Main.getPermissionsPlugin().getPermissionsIntegration();
        if(permissionsIntegration == null) return;
        List<String> groups = permissionsIntegration.getUserGroups(player);
        Map<Long, String> groupRoles = BotController.getGroupRoles();
        for(String s : groups) {
            if(groupRoles.containsValue(s)) {
                groupRoles.forEach((groupId, group) -> { if(group.equals(s)) {
                    Role role = BotController.getJda().getRoleById(groupId);
                    BotController.getJda().getGuilds().forEach(guild -> {
                        if(DiscordUtils.isVerified(player)) {
                            User user = DiscordUtils.getDiscordUser(player);
                            if(guild.retrieveMember(user).complete() != null) {
                                if(role != null) guild.addRoleToMember(guild.retrieveMember(user).complete(), role).queue();
                            }
                        }
                    });
                }});
            }
        }
    }

    private void checkTwoFactor(Player player, Cancellable event) {
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            event.setCancelled(true);
            player.sendMessage(Message.TWOFACTOR_NEEDED.getText(true));
        }
    }

}
