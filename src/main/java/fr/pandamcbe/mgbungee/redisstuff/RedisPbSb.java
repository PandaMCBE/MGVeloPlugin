package fr.pandamcbe.mgbungee.redisstuff;

import com.google.gson.Gson;
import fr.pandamcbe.mymgsql.core.rediscache.RedisAccountsManager;
import fr.pandamcbe.mymgsql.core.rediscache.RedisInteractionManager;
import fr.pandamcbe.mymgsql.data.management.enums_strings.AllNames;
import fr.pandamcbe.mymgsql.data.management.redis.RedisAccess;
import fr.pandamcbe.mymgsql.data.management.redis.RedisMessage;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

public class RedisPbSb {

    final private RedisAccountsManager redisAccountsManager = new RedisAccountsManager();
    final private RedisInteractionManager redisInteractionManager = new RedisInteractionManager();
    final private RedisAccess redisAccess = redisInteractionManager.getRedisAccess();
    final private Gson gson = new Gson();

    public void publishMessage(String channel, Object message) {

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
        topic.publishAsync(gson.toJson(message));
    }


    public void subChannels() {
        System.out.println("Subscription Processing...");
        RedissonClient redisson = RedisAccess.INSTANCE.getRedissonClient();
        if (redisson == null) {
            System.out.println("Erreur: RedissonClient est null !");
            return;
        }
        String[] channels = {};

        for(String channel : channels) {
            RTopic topic = redisson.getTopic(channel);
            topic.addListener(String.class, (channelName, message) -> handleRedisMessage(channelName.toString(), message));
        }

        System.out.println("Subscription method finished");
    }

    public void handleRedisMessage(String channelName, String message) {
        AllNames allNames = AllNames.fromName(channelName);
        RedisMessage redisMessage = gson.fromJson(message, RedisMessage.class);
        if(allNames != null){

        }
    }
}