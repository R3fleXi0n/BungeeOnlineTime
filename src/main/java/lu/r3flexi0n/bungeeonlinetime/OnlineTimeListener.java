package lu.r3flexi0n.bungeeonlinetime;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.UUID;
import lu.r3flexi0n.bungeeonlinetime.utils.Language;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnlineTimeListener implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        if (!player.hasPermission("onlinetime.save")) {
            return;
        }

        BungeeOnlineTime.ONLINE_PLAYERS.put(uuid, new OnlinePlayer());

        if (!BungeeOnlineTime.MYSQL_ENABLED) {
            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {
                    BungeeOnlineTime.SQL.addOnlineTimeSQLite(uuid, name);
                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR_SAVING);
                    ex.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();

        OnlinePlayer onlinePlayer = BungeeOnlineTime.ONLINE_PLAYERS.get(player.getUniqueId());
        if (onlinePlayer == null) {
            return;
        }

        ServerInfo server = player.getServer().getInfo();
        if (BungeeOnlineTime.DISABLED_SERVERS.contains(server.getName())) {
            onlinePlayer.joinAFK();
        } else {
            onlinePlayer.leaveAFK();
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        OnlinePlayer onlinePlayer = BungeeOnlineTime.ONLINE_PLAYERS.get(uuid);
        if (onlinePlayer == null) {
            return;
        }
        BungeeOnlineTime.ONLINE_PLAYERS.remove(uuid);

        onlinePlayer.leaveAFK();

        long time = onlinePlayer.getNoAFKTime();
        if (time < 5000) {
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
            try {
                BungeeOnlineTime.SQL.updateOnlineTime(uuid, name, time);
            } catch (Exception ex) {
                player.sendMessage(Language.ERROR_SAVING);
                ex.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {

        if (!e.getTag().equals(BungeeOnlineTime.CHANNEL)) {
            return;
        }

        if (!(e.getReceiver() instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) e.getReceiver();
        UUID uuid = player.getUniqueId();
        Server server = player.getServer();

        ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
            try {
                OnlineTime onlineTime = BungeeOnlineTime.SQL.getOnlineTime(uuid);

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeLong(onlineTime.getTime() / 1000);
                server.sendData(BungeeOnlineTime.CHANNEL, out.toByteArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
