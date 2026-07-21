package fr.pandamcbe.mgbungee.commands.privatemessage;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MsgRCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();

        if(!(sender instanceof Player)) {
            return;
        }

        Player proxiedPlayer = (Player) sender;

        if(args.length == 0) {
            proxiedPlayer.sendMessage(Main.mm("<aqua><bold>MG-Land> <white>Tapez: /r[respond] <message> </white></bold>"));
            return;
        }

        if(Main.message.containsKey(proxiedPlayer)) {
            if(!Main.message.get(proxiedPlayer).getCurrentServer().isPresent()) {
                proxiedPlayer.sendMessage(Main.mm("<aqua><bold>MG-Land> <red>Ce joueur n'est pas en ligne.. </red>:(</bold>"));
            } else {
                Player pDest = Main.message.get(proxiedPlayer);

                String msg = "";

                for(int i = 0; i != args.length; i++) msg += args[i] + " ";

                if(pDest != proxiedPlayer){
                    if(msg != null){
                        proxiedPlayer.sendMessage(Main.mm("<aqua><boldMG-Land> </aqua><white>Message <green>envoyé </green><white>à <dark_aqua>" + pDest.getUsername() + ": </dark_aqua><gray>" + msg + "</gray></bold>"));
                        pDest.sendMessage(Main.mm("<aqua><bold>MG-Land> </aqua><white>Message <green>reçu de " + proxiedPlayer.getUsername() + "</green>: <light_purple><italic>" + msg + "</italic></light_purple></bold>"));
                    }
                }
            }
        } else {
            proxiedPlayer.sendMessage(Main.mm("<aqua><bold>MG-Land> </aqua><red>Vous n'avez personne à qui répondre. Triste...</bold>"));
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
