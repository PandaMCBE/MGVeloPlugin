package fr.pandamcbe.mgbungee.commands.penalities;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
import fr.pandamcbe.mymgsql.data.management.sql.databaseconnexion.DatabaseAccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UnMuteCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        // Get the arguments after the command alias
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sender.sendMessage(Main.mm("<red>/unmute <joueur></red>"));
            return;
        }
        final String targetName = args[0];
        final UUID targetUUID = DatabaseAccess.getUUID(targetName);

        final Player target = Main.INSTANCE.server.getPlayer(targetUUID).get();

        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
            try {
                if(target == null) {
                    if (!Main.INSTANCE.redisMuteManager.isMuted(targetUUID)) {
                        sender.sendMessage(Main.mm("<red>Ce joueur n'est pas mute !</red>"));
                        return;
                    }
                    Main.INSTANCE.redisMuteManager.unmute(targetUUID);
                    sender.sendMessage(Main.mm("<green>Vous avez unmute <gold>" + targetName + "</gold>"));
                } else {
                    if (Main.INSTANCE.redisMuteManager.isMutedInGame(target) == 0) {
                        sender.sendMessage(Main.mm("<red>Ce joueur n'est pas mute !</red>"));
                        return;
                    }
                    Main.INSTANCE.redisMuteManager.unmuteInGame(target);
                    sender.sendMessage(Main.mm("<green>Vous avez unmute <gold>" + targetName + "</gold>"));
                }

            }catch (AccountNotFoundException | SQLException e) {
                System.err.println(e.getMessage());
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
        return invocation.source().hasPermission("mgbungee.unmute");
    }
}
