package co.mcsky.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RestUtil {

    private static final Map<UUID, UserData> restData = new HashMap<>();

    /**
     * Uses the cool down of given user. In other words, this method updates the
     * last use time to {@code now} for given user.
     *
     * @param user The user who uses the coolDown
     */
    public static void use(UUID user) {
        if (restData.get(user) != null) {
            restData.get(user).lastUsedTime = System.currentTimeMillis();
        } else {
            throw new NullPointerException("specific coolDown key doesn't exist");
        }
    }

    /**
     * Check if the coolDown of given user is ready.
     *
     * @param user     The user who uses the coolDown
     * @param coolDown The defined coolDown duration (in second)
     *
     * @return True if the coolDown is ready (i.e. by design, user can again
     * uses the functionality corresponding to the cool down).
     */
    public static boolean check(UUID user, int coolDown) {
        if (restData.containsKey(user)) {
            return diff(user) > restData.get(user).coolDown;
        } else {
            create(user, coolDown);
            return true;
        }
    }

    /**
     * Check the remaining time for the coolDown to be ready.
     *
     * @param user     The user to be checked
     * @param coolDown The defined coolDown duration in second
     *
     * @return The time which has to pass before the coolDown is ready.
     */
    public static long remaining(UUID user, int coolDown) {
        if (!restData.containsKey(user)) {
            create(user, coolDown);
        }
        return restData.get(user).coolDown - diff(user);
    }

    /**
     * Reset the cool down of given user (i.e. forcing to let the cool down be
     * ready)
     *
     * @param user The user to be reset cooldown
     */
    public static void reset(UUID user) {
        if (restData.get(user) != null)
            restData.get(user).lastUsedTime = 0;
    }

    /**
     * Reset all cool down data.
     */
    public static void resetAll() {
        restData.clear();
    }

    private static long diff(UUID user) {
        UserData userData = restData.get(user);
        long now = System.currentTimeMillis();
        long lastUsedTime = userData.lastUsedTime;
        return TimeUnit.MILLISECONDS.toSeconds(now - lastUsedTime);
    }

    private static void create(UUID user, int coolDown) {
        restData.put(user, new UserData(coolDown));
    }

    private static class UserData {

        /**
         * The duration (in second) player has to wait for
         */
        final int coolDown;
        /**
         * The time (in millisecond) when the player last used it.
         */
        long lastUsedTime;

        UserData(int coolDown) {
            this.coolDown = coolDown;
            this.lastUsedTime = 0;
        }

    }

}
