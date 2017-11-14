package cashe;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dmitrii Stoianov
 */


public class SessionCache {
    private static SessionCache ourInstance = new SessionCache();

    public static SessionCache getInstance() {
        return ourInstance;
    }

    private ConcurrentHashMap<String, String> sessions;

    private SessionCache() {
        sessions = new ConcurrentHashMap<>();
    }

    public String getUsernameBySession(String session) {
        return sessions.get(session);
    }

    public void addSession(String session, String username) {
        sessions.put(session, username);
    }

}
