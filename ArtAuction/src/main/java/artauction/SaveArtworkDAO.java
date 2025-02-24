package artauction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveArtworkDAO extends DAO {
    public boolean favoriteArtwork(int userID, int artworkID) {
        loadDriver(dbdriver);

        String sql = "INSERT into favorite (userID, artworkID) VALUES (?,?)";

        try (

                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userID);
            ps.setInt(2, artworkID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSave(int userID, int artworkID) {
        loadDriver(dbdriver);

        String sql = "DELETE FROM favorite WHERE userID = ? AND artworkID = ?";

        try (

                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {
            ps.setInt(1, userID);
            ps.setInt(2, artworkID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkSave(int userID, int artworkID) {
        loadDriver(dbdriver);
        String sql = "SELECT COUNT(*) FROM favorite WHERE userID = ? AND artworkID = ?";

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setInt(2, artworkID);

            var rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}