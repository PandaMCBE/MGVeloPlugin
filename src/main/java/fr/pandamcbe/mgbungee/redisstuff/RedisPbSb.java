package fr.pandamcbe.mgbungee.redisstuff;

import com.google.gson.Gson;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mymgsql.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mymgsql.core.rediscache.RedisInteractionManager;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AccountType;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.redis.RedisAccess;
import org.redisson.api.RBatch;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RedisPbSb {

    final private RedisAccountsManager redisAccountsManager = new RedisAccountsManager();
    final private RedisInteractionManager redisInteractionManager = new RedisInteractionManager();
    private final RedisAccess redisAccess = redisInteractionManager.getRedisAccess();

    public void publishMessage(String channel, Object message) {

        if (redisAccess == null) {
            System.out.println("Erreur: RedisAccess est null !");
            return;
        }
        Gson gson = new Gson();
        RedissonClient redisson = redisAccess.getRedissonClient();

        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }
        RTopic topic = redisson.getTopic(channel);
        topic.publishAsync(gson.toJson(message));
    }

    public void SubToChannels() {
        System.out.println("Subscription Processing...");
        RedissonClient redisson = redisAccess.getRedissonClient();
        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }

        String channel = AllNames.PROXY.getName();

        RTopic topic = redisson.getTopic(channel);
        topic.addListener(String.class, (channelName, message) ->  {
            CompletableFuture.runAsync(() -> {
                try {
//                    handleRedisMessage(message, redisAccess);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        System.out.println("Subscription method finished");
    }

//    private void handleRedisMessage(String message, RedisAccess redisAccess) {
////        MyMGSql main = MyMGSql.getInstance();
//        CompletableFuture.runAsync(() -> {
//
//            Gson gson = new Gson();
//            RedisMessage redisMessage = gson.fromJson(message, RedisMessage.class);
//            System.out.println("Message Handling...");
//            Map<String, String> mainMap = redisMessage.getMainMapAccounts();
//            if (!mainMap.isEmpty()) {
//                RBatch rBatch = redisAccess.getRedissonClient().createBatch();
//                for (String uuidString : mainMap.keySet()) {
//                    Type type = new TypeToken<Map<String, JsonObject>>() {
//                    }.getType();
//                    Map<String, JsonObject> secondMap = gson.fromJson(mainMap.get(uuidString), type);
//                    System.out.println("Debugging : " + secondMap.entrySet());
//                    UUID uuid = UUID.fromString(uuidString);
//                    if (secondMap.isEmpty()) return;
//                    secondMap.keySet().forEach(name -> pushAccounts(rBatch, uuid, name, secondMap.get(name)));
//                    System.out.println("ça marche ?");
//
//                }
//                System.out.println("Rbatch ?");
//                BatchResult<?> result = rBatch.execute();
//                System.out.println(result.getResponses());
//
//            }
//        });
//
//    }

    public void pushAccounts(RBatch rBatch, UUID uuid, String name, Object account){
        if(RedisAccountsManager.ifContainsCategory(name).equalsIgnoreCase(AccountType.GAMESSTATS.getKeyPrefix())) {
            String[] serverName = name.split(AllNames.SPLITCG.getName());
            StatsAccount statsAccount = (StatsAccount) account;
            StatsAccount stats = (StatsAccount) redisAccountsManager.getStatsAccount(uuid, AccountType.statsKey(serverName[1]));
            stats.setKills(stats.getKills()+ statsAccount.getKills());
            stats.setKillstreaks(stats.getKillstreaks() + statsAccount.getKillstreaks());
        } else {
            redisInteractionManager.sendRBucketAsyncToRedis(rBatch, name+AllNames.SEPMARK.getName()+uuid.toString(), account, Duration.ofHours(2));
        }

    }
}