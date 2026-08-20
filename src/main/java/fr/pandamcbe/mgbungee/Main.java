package fr.pandamcbe.mgbungee;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.pandamcbe.mgbungee.commands.general.HubCommands;
import fr.pandamcbe.mgbungee.commands.general.RulesCmd;
import fr.pandamcbe.mgbungee.commands.general.StatsCommand;
import fr.pandamcbe.mgbungee.commands.penalities.*;
import fr.pandamcbe.mgbungee.commands.privatemessage.MsgCommand;
import fr.pandamcbe.mgbungee.commands.privatemessage.MsgRCommand;
import fr.pandamcbe.mgbungee.listeners.BungeeListener;
import fr.pandamcbe.mgbungee.mysql.SQLBanIpManager;
import fr.pandamcbe.mgbungee.mysql.SQLBanManager;
import fr.pandamcbe.mgbungee.redisstuff.MuteManager;
import fr.pandamcbe.mgbungee.redisstuff.RedisPbSb;
import fr.pandamcbe.mgbungee.tasks.PubliciteTask;
import fr.pandamcbe.mgbungee.tasks.SavingDatas;
import fr.pandamcbe.mymgsql.MyMGSql;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Plugin(id = "mgnungee", name = "MGBungee", version = "1.0",
        url = "fr.pandamcbe", description = "proxy plugin", authors = {"PandaMCBE"},dependencies = {
        @Dependency(id = "mymgsql")
})
public final class Main{

    public static Main INSTANCE;

    public static Map<Player, Player> message = new HashMap<Player, Player>();

    public PubliciteTask task;
//    public AccountProvider accountProvider;
    public MuteManager redisMuteManager;
    final public SQLBanManager banManager = new SQLBanManager();
    final public SQLBanIpManager banIpManager = new SQLBanIpManager();
    public RedisPbSb redisPbSb;
    public NetworkState networkState;
    public final ProxyServer server;
    private final Logger logger;
    private final SavingDatas savingDatas = new SavingDatas();

    @Inject
    public Main(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
        logger.info("§2[DEBUG] MGBungee chargé !");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        INSTANCE = this;
        // Do some operation demanding access to the Velocity API here.
        // For instance, we could register an event:
        CommandsInitialization();
        redisPbSb = new RedisPbSb();
        redisPbSb.SubToChannels();
        networkState = new NetworkState();
        MyMGSql.getInstance().redisInteractionManager.sendRSetToRedis("ServerNames-List",MyMGSql.getInstance().getOnlineServerNames(), Duration.ZERO);
        networkState.setServerNames(MyMGSql.getInstance().getOnlineServerNames());
        redisMuteManager = new MuteManager();
        savingDatas.saveIntoSql();
        server.getEventManager().register(this, new BungeeListener());
        task = new PubliciteTask(this);
        task.startTask();

    }

    @Subscribe
    public void onProxyStopping(ProxyShutdownEvent event){
    }

    public static Component mm(String miniMessageString) { // mm, short for MiniMessage
        return MiniMessage.miniMessage().deserialize(miniMessageString);
    }

    private void CommandsInitialization(){
        CommandManager commandManager = server.getCommandManager();
        CommandMeta banMeta = commandManager.metaBuilder("ban").plugin(this).build();
        CommandMeta banIpMeta = commandManager.metaBuilder("ban-ip").plugin(this).build();
        CommandMeta unbanMeta = commandManager.metaBuilder("unban").plugin(this).build();
        CommandMeta unbanIpMeta = commandManager.metaBuilder("unbanIp").plugin(this).build();
        CommandMeta muteMeta = commandManager.metaBuilder("mute").plugin(this).build();
        CommandMeta unmuteMeta = commandManager.metaBuilder("unmute").plugin(this).build();
        CommandMeta msgMeta = commandManager.metaBuilder("msg").aliases("w", "mp").plugin(this).build();
        CommandMeta msgRMeta = commandManager.metaBuilder("msgR").aliases("r", "respond").plugin(this).build();
        CommandMeta rulesMeta = commandManager.metaBuilder("rules").aliases("rl", "règles", "rgl").plugin(this).build();
        CommandMeta hubMeta = commandManager.metaBuilder("hub").aliases("lobby", "spawn").plugin(this).build();
        CommandMeta statsMeta = commandManager.metaBuilder("stats").aliases( "sts").plugin(this).build();
        SimpleCommand banCmd = new BanCommand();
        SimpleCommand banIpCmd = new BanIpCommand();
        SimpleCommand unbanCmd = new UnBanCommand();
        SimpleCommand unbanIpCmd = new UnBanIpCommand();
        SimpleCommand muteCmd = new MuteCommand();
        SimpleCommand unmuteCmd = new UnMuteCommand();
        SimpleCommand msgCmd = new MsgCommand();
        SimpleCommand msgRCmd = new MsgRCommand();
        SimpleCommand statsCmd = new StatsCommand();
        RawCommand rulesCmd = new RulesCmd();
        RawCommand hubCmd = new HubCommands();
        commandManager.register(banMeta, banCmd);
        commandManager.register(banIpMeta, banIpCmd);
        commandManager.register(unbanMeta, unbanCmd);
        commandManager.register(unbanIpMeta, unbanIpCmd);
        commandManager.register(muteMeta, muteCmd);
        commandManager.register(unmuteMeta, unmuteCmd);
        commandManager.register(msgMeta, msgCmd);
        commandManager.register(msgRMeta, msgRCmd);
        commandManager.register(rulesMeta, rulesCmd);
        commandManager.register(hubMeta, hubCmd);
        commandManager.register(statsMeta, statsCmd);
    }

    public static Main getINSTANCE() {
        return INSTANCE;
    }

    public Logger getLogger() {
        return logger;
    }

    public void getOnlineServersPlayersCount(){
        for(String name : MyMGSql.getInstance().getOnlineServerNames()) {
            if(server.getServer(name).isPresent()) {
                RegisteredServer registeredServer = server.getServer(name).get();
                networkState.getServersPlayerCount().put(name, registeredServer.getPlayersConnected().size());
            }
        }
    }
}