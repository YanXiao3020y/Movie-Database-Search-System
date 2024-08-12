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


@WebServlet(name = "InsertMovieServlet", urlPatterns = "/api/insert-movie")
public class InsertMovieServlet extends HttpServlet {
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
        // get parameters & provide correct type
        String movie = request.getParameter("movie");
        String year_string = request.getParameter("year");
        int year = Integer.parseInt(year_string);
        String director = request.getParameter("director");
        String star = request.getParameter("star");
//        String birth_year_string = request.getParameter("birth_year");
//        int birth_year = 0;
//        if (birth_year_string != "")
//            birth_year = Integer.parseInt(birth_year_string);
        String genre = request.getParameter("genre");

        try (Connection conn = dataSource.getConnection())
        {
            // Insert movie into database
            String query1 = "Call add_movie(?, ?, ?, ?, ?, @success_msg, @mid_result, @sid_result, @gid_result)";
            PreparedStatement statement1 = conn.prepareStatement(query1);
            statement1.setString(1, movie);
            statement1.setInt(2, year);
            statement1.setString(3, director);
            statement1.setString(4, star);
//            if (birth_year != 0)
//                statement1.setInt(5, birth_year);
//            else
//                statement1.setString(5, null);
            statement1.setString(5, genre);

            statement1.executeQuery();

            // find movie id
            String query2 = "SELECT @success_msg, @mid_result, @sid_result, @gid_result";
            Statement statement2 = conn.createStatement();
            ResultSet rs = statement2.executeQuery(query2);
            JsonObject jsonObject = new JsonObject();
            if (rs.next())
            {
                String success_msg = rs.getString("@success_msg");
                if (success_msg.equals("success"))
                {
                    String mid_result = rs.getString("@mid_result");
                    String sid_result = rs.getString("@sid_result");
                    String gid_result = rs.getString("@gid_result");

                    jsonObject.addProperty("status", "success");
                    jsonObject.addProperty("message", "Success! movieID:" + mid_result
                            + " startID: " + sid_result + " genreID: " + gid_result);


                }

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
