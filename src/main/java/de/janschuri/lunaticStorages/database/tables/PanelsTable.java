package de.janschuri.lunaticStorages.database.tables;

import de.janschuri.lunaticStorages.database.Database;
import de.janschuri.lunaticStorages.utils.Logger;
import de.janschuri.lunaticlib.database.Datatype;
import de.janschuri.lunaticlib.database.Error;
import de.janschuri.lunaticlib.database.Table;
import de.janschuri.lunaticlib.database.columns.Column;
import de.janschuri.lunaticlib.database.columns.PrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class PanelsTable {

    private static final String NAME = "panels";
    private static final PrimaryKey PRIMARY_KEY = new PrimaryKey("id", Datatype.INTEGER, true);

    private static final Column[] COLUMNS = {
            new Column("world", Datatype.VARCHAR),
            new Column("coords", Datatype.VARCHAR),
            new Column("storageItem", Datatype.VARBINARY, true),
    };

    private static final Table TABLE = new Table(NAME, PRIMARY_KEY, COLUMNS);

    private PanelsTable() {
    }

    public static Table getTable() {
        return TABLE;
    }

    public static Connection getSQLConnection() {
        return Database.getDatabase().getSQLConnection();
    }

    public static boolean isPanelInDatabase(String world, String coords) {
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

    public static byte[] getPanelsStorageItem(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + NAME + " WHERE id = ?;");
            ps.setInt(1, id);

            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getInt("id") == id){
                    return rs.getBytes("storageItem");
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

    public static int getPanelsID(String world, String coords) {
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
                if(rs.getString("coords").equals(coords)){
                    return rs.getInt("id");
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

    public static void savePanelsData(int id, byte[] storageItem) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + NAME + " SET storageItem = ? WHERE id = ?");
            ps.setBytes(1, storageItem);
            ps.setInt(2, id);
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

    public static void savePanelsData(String world, String coords, byte[] storageItem) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + NAME + " (world,coords,storageItem) VALUES(?,?,?)");
            ps.setString(1, world);
            ps.setString(2, coords);
            ps.setBytes(3, storageItem);
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

    public static void removePanel(String world, String coords) {
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
