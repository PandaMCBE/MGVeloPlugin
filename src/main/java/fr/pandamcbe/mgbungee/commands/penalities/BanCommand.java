package fr.pandamcbe.mgbungee.commands.penalities;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.bans.TimeUnit;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DatabaseAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand implements SimpleCommand {

    public void helpMessage(CommandSource sender){
        sender.sendMessage(Main.mm("<red>/ban <joueur> perm <raison> </red>"));
        sender.sendMessage(Main.mm("<red>/ban <joueur> <durée>:<unité> <raison> </red>"));
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 3) {
            helpMessage(source);
            return;
        }
        final String targetName = args[0];
        final UUID targetUUID = DatabaseAccess.getUUID(targetName);
        final Optional<Player> targetOptional = Main.INSTANCE.server.getPlayer(targetUUID);
        final Player target = Main.INSTANCE.server.getPlayer(targetUUID).get();


//            String SQLIP = Main.INSTANCE.banManager.getIp(targetUUID);

        if(!targetOptional.isPresent() && Main.INSTANCE.banManager.isBanned(targetUUID)) {
            target.sendMessage(Main.mm("<red>Ce joueur est déjà banni ! </red>"));
            return;
        }
        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
        String reason = "";
        for (int i = 2; i < args.length; i++) {
            reason += args[i] + " ";
        }

            if (args[1].equalsIgnoreCase("perm")) {

                Main.INSTANCE.banManager.ban(targetUUID, -1, reason);
                source.sendMessage(Main.mm("<green>Vous avez banni </green><gold>" + targetName + "</gold> <red>(Permanent) </red><green>pour : </green><yellow>" + reason + "</yellow>"));
                return;

            }

            if (!args[1].contains(":")) {
                helpMessage(source);
                return;
            }

            int duration = 0;
            try {
                duration = Integer.parseInt(args[1].split(":")[0]);
            } catch (NumberFormatException e) {
                source.sendMessage(Main.mm("<red>La valeur 'durée' doit être un nombre !</red>"));
                return;
            }

            if (!TimeUnit.existFromShortcut(args[1].split(":")[1])) {
                source.sendMessage(Main.mm("<red>Cette unité de temps n'existe pas !</red>"));
                for (TimeUnit units : TimeUnit.values()) {
                    source.sendMessage(Main.mm("<aqua>" + units.getName() + "</aqua> <white>: <yellow>" + units.getShortcut() + "</yellow>"));
                }
                return;
            }

            TimeUnit unit = TimeUnit.getFromShortcut(args[1].split(":")[1]);
            long banTime = unit.getToSecond() * duration;

            Main.INSTANCE.banManager.ban(targetUUID, banTime, reason);

            source.sendMessage(Main.mm("<green>Vous avez banni </green><gold>" + targetName + "</gold> <aqua>(" + duration + " " + unit.getName() + ")</aqua> <green>pour : <yellow>" + reason + "</yellow>"));
        });
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
        return invocation.source().hasPermission("mgbungee.ban");
    }
}