package fr.pandamcbe.mgbungee.redisstuff;

import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.commons.PenaltyAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.MyMGSql;
import fr.pandamcbe.mymgsql.core.rediscache.CacheProvider;
import fr.pandamcbe.mymgsql.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
import fr.pandamcbe.mymgsql.data.management.redis.RedisAccess;

import java.sql.SQLException;
import java.util.*;

public class LoadRedisAccounts {
    public static void LoadTheseRedisAccounts(Player player) throws AccountNotFoundException, SQLException {
        //Faire en sorte de faire une boucle qui charge tous les comptes d'un coup
        final CallAccountsFromRedis connections = new CallAccountsFromRedis();
        RedisAccess redisAccess = connections.getRedisAccess();
        UUID uuid = player.getUniqueId();
        CacheProvider cacheProvider = new CacheProvider();
        Map<String, Object> accounts = new HashMap<>();

        Set<String> Rbucketkeys = new HashSet<>();
        Rbucketkeys.add(AllNames.PROFILKEY.getName());
        Rbucketkeys.add(AllNames.WORNKEY.getName());
        Rbucketkeys.add(AllNames.FFAKITSKEY.getName());
        MyMGSql.getInstance().getOnlineServerNames().forEach(name -> Rbucketkeys.add(AllNames.statsKey(name)));
        Rbucketkeys.forEach(keysName -> accounts.put(keysName, cacheProvider.getRBucketAccountFromRedis(redisAccess, keysName, uuid)));

        final PenaltyAccount penalityAccount = (PenaltyAccount) connections.getAccount(uuid, AllNames.PENALTY.getName());
        accounts.put(AllNames.cosmeKey(AllNames.SOUND_ID.getName()), cacheProvider.getRSetAccountFromRedis(redisAccess, AllNames.cosmeKey(AllNames.SOUND_ID.getName()), uuid));

        connections.AccountExists(uuid, accounts);

        if (Main.INSTANCE.redisMuteManager.isMuted(uuid))
            new CacheProvider().sendRBucketAccountToRedis(connections.getRedisAccess(), AllNames.PENALTY.getName(), uuid, penalityAccount);
    }
}