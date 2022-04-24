package md.mirrerror.discordutils.database;

import md.mirrerror.discordutils.Main;

import java.sql.*;
import java.util.UUID;

public class MySQLManager implements DatabaseManager {

    private Connection connection;

    public MySQLManager() {
        connect();
    }

    @Override
    public void connect() {
        String host = Main.getInstance().getConfigManager().getConfig().getString("Database.Host");
        int port = Main.getInstance().getConfigManager().getConfig().getInt("Database.Port");
        String database = Main.getInstance().getConfigManager().getConfig().getString("Database.Database");
        String username = Main.getInstance().getConfigManager().getConfig().getString("Database.Username");
        String password = Main.getInstance().getConfigManager().getConfig().getString("Database.Password");

        try {
            if (getConnection() != null && !getConnection().isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            setupTable();
        } catch (SQLException | ClassNotFoundException ignored) {
            Main.getInstance().getLogger().severe("Something went wrong while connecting to the database! Check your settings!");
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void setupTable() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players (uuid varchar(255), userId bigint, twoFactor boolean);");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while setting up the database table!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    @Override
    public void registerPlayer(UUID uuid, long userId, boolean twoFactor) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, userId, twoFactor) VALUES (?,?,?)");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setLong(2, userId);
            preparedStatement.setBoolean(3, twoFactor);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while registering a player in the database!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    @Override
    public void unregisterPlayer(UUID uuid) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM players WHERE uuid=?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while unregistering a player from the database!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    @Override
    public boolean playerExists(UUID uuid) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while checking if a player is registered in the database!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
        return false;
    }

    @Override
    public boolean userLinked(long userId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE userId=?");
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while checking if a player is verified (database)!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
        return false;
    }

    @Override
    public UUID getPlayer(long userId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE userId=?");
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return UUID.fromString(resultSet.getString("uuid"));
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while getting a player from the database!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
        return null;
    }

    @Override
    public void setTwoFactor(UUID uuid, boolean twoFactor) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET twoFactor=? WHERE uuid=?");
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setBoolean(1, twoFactor);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while changing a player's 2FA settings!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    @Override
    public boolean hasTwoFactor(UUID uuid) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getBoolean("twoFactor");
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while checking if a player has 2FA enabled (database)!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
        return false;
    }

    @Override
    public void setUserId(UUID uuid, long userId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET userId=? WHERE uuid=?");
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while setting a player's Discord user ID (database)!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
    }

    @Override
    public long getUserId(UUID uuid) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getLong("userId");
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("Something went wrong while getting a player's Discord user ID (database)!");
            Main.getInstance().getLogger().severe("Cause: " + e.getCause() + "; message: " + e.getMessage() + ".");
        }
        return -1;
    }
}
