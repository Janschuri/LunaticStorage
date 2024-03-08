package de.janschuri.lunaticStorages.database;

import de.janschuri.lunaticStorages.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class MySQL extends Database {
    String host, database, username, password;
    int port;

    public MySQL(Main instance) {
        super(instance);
        host = plugin.getConfig().getString("Database.MySQL.Host", "localhost");
        port = plugin.getConfig().getInt("Database.MySQL.Port", 3306);
        database = plugin.getConfig().getString("Database.MySQL.Database", "lunaticstorage");
        username = plugin.getConfig().getString("Database.MySQL.Username", "root");
        password = plugin.getConfig().getString("Database.MySQL.Password", "");
    }

    public Connection getSQLConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "MySQL exception on initialize", ex);
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(MySQLCreateChestsTable);
            s.executeUpdate(MySQLCreatePanelsTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }

    String MySQLCreateChestsTable = "CREATE TABLE IF NOT EXISTS chests (" +
            "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "`coords` varchar(36) NOT NULL" +
            ") AUTO_INCREMENT=1;";

    String MySQLCreatePanelsTable = "CREATE TABLE IF NOT EXISTS panels (" +
            "`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "`coords` varchar(36) NOT NULL," +
            "`storageItem` varbinary(1000)" +
            ") AUTO_INCREMENT=1;";
}

