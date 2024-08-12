import java.sql.*;

import com.google.gson.JsonObject;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class VerifyPassword {

    /*
     * After you update the passwords in customers table,
     *   you can use this program as an example to verify the password.
     *
     * Verify the password is simple:
     * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
     *
     * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
     *
     */
    public static void main(String[] args) throws Exception {

        System.out.println(verifyCredentials("a@email.com", "a2", "customers"));
        System.out.println(verifyCredentials("a@email.com", "a3", "customers"));

    }

    public static boolean verifyCredentials(String email, String password, String userType) throws Exception {

//        String loginUser = "mytestuser";
//        String loginPasswd = "My6$Password";
//        String loginUrl = "jdbc:mysql://localhost:3306/moviedbR";


//        Class.forName("com.mysql.jdbc.Driver").newInstance();
//        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
//        PreparedStatement statement = connection.createStatement();

        DataSource dataSource=null;
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbR");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT * from " + userType + " where email = ?\n";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, email);
            ResultSet rs = null;

//        Statement statement = connection.createStatement();
//
//        String query = String.format("SELECT * from %s where email='%s'", userType, email);
//
//        ResultSet rs = statement.executeQuery(query);


            try {
                rs = statement.executeQuery();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            //        ResultSet rs_access = statement.executeQuery(query_access);

            boolean success = false;
            if (rs.next()) {
                // get the encrypted password from the database
                String encryptedPassword = rs.getString("password");

                // use the same encryptor to compare the user input password with encrypted password stored in DB
                success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
            }

            rs.close();
            statement.close();
//            connection.close();

            System.out.println("verify " + email + " - " + password);

            return success;
        } catch (Exception e) {

            // Write error message JSON object to output
            //JsonObject jsonObject = new JsonObject();
            //jsonObject.addProperty("errorMessage", e.getMessage());
            //out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            //response.setStatus(500);
            return false;
        }

    }

}