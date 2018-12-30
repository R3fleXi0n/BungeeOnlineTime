package lu.r3flexi0n.bungeeonlinetime;

import lu.r3flexi0n.bungeeonlinetime.utils.Language;
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
            sender.sendMessage(Language.ONLY_PLAYER);
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {

            if (!player.hasPermission("onlinetime.own")) {
                player.sendMessage(Language.NO_PERMISSION);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {

                    OnlineTime onlineTime = BungeeOnlineTime.SQL.getOnlineTime(player.getUniqueId());
                    if (onlineTime == null) {
                        player.sendMessage(Language.PLAYER_NOT_FOUND
                                .replace("%PLAYER%", player.getName()));
                        return;
                    }

                    long seconds = onlineTime.getTime() / 1000;
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(Language.ONLINE_TIME
                            .replace("%PLAYER%", player.getName())
                            .replace("%HOURS%", String.valueOf(hours))
                            .replace("%MINUTES%", String.valueOf(minutes)));

                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR);
                    ex.printStackTrace();
                }
            });

        } else if (args.length == 2 && args[0].equals("get")) {

            if (!player.hasPermission("onlinetime.others")) {
                player.sendMessage(Language.NO_PERMISSION);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {

                    OnlineTime onlineTime = BungeeOnlineTime.SQL.getOnlineTime(args[1]);
                    if (onlineTime == null) {
                        player.sendMessage(Language.PLAYER_NOT_FOUND
                                .replace("%PLAYER%", args[1]));
                        return;
                    }

                    long seconds = onlineTime.getTime() / 1000;
                    int hours = (int) (seconds / 3600);
                    int minutes = (int) ((seconds % 3600) / 60);

                    player.sendMessage(Language.ONLINE_TIME
                            .replace("%PLAYER%", onlineTime.getName())
                            .replace("%HOURS%", String.valueOf(hours))
                            .replace("%MINUTES%", String.valueOf(minutes)));

                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR);
                    ex.printStackTrace();
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("top")) {

            if (!player.hasPermission("onlinetime.top")) {
                player.sendMessage(Language.NO_PERMISSION);
                return;
            }

            player.sendMessage(Language.TOP_TIME_LOADING);

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {

                    StringBuilder builder = new StringBuilder();
                    builder.append(Language.TOP_TIME_ABOVE);
                    builder.append("\n");
                    for (OnlineTime onlineTimes : BungeeOnlineTime.SQL.getTopOnlineTimes(BungeeOnlineTime.TOP_ONLINETIMES_LIMIT)) {

                        long seconds = onlineTimes.getTime() / 1000;
                        int hours = (int) (seconds / 3600);
                        int minutes = (int) ((seconds % 3600) / 60);

                        builder.append(Language.TOP_TIME
                                .replace("%PLAYER%", onlineTimes.getName())
                                .replace("%HOURS%", String.valueOf(hours))
                                .replace("%MINUTES%", String.valueOf(minutes)));
                        builder.append("\n");
                    }
                    builder.append(Language.TOP_TIME_BELOW);

                    player.sendMessage(builder.toString());

                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR);
                    ex.printStackTrace();
                }
            });

        } else if (args.length == 1 && args[0].equalsIgnoreCase("resetall")) {

            if (!player.hasPermission("onlinetime.resetall")) {
                player.sendMessage(Language.NO_PERMISSION);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {

                    BungeeOnlineTime.SQL.resetAll();
                    player.sendMessage(Language.RESET_ALL);

                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR);
                    ex.printStackTrace();
                }
            });

        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {

            if (!player.hasPermission("onlinetime.reset")) {
                player.sendMessage(Language.NO_PERMISSION);
                return;
            }

            ProxyServer.getInstance().getScheduler().runAsync(BungeeOnlineTime.INSTANCE, () -> {
                try {

                    BungeeOnlineTime.SQL.reset(args[1]);
                    player.sendMessage(Language.RESET_PLAYER
                            .replace("%PLAYER%", args[1]));

                } catch (Exception ex) {
                    player.sendMessage(Language.ERROR);
                    ex.printStackTrace();
                }
            });

        } else {
            player.sendMessage("§7Usage:");
            player.sendMessage("§7/onlinetime");
            player.sendMessage("§7/onlinetime get <player>");
            player.sendMessage("§7/onlinetime top");
            player.sendMessage("§7/onlinetime reset <player>");
            player.sendMessage("§7/onlinetime resetall");
        }
    }
}
