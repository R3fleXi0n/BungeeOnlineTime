package lu.r3flexi0n.bungeeonlinetime;

import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTime;
import lu.r3flexi0n.bungeeonlinetime.objects.OnlineTimePlayer;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OnlineTimeCommand extends Command {

    private final BungeeOnlineTimePlugin plugin;

    private final int topOnlineTimePageLimit;

    public OnlineTimeCommand(BungeeOnlineTimePlugin plugin, String command, String permission, String... aliases) {
        super(command, permission, aliases);

        this.plugin = plugin;

        this.topOnlineTimePageLimit = (int) plugin.settings.get("Plugin.topOnlineTimePageLimit");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            sendMessage(sender, "Language.onlyPlayer");
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {

            if (!player.hasPermission("onlinetime.own")) {
                sendMessage(player, "Language.noPermission");
                return;
            }

            String name = player.getName();
            sendOnlineTime(player, name);

        } else if (args.length == 2 && args[0].equals("get")) {

            if (!player.hasPermission("onlinetime.others")) {
                sendMessage(player, "Language.noPermission");
                return;
            }

            String name = args[1];
            sendOnlineTime(player, name);

        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("top")) {

            if (!player.hasPermission("onlinetime.top")) {
                sendMessage(player, "Language.noPermission");
                return;
            }

            int page;
            try {
                page = Integer.max(Integer.parseInt(args[1]), 1);
            } catch (Exception ex) {
                page = 1;
            }
            sendTopOnlineTimes(player, page);

        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            if (!player.hasPermission("onlinetime.reset")) {
                sendMessage(player, "Language.noPermission");
                return;
            }

            String name = args[1];
            sendReset(player, name);

        } else if (args.length == 1 && args[0].equalsIgnoreCase("resetall")) {

            if (!player.hasPermission("onlinetime.resetall")) {
                sendMessage(player, "Language.noPermission");
                return;
            }

            sendResetAll(player);

        } else {

            sendMessage(player, "Language.help");

        }
    }

    private void sendOnlineTime(ProxiedPlayer player, String targetPlayerName) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {

                List<OnlineTime> onlineTimes = plugin.database.getOnlineTime(targetPlayerName);
                if (onlineTimes.isEmpty()) {
                    HashMap<String, Object> placeholders = new HashMap<>();
                    placeholders.put("%PLAYER%", targetPlayerName);
                    sendMessage(player, "Language.playerNotFound", placeholders);
                    return;
                }

                for (OnlineTime onlineTime : onlineTimes) {
                    UUID uuid = onlineTime.getUUID();
                    String name = onlineTime.getName();
                    long totalOnlineTime = onlineTime.getTime();

                    OnlineTimePlayer onlineTimePlayer = plugin.onlineTimePlayers.get(uuid);
                    if (onlineTimePlayer != null) {
                        totalOnlineTime += onlineTimePlayer.getSessionOnlineTime();
                    }

                    long seconds = totalOnlineTime / 1000;
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    HashMap<String, Object> placeholders = new HashMap<>();
                    placeholders.put("%PLAYER%", name);
                    placeholders.put("%HOURS%", hours);
                    placeholders.put("%MINUTES%", minutes);
                    sendMessage(player, "Language.onlineTime", placeholders);
                }

            } catch (SQLException ex) {
                sendMessage(player, "Language.error");
                Utils.log("Error while loading online time for player " + targetPlayerName + ".");
                ex.printStackTrace();
            }
        });
    }

    private void sendTopOnlineTimes(ProxiedPlayer player, int page) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {

                HashMap<String, Object> headerPlaceholders = new HashMap<>();
                headerPlaceholders.put("%PAGE%", page);

                int rank = (page - 1) * topOnlineTimePageLimit + 1;

                sendMessage(player, "Language.topTimeAbove", headerPlaceholders);
                for (OnlineTime onlineTime : plugin.database.getTopOnlineTimes(page, topOnlineTimePageLimit)) {
                    UUID uuid = onlineTime.getUUID();
                    String name = onlineTime.getName();
                    long totalOnlineTime = onlineTime.getTime();

                    OnlineTimePlayer onlineTimePlayer = plugin.onlineTimePlayers.get(uuid);
                    if (onlineTimePlayer != null) {
                        totalOnlineTime += onlineTimePlayer.getSessionOnlineTime();
                    }

                    long seconds = totalOnlineTime / 1000;
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    HashMap<String, Object> placeholders = new HashMap<>();
                    placeholders.put("%RANK%", rank);
                    placeholders.put("%PLAYER%", name);
                    placeholders.put("%HOURS%", hours);
                    placeholders.put("%MINUTES%", minutes);
                    sendMessage(player, "Language.topTime", placeholders);

                    rank++;
                }
                sendMessage(player, "Language.topTimeBelow", headerPlaceholders);

            } catch (SQLException ex) {
                sendMessage(player, "Language.error");
                Utils.log("Error while loading top online times.");
                ex.printStackTrace();
            }
        });
    }

    private void sendReset(ProxiedPlayer player, String targetPlayerName) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {

                plugin.database.resetOnlineTime(targetPlayerName);

                HashMap<String, Object> placeholders = new HashMap<>();
                placeholders.put("%PLAYER%", targetPlayerName);
                sendMessage(player, "Language.resetPlayer", placeholders);

            } catch (SQLException ex) {
                sendMessage(player, "Language.error");
                Utils.log("Error while resetting online time for player " + targetPlayerName + ".");
                ex.printStackTrace();
            }
        });
    }

    private void sendResetAll(ProxiedPlayer player) {
        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            try {

                plugin.database.resetAllOnlineTimes();

                sendMessage(player, "Language.resetAll");

            } catch (SQLException ex) {
                sendMessage(player, "Language.error");
                Utils.log("Error while resetting online time database.");
                ex.printStackTrace();
            }
        });
    }

    private void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, new HashMap<>());
    }

    private void sendMessage(CommandSender sender, String messageId, HashMap<String, Object> placeholders) {
        String message = (String) plugin.settings.get(messageId);
        for (Map.Entry<String, Object> entries : placeholders.entrySet()) {
            message = message.replace(entries.getKey(), entries.getValue().toString());
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(message);
    }
}
