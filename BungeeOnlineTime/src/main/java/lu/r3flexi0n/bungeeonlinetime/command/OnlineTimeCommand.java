package lu.r3flexi0n.bungeeonlinetime.command;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import lu.r3flexi0n.bungeeonlinetime.BungeeOnlineTime;
import lu.r3flexi0n.bungeeonlinetime.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

public class OnlineTimeCommand extends Command {

    public OnlineTimeCommand(String command, String permission, String... aliases) {
        super(command, null, aliases);
    }

    public void execute(final CommandSender sender, String[] args) {

        // get own onlinetime
        if (args.length == 0) {

            if (!sender.hasPermission("onlinetime.own")) {
                sender.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            if (!(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(BungeeOnlineTime.onlyPlayer);
                return;
            }
            final ProxiedPlayer player = (ProxiedPlayer) sender;

            BungeeOnlineTime.mysql.queryAsync("SELECT OnlineTime FROM onlineTime WHERE UUID = '" + player.getUniqueId() + "';", new Consumer<ResultSet>() {
                public void accept(ResultSet t) {
                    if (t != null) {
                        try {
                            if (t.next()) {
                                int time = t.getInt("OnlineTime");
                                int hours = time / 60;
                                int minutes = time % 60;
                                player.sendMessage(BungeeOnlineTime.onlineTime.replace("%DATE%", BungeeOnlineTime.lastReset).replace("%PLAYER%", player.getName()).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));
                            } else {
                                sender.sendMessage(BungeeOnlineTime.playerNotFound.replace("%PLAYER%", player.getName()));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            player.sendMessage(BungeeOnlineTime.error);
                        }
                    } else {
                        player.sendMessage(BungeeOnlineTime.error);
                    }
                }
            });

        } // get top onlinetimes
        else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {

            if (!sender.hasPermission("onlinetime.top")) {
                sender.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            BungeeOnlineTime.mysql.queryAsync("SELECT * FROM onlineTime ORDER BY OnlineTime DESC LIMIT 10;", new Consumer<ResultSet>() {
                public void accept(ResultSet t) {
                    if (t != null) {
                        try {
                            sender.sendMessage(BungeeOnlineTime.topPlayersAbove.replace("%DATE%", BungeeOnlineTime.lastReset));
                            String name;
                            int time;
                            int hours;
                            int minutes;
                            while (t.next()) {
                                name = t.getString("Name");
                                time = t.getInt("OnlineTime");
                                hours = time / 60;
                                minutes = time % 60;
                                sender.sendMessage(BungeeOnlineTime.topPlayers.replace("%PLAYER%", name).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));
                            }
                            sender.sendMessage(BungeeOnlineTime.topPlayersBelow.replace("%DATE%", BungeeOnlineTime.lastReset));
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sender.sendMessage(BungeeOnlineTime.error);
                        }
                    } else {
                        sender.sendMessage(BungeeOnlineTime.error);
                    }
                }
            });
        } // reset all onlinetimes
        else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {

            if (!sender.hasPermission("onlinetime.reset.all")) {
                sender.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            BungeeOnlineTime.mysql.updateAsync("UPDATE onlineTime SET OnlineTime = 0", new Consumer<Boolean>() {
                public void accept(Boolean t) {
                    if (t) {
                        try {
                            BungeeOnlineTime.lastReset = Utils.getDate();
                            Configuration config = BungeeOnlineTime.configurationProvider.load(BungeeOnlineTime.configFile);
                            config.set("lastReset", BungeeOnlineTime.lastReset);
                            BungeeOnlineTime.configurationProvider.save(config, BungeeOnlineTime.configFile);
                            sender.sendMessage(BungeeOnlineTime.resetDatabase);
                        } catch (IOException e) {
                            e.printStackTrace();
                            sender.sendMessage(BungeeOnlineTime.error);
                        }
                    } else {
                        sender.sendMessage(BungeeOnlineTime.error);
                    }
                }
            });
        } // reset onlinetime
        else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            if (!sender.hasPermission("onlinetime.reset")) {
                sender.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            final String name = args[1];
            BungeeOnlineTime.mysql.updateAsync("UPDATE onlineTime SET OnlineTime = 0 WHERE Name = '" + name + "';", new Consumer<Boolean>() {
                public void accept(Boolean t) {
                    if (t) {
                        sender.sendMessage(BungeeOnlineTime.resetPlayer.replace("%PLAYER%", name));
                    } else {
                        sender.sendMessage(BungeeOnlineTime.error);
                    }
                }
            });
        } // get other onlinetime
        else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {

            if (!sender.hasPermission("onlinetime.others")) {
                sender.sendMessage(BungeeOnlineTime.noPermission);
                return;
            }

            final String name = args[1];

            BungeeOnlineTime.mysql.queryAsync("SELECT OnlineTime FROM onlineTime WHERE Name = '" + name + "';", new Consumer<ResultSet>() {
                public void accept(ResultSet t) {
                    if (t != null) {
                        try {
                            if (t.next()) {
                                int time = t.getInt("OnlineTime");
                                int hours = time / 60;
                                int minutes = time % 60;
                                sender.sendMessage(BungeeOnlineTime.onlineTime.replace("%DATE%", BungeeOnlineTime.lastReset).replace("%PLAYER%", name).replace("%HOURS%", String.valueOf(hours)).replace("%MINUTES%", String.valueOf(minutes)));
                            } else {
                                sender.sendMessage(BungeeOnlineTime.playerNotFound.replace("%PLAYER%", name));
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            sender.sendMessage(BungeeOnlineTime.error);
                        }
                    } else {
                        sender.sendMessage(BungeeOnlineTime.error);
                    }
                }
            });
        } // usage
        else {
            sender.sendMessage("§7Usage:");
            sender.sendMessage("§7/onlinetime §6| §7Own onlinetime");
            sender.sendMessage("§7/onlinetime top §6| §7Top 10 onlinetimes");
            sender.sendMessage("§7/onlinetime reset §6| §7Reset all onlinetimes");
            sender.sendMessage("§7/onlinetime reset <player> §6| §7Reset players onlinetime");
            sender.sendMessage("§7/onlinetime get <player> §6| §7Get players onlinetime");
        }
    }
}
