package md.mirrerror.discordutils.events;

import md.mirrerror.discordutils.Main;
import md.mirrerror.discordutils.config.Message;
import md.mirrerror.discordutils.discord.BotController;
import md.mirrerror.discordutils.discord.DiscordUtils;
import md.mirrerror.discordutils.discord.EmbedManager;
import md.mirrerror.discordutils.discord.TwoFactorSession;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Events implements Listener {

    private final List<String> allowedCommands = new ArrayList<>();

    public Events() {
        allowedCommands.addAll(Main.getInstance().getConfigManager().getConfig().getStringList("Discord.AllowedCommandsBeforePassing2FA"));
        Iterator<String> stringIterator = allowedCommands.iterator();
        int index = 0;
        while(stringIterator.hasNext()) {
            String command = stringIterator.next();
            if(command.startsWith("/")) command = command.substring(1);
            allowedCommands.set(index, command);
            index += 1;
        }
        allowedCommands.add("discordutils link");
        allowedCommands.add("disutils link");
        allowedCommands.add("du link");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> DiscordUtils.checkRoles(player));
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> DiscordUtils.checkNames(player));
        if(DiscordUtils.hasTwoFactor(player)) {
            String playerIp = StringUtils.remove(player.getAddress().getAddress().toString(), '/');

            if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.2FASessions"))
                if(BotController.getSessions().containsKey(player.getUniqueId())) {
                    if(Main.getInstance().getConfigManager().getConfig().getLong("Discord.2FASessionTime") > 0) {

                        if(BotController.getSessions().get(player.getUniqueId()).getEnd().isAfter(LocalDateTime.now()))
                            if(BotController.getSessions().get(player.getUniqueId()).getIpAddress().equals(playerIp)) return;

                    } else if(BotController.getSessions().get(player.getUniqueId()).getIpAddress().equals(playerIp)) return;
                }

            EmbedManager embedManager = new EmbedManager();
            if(Main.getTwoFactorType() == Main.TwoFactorType.REACTION) {
                DiscordUtils.getDiscordUser(player).openPrivateChannel().submit()
                .thenCompose(channel -> channel.sendMessageEmbeds(embedManager.infoEmbed(Message.TWOFACTOR_REACTION_MESSAGE.getText().replace("%playerIp%", playerIp))).submit())
                .whenComplete((msg, error) -> {
                    if (error == null) {
                        msg.addReaction("✅").queue();
                        msg.addReaction("❎").queue();
                        BotController.getTwoFactorPlayers().put(player, msg.getId());
                        return;
                    }
                    Message.CAN_NOT_SEND_MESSAGE.getFormattedText(true).forEach(player::sendMessage);
                });
            }
            if(Main.getTwoFactorType() == Main.TwoFactorType.CODE) {
                String code = "";
                byte[] secureRandomSeed = new SecureRandom().generateSeed(20);
                for(byte b : secureRandomSeed) code += b;
                code = code.replace("-", "");

                final String FINAL_CODE = code;
                DiscordUtils.getDiscordUser(player).openPrivateChannel().submit()
                .thenCompose(channel -> channel.sendMessageEmbeds(embedManager.infoEmbed(Message.TWOFACTOR_CODE_MESSAGE.getText().replace("%code%", FINAL_CODE).replace("%playerIp%", playerIp))).submit())
                .whenComplete((msg, error) -> {
                    if (error == null) {
                        BotController.getTwoFactorPlayers().put(player, FINAL_CODE);
                        return;
                    }
                    Message.CAN_NOT_SEND_MESSAGE.getFormattedText(true).forEach(player::sendMessage);
                });
            }

            long timeToAuthorize = Main.getInstance().getConfigManager().getConfig().getLong("Discord.2FATimeToAuthorize");

            if(timeToAuthorize > 0) Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if(player != null) {
                    if(BotController.getTwoFactorPlayers().containsKey(player)) player.kickPlayer(Message.TWOFACTOR_TIME_TO_AUTHORIZE_HAS_EXPIRED.getText());
                }
            }, timeToAuthorize*20L);
        }

        if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.NotifyAboutDisabled2FA")) {
            if(!DiscordUtils.hasTwoFactor(player)) Message.TWOFACTOR_DISABLED_REMINDER.getFormattedText(true).forEach(player::sendMessage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> DiscordUtils.checkRoles(player));
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> DiscordUtils.checkNames(player));
        BotController.getTwoFactorPlayers().remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDrop(PlayerDropItemEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if(!allowedCommands.contains(event.getMessage().substring(1))) {
            checkTwoFactor(event.getPlayer(), event);
            checkVerification(event.getPlayer(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(Main.getTwoFactorType() == Main.TwoFactorType.CODE && BotController.getTwoFactorPlayers().containsKey(player)) {
            event.setCancelled(true);
            String message = event.getMessage();
            String playerIp = StringUtils.remove(player.getAddress().getAddress().toString(), '/');
            if(message.replace(" ", "").equals(BotController.getTwoFactorPlayers().get(player))) {
                BotController.getTwoFactorPlayers().remove(player);
                Message.TWOFACTOR_AUTHORIZED.getFormattedText(true).forEach(player::sendMessage);
                BotController.getSessions().put(player.getUniqueId(), new TwoFactorSession(playerIp,
                        LocalDateTime.now().plusSeconds(Main.getInstance().getConfigManager().getConfig().getLong("Discord.2FASessionTime"))));
            } else {
                int attempts = 1;
                if(BotController.getTwoFactorAttempts().containsKey(playerIp)) {
                    attempts = BotController.getTwoFactorAttempts().get(playerIp)+1;
                    BotController.getTwoFactorAttempts().put(playerIp, attempts);
                } else {
                    BotController.getTwoFactorAttempts().put(playerIp, attempts);
                }
                if(Main.getInstance().getConfigManager().getConfig().getConfigurationSection("Discord.ActionsAfterFailing2FA." + attempts) != null) {
                    List<String> messages = Main.getInstance().getConfigManager().getConfig().getStringList("Discord.ActionsAfterFailing2FA." + attempts + ".Messages");
                    List<String> commands = Main.getInstance().getConfigManager().getConfig().getStringList("Discord.ActionsAfterFailing2FA." + attempts + ".Commands");
                    if(messages != null) {
                        messages.forEach(msg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%player%", player.getName()))));
                    }
                    if(commands != null) {
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            commands.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName())));
                        });
                    }
                }
            }
        } else {
            checkTwoFactor(event.getPlayer(), event);
            checkVerification(event.getPlayer(), event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        checkTwoFactor((Player) event.getEntity(), event);
        checkVerification((Player) event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onConsume(PlayerItemConsumeEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakItem(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            player.getInventory().addItem(event.getBrokenItem());
            Message.TWOFACTOR_NEEDED.getFormattedText(true).forEach(player::sendMessage);
        }
        if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.ForceVerification") && !DiscordUtils.isVerified(player)) {
            player.getInventory().addItem(event.getBrokenItem());
            Message.VERIFICATION_NEEDED.getFormattedText(true).forEach(player::sendMessage);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageItem(PlayerItemDamageEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHeldItem(PlayerItemHeldEvent event) {
        checkTwoFactor(event.getPlayer(), event);
        checkVerification(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        checkTwoFactor((Player) event.getWhoClicked(), event);
        checkVerification((Player) event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpenInventory(InventoryOpenEvent event) {
        checkTwoFactor((Player) event.getPlayer(), event);
        checkVerification((Player) event.getPlayer(), event);
    }

    private void checkTwoFactor(Player player, Cancellable event) {
        if(BotController.getTwoFactorPlayers().containsKey(player)) {
            event.setCancelled(true);
            Message.TWOFACTOR_NEEDED.getFormattedText(true).forEach(player::sendMessage);
        }
    }

    private void checkVerification(Player player, Cancellable event) {
        if(Main.getInstance().getConfigManager().getConfig().getBoolean("Discord.ForceVerification")) {
            if(!DiscordUtils.isVerified(player)) {
                event.setCancelled(true);
                Message.VERIFICATION_NEEDED.getFormattedText(true).forEach(player::sendMessage);
            }
        }
    }

}
