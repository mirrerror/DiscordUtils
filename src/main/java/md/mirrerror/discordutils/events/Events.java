package md.mirrerror.discordutils.events;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import org.apache.commons.lang.StringUtils;
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

import java.security.SecureRandom;

public class Events implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DiscordUtils.checkRoles(player);
        if(DiscordUtils.hasTwoFactor(player)) {
            String playerIp = StringUtils.remove(player.getAddress().getAddress().toString(), '/');

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.2FASessions"))
                if(BotController.getSessions().containsKey(player.getUniqueId())) if(BotController.getSessions().get(player.getUniqueId()).equals(playerIp)) return;

            EmbedManager embedManager = new EmbedManager();
            if(Main.getTwoFactorType() == Main.TwoFactorType.REACTION) {
                DiscordUtils.getDiscordUser(player).openPrivateChannel().complete().sendMessageEmbeds(embedManager.infoEmbed(Message.TWOFACTOR_REACTION_MESSAGE.getText().replace("%playerIp%", playerIp))).queue(message -> {
                    message.addReaction("✅").queue();
                    message.addReaction("❎").queue();
                });
                BotController.getTwoFactorPlayers().put(player, "reaction");
            }
            if(Main.getTwoFactorType() == Main.TwoFactorType.CODE) {
                String code = "";
                byte[] secureRandomSeed = new SecureRandom().generateSeed(20);
                for(byte b : secureRandomSeed) code += b;
                code = code.replace("-", "");
                DiscordUtils.getDiscordUser(player).openPrivateChannel().complete().sendMessageEmbeds(embedManager.infoEmbed(Message.TWOFACTOR_CODE_MESSAGE.getText().replace("%code%", code).replace("%playerIp%", playerIp))).queue();
                BotController.getTwoFactorPlayers().put(player, code);
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        DiscordUtils.checkRoles(player);
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
            if(message.replace(" ", "").equals(BotController.getTwoFactorPlayers().get(player))) {
                BotController.getTwoFactorPlayers().remove(player);
                player.sendMessage(Message.TWOFACTOR_AUTHORIZED.getText(true));
                BotController.getSessions().put(player.getUniqueId(), StringUtils.remove(player.getAddress().getAddress().toString(), '/'));
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

    private void checkTwoFactor(Player player, Cancellable event) {
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            event.setCancelled(true);
            player.sendMessage(Message.TWOFACTOR_NEEDED.getText(true));
        }
    }

}
