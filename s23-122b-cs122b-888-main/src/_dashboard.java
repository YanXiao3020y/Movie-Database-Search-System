import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "_dashboardServlet", urlPatterns = "/api/_dashboard")
public class _dashboard extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private VerifyPassword vp;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbR");
            vp = new VerifyPassword();
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json"); // Response mime type ------might need to uncomment
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        request.getSession().setAttribute("userType", "employee");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
        String errString = "";
        String msgString = "";
        String status = "";
        try{
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        }catch(Exception e){
            errString += e.getMessage();
            msgString += "Please complete ReCaptcha.";
            status = "fail";

            System.out.println("Failed Recaptcha");

        }
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);//trying checking if user is a bot

            // Declare our statement
//            Statement statement = conn.createStatement();

//            String query_access = "SELECT * FROM customers WHERE email='" + username + "'" +
//                    " AND password='" + password + "'";
            /* String query_access = "SELECT * FROM customers WHERE email=?" +
                    " AND password=?";
            PreparedStatement statement = conn.prepareStatement(query_access);
            statement.setString(1, username);
            statement.setString(2, password);
            // Perform the query
//            ResultSet rs_access = statement.executeQuery(query_access);
            ResultSet rs_access = statement.executeQuery();*/
            Boolean success_check = vp.verifyCredentials(username, password, "employees");
            if(success_check)
            {
                // Login success:

                // set this user into the session

                if (status.equals("")) {
                    request.getSession().setAttribute("employee", new User(username));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }else{
                    responseJsonObject.addProperty("status", status);
                    responseJsonObject.addProperty("message", msgString);
                    responseJsonObject.addProperty("errorMessage", errString);
                }
            }
            else  {
                String query_username = "SELECT * FROM employees WHERE email=?";
//                ResultSet rs_username = statement.executeQuery(query_username);
                PreparedStatement statement_username = conn.prepareStatement(query_username);
                statement_username.setString(1, username);
                ResultSet rs_username = statement_username.executeQuery();

                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                if (!rs_username.next()) {
                    String messageComb = String.join(" ","User " + username + " doesn't exist.", msgString);
                    responseJsonObject.addProperty("message", messageComb);
                } else {
                    String messageComb = String.join(" ","Incorrect Password.", msgString);
                    responseJsonObject.addProperty("message", messageComb);
                }
                responseJsonObject.addProperty("errorMessage", errString);
                rs_username.close();
                statement_username.close();

            }
//            rs_access.close();
//            statement.close();
            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }
        catch (Exception e) {
            //??? fix what type of error?

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage() + " "+errString);
            jsonObject.addProperty("message", "Error.");
            jsonObject.addProperty("status", "Fail");
            response.getWriter().write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }




    }
}
