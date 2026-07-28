package fr.pandamcbe.mgbungee.listeners;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.redisstuff.LoadRedisAccounts;
import fr.pandamcbe.mymgsql.MyMGSql;
import fr.pandamcbe.mymgsql.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
import fr.pandamcbe.mymgsql.data.management.savingdatatask.PushRedisToDatabase;

import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BungeeListener {
    private final Map<UUID, ScheduledTask> isConnectedConditon = new HashMap<>();
    @Subscribe
    public void onJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        ProxyServer server = MyMGSql.getInstance().server;
        server.getScheduler().buildTask(Main.getINSTANCE(), () -> {
            try {
                LoadRedisAccounts.LoadTheseRedisAccounts(player);
                MyMGSql.getInstance().callAccountsFromRedis.sendEveryCosmeAccounts(player);
            } catch (SQLException | AccountNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }).schedule();

        if(isConnectedConditon.get(player.getUniqueId()) != null){
            cancelTask(player.getUniqueId());
        }
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();
        ProxyServer server = MyMGSql.getInstance().server;
        //Revoir le fonctionnement
        ScheduledTask task = server.getScheduler().buildTask(Main.getINSTANCE(), () -> {
            Optional<Player> playerOptional = Main.INSTANCE.server.getPlayer(uuid);
            if (!playerOptional.isPresent()) {
                PushRedisToDatabase pushRedisToDatabase = new PushRedisToDatabase(p);
                pushRedisToDatabase.pushAllAtOnce();
                Main.getINSTANCE().redisMuteManager.UpdateMute(p);
            }

        }).delay(Duration.ofMinutes(5L)).schedule();
        isConnectedConditon.put(uuid, task);

    }

    private void cancelTask (UUID uuid){
        ScheduledTask scheduledTask = isConnectedConditon.remove(uuid);
            if(scheduledTask != null){
                scheduledTask.cancel();
        }
    }

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        player.sendPlayerListHeaderAndFooter(Main.mm("<bold><yellow>Bienvenue sur <green>MG-Land<yellow>!\n" + "<gold><bold>Là où le FUN s'ouvre à vous !!</bold>\n"), Main.mm("\n<dark_aqua><bold>Discord :</bold> <white><bold>discord.gg/Rb47GF673w</bold>\n" + "<red><bold>Youtube :</bold> <white><bold>youtube.com/@Mg-LandServeur</bold>"));
//        player.sendMessage(Main.mm("<aqua><bold>Mg-Land></bold></aqua> <reset>NOUVEAU : CJFFA/PerfectShot\n" + "<bold><st><dark_gray><st>-----</dark_gray></bold><reset><bold><dark_purple>BON JEU!</dark_purple><dark_gray><st>-----</dark_gray></bold>"));
//        Title title = Title.title(Main.mm("<green><bold>MG</green><gray>-</gray><gold><bold>Land</gold>"), Main.mm("<gold><bold>Serveur <light_purple>PvP</light_purple><green>x</green><light_purple>Mini-Jeux</light_purple></bold>"));
//        player.showTitle(title);


        //        server.getScheduler().buildTask(Main.getINSTANCE(), () -> player.sendPlayerListHeaderAndFooter(Main.mm("<yellow><bold>Bienvenue sur <green>MG-Land<yellow>!</bold>\n" + "<gold><bold>Là où le FUN s'ouvre à vous !!</bold>\n"), Main.mm("\n<dark_aqua><bold>Discord :</bold> <white><bold>discord.gg/Rb47GF673w</bold>\n" + "<red><bold>Youtube :</bold> <white><bold>youtube.com/@Mg-LandServeur</bold>"))).delay(2, TimeUnit.SECONDS).schedule();

//       if(task != null)
//        task.cancel();
    }

//    @Subscribe
//    public void onProxyPing(ProxyPingEvent event) {
//        ServerPing serverPing = event.getPing();
//        Component motd = Main.mm("   <bold><blue>><white>><red>> <dark_gray>-[<green>MG<gray>-<gold><u>Land</u><dark_gray>]- <red>1<gray>.<red>0<white> {1<dark_purple>.<white>8<dark_aqua>+<white>}</bold>\n" +
//                        "   <bold><aqua>PandaFun <yellow>6<gray>.<yellow>0</bold> <gray>| <dark_red>[PROCHAINEMENT]");
//
//        ServerPing newPing = ServerPing.builder().version(serverPing.getVersion())
//                .description(Component.text("§aBienvenue sur le serveur!"))
//                .favicon(serverPing.getFavicon().orElse(null))
//                .samplePlayers(new ServerPing.Players(
//                        1, // Nombre de joueurs connectés (pour l'affichage)
//                        50, // Max joueurs
//                        List.of() // Liste des joueurs affichés (optionnel)
//                ))
//                .build();
//
//        event.setPing(newPing);
//    }

    @Subscribe
    public void onLogin(LoginEvent event) {

        Main.getINSTANCE().banManager.checkDuration(event.getPlayer().getUniqueId());
        if (Main.getINSTANCE().banManager.isBanned(event.getPlayer().getUniqueId())) {

            event.setResult(ResultedEvent.ComponentResult.denied(Main.mm("<red>Vous êtes banni !</red>\n " +
                    "\n " +
                    "<gold>Raison : </gold><white>" + Main.getINSTANCE().banManager.getReason(event.getPlayer().getUniqueId()) + "\n " +
                    "\n " +
                    "</white><green>Temps restant : </green><white>" + Main.getINSTANCE().banManager.getTimeLeft(event.getPlayer().getUniqueId()) + "</white>")));
        }
            if(event.getPlayer().getRemoteAddress().getAddress().getHostAddress() != null) {
                String ip = event.getPlayer().getRemoteAddress().getAddress().getHostAddress();
                Main.getINSTANCE().banIpManager.checkDurationIp(ip);
                if (Main.getINSTANCE().banIpManager.isBannedIp(ip)) {
                    event.setResult(ResultedEvent.ComponentResult.denied(Main.mm("<red>Vous êtes banni !</red>\n " +
                            "\n " +
                            "<gold>Raison : <white>" + Main.getINSTANCE().banIpManager.getReasonIp(ip) + "</white>\n " +
                            "\n " +
                            "</gold><green>Temps restant : </green><white>" + Main.getINSTANCE().banIpManager.getTimeLeftIp(ip) + "</white>")));
                }
            }
    }
    @Subscribe
    public void onChat(PlayerChatEvent event) {

                Player sender = event.getPlayer();
        ProxyServer server = MyMGSql.getInstance().server;
        server.getScheduler().buildTask(Main.INSTANCE, ()-> {
                    try {
                        if (Main.getINSTANCE().redisMuteManager.isMutedInGame(sender) == 1) {
                            event.setResult(PlayerChatEvent.ChatResult.denied());

                            Main.getINSTANCE().redisMuteManager.checkDuration(sender);
                            sender.sendMessage(Main.mm("<red>Vous avez été Mute !\n \n </red><gold>Commentaire : </gold><white>" + Main.INSTANCE.redisMuteManager.getComment(sender) + "\n \n</white><green>Temps restant : </green><white>" + Main.INSTANCE.redisMuteManager.getTimeLeft(sender) + "</white>"));

                        }
                    } catch (AccountNotFoundException e) {
                        System.err.println(e.getMessage());
                    }
            CallAccountsFromRedis connections = MyMGSql.getInstance().callAccountsFromRedis;
            ProfilAccount profilAccount = (ProfilAccount) connections.getAccount(sender.getUniqueId(), AllNames.PROFILKEY.getName());
            if(profilAccount.getC_visibility() == 0) {
                sender.sendMessage(Main.mm("<red>Tu as désactivé ton chat !</red>"));
            }
        }).schedule();

    }

}