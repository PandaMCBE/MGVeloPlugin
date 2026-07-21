package fr.pandamcbe.mgbungee.redisstuff;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fr.pandamcbe.commons.FFAKitsAccount;
import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.commons.WornAccount;
import fr.pandamcbe.mymgsql.core.rediscache.CallAccountsFromRedis;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.redis.RedisAccess;
import fr.pandamcbe.mymgsql.data.management.redis.RedisMessage;
import fr.pandamcbe.mymgsql.data.management.savingdatatask.PushMemoryToRedis;
import org.redisson.api.RBatch;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class RedisPbSb {

    private RedisAccess redisAccess;

    public RedisPbSb() {
        this.redisAccess = RedisAccess.INSTANCE;
    }

    public void publishMessage(String channel, String message) {
        if (redisAccess == null) {
            System.out.println("Erreur: RedisAccess est null !");
            return;
        }

        RedissonClient redisson = redisAccess.getRedissonClient();

        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }
        RTopic topic = redisson.getTopic(channel);
        topic.publishAsync(message);
    }

    public void SubToChannels() {
        System.out.println("Subscription Processing...");
        RedissonClient redisson = redisAccess.getRedissonClient();
        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }

        String channel = AllNames.SAVESIGNAL.getName();

        RTopic topic = redisson.getTopic(channel);
        topic.addListener(String.class, (channelName, message) ->  handleRedisMessage(message));

        System.out.println("Subscription method finished");
    }

    private void handleRedisMessage(String message) {
//        MyMGSql main = MyMGSql.getInstance();
        PushMemoryToRedis pushMemoryToRedis = new PushMemoryToRedis();
        Gson gson = new Gson();
        System.out.println(message);
        RedisMessage redisMessage = gson.fromJson(message, RedisMessage.class);

            Map<String, String> mainMap = redisMessage.getMainMapAccounts();
            if (!mainMap.isEmpty()) {
                RBatch rBatch = redisAccess.getRedissonClient().createBatch();
                for (String uuidString : mainMap.keySet()) {
                        Type type = new TypeToken<Map<String, JsonObject>>(){}.getType();
                        Map<String, JsonObject> secondMap = gson.fromJson(mainMap.get(uuidString), type);
                        UUID uuid = UUID.fromString(uuidString);
//                        if (secondMap.isEmpty()) continue;

                        for (String accountNames : secondMap.keySet()) {
                            String newAccountName = CallAccountsFromRedis.ifContainsCategory(accountNames);
                            AllNames allNames = AllNames.fromName(newAccountName);
                            if (allNames != null)
                                switch (allNames) {
                                    case STATSPOJO:
                                        String[] serverName = accountNames.split(AllNames.SPLITCG.getName());
                                        StatsAccount statsAccount = gson.fromJson(secondMap.get(accountNames), StatsAccount.class);
                                        pushMemoryToRedis.pushMStats(rBatch, uuid, statsAccount, serverName[1]);
                                        break;
                                    case WORNPOJO:
                                        WornAccount wornAccount = gson.fromJson(secondMap.get(accountNames), WornAccount.class);
                                        pushMemoryToRedis.pushMWorn(rBatch, uuid, wornAccount);
                                        break;
                                    case FFAKITSPOJO:
                                        FFAKitsAccount ffaKitsAccount = gson.fromJson(secondMap.get(accountNames), FFAKitsAccount.class);
                                        pushMemoryToRedis.pushMKits(rBatch, uuid, ffaKitsAccount);
                                        break;
                                    case PROFILPOJO:
                                        ProfilAccount profilAccount = gson.fromJson(secondMap.get(accountNames), ProfilAccount.class);
                                        pushMemoryToRedis.pushMProfil(rBatch, uuid, profilAccount);
                                        break;
                                    default:
                                        break;
                                }
                        }
                }
                rBatch.execute();
            }

    }
}