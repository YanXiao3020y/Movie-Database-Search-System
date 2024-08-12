import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "ModifyQuantityServlet", urlPatterns = "/api/modify-cart")
public class ModifyQuantityServlet extends HttpServlet {

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

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession();


        //===============================================================
        // for button function add, sub, delete

        try
        {
            String action = request.getParameter("action");
            String current_Title = request.getParameter("title");

            HashMap<String, HashMap<Double, Integer>> previousItems2 = (HashMap<String, HashMap<Double, Integer>>) session.getAttribute("previousItems");
            HashMap<Double, Integer> existingInnerMap = previousItems2.get(current_Title); // {title: {price, quantity}}
            Map.Entry<Double, Integer> entry = existingInnerMap.entrySet().iterator().next();
            HashMap<Double, Integer> property = new HashMap<>();

            if (action.equals("add")) {
                property.put(entry.getKey(), entry.getValue() + 1);
                previousItems2.put(current_Title, property);
            } else if (action.equals("subtract")) {
                property.put(entry.getKey(), entry.getValue() - 1);
                previousItems2.put(current_Title, property);
            } else if (action.equals("delete"))
                previousItems2.remove(current_Title);

            session.setAttribute("previousItems", previousItems2);

            response.setStatus(200);
        }
        catch (Exception e)
        {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }

    }
}
