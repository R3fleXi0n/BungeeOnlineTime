package lu.r3flexi0n.bungeeonlinetime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnlineTimeListener implements Listener {

    public static final Map<UUID, Long> JOIN_TIMES = new HashMap<>();

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if (player.hasPermission("onlinetime.save")) {
            JOIN_TIMES.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!player.hasPermission("onlinetime.save")) {
            return;
        }

        long joinTime = JOIN_TIMES.get(uuid);
        long leaveTime = System.currentTimeMillis();

        JOIN_TIMES.remove(uuid);

        if (leaveTime - joinTime < 5000) {
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
            try {
                BungeeOnlineTime.sql.addOnlineTime(uuid, joinTime, leaveTime);
            } catch (Exception ex) {
                player.sendMessage(BungeeOnlineTime.errorSaving);
            }
        });
    }
}
