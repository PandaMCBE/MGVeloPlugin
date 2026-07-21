package fr.pandamcbe.mgbungee.tasks;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.mgbungee.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;


import java.util.concurrent.TimeUnit;

public class PubliciteTask {

    private final Main server;

    public PubliciteTask(Main server) {
        this.server = server;
    }

    public void startTask() {
        Main.getINSTANCE().server.getScheduler().buildTask(server, () -> {
            for (Player player : Main.getINSTANCE().server.getAllPlayers()) {
                sendAdvertisement(player);
            }
        }).repeat(20 * 60, TimeUnit.SECONDS).schedule();
    }

    private void sendAdvertisement(Player player) {
        //Discord :
        Component component = Main.mm("<bold><blue><italic>Notre </blue><dark_aqua>Discord</bold>")
                .clickEvent(ClickEvent.openUrl("https://discord.gg/Rb47GF673w"));
        //Youtube :
        Component component2 = Main.mm("<bold><italic><dark_red>Notre</dark_red> <white>chaîne <red>Youtube</italic></bold>")
                .clickEvent(ClickEvent.openUrl("https://www.youtube.com/@Mg-LandServeur"));

        player.sendMessage(Main.mm("<reset>"));
        player.sendMessage(Main.mm("<bold><green>--------------------------</green></bold>"));
        player.sendMessage(Main.mm("<reset>"));
        player.sendMessage(component);
        player.sendMessage(Main.mm("<reset>"));
        player.sendMessage(component2);
        player.sendMessage(Main.mm("<reset>"));
        player.sendMessage(Main.mm("<bold><green>--------------------------</green></bold>"));
    }
}