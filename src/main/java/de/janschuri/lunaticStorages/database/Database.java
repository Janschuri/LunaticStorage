package de.janschuri.lunaticStorages.database;

import de.janschuri.lunaticStorages.Main;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Arrays;
import java.util.logging.Level;


public abstract class Database {
    Main plugin;
    Connection connection;
    public String chests = "chests";
    public String panels = "panels";
    public Database(Main instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement psChests = connection.prepareStatement("SELECT * FROM " + chests);
            ResultSet rsChests = psChests.executeQuery();
            close(psChests,rsChests);

            PreparedStatement psPanels = connection.prepareStatement("SELECT * FROM " + panels);
            ResultSet rsPanels = psPanels.executeQuery();
            close(psPanels,rsPanels);

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    public int getChestID(String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + chests + " WHERE coords = '"+coords+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("coords").equals(coords)){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("id"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }
    public String getChestCoords(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + chests + " WHERE id = '"+id+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getString("coords");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public boolean isChestInDatabase(String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM " + chests + " WHERE coords = ?;");
            ps.setString(1, coords);

            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // If count > 0, the UUID exists in the database
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return false;
    }

    public boolean isPanelInDatabase(String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM " + panels + " WHERE coords = ?;");
            ps.setString(1, coords);

            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // If count > 0, the UUID exists in the database
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return false;
    }

    public byte[] getPanelsStorageItem(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + panels + " WHERE id = '"+id+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getBytes("storageItem");
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    public int getPanelsID(String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + panels + " WHERE coords = '"+coords+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("coords").equals(coords)){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("id"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }

    public void saveChestData(int id, String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + chests + " (id,coords) VALUES(?,?)");
            ps.setInt(1, id);
            ps.setString(2, coords);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;
    }

    public void saveChestData(String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + chests + " (coords) VALUES(?)");
            ps.setString(1, coords);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;
    }

    public void savePanelsData(int id, byte[] storageItem) {
        Connection conn = null;
        PreparedStatement ps = null;
        Bukkit.getLogger().info("Save: " + Arrays.toString(storageItem));
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + panels + " SET storageItem = ? WHERE id = ?");
            ps.setBytes(1, storageItem);
            ps.setInt(2, id);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;
    }

    public void savePanelsData(String coords, byte[] storageItem) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + panels + " (coords,storageItem) VALUES(?,?)");
            ps.setString(1, coords);
            ps.setBytes(2, storageItem);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return;
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}
