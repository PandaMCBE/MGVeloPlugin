package fr.pandamcbe.mgbungee.commands.general;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.redisstuff.RedisPbSb;
import fr.pandamcbe.mymgsql.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AccountType;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.redis.RedisMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StatsCommand implements SimpleCommand {

    private final RedisAccountsManager redisAccountsManager = new RedisAccountsManager();
    private final RedisPbSb redisPbSb = new RedisPbSb();

    private boolean verifyName(String serverName){
        for(String s : Main.getINSTANCE().networkState.getServerNames()){
            if(!s.equalsIgnoreCase(AllNames.LOBBY.getName())) {
                if(serverName.equalsIgnoreCase(s))
                    return true;
            }
        }
        return false;
    }
    @Override
    public void execute(Invocation invocation) {
        ProxyServer proxyServer = Main.INSTANCE.server;
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if(!(sender instanceof Player)) {
            return;
        }
            Player player = (Player) sender;
            if (args.length > 0) {
                final String serverName = args[0];
                if (verifyName(serverName)) {
                    if (args.length == 2) {
                        StatsAccount statsAccount = (StatsAccount) redisAccountsManager.getStatsAccount(player.getUniqueId(), AccountType.statsKey(serverName));
                        ProfilAccount profilAccount = (ProfilAccount) redisAccountsManager.getProfilAccount(player.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
                        player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + args[1] + " a actuellement au " + serverName + "</white>: <yellow>" + profilAccount.getCoins() + "</yellow> <gold>MGCoins</gold> <dark_purple>" + statsAccount.getElos() + " MGElos</dark_purple> <red>" + statsAccount.getKills() + " MGKills</red> <green>" + statsAccount.getKillstreaks() + " MGKillStreaks</green> <aqua>" + statsAccount.getWins() + " MGWins</aqua>"));
                    } else if (args.length < 2) {
                        player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <red>/stats [serverName] [playerName] [stat(coins, elos...)] [add/remove] [amount]</red>"));
                    }
                    if (args.length >= 4) {
                        if (proxyServer.getPlayer(args[1]).isPresent()) {
                            Player target = proxyServer.getPlayer(args[1]).get();
                            editCoins(args, player, target);
                            editElos(args, player, target, serverName);
                            editKills(args, player, target, serverName);
                            editWins(args, player, target, serverName);
                            editKillStreaks(args, player, target, serverName);
                        }
                    }
                } else {
                    player.sendMessage(Main.mm("<red>Nom de serveur incorrècte, veuillez réessayer.</red>"));
                }
            }
    }

    public void editCoins(String[] args, Player player, Player target){
        if (args[2].equalsIgnoreCase("coins")) {
            ProfilAccount profilAccount = (ProfilAccount) redisAccountsManager.getProfilAccount(target.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    profilAccount.setCoins(profilAccount.getCoins() + amount);

                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + "a reçu </white><yellow>" + amount + "</yellow> <gold>MGCoins</gold>"));


                }
                // /coins remove <le nombre de coins> <le joueur>
            } else if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) {

                    int amount = Integer.parseInt(args[4]);
                    profilAccount.setCoins(profilAccount.getCoins() - amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + " a perdu </white><yellow>" + amount + " </yellow><gold>MGCoins</gold>"));


                }
            }
            redisAccountsManager.sendAccountToRedisType(target.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);
            redisPbSb.publishMessage(AllNames.STATSCOMMAND.getName(), new RedisMessage(target.getUniqueId(), profilAccount));
        }
    }

    public void editElos(String[] args, Player player, Player target, String serverName) {
        if (args[2].equalsIgnoreCase("elos")) {
            StatsAccount statsAccount = (StatsAccount) redisAccountsManager.getStatsAccount(target.getUniqueId(), AccountType.statsKey(serverName));
            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) {

                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setElos(statsAccount.getElos() + amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + "a reçu </white><yellow>" + amount + "</yellow> <dark_purple>MGElos</dark_purple>"));

                }

            } else if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setElos(statsAccount.getElos() - amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + " a perdu </white><yellow>" + amount + " </yellow><dark_purple>MGElos</dark_purple>"));
                }
            }
            redisAccountsManager.sendAccountToRedisType(target.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
            redisPbSb.publishMessage(AllNames.STATSCOMMAND.getName(), new RedisMessage(target.getUniqueId(), statsAccount));
        }
    }

    public void editKills(String[] args, Player player, Player target, String serverName) {
        if (args[2].equalsIgnoreCase("kills")) {
            StatsAccount statsAccount = (StatsAccount) redisAccountsManager.getStatsAccount(target.getUniqueId(), AccountType.statsKey(serverName));

            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setKills(statsAccount.getKills() + amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + "a reçu </white><yellow>" + amount + "</yellow> <red>MGKills</red>"));

                }

            } else if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setKills(statsAccount.getKills() - amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + " a perdu </white><yellow>" + amount + " </yellow><red>MGKills</red>"));

                }
            }
            redisAccountsManager.sendAccountToRedisType(target.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
            redisPbSb.publishMessage(AllNames.STATSCOMMAND.getName(), new RedisMessage(target.getUniqueId(), statsAccount));

        }
    }

    public void editWins(String[] args, Player player, Player target, String serverName){
        if (args[2].equalsIgnoreCase("wins")) {
            StatsAccount statsAccount = (StatsAccount) redisAccountsManager.getStatsAccount(target.getUniqueId(), AccountType.statsKey(serverName));

            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setWins(statsAccount.getWins() + amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + "a reçu </white><yellow>" + amount + "</yellow> <aqua>MGWins</aqua>"));

                }

            } else if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setWins(statsAccount.getWins() - amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + " a perdu </white><yellow>" + amount + " </yellow><aqua>MGWins</aqua>"));
                }
            }
            redisAccountsManager.sendAccountToRedisType(target.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
            redisPbSb.publishMessage(AllNames.STATSCOMMAND.getName(), new RedisMessage(target.getUniqueId(), statsAccount));

        }
    }

    public void editKillStreaks(String[] args, Player player, Player target, String serverName) {
        if (args[2].equalsIgnoreCase("killstreaks")) {
            StatsAccount statsAccount = (StatsAccount) redisAccountsManager.getStatsAccount(target.getUniqueId(), AccountType.statsKey(serverName));

            if (args[3].equalsIgnoreCase("add")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setKillstreaks(statsAccount.getKillstreaks() + amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + "a reçu </white><yellow>" + amount + "</yellow> <green>MGKillstreaks</green>"));

                }

            } else if (args[3].equalsIgnoreCase("remove")) {
                if (args.length == 5) {
                    int amount = Integer.parseInt(args[4]);
                    statsAccount.setKillstreaks(statsAccount.getKillstreaks() - amount);
                    player.sendMessage(Main.mm("<aqua><b>MG-Land></b></aqua> <white>" + target.getUsername() + " a perdu </white><yellow>" + amount + " </yellow><green>MGKillstreaks</green>"));

                }
            }
            redisAccountsManager.sendAccountToRedisType(target.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
            redisPbSb.publishMessage(AllNames.STATSCOMMAND.getName(), new RedisMessage(target.getUniqueId(), statsAccount));

        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("mgbungee.stats");
    }
}
