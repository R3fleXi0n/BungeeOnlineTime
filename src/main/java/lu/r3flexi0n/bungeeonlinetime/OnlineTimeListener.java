package lu.r3flexi0n.bungeeonlinetime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnlineTimeListener implements Listener {

    public static final Map<UUID, OnlineTime> ONLINE_TIMES = new HashMap<>();

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        if (player.hasPermission("onlinetime.save")) {
            ONLINE_TIMES.put(player.getUniqueId(), new OnlineTime());
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();
        OnlineTime time = ONLINE_TIMES.get(player.getUniqueId());
        if (time == null) {
            return;
        }
        ServerInfo server = player.getServer().getInfo();

        if (BungeeOnlineTime.disabledServers.contains(server.getName())) {
            time.joinAFK();
        } else {
            time.leaveAFK();
        }

    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        OnlineTime time = ONLINE_TIMES.get(uuid);
        ONLINE_TIMES.remove(uuid);

        if (!player.hasPermission("onlinetime.save")) {
            return;
        }

        long joinTime = time.getJoinTime();
        long leaveTime = System.currentTimeMillis();
        long afkTime = time.getAFK();

        if (leaveTime - joinTime - afkTime < 5000) {
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
            try {
                BungeeOnlineTime.sql.addOnlineTime(uuid, joinTime, leaveTime, afkTime);
            } catch (Exception ex) {
                player.sendMessage(BungeeOnlineTime.errorSaving);
            }
        });
    }
}
