package fr.pandamcbe.mgbungee.commands.privatemessage;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AccountType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MsgCommand implements SimpleCommand {


    @Override
    public void execute(Invocation invocation) {
        ProxyServer proxyServer = Main.INSTANCE.server;
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if(!(sender instanceof Player)) {
            return;
        }

        final Player proxiedPlayer = (Player) sender;

        if(args.length == 0) {
            proxiedPlayer.sendMessage(Main.mm("<aqua><bold>MG-Land> <white>Tapez: /w[msg,mp] <joueur> <message> </white></bold>"));
            return;
        }

        if(args.length > 1) {
            String dest = args[0];

            if (!proxyServer.getPlayer(dest).isPresent()) {
                proxiedPlayer.sendMessage(Main.mm("<aqua><bold>MG-Land> <red>Ce joueur n'est pas en ligne.. </red>:(</bold>"));
            } else {
                final Player playerDest = Main.INSTANCE.server.getPlayer(dest).get();
                proxyServer.getScheduler().buildTask(Main.INSTANCE, ()-> {
                    final RedisAccountsManager connections = new RedisAccountsManager();
                    final ProfilAccount account = (ProfilAccount) connections.getProfilAccount(playerDest.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
                    String msg = "";

                    for(int i = 1; i != args.length; i++) msg += args[i] + " ";

                    if(account.getMsg_allow() == 1) {

                        if (playerDest != proxiedPlayer) {

                            if (msg != null) {
                                proxiedPlayer.sendMessage(Main.mm("<aqua><boldMG-Land> </aqua><white>Message <green>envoyé </green><white>à <dark_aqua>" + playerDest.getUsername() + ": </dark_aqua><gray>" + msg + "</gray></bold>"));
                                playerDest.sendMessage(Main.mm("<aqua><bold>MG-Land> </aqua><white>Message <green>reçu de " + proxiedPlayer.getUsername() + "</green>: <light_purple><italic>" + msg + "</italic></light_purple></bold>"));
                                Main.message.put(playerDest, proxiedPlayer);

                            }

                        } else {
                            proxiedPlayer.sendMessage(Main.mm("<red>Vous ne pouvez pas vous MP à vous même !</red"));

                        }

                    } else {
                        proxiedPlayer.sendMessage(Main.mm("<red>Cette personne n'accepte pas les demandes de MP !</red"));

                    }
                });
            }

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
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }
}