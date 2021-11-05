package md.mirrerror.discordutils.integrations.permissions;

import md.mirrerror.discordutils.Main;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class VaultIntegration implements PermissionsIntegration {

    private static Permission perms = null;

    public VaultIntegration() {
        if(!setupPermissions()) {
            Main.getInstance().getLogger().severe("Vault plugin or any permissions plugin not found.");
        }
    }

    @Override
    public List<String> getUserGroups(Player player) {
        return new ArrayList<>(Arrays.asList(perms.getPlayerGroups(player)));
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
