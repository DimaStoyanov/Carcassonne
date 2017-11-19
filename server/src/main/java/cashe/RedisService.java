package cashe;

import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * Created by Dmitrii Stoianov
 */


public class RedisService {

    private static RMapCache<String, String> sessionsCache;
    private static RSetCache<String> emailRequests;
    private static RMapCache<String, Integer> ipAddresses;

    private static RedisService ourInstance = new RedisService();

    public static RedisService getInstance() {
        return ourInstance;
    }


    private RedisService() {
        RedissonClient redisson = Redisson.create();
        sessionsCache = redisson.getMapCache("sessions");
        emailRequests = redisson.getSetCache("email_requests");
        ipAddresses = redisson.getMapCache("ip_addresses");
    }


    /**
     * Check if the user has requested a message in last 30 seconds
     *
     * @param username - name of player
     * @return True if time between last request is less then 30 sec, otherwise true
     */
    public boolean isUserAlreadyRequested(String username) {
        return emailRequests.contains(username);
    }

    /**
     * Add note about last request of user.
     * It will be automatically delete after 30 seconds.
     *
     * @param username - name of player
     */
    public void addEmailRequest(String username) {
        emailRequests.addAsync(username, 30, TimeUnit.SECONDS);
    }


    public int getAndUpdateCountIPRequests(String ip) {
        Integer count = ipAddresses.get(ip);
        count = count == null ? 0 : count + 1;
        ipAddresses.fastPutAsync(ip, count, 1, TimeUnit.SECONDS);
        return count;
    }

    /**
     * Add sessions id to cache for one day.
     *
     * @param session  - id of player session
     * @param username - name of player
     */
    public void addSession(String session, String username) {
        sessionsCache.fastPutAsync(session, username, 1, TimeUnit.DAYS);
    }

    /**
     * @param session - id of player session
     * @return name of player
     */
    public String getUsernameBySession(String session) {
        return sessionsCache.get(session);
    }


}
