package lu.r3flexi0n.bungeeonlinetime;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnlineTimeListener implements Listener {

    public static final HashMap<UUID, Long> joinTimes = new HashMap<>();

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        joinTimes.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        long joinTime = joinTimes.get(uuid);
        long leaveTime = System.currentTimeMillis();

        joinTimes.remove(uuid);

        if (leaveTime - joinTime < 5000) {
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
            try {
                BungeeOnlineTime.mysql.addOnlineTime(uuid, joinTime, leaveTime);
            } catch (SQLException | ClassNotFoundException ex) {
                player.sendMessage(BungeeOnlineTime.errorSaving);
            }
        });
    }
}
