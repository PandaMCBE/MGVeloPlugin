package fr.pandamcbe.mgbungee.commands.penalities;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mgbungee.bans.TimeUnit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanIpCommand implements SimpleCommand {

    public void helpMessage(CommandSource sender){
        sender.sendMessage(Main.mm("<red>/ban <joueur> perm <raison></red>"));
        sender.sendMessage(Main.mm("<red>/ban <joueur> <durée>:<unité> <raison></red>"));
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
        final UUID targetUUID = Main.INSTANCE.server.getPlayer(targetName).get().getUniqueId();
        final Player target = Main.INSTANCE.server.getPlayer(targetUUID).get();
        final Optional<Player> targetOptional = Main.INSTANCE.server.getPlayer(targetUUID);
        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
            if (!targetOptional.isPresent()) {
                final String SQLIP = Main.INSTANCE.banIpManager.getIp(targetUUID);
                if (Main.INSTANCE.banIpManager.isBannedIp(SQLIP)) {
                    target.sendMessage(Main.mm("<red>Ce joueur est déjà banni !</red>"));
                    return;
                }
            }
            if (targetOptional.isPresent() && Main.INSTANCE.banIpManager.isBannedIp(Main.INSTANCE.server.getPlayer(targetUUID).get().getRemoteAddress().getAddress().getHostAddress())) {
                sender.sendMessage(Main.mm("<red>Ce joueur est déjà banni !</red>"));
                return;
            }

            String reason = "";
            for (int i = 2; i < args.length; i++) {
                reason += args[i] + " ";
            }

            if (args[1].equalsIgnoreCase("perm")) {
                Main.INSTANCE.banIpManager.banIp(Main.INSTANCE.server.getPlayer(targetUUID).get().getRemoteAddress().getAddress().getHostAddress(), targetUUID, -1, reason);
                sender.sendMessage(Main.mm("<green>Vous avez banni </green><gold>" + targetName + "</gold> <red>(Permanent) </red><green>pour : </green><yellow>" + reason + "</yellow>"));
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
                sender.sendMessage(Main.mm("<red>Cette unité de temps n'existe pas !</red>"));
                for (TimeUnit units : TimeUnit.values()) {
                    sender.sendMessage(Main.mm("<aqua>" + units.getName() + "</aqua> <white>: <yellow>" + units.getShortcut() + "</yellow>"));
                }
                return;
            }

            TimeUnit unit = TimeUnit.getFromShortcut(args[1].split(":")[1]);
            long banTime = unit.getToSecond() * duration;

            Main.INSTANCE.banIpManager.banIp(Main.INSTANCE.server.getPlayer(targetUUID).get().getRemoteAddress().getAddress().getHostAddress(), targetUUID, banTime, reason);

            sender.sendMessage(Main.mm("<green>Vous avez banni </green><gold>" + targetName + "</gold> <aqua>(" + duration + " " + unit.getName() + ")</aqua> <green>pour : <yellow>" + reason + "</yellow>"));
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
        return invocation.source().hasPermission("mgbungee.banip");
    }
}

