import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


@WebServlet(name = "CreditCardServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbR");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("application/json"); // Response mime type ------might need to uncomment
        String cardNumber = request.getParameter("cardNumber");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String experationDate = request.getParameter("experationDate");

        PrintWriter out = response.getWriter(); // Output stream to STDOUT

        try (Connection conn = dataSource.getConnection()) {
            JsonObject responseJsonObject = new JsonObject();

            if(cardNumber != "" && firstName != "" && lastName != "" && experationDate != "")
            {
                String query = "SELECT * FROM creditcards where id = ?";
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, cardNumber);
                ResultSet rs_access = statement.executeQuery();

                boolean isFound = false;
                while(rs_access.next())
                {
                    String first_name = rs_access.getString("firstName");
                    String last_name = rs_access.getString("lastName");
                    Date expiration_date = rs_access.getDate("expiration"); //Retrieving the Date object
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String expir_date = dateFormat.format(expiration_date);

                    if(first_name.equals(firstName) && last_name.equals(lastName) && expir_date.equals(experationDate))
                    {
                        isFound = true;
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Credit card information correct!");
                        break;
                    }
                }

                if(!isFound)
                {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Error: credit card information incorrect!");
                }
                rs_access.close();
                statement.close();
            }
            else  {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Credit Card information incorrect! Please try again!");

                // Log to localhost log
                request.getServletContext().log("Credit Card information incorrect!");
            }

            out.write(responseJsonObject.toString());
//            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
//            response.getWriter().write(jsonObject.toString());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally
        {
            out.close();
        }



    }
}
