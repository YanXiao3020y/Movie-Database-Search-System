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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "shoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

//        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
//        if (previousItems == null) {
//            previousItems = new HashMap<String, Integer>();
//        }
        HashMap<String, HashMap<Double, Integer>> previousItems = (HashMap<String, HashMap<Double, Integer>>) session.getAttribute("previousItems");
        //generate a random number for price between 1-100
        if (previousItems == null)
            previousItems = new HashMap<>();

        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");

        int counter = 1;
        JsonArray previousItemsJsonArray = new JsonArray();
//        previousItems.forEach(previousItemsJsonArray::add);
        for(Map.Entry<String, HashMap<Double, Integer>> outerEntry : previousItems.entrySet())
        {
            System.out.println(counter);
            for (Map.Entry<Double, Integer> innerEntry : outerEntry.getValue().entrySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", outerEntry.getKey());
                jsonObject.addProperty("price", innerEntry.getKey());
                jsonObject.addProperty("quantity", innerEntry.getValue());

                previousItemsJsonArray.add(jsonObject);

                System.out.println(counter);
            }
            counter++;
        }
//        JsonArray previousItemsJsonArray = new JsonArray();
////        previousItems.forEach(previousItemsJsonArray::add);
//
//        for (Map.Entry<String,Integer> entry: previousItems.entrySet())
//        {
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("title", entry.getKey());
//            jsonObject.addProperty("quantity", entry.getValue());
//            previousItemsJsonArray.add(jsonObject);
//        }
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        PrintWriter out = response.getWriter();

        //get movie title using query
        String id = request.getParameter("id");
        String title = "";
        try (Connection conn = dataSource.getConnection())
        {
            String query = "select title from movies where id=?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next())
                title = rs.getString("title");

            rs.close();
            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);
        }
        catch (Exception e)
        {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }


        HttpSession session = request.getSession();

        HashMap<String, HashMap<Double, Integer>> previousItems = (HashMap<String, HashMap<Double, Integer>>) session.getAttribute("previousItems");
        //generate a random number for price between 1-100
        double price = Math.floor(Math.random() * 100) + 1;

        if (previousItems == null)
        {
            HashMap<Double, Integer > property = new HashMap<>();
            property.put(price, 1);

            previousItems = new HashMap<>();
            previousItems.put(title,property); //title & quantity
            session.setAttribute("previousItems", previousItems);

        }
        else
        {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems)
            {
                try {

                    if (previousItems.containsKey(title)) //existing item, then quantity+1
                    {
                        HashMap<Double, Integer> existingInnerMap = previousItems.get(title); // {title: {price, quantity}}
                        Map.Entry<Double, Integer> entry = existingInnerMap.entrySet().iterator().next();

                        if (existingInnerMap != null && existingInnerMap.size() == 1)
                        {
                            int newQuantity = entry.getValue() + 1;
                            existingInnerMap.put(entry.getKey(), newQuantity);
                            previousItems.put(title, existingInnerMap);
                        }
                    }
                    else
                    {
                        HashMap<Double, Integer> existingInnerMap =  new HashMap<>();
                        existingInnerMap.put(price, 1);
                        previousItems.put(title, existingInnerMap);
                    }
                }
                catch (Exception e)
                {
                    // write error message JSON object to output
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("errorMessage", e.getMessage());
                    out.write(jsonObject.toString());

                    // set response status to 500 (Internal Server Error)
                    response.setStatus(500);
                }

            }
        }

        JsonObject responseJsonObject = new JsonObject();

        int counter = 1;
        JsonArray previousItemsJsonArray = new JsonArray();
//        previousItems.forEach(previousItemsJsonArray::add);
        for(Map.Entry<String, HashMap<Double, Integer>> outerEntry : previousItems.entrySet())
        {
            System.out.println(counter);
            for (Map.Entry<Double, Integer> innerEntry : outerEntry.getValue().entrySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", outerEntry.getKey());
                jsonObject.addProperty("price", innerEntry.getKey());
                jsonObject.addProperty("quantity", innerEntry.getValue());

                previousItemsJsonArray.add(jsonObject);

                System.out.println(counter);
            }
            counter++;
        }
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        out.write(responseJsonObject.toString());

    }
}


