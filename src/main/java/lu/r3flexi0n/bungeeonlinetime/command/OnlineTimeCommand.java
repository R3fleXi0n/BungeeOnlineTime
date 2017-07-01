package lu.r3flexi0n.bungeeonlinetime.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class OnlineTimeCommand extends Command {

    public OnlineTimeCommand(String command, String permission, String... aliases) {
        super(command, null, aliases);
    }

    public void execute(final CommandSender sender, String[] args) {

        if (args.length == 0) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (!player.hasPermission("onlinetime.own")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    int time = BungeeOnlineTime.mysql.getOnlineTime(player.getUniqueId());
                    int hours = time / 60;
                    int minutes = time % 60;
                    player.sendMessage(BungeeOnlineTime.onlineTime.replace("%DATE%", BungeeOnlineTime.lastReset).replace("%PLAYER%", player.getName()).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));

                } catch (SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (!player.hasPermission("onlinetime.top")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    sender.sendMessage(BungeeOnlineTime.topWait);

                    ArrayList<String> top10 = BungeeOnlineTime.mysql.getTopOnlineTimes();
                    String[] data;
                    UUID uuid;
                    int time;
                    int hours;
                    int minutes;
                    String name;
                    ArrayList<String> messages = new ArrayList<String>();
                    for (String timeData : top10) {
                        data = timeData.split(",");
                        uuid = UUID.fromString(data[0]);
                        time = Integer.valueOf(data[1]);
                        hours = time / 60;
                        minutes = time % 60;
                        name = Utils.getName(uuid);
                        messages.add(BungeeOnlineTime.topPlayers.replace("%PLAYER%", name).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));
                    }

                    sender.sendMessage(BungeeOnlineTime.topPlayersAbove.replace("%DATE%", BungeeOnlineTime.lastReset));
                    for (String msg : messages) {
                        sender.sendMessage(msg);
                    }
                    sender.sendMessage(BungeeOnlineTime.topPlayersBelow.replace("%DATE%", BungeeOnlineTime.lastReset));

                } catch (Exception ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (!player.hasPermission("onlinetime.reset.all")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    BungeeOnlineTime.mysql.resetOnlineTimes();
                    BungeeOnlineTime.lastReset = Utils.getDate();
                    Configuration config = BungeeOnlineTime.configurationProvider.load(BungeeOnlineTime.configFile);
                    config.set("lastReset", BungeeOnlineTime.lastReset);
                    BungeeOnlineTime.configurationProvider.save(config, BungeeOnlineTime.configFile);
                    sender.sendMessage(BungeeOnlineTime.resetDatabase);

                } catch (SQLException | ClassNotFoundException | IOException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (!player.hasPermission("onlinetime.reset")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);
                    BungeeOnlineTime.mysql.resetOnlineTime(uuid);
                    sender.sendMessage(BungeeOnlineTime.resetPlayer.replace("%PLAYER%", args[1]));

                } catch (Exception ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if (!player.hasPermission("onlinetime.others")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);

                    int time = BungeeOnlineTime.mysql.getOnlineTime(uuid);
                    int hours = time / 60;
                    int minutes = time % 60;
                    sender.sendMessage(BungeeOnlineTime.onlineTime.replace("%DATE%", BungeeOnlineTime.lastReset).replace("%PLAYER%", args[1]).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));

                } catch (Exception ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else {
            sender.sendMessage("§7Usage:");
            sender.sendMessage("§7/onlinetime §6| §7Own onlinetime");
            sender.sendMessage("§7/onlinetime top §6| §7Top 10 onlinetimes");
            sender.sendMessage("§7/onlinetime reset §6| §7Reset all onlinetimes");
            sender.sendMessage("§7/onlinetime reset <player> §6| §7Reset players onlinetime");
            sender.sendMessage("§7/onlinetime get <player> §6| §7Get players onlinetime");
        }
    }
}
