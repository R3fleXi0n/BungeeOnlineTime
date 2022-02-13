package lu.r3flexi0n.bungeeonlinetime;

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTime;
import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class OnlineTimeListener implements Listener {

    private final BungeeOnlineTimePlugin plugin;

    private final List<String> disabledServers;
    private final boolean usePlaceholderApi;

    public OnlineTimeListener(BungeeOnlineTimePlugin plugin) {
        this.plugin = plugin;

        this.disabledServers = (List<String>) plugin.settings.get("Plugin.disabledServers");
        this.usePlaceholderApi = (boolean) plugin.settings.get("Plugin.usePlaceholderApi");
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {

        ProxiedPlayer player = e.getPlayer();
        if (!player.hasPermission("onlinetime.save")) {
            return;
        }

        OnlineTimePlayer onlineTimePlayer = new OnlineTimePlayer();
        plugin.onlineTimePlayers.put(player.getUniqueId(), onlineTimePlayer);

        if (usePlaceholderApi) {
            UUID uuid = player.getUniqueId();
            String name = player.getName();

            ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
                try {
                    List<OnlineTime> onlineTime = plugin.database.getOnlineTime(uuid.toString());
                    if (onlineTime.isEmpty()) {
                        onlineTimePlayer.setTotalOnlineTime(new OnlineTime(uuid, name, 0L));
                    } else {
                        onlineTimePlayer.setTotalOnlineTime(onlineTime.get(0));
                    }
                } catch (SQLException ex) {
                    Utils.log("Error while loading online time for player " + name + ".");
                    ex.printStackTrace();
                }
            });
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        ProxiedPlayer player = e.getPlayer();
        OnlineTimePlayer onlineTimePlayer = plugin.onlineTimePlayers.get(player.getUniqueId());
        if (onlineTimePlayer == null) {
            return;
        }

        ServerInfo server = player.getServer().getInfo();
        if (disabledServers.contains(server.getName())) {
            onlineTimePlayer.joinDisabledServer();
        } else {
            onlineTimePlayer.leaveDisabledServer();
        }

        if (usePlaceholderApi) {
            sendOnlineTimeToServer(server, onlineTimePlayer);
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {

        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        OnlineTimePlayer onlinePlayer = plugin.onlineTimePlayers.get(uuid);
        if (onlinePlayer == null) {
            return;
        }

        plugin.onlineTimePlayers.remove(uuid);

        onlinePlayer.leaveDisabledServer();

        long time = onlinePlayer.getSessionOnlineTime();
        if (time < 5000L) {
            return;
        }

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {
                plugin.database.updateOnlineTime(uuid.toString(), name, time);
            } catch (SQLException ex) {
                Utils.log("Error while saving online time for player " + name + ".");
                ex.printStackTrace();
            }
        });
    }

    private void sendOnlineTimeToServer(ServerInfo server, OnlineTimePlayer onlineTimePlayer) {
        OnlineTime totalOnlineTime = onlineTimePlayer.getTotalOnlineTime();
        if (totalOnlineTime == null) {
            return;
        }

        long time = totalOnlineTime.getTime() + onlineTimePlayer.getSessionOnlineTime();

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(totalOnlineTime.getUUID().toString());
            dataOutputStream.writeLong(time / 1000);
            server.sendData(plugin.pluginMessageChannel, byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (IOException ex) {
            Utils.log("Error while sending plugin message.");
            ex.printStackTrace();
        }
    }
}
