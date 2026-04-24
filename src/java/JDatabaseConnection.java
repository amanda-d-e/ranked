import java.sql.*;

// modified from https://www.youtube.com/watch?v=9ntKSLLDeSs

public class JDatabaseConnection {
    public static void main(String[] args) {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3307/ranked", "root", "p9J4VrT%R#HRV^tvzF"
            );

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM ITEMS");

            while(resultSet.next()) {
                int ranking = resultSet.getInt("rank");
                String title = resultSet.getString("title");
                System.out.println("Rank: " + ranking + " | Title: " + title);
            }

            resultSet.close();
            statement.close();
            connection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }

    }
}
