package fr.pandamcbe.mgbungee.tasks;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.savingdatatask.BatchRequests;

import java.util.concurrent.TimeUnit;

public class SavingDatas {

    public void saveIntoSql() {
        ProxyServer server = Main.getINSTANCE().server;
        BatchRequests batchRequests = new BatchRequests();
        server.getScheduler().buildTask(Main.getINSTANCE(), ()-> {
            batchRequests.batchAllAtOnce();
            System.out.println("saving Redis to SQL");
            saveIntoSql();
        }).delay(3, TimeUnit.MINUTES).schedule();

    }

    public void saveIntoRedis(){
        ProxyServer server = Main.INSTANCE.server;
        server.getScheduler().buildTask(Main.getINSTANCE(), ()-> {
            Main.getINSTANCE().redisPbSb.publishMessage(AllNames.SAVESIGNAL.getName(), "SAVING NOW!");
            System.out.println("saving memory to Redis");
            saveIntoRedis();
    }).delay(1, TimeUnit.MINUTES).schedule();

    }
}