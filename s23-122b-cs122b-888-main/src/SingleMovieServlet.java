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
import java.util.HashMap;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-Movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbR");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, String> prevSession = (HashMap<String, String>) session.getAttribute("movieListRecord");
        JsonObject metaData = new JsonObject();
        if (prevSession != null)
        {
            String titlePrefix = prevSession.get("titlePrefix");//added for the list after log in
            String genreID = prevSession.get("genreID");
            String title = prevSession.get("title");
            String year = prevSession.get("year");
            String director = prevSession.get("director");
            String star = prevSession.get("star");
            String rowCT = prevSession.get("rowCT");
            String sort = prevSession.get("sort");
            String page = prevSession.get("page");
            metaData.addProperty("titlePrefix", titlePrefix);
            metaData.addProperty("genreID", genreID);
            metaData.addProperty("title", title);
            metaData.addProperty("year", year);
            metaData.addProperty("director", director);
            metaData.addProperty("star", star);
            metaData.addProperty("rowCT", rowCT);
            metaData.addProperty("sort", sort);
            metaData.addProperty("page", page);
            //added
            String mtitle = prevSession.get("mtitle");
            String titleID = prevSession.get("titleID");
            metaData.addProperty("mtitle", mtitle);
            metaData.addProperty("titleID", titleID);
        }



        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            // String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m " +
            //         "where m.id = sim.movieId and sim.starId = s.id and s.id = ?";
            String query = "SELECT query2.id, query2.title, query2.year, query2.director, GROUP_CONCAT(genres.name ORDER BY genres.name) AS genres_name, GROUP_CONCAT(genres.id ORDER BY genres.name) AS genres_id, query2.stars_id, query2.stars_name, query2.rating\n" +
                    "FROM (\n" +
                    "\tSELECT topMovie.id, topMovie.title, topMovie.year, topMovie.director, \n" +
                    "\tGROUP_CONCAT(DISTINCT star_order.id ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_id, \n" +
                    "\tGROUP_CONCAT(DISTINCT star_order.name ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_name,\n" +
                    "\ttopMovie.rating\n" +
                    "           \n" +
                    "      FROM (\n" +
                    "      \tSELECT * FROM movies m \n" +
                    "      \tLEFT JOIN ratings r ON m.id=r.movieId WHERE m.id = ?\n" +
                    "      \t) AS topMovie\n" +
                    "      LEFT JOIN stars_in_movies sm ON sm.movieId=topMovie.id \n" +
                    "      LEFT JOIN (SELECT s.id, s.name, count(*) as smct\n" +
                    "\t\tFROM stars s, stars_in_movies sim2\n" +
                    "\t\tWHERE s.id IN (SELECT sim3.starId\n" +
                    "                                    FROM stars_in_movies sim3\n" +
                    "                                    WHERE sim3.movieId = ?) AND sim2.starId = s.id \n" +
                    "\t\tGROUP BY s.id\n" +
                    "\t\tORDER BY count(sim2.movieId) DESC\n" +
                    "\t\t) as star_order ON star_order.id = sm.starId\n" +
                    "      GROUP BY topMovie.id\n" +
                    "      ORDER BY topMovie.rating DESC) AS query2\n" +
                    "                    \n" +
                    "LEFT JOIN genres_in_movies gm ON gm.movieId = query2.id\n" +
                    "LEFT JOIN genres ON genres.id = gm.genreId\n" +
                    "GROUP BY query2.id\n" +
                    "ORDER BY query2.rating DESC";
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);
            statement.setString(2, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            jsonArray.add(metaData);

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                String movie_genres_name = rs.getString("genres_name");
                String movie_genres_id = rs.getString("genres_id");
                String movie_stars_name = rs.getString("stars_name");
                String movie_stars_id = rs.getString("stars_id");
//                System.out.println(movie_id);
//                System.out.println(movie_title);
//                System.out.println(movie_director);
//                System.out.println(movie_stars_name);

//                 String[] array_movie_genres_name = movie_genres_name.split(","); //split string to array
//                int size_movie_genres_name = Math.min(3, array_movie_genres_name.length); //find min size
//                movie_genres_name = "";
//                for(int i=0; i<size_movie_genres_name-1; i++)
//                    movie_genres_name += array_movie_genres_name[i] + ", ";
//                movie_genres_name += array_movie_genres_name[size_movie_genres_name-1];

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_genres_name", movie_genres_name);
                jsonObject.addProperty("movie_genres_id", movie_genres_id);
                jsonObject.addProperty("movie_stars_name", movie_stars_name);
                jsonObject.addProperty("movie_stars_id", movie_stars_id);
                jsonArray.add(jsonObject);

            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
