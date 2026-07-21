package fr.pandamcbe.mgbungee.commands.penalities;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DatabaseAccess;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnBanCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sender.sendMessage(Main.mm("<red>/unban <joueur></red>"));
            return;
        }
        final String targetName = args[0];
//        UUID targetUUID = ProxyServer.getInstance().getPlayer(targetName).getUniqueId();
        final UUID targetUUID = DatabaseAccess.getUUID(targetName);
        final Player target = Main.INSTANCE.server.getPlayer(targetUUID).get();
        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
                    if (!Main.INSTANCE.banManager.isBanned(targetUUID)) {
                        sender.sendMessage(Main.mm("<red>Ce joueur n'a jamais été banni ou a déjà été débanni !</red"));
                        return;
                    }

                    Main.INSTANCE.banManager.unban(targetUUID);
                });

        sender.sendMessage(Main.mm("<green>Vous avez débanni <gold>" + targetName + "</gold>"));
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
        return invocation.source().hasPermission("mgbungee.unban");
    }
}