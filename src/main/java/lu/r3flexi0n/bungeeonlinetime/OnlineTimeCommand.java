package lu.r3flexi0n.bungeeonlinetime;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class OnlineTimeCommand extends Command {

    public OnlineTimeCommand(String command, String permission, String... aliases) {
        super(command, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(BungeeOnlineTime.onlyPlayer);
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {

            if (!player.hasPermission("onlinetime.own")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    long seconds = BungeeOnlineTime.mysql.getOnlineTime(player.getUniqueId(), 0);
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(BungeeOnlineTime.onlineTime
                            .replace("%PLAYER%", player.getName())
                            .replace("%HOURS%", String.valueOf(hours))
                            .replace("%MINUTES%", String.valueOf(minutes)));

                } catch (SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equals("get")) {

            if (!player.hasPermission("onlinetime.others")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);
                    if (uuid == null) {
                        player.sendMessage(BungeeOnlineTime.playerNotFound
                                .replace("%PLAYER%", args[1]));
                        return;
                    }

                    long seconds = BungeeOnlineTime.mysql.getOnlineTime(uuid, 0);
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(BungeeOnlineTime.onlineTime
                            .replace("%PLAYER%", args[1])
                            .replace("%HOURS%", String.valueOf(hours))
                            .replace("%MINUTES%", String.valueOf(minutes)));

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 3 && args[0].equals("get")) {

            if (!player.hasPermission("onlinetime.others.since")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            Date date;
            try {
                date = Utils.getDate(args[2]);
            } catch (ParseException ex) {
                player.sendMessage(BungeeOnlineTime.wrongFormat
                        .replace("%FORMAT%", BungeeOnlineTime.dateFormat));
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);
                    if (uuid == null) {
                        player.sendMessage(BungeeOnlineTime.playerNotFound
                                .replace("%PLAYER%", args[1]));
                        return;
                    }

                    long seconds = BungeeOnlineTime.mysql.getOnlineTime(uuid, date.getTime());
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(BungeeOnlineTime.onlineTimeSince
                            .replace("%PLAYER%", args[1])
                            .replace("%DATE%", args[2])
                            .replace("%HOURS%", String.valueOf(hours))
                            .replace("%MINUTES%", String.valueOf(minutes)));

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {

            if (!player.hasPermission("onlinetime.top")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    LinkedHashMap<UUID, Long> top = BungeeOnlineTime.mysql.getTopOnlineTimes(10, 0);

                    StringBuilder builder = new StringBuilder();
                    builder.append(BungeeOnlineTime.topTimeAbove);
                    builder.append("\n");
                    for (Entry<UUID, Long> entries : top.entrySet()) {

                        String name = Utils.getName(entries.getKey());

                        long seconds = entries.getValue();
                        int hours = (int) (seconds / 3600);
                        int minutes = (int) ((seconds % 3600) / 60);

                        builder.append(BungeeOnlineTime.topTime
                                .replace("%PLAYER%", name)
                                .replace("%HOURS%", String.valueOf(hours))
                                .replace("%MINUTES%", String.valueOf(minutes)));
                        builder.append("\n");
                    }
                    builder.append(BungeeOnlineTime.topTimeBelow);

                    player.sendMessage(builder.toString());

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {

            if (!player.hasPermission("onlinetime.top.since")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            Date date;
            try {
                date = Utils.getDate(args[1]);
            } catch (ParseException ex) {
                player.sendMessage(BungeeOnlineTime.wrongFormat
                        .replace("%FORMAT%", BungeeOnlineTime.dateFormat));
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    LinkedHashMap<UUID, Long> top = BungeeOnlineTime.mysql.getTopOnlineTimes(10, date.getTime());

                    StringBuilder builder = new StringBuilder();
                    builder.append(BungeeOnlineTime.topTimeSinceAbove.replace("%DATE%", args[1]));
                    builder.append("\n");
                    for (Entry<UUID, Long> entries : top.entrySet()) {

                        String name = Utils.getName(entries.getKey());

                        long seconds = entries.getValue();
                        int hours = (int) (seconds / 3600);
                        int minutes = (int) ((seconds % 3600) / 60);

                        builder.append(BungeeOnlineTime.topTimeSince
                                .replace("%PLAYER%", name)
                                .replace("%HOURS%", String.valueOf(hours))
                                .replace("%MINUTES%", String.valueOf(minutes)));
                        builder.append("\n");
                    }
                    builder.append(BungeeOnlineTime.topTimeSinceBelow.replace("%DATE%", args[1]));

                    player.sendMessage(builder.toString());

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("resetall")) {

            if (!player.hasPermission("onlinetime.resetall")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    BungeeOnlineTime.mysql.resetAll(System.currentTimeMillis());
                    player.sendMessage(BungeeOnlineTime.resetAll);

                } catch (SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("resetall")) {

            if (!player.hasPermission("onlinetime.resetall.since")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            Date date;
            try {
                date = Utils.getDate(args[1]);
            } catch (ParseException ex) {
                player.sendMessage(BungeeOnlineTime.wrongFormat
                        .replace("%FORMAT%", BungeeOnlineTime.dateFormat));
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    BungeeOnlineTime.mysql.resetAll(date.getTime());
                    player.sendMessage(BungeeOnlineTime.resetAllBefore
                            .replace("%DATE%", args[1]));

                } catch (SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            if (!player.hasPermission("onlinetime.reset")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);
                    if (uuid == null) {
                        player.sendMessage(BungeeOnlineTime.playerNotFound
                                .replace("%PLAYER%", args[1]));
                        return;
                    }

                    BungeeOnlineTime.mysql.reset(uuid, System.currentTimeMillis());
                    player.sendMessage(BungeeOnlineTime.resetPlayer
                            .replace("%PLAYER%", args[1]));

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else if (args.length == 3 && args[0].equalsIgnoreCase("reset")) {

            if (!player.hasPermission("onlinetime.reset")) {
                player.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            Date date;
            try {
                date = Utils.getDate(args[2]);
            } catch (ParseException ex) {
                player.sendMessage(BungeeOnlineTime.wrongFormat
                        .replace("%FORMAT%", BungeeOnlineTime.dateFormat));
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.instance, () -> {
                try {

                    UUID uuid = Utils.getUUID(args[1]);
                    if (uuid == null) {
                        player.sendMessage(BungeeOnlineTime.playerNotFound
                                .replace("%PLAYER%", args[1]));
                        return;
                    }

                    BungeeOnlineTime.mysql.reset(uuid, date.getTime());
                    player.sendMessage(BungeeOnlineTime.resetPlayerBefore
                            .replace("%PLAYER%", args[1])
                            .replace("%DATE%", args[2]));

                } catch (IOException | SQLException | ClassNotFoundException ex) {
                    player.sendMessage(BungeeOnlineTime.error);
                }
            });

        } else {
            player.sendMessage("§7Usage:");
            player.sendMessage("§7/onlinetime");
            player.sendMessage("§7/onlinetime get <player> [since]");
            player.sendMessage("§7/onlinetime top [since]");
            player.sendMessage("§7/onlinetime reset <player> [before]");
            player.sendMessage("§7/onlinetime resetall [before]");
        }
    }
}
