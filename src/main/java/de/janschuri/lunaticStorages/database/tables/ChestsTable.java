package de.janschuri.lunaticStorages.database.tables;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.utils.Utils;
import de.janschuri.lunaticlib.common.database.Datatype;
import de.janschuri.lunaticlib.common.database.Error;
import de.janschuri.lunaticlib.common.database.Table;
import de.janschuri.lunaticlib.common.database.columns.Column;
import de.janschuri.lunaticlib.common.database.columns.PrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public final class ChestsTable {


    private static final String NAME = "chests";
    private static final PrimaryKey PRIMARY_KEY = new PrimaryKey("id", Datatype.INTEGER, true);

    private static final Column[] COLUMNS = {
            new Column("world", Datatype.VARCHAR),
            new Column("coords", Datatype.VARCHAR),
    };

    private static final Table TABLE = new Table(NAME, PRIMARY_KEY, COLUMNS);

    private ChestsTable() {
    }

    public static Table getTable() {
        return TABLE;
    }

    public static Connection getSQLConnection() {
        return Database.getDatabase().getSQLConnection();
    }

    public static int getChestID(String world, String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + NAME + " WHERE world = ? AND coords = ?;");
            ps.setString(1, world);
            ps.setString(2, coords);

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("coords").equals(coords)){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("id"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
        return 0;
    }

    public static int[] getChestCoords(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + NAME + " WHERE id = '"+id+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){

                    return Utils.parseCoords(rs.getString("coords"));
                }
            }
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
        return null;
    }

    public static boolean isChestInDatabase(String world, String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM " + NAME + " WHERE world = ? AND coords = ?;");
            ps.setString(1, world);
            ps.setString(2, coords);

            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // If count > 0, the UUID exists in the database
            }
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
        return false;
    }

    public static boolean isChestInDatabase(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT COUNT(*) AS count FROM " + NAME + " WHERE id = ?;");
            ps.setInt(1, id);

            rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
        return false;
    }

    public static void saveChestData(String world, String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + NAME + " (world,coords) VALUES(?,?)");
            ps.setString(1, world);
            ps.setString(2, coords);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
        return;
    }

    public static void removeChest(String world, String coords) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + NAME + " WHERE world = ? AND coords = ?");
            ps.setString(1, world);
            ps.setString(2, coords);
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            Error.execute(ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                Error.close(ex);
            }
        }
    }
}
