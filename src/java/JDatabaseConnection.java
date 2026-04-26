import java.sql.*;

// modified from https://www.youtube.com/watch?v=9ntKSLLDeSs

public class JDatabaseConnection {
    static final String url = "jdbc:mysql://127.0.0.1:3307/ranked";
    static final String user = "root";
    static final String password = "p9J4VrT%R#HRV^tvzF";

    public JDatabaseConnection() {

    }

    /// Changes the rank of an item and updates the ranks of all other items accordingly.
    ///
    /// @param title            The title of the item to be moved.
    /// @param destinationRank  The desired rank to move the item to.
    /// @param updateDownwards  For recursive calls: the current direction in which update is moving.
    ///                         Always set to false on first call of method.
    /// @return true if the rank adjustment was successful, false otherwise
    public static boolean moveRank(String title, int destinationRank, boolean updateDownwards){
        if (title.isEmpty()) {
            return true;
        }
        Connection connection = null;
        String updateString = "UPDATE Items SET `rank`=? WHERE title=?";
        String selectTitleString = "SELECT title FROM Items WHERE `rank`=?";
        String selectRankString = "SELECT `rank` FROM Items WHERE title=?";
        try {
            connection = DriverManager.getConnection(url, user, password);

            PreparedStatement selectRank = connection.prepareStatement(selectRankString);
            selectRank.setString(1, title);
            ResultSet selectRankResult = selectRank.executeQuery();
            int oldRank = -1;

            if (selectRankResult != null) {
                if (selectRankResult.next()) {
                    oldRank = selectRankResult.getInt("rank");
                    if (oldRank == destinationRank) {
                        System.out.println("Item is already at target rank");
                        selectRankResult.close();
                        selectRank.close();
                        connection.close();
                        return false;
                    }
                }
            }

            PreparedStatement selectTitle = connection.prepareStatement(selectTitleString);
            selectTitle.setInt(1, destinationRank);
            ResultSet selectTitleResult = selectTitle.executeQuery();

            PreparedStatement updateRank = connection.prepareStatement(updateString);
            int updateSuccessful1 = 1;
            String oldTitle = "";

            if (selectTitleResult != null) {
                if (selectTitleResult.next()) {
                    oldTitle = selectTitleResult.getString("title");

                    updateRank.setInt(1, 0);
                    updateRank.setString(2, oldTitle);
                    updateSuccessful1 = updateRank.executeUpdate();
                }
            }

            updateRank.setInt(1, destinationRank);
            updateRank.setString(2, title);
            int updateSuccessful2 = updateRank.executeUpdate();

            selectTitleResult.close();
            selectRankResult.close();
            selectTitle.close();
            selectRank.close();
            connection.close();

            if (updateSuccessful1 > 0 && updateSuccessful2 > 0 && oldRank != -1) {
                if (oldRank > destinationRank || updateDownwards) {
                    return moveRank(oldTitle, destinationRank + 1, true);
                } else {
                    return moveRank(oldTitle, destinationRank - 1, false);
                }

            } else {
                return true;
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public static int getLowestRank() {
        int lowest = 0;
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, user, password);

            Statement select = connection.createStatement();

            ResultSet selectResult = select.executeQuery("SELECT COUNT(*) AS rowCount FROM Items");

            if (selectResult.next()) {
                lowest = selectResult.getInt("rowCount");
            }

            selectResult.close();
            select.close();
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return lowest;
    }

    public static boolean insertNewItem(String title, int rank) {
        Connection connection = null;
        String insertString = "INSERT INTO Items VALUES (?, ?)";

        try {
            connection = DriverManager.getConnection(url, user, password);
            PreparedStatement insert = connection.prepareStatement(insertString);

            insert.setString(1, title);
            insert.setInt(2, getLowestRank() + 1);
            int insertionSuccessful = insert.executeUpdate();
            if (getLowestRank() + 1 != rank) {
                insert.close();
                connection.close();
                return moveRank(title, rank, false);
            }

            insert.close();
            connection.close();

            if (insertionSuccessful > 0) {
                return true;
            }
        }catch(SQLIntegrityConstraintViolationException e){
            System.out.println("Item: " + title + " already exists in the database");
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public static boolean deleteItem(String title, int rank) {
        Connection connection = null;
        String deleteString = "DELETE FROM Items WHERE title=?";
        // REMOVE rank AS A PARAMETER AND GET THE RANK GIVEN THE TITLE ONLY ***

        try {
            connection = DriverManager.getConnection(url, user, password);
            PreparedStatement delete = connection.prepareStatement(deleteString);

            delete.setString(1, title);
            boolean moveSuccessful = true;
            if (getLowestRank() + 1 != rank) {
                moveSuccessful = moveRank(title, getLowestRank(), false);
            }

            int insertionSuccessful = delete.executeUpdate();

            delete.close();
            connection.close();

            if (insertionSuccessful > 0 && moveSuccessful) {
                return true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;
    }

    public static void printRankings() {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, user, password);

            Statement select = connection.createStatement();

            ResultSet selectResult = select.executeQuery("SELECT * FROM Items ORDER BY `rank`");

            while(selectResult.next()) {
                int ranking = selectResult.getInt("rank");
                String title = selectResult.getString("title");
                System.out.println(ranking + ". " + title);
            }

            selectResult.close();
            select.close();
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

/// TODO:
/// could extract a method that gets the rank of an item given its title,
/// and another one for getting the name of an item given its rank


    public static void main(String[] args) {
//        printRankings();
//
//        boolean moveRankSuccessful = moveRank("Jojo's Bizarre Adventure", 5, false);
//
//        if (moveRankSuccessful) {
//            System.out.println("The rank was changed successfully");
//        } else {
//            System.out.println("There was a problem changing the rank");
//        }

        printRankings();

        insertNewItem("Solo Leveling", 1);
        printRankings();
        deleteItem("Solo Leveling", 1);

        printRankings();

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, user, password);

            Statement insert = connection.createStatement();
            Statement update = connection.createStatement();

            //update.execute("UPDATE Items SET `rank`=1 WHERE title='Hunter X Hunter'");
            //insert.execute("INSERT INTO Items VALUES ('Frieren', 4)");

            insert.close();
            update.close();
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
}
