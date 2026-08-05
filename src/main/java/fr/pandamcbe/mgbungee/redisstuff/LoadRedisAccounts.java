package fr.pandamcbe.mgbungee.redisstuff;

import com.velocitypowered.api.proxy.Player;
import fr.pandamcbe.commons.PenaltyAccount;
import fr.pandamcbe.mgbungee.Main;
import fr.pandamcbe.mymgsql.MyMGSql;
import fr.pandamcbe.mymgsql.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mymgsql.core.rediscache.RedisInteractionManager;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AccountType;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.exceptions.AccountNotFoundException;

import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

public class LoadRedisAccounts {
    public static void LoadTheseRedisAccounts(Player player) throws AccountNotFoundException, SQLException {
        final RedisAccountsManager connections = new RedisAccountsManager();
        RedisInteractionManager redisInteractionManager = new RedisInteractionManager();
        UUID uuid = player.getUniqueId();

        connections.ProfilAccountNullChecking(uuid, AccountType.PROFIL.getKeyPrefix());
        connections.ShopAccountNullChecking(uuid, AccountType.COSMEWORN.getKeyPrefix(), AccountType.cosmeKey(AllNames.SOUND_ID.getName()));
        MyMGSql.getInstance().getOnlineServerNames().forEach(name -> { if(name.equalsIgnoreCase(AllNames.LOBBY.getName()))
            connections.ShopAccountNullChecking(uuid, AccountType.statsKey(name));
        });
        final PenaltyAccount penalityAccount = (PenaltyAccount) connections.getProfilAccount(uuid, AccountType.PENALITIES.getKeyPrefix());


        if (Main.INSTANCE.redisMuteManager.isMuted(uuid))
            redisInteractionManager.sendRBucketToRedis(AccountType.PENALITIES.getKeyPrefix(), penalityAccount, Duration.ZERO);
    }
}