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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "Top20MovieServlet", urlPatterns = "/api/top2Movies")
public class Top20MovieServlet extends HttpServlet {
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
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT query2.id, query2.title, query2.year, query2.director, GROUP_CONCAT(genres.name) AS genres_name, query2.stars_id, query2.stars_name, query2.rating\n" +
                    "\n" +
                    "FROM (\n" +
                    "\n" +
                    "SELECT topMovie.id, topMovie.title, topMovie.year, topMovie.director, GROUP_CONCAT(stars.id) AS stars_id, GROUP_CONCAT(stars.name) AS stars_name, topMovie.rating\n" +
                    "\n" +
                    "FROM (\n" +
                    "\tSELECT * FROM movies m \n" +
                    "\tINNER JOIN ratings r ON m.id=r.movieId  \n" +
                    "\tORDER BY rating DESC LIMIT 20) AS topMovie\n" +
                    "\n" +
                    "\tCROSS JOIN stars_in_movies sm ON sm.movieId=topMovie.id \n" +
                    "\tCROSS JOIN stars ON sm.starId=stars.id\n" +
                    "\tGROUP BY topMovie.id\n" +
                    "\tORDER BY topMovie.rating DESC) AS query2\n" +
                    "\n" +
                    "\tCROSS JOIN genres_in_movies gm ON gm.movieId = query2.id\n" +
                    "\tCROSS JOIN genres ON genres.id = gm.genreId\n" +
                    "\tGROUP BY query2.id\n" +
                    "\tORDER BY query2.rating DESC\n" +
                    "\n" +
                    "\n";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();
            System.out.println(rs);

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                String movie_genres_name = rs.getString("genres_name");
                String movie_stars_name = rs.getString("stars_name");
                String movie_stars_id = rs.getString("stars_id");


//                String[] array_all_movie_stars_name = movie_stars_name.split(","); //split string to array
//                int size_movie_stars_name = Math.min(3, array_all_movie_stars_name.length); //find min size
//                String[] array_movie_stars_name = new String [size_movie_stars_name]; //create an array that contain size_movie_stars_name elements string
//                for(int i=0; i<size_movie_stars_name; i++)
//                    array_movie_stars_name[i] = array_all_movie_stars_name[i];

                String[] array_movie_genres_name = movie_genres_name.split(","); //split string to array
                int size_movie_genres_name = Math.min(3, array_movie_genres_name.length); //find min size
                movie_genres_name = "";
                for(int i=0; i<size_movie_genres_name-1; i++)
                    movie_genres_name += array_movie_genres_name[i] + ", ";
                movie_genres_name += array_movie_genres_name[size_movie_genres_name-1];

//                System.out.println(movie_genres_name);


//                //display first three genres
//                // Declare our statement
//                Statement statement1 = conn.createStatement();
//                String query1 = "SELECT * FROM movies m INNER JOIN ratings r ON m.id=r.movieId ORDER BY rating LIMIT 20";
//                // Perform the query
//                ResultSet rs1 = statement.executeQuery(query);
//                JsonArray jsonArrayGenres = new JsonArray();
//                while(rs1.next()) {
//                    String movie_genres = rs1.getString("genres");
//                    JsonObject jsonObject1 = new JsonObject();
//                    jsonObject1.addProperty("movie_genres", movie_genres);
//                    jsonArrayGenres.add(jsonObject1);
//                }

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_genres_name", movie_genres_name);
                jsonObject.addProperty("movie_stars_name", movie_stars_name);
                jsonObject.addProperty("movie_stars_id", movie_stars_id);

//                System.out.println(movie_stars_id);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}