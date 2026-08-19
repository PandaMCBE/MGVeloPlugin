package fr.pandamcbe.mgbungee.commands.general;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.redis.RedisMessage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HubCommands implements RawCommand {

    ProxyServer proxyServer = Main.INSTANCE.server;

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        Player player = (Player) source;
        if(player.getCurrentServer().isPresent()) {
            if (player.getCurrentServer().get().getServerInfo().getName().equalsIgnoreCase(AllNames.LOBBY.getName())) {
                Main.getINSTANCE().redisPbSb.publishMessage(AllNames.TELEPORTSPAWN.getName(), new RedisMessage(player.getUniqueId()));
            } else {
                Optional<RegisteredServer> server = proxyServer.getServer(AllNames.LOBBY.getName());
                server.ifPresent(targetServer -> player.createConnectionRequest(targetServer).connect());
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return RawCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return RawCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return RawCommand.super.hasPermission(invocation);
    }
}