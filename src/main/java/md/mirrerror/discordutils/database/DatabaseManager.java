package md.mirrerror.discordutils.database;

import java.sql.Connection;
import java.util.UUID;

public interface DatabaseManager {
    void connect();
    Connection getConnection();

    void setupTable();
    void registerPlayer(UUID uuid, long userId, boolean twoFactor);
    void unregisterPlayer(UUID uuid);
    boolean playerExists(UUID uuid);

    boolean userLinked(long userId);
    UUID getPlayer(long userId);

    void setTwoFactor(UUID uuid, boolean twoFactor);
    boolean hasTwoFactor(UUID uuid);

    void setUserId(UUID uuid, long userId);
    long getUserId(UUID uuid);
}
