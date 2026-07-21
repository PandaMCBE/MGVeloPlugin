package fr.pandamcbe.mgbungee.commands.penalities;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import fr.pandamcbe.mgbungee.Main;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnBanIpCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sender.sendMessage(Main.mm("<red>/unban-ip <ip></red>"));
            return;
        }
        final String targetIp = args[0];
//        UUID targetUUID = DatabaseAccess.getUUID(targetIp);

//        String SQLIP = Main.INSTANCE.banIpManager.getIp(targetUUID);
//        String SQLIP = ProxyServer.getInstance().getPlayer(targetUUID).getAddress().getAddress().getHostAddress();
        Main.INSTANCE.server.getScheduler().buildTask(Main.INSTANCE, ()-> {
                    if (!Main.INSTANCE.banIpManager.isBannedIp(targetIp)) {
                        sender.sendMessage(Main.mm("<red>Ce joueur n'a jamais été banni ou a déjà été débanni !</red>"));
                        return;
                    }
                    Main.INSTANCE.banIpManager.unbanIp(targetIp);
                });

        sender.sendMessage(Main.mm("<green>Vous avez débanni <gold>" + targetIp + "</gold>"));
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
        return invocation.source().hasPermission("mgbungee.unbanip");
    }
}