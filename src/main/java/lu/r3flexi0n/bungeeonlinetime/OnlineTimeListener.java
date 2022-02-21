package lu.r3flexi0n.bungeeonlinetime;

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTime;
import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer;
import lu.r3flexi0n.bungeeonlinetime.utils.AsyncTask;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
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

        UUID uuid = player.getUniqueId();
        OnlineTimePlayer onlineTimePlayer = new OnlineTimePlayer();
        plugin.onlineTimePlayers.put(uuid, onlineTimePlayer);

        if (usePlaceholderApi) {
            String name = player.getName();
            new AsyncTask(plugin).execute(new AsyncTask.Task<List<OnlineTime>>() {

                @Override
                public List<OnlineTime> doTask() throws Exception {
                    return plugin.database.getOnlineTime(uuid.toString());
                }

                @Override
                public void onSuccess(List<OnlineTime> onlineTimes) {
                    long savedOnlineTime = !onlineTimes.isEmpty() ? onlineTimes.get(0).getTime() : 0L;
                    onlineTimePlayer.setSavedOnlineTime(savedOnlineTime);
                    sendOnlineTimeToServer(player, savedOnlineTime);
                }

                @Override
                public void onError(Exception exception) {
                    Utils.log("Error while loading online time for player " + name + ".");
                    exception.printStackTrace();
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
            Long savedOnlineTime = onlineTimePlayer.getSavedOnlineTime();
            if (savedOnlineTime != null) {
                long totalOnlineTime = savedOnlineTime + onlineTimePlayer.getSessionOnlineTime();
                sendOnlineTimeToServer(player, totalOnlineTime);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {

        ProxiedPlayer player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        OnlineTimePlayer onlinePlayer = plugin.onlineTimePlayers.get(uuid);
        if (onlinePlayer == null) {
            return;
        }

        plugin.onlineTimePlayers.remove(uuid);

        long time = onlinePlayer.getSessionOnlineTime();
        if (time < 5000L) {
            return;
        }

        String name = player.getName();

        new AsyncTask(plugin).execute(new AsyncTask.Task<Void>() {

            @Override
            public Void doTask() throws Exception {
                plugin.database.updateOnlineTime(uuid.toString(), name, time);
                return null;
            }

            @Override
            public void onSuccess(Void v) {
            }

            @Override
            public void onError(Exception exception) {
                Utils.log("Error while saving online time for player " + name + ".");
                exception.printStackTrace();
            }

        });
    }

    private void sendOnlineTimeToServer(ProxiedPlayer player, long onlineTime) {
        if (player == null || player.getServer() == null) {
            return;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeUTF(player.getUniqueId().toString());
            dataOutputStream.writeLong(onlineTime / 1000);
            player.getServer().sendData(plugin.pluginMessageChannel, byteArrayOutputStream.toByteArray());
            dataOutputStream.close();
        } catch (IOException ex) {
            Utils.log("Error while sending plugin message.");
            ex.printStackTrace();
        }
    }
}
