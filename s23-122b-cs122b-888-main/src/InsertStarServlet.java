import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;


@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insert-star")
public class InsertStarServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        String birth_year_string = request.getParameter("birth_year");
        int birth_year = 0;
        if (birth_year_string != "")
            birth_year = Integer.parseInt(birth_year_string);



        try (Connection conn = dataSource.getConnection())
        {
            // Insert star into database
            String query1 = "Call add_star(?, ?, @out_result)";
            PreparedStatement statement1 = conn.prepareStatement(query1);
            statement1.setString(1, name);
            if (birth_year != 0)
                statement1.setInt(2, birth_year);
            else
                statement1.setString(2, null);
            statement1.executeQuery();

            // find star id
            String query3 = "SELECT @out_result";
            Statement statement2 = conn.createStatement();
            ResultSet rs = statement2.executeQuery(query3);
            JsonObject jsonObject = new JsonObject();
            if (rs.next())
            {
                String starId = rs.getString("@out_result");
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("message", "The star is inserted successfully with star ID: " + starId);
            }
            rs.close();
            statement2.close();

            response.getWriter().write(jsonObject.toString());
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
