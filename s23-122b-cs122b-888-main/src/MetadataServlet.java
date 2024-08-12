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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
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

        try (Connection conn = dataSource.getConnection())
        {
            JsonArray tables = new JsonArray();

            Statement stmt1 = conn.createStatement();
            String query1 = "show tables";
            ResultSet rs1 = stmt1.executeQuery(query1);

            while (rs1.next())
            {
                JsonObject aTable = new JsonObject();
                JsonArray aTableAttributes = new JsonArray();

                Statement stmt2 = conn.createStatement();
                String query2 = "describe ";
                String tableName = rs1.getString("Tables_in_moviedb");
                query2 += tableName;

                ResultSet rs2 = stmt2.executeQuery(query2);
                while (rs2.next())
                {
                    String type = rs2.getString("Type");
                    String field = rs2.getString("Field");

                    JsonObject attribute = new JsonObject();
                    attribute.addProperty("type", type);
                    attribute.addProperty("field", field);
                    aTableAttributes.add(attribute);
                }
                rs2.close();
                stmt2.close();

                aTable.addProperty("name", tableName);
                aTable.add("attributes", aTableAttributes);
                tables.add(aTable);

            }
            rs1.close();
            stmt1.close();

            response.setStatus(200);
            response.getWriter().write(tables.toString());

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
    }
}
