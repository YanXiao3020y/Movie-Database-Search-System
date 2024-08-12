import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbWR");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Entered into post");
        System.out.println("dataSource" + dataSource);
        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource.getConnection())
        {
            System.out.println("Entered into connection");

//            String card_first_name = request.getParameter("firstName");
//            String card_last_name = request.getParameter("lastName");
//            String card_number = request.getParameter("cardNumber");
//            String card_expiry_date = request.getParameter("experationDate");



//            //check the existing of the credit cards
//            String query1 = "select * from creditcards where id=? and expiration=?";
//            PreparedStatement statement1 = conn.prepareStatement(query1);
//            statement1.setString(1, card_number);
//            statement1.setDate(2, java.sql.Date.valueOf(card_expiry_date));
//            ResultSet rs1 = statement1.executeQuery();
//
//            if (rs1.next()) //correct user
//            {
                //find customer id to connect sales table
//                int customerId = 0;
//                String query2 = "select id from customers where firstName=? and lastName=?";
//                PreparedStatement statement2 = conn.prepareStatement(query2);
//                statement2.setString(1, card_first_name);
//                statement2.setString(2, card_last_name);
//                ResultSet rs2 = statement2.executeQuery();
//                if (rs2.next())
//                    customerId = rs2.getInt("id");

                HttpSession session = request.getSession();

                //get email address
                User myInfo = (User) session.getAttribute("user");
                String email = myInfo.getUsername();

                //get customer id
                int customerId = 0;
                String query2 = "Select id from customers where email = ?";
                PreparedStatement statement2 = conn.prepareStatement(query2);
                statement2.setString(1, email);
                ResultSet rs2 = statement2.executeQuery();
            System.out.println("After into search customerid");

            if(rs2.next())
                    customerId = rs2.getInt("id");

                // get customerId which will be recorded into sales
//                String cusId = cusIdRs.getString("id");

            //find customer id to connect sales table
//                int customerId = 0;
//                String query2 = "select id from customers where firstName=? and lastName=?";
//                PreparedStatement statement2 = conn.prepareStatement(query2);
//                statement2.setString(1, card_first_name);
//                statement2.setString(2, card_last_name);
//                ResultSet rs2 = statement2.executeQuery();
//                if (rs2.next())
//                    customerId = rs2.getInt("id");

                //record today date
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern("yyyy-MM-dd");
                Date date = new Date();
                String today_date = sdf.format(date);

                JsonArray jsonArray = new JsonArray();

                HashMap<String, HashMap<Double, Integer>> previousItems = (HashMap<String, HashMap<Double, Integer>>) session.getAttribute("previousItems");
                for(Map.Entry<String, HashMap<Double, Integer>> outerEntry : previousItems.entrySet()) {
                    for (Map.Entry<Double, Integer> innerEntry : outerEntry.getValue().entrySet()) {
//                        JsonObject jsonObject = new JsonObject();
//                        jsonObject.addProperty("title", outerEntry.getKey());
//                        jsonObject.addProperty("price", innerEntry.getKey());
//                        jsonObject.addProperty("quantity", innerEntry.getValue());

                        String title = outerEntry.getKey();
                        int quantity = innerEntry.getValue();
                        double price =  innerEntry.getKey();
                        double total = price * quantity;

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("title", title);
                        jsonObject.addProperty("quantity", quantity);
                        jsonObject.addProperty("price", price);
                        jsonObject.addProperty("total", total);



                        //get movie id
                        String movieId = "";
                        String query3 = "select id from movies where title=?";
                        PreparedStatement statement3 = conn.prepareStatement(query3);
                        statement3.setString(1, title);
                        ResultSet rs3 = statement3.executeQuery();
                        if (rs3.next())
                            movieId = rs3.getString("id");
                        System.out.println("after searching movie id");

                        String updateQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
                        PreparedStatement statement4 = conn.prepareStatement(updateQuery);
                        statement4.setInt(1, customerId);
                        statement4.setString(2, movieId);
                        statement4.setDate(3, java.sql.Date.valueOf(today_date));
                        statement4.setInt(4, quantity);
                        statement4.executeUpdate();
                        System.out.println("after insert");

                        String saleId = "";
                        String query5 = "SELECT id FROM sales WHERE saleDate=? AND customerId=? AND movieId=?";
                        PreparedStatement statement5 = conn.prepareStatement(query5);
                        statement5.setDate(1, java.sql.Date.valueOf(today_date));
                        statement5.setInt(2, customerId); // replace customerId with the actual value
                        statement5.setString(3, movieId); // replace movieId with the actual value
                        ResultSet rs5 = statement5.executeQuery();
                        if (rs5.next())
                            saleId = rs5.getString("id");
                        jsonObject.addProperty("saleId", saleId);

                        jsonArray.add(jsonObject);

                        rs5.close();
                        statement5.close();
                        rs3.close();
                        statement3.close();
                    }
                }

                rs2.close();
                statement2.close();

                // Write JSON string to output
                response.getWriter().write(jsonArray.toString());

                session.removeAttribute("previousItems"); //release it for new purchase

                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
//            }
//            else
//            {
//                JsonArray jsonArray = new JsonArray();
//                responseJsonObject.addProperty("status", "fail");
//
//                responseJsonObject.addProperty("message", "Incorrect payment information, please try again.");
//                jsonArray.add(responseJsonObject);
//                response.getWriter().write(jsonArray.toString());
//            }

//            rs1.close();
//            statement1.close();

//

            // Set response status to 200 (OK)
            response.setStatus(200);
        }
        catch (Exception e)
        {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", "error message catch");
            response.getWriter().write(jsonObject.toString());
            System.out.println("error message catch");
            e.printStackTrace();
            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

    }
}
