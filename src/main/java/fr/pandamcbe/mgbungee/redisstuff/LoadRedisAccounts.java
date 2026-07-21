package fr.pandamcbe.mgbungee.redisstuff;

import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.commons.PenaltyAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.core.rediscache.CacheProvider;
import fr.pandamcbe.mymgsql.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;
import fr.pandamcbe.mymgsql.data.management.redis.RedisAccess;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoadRedisAccounts {

    public static void LoadTheseRedisAccounts(Player player) throws AccountNotFoundException, SQLException {
        final CallAccountsFromRedis connections = new CallAccountsFromRedis();
        RedisAccess redisAccess = connections.getRedisAccess();
        UUID uuid = player.getUniqueId();
        CacheProvider cacheProvider = new CacheProvider();
        Map<String, Object> accounts = new HashMap<>();

        final PenaltyAccount penalityAccount = (PenaltyAccount) connections.getAccount(uuid, AllNames.PENALTY.getName());
        accounts.put(AllNames.PROFILKEY.getName(), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.PROFILKEY.getName(), uuid));
        accounts.put(AllNames.WORNKEY.getName(), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.WORNKEY.getName(), uuid));
        accounts.put(AllNames.FFAKITS.getName(), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.FFAKITSKEY.getName(), uuid));

        accounts.put(AllNames.statsKey(AllNames.CJFFA.getName()), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.statsKey(AllNames.CJFFA.getName()), uuid));
        accounts.put(AllNames.statsKey(AllNames.PFTSHOT.getName()), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.statsKey(AllNames.PFTSHOT.getName()), uuid));
        accounts.put(AllNames.statsKey(AllNames.DUELS.getName()), cacheProvider.getRBucketAccountFromRedis(redisAccess, AllNames.statsKey(AllNames.DUELS.getName()), uuid));

        accounts.put(AllNames.cosmeKey(AllNames.SOUND_ID.getName()), cacheProvider.getRSetAccountFromRedis(redisAccess, AllNames.cosmeKey(AllNames.SOUND_ID.getName()), uuid));

        connections.AccountExists(uuid, accounts);

        if (Main.INSTANCE.redisMuteManager.isMuted(uuid))
            new CacheProvider().sendRBucketAccountToRedis(connections.getRedisAccess(), AllNames.PENALTY.getName(), uuid, penalityAccount);
    }
}