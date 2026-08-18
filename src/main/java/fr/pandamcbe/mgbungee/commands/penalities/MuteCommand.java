package fr.pandamcbe.mgbungee.commands.penalities;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.bans.TimeUnit;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MuteCommand implements SimpleCommand {
    public void helpMessage(CommandSource sender){
        sender.sendMessage(Main.mm("<red>/mute <joueur> perm <commentaire></red>"));
        sender.sendMessage(Main.mm("<red>/mute <joueur> <durée>:<unité> <commentaire></red>"));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 3) {
            helpMessage(sender);
            return;
        }
        final String targetName = args[0];


//            if (Main.INSTANCE.accountProvider.getAccount() == null) {
//                sender.sendMessage("§cCe joueur ne s'est jamais connecté au serveur !");
//                return;
//            }

//        UUID targetUUID = BungeeCord.getInstance().getPlayer(targetName).getUniqueId();
        final Player target = Main.INSTANCE.server.getPlayer(targetName).get();
        final Optional<Player> targetOptional = Main.INSTANCE.server.getPlayer(targetName);

        if(target == null) {
            sender.sendMessage(Main.mm("<red>" + targetName + " n'est pas connecté.</red>"));
            return;
        }
        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
            try {
//                final AccountProvider accountProvider = new AccountProvider(target);
//                final Account account = accountProvider.getAccount();

                if (targetOptional.isPresent() && Main.INSTANCE.redisMuteManager.isMutedInGame(target) == 1){
                    sender.sendMessage(Main.mm("<red>Ce joueur est déjà mute !</red>"));
                    return;
                }

                String comment = "";
                for (int i = 2; i < args.length; i++) {
                    comment += args[i] + " ";
                }


                if (args[1].equalsIgnoreCase("perm")) {

                    //Si j'enlève la vérification de null ou si le joueur est co, cela devrait normalement générer une erreur dans la console
                    Main.INSTANCE.redisMuteManager.mute(target, -1, comment);
                    sender.sendMessage(Main.mm("<green>Vous avez mute </green><gold>" + targetName + "</gold> <red>(Permanent) </red><green>pour : </green><yellow>" + comment + "</yellow>"));

                    return;

                }

                if (!args[1].contains(":")) {
                    helpMessage(sender);
                    return;
                }

                int duration = 0;
                try {
                    duration = Integer.parseInt(args[1].split(":")[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Main.mm("<red>La valeur 'durée' doit être un nombre !</red>"));
                    return;
                }

                if (!TimeUnit.existFromShortcut(args[1].split(":")[1])) {
                    sender.sendMessage(Main.mm("<red>Cette unité de temps n'existe pas ! </red>"));
                    for (TimeUnit units : TimeUnit.values()) {
                        sender.sendMessage(Main.mm("<aqua>" + units.getName() + "</aqua> <white>: <yellow>" + units.getShortcut() + "</yellow>"));
                    }
                    return;
                }

                TimeUnit unit = TimeUnit.getFromShortcut(args[1].split(":")[1]);
                long muteTime = unit.getToSecond() * duration;

                Main.INSTANCE.redisMuteManager.mute(target, muteTime, comment);
                sender.sendMessage(Main.mm("<green>Vous avez banni </green><gold>" + targetName + "</gold> <aqua>(" + duration + " " + unit.getName() + ")</aqua> <green>pour : <yellow>" + comment + "</yellow>"));


            }catch (AccountNotFoundException e) {
                target.disconnect(Main.mm("<red>Votre compte n'a pas été créé ou trouvé. :(</red>"));
            }

        }).schedule();

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
        return invocation.source().hasPermission("mgbungee.mute");
    }
}
