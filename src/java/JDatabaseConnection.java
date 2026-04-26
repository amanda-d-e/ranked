import java.sql.*;

// modified from https://www.youtube.com/watch?v=9ntKSLLDeSs

public class JDatabaseConnection {
    static final String url = "jdbc:mysql://127.0.0.1:3307/ranked";
    static final String user = "root";
    static final String password = "p9J4VrT%R#HRV^tvzF";

    public JDatabaseConnection() {

    }

    public static boolean moveRank(String title, int destinationRank){
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
                if (oldRank > destinationRank) {
                    System.out.println("oldRank: " + oldRank + ", destinationRank: " + destinationRank);
                    System.out.println("Calling moveRank(" + oldTitle + ", " + (destinationRank + 1) + ")");
                    return moveRank(oldTitle, destinationRank + 1);
                } else if (oldRank < destinationRank) {
                    System.out.println("oldRank: " + oldRank + ", destinationRank: " + destinationRank);
                    System.out.println("Calling moveRank(" + oldTitle + ", " + (destinationRank - 1) + ")");
                    return moveRank(oldTitle, destinationRank - 1);
                }
                System.out.println("Broke out of if statement");

            } else {
                return true;
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        boolean moveRankSuccessful = moveRank("Mob Psycho 100", 2);

        if (moveRankSuccessful) {
            System.out.println("The rank was changed successfully");
        } else {
            System.out.println("There was a problem changing the rank");
        }
        // Before
//        1. Hunter X Hunter
//        2. Nichijou
//        3. Mob Psycho 100

        // Expected
//        1. Hunter X Hunter
//        2. Mob Psycho 100
//        3. Nichijou


        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, user, password);

            Statement select = connection.createStatement();
            Statement insert = connection.createStatement();
            Statement update = connection.createStatement();

            //update.execute("UPDATE Items SET `rank`=1 WHERE title='Nichijou'");
            //insert.execute("INSERT INTO Items VALUES ('Mob Psycho 100', 3)");
            ResultSet selectResult = select.executeQuery("SELECT * FROM Items ORDER BY `rank`");


            while(selectResult.next()) {
                int ranking = selectResult.getInt("rank");
                String title = selectResult.getString("title");
                System.out.println(ranking + ". " + title);
            }

            selectResult.close();
            insert.close();
            select.close();
            update.close();
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
}
