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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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
        String titlePrefix = request.getParameter("titlePrefix");//added for the list after log in
        String genreID = request.getParameter("genreID");
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");
        String rowCT = request.getParameter("rowCT");
        String sort = request.getParameter("sort");
        String page = request.getParameter("page");
        HttpSession session = request.getSession();
//        HashMap<String, String> prevSession = (HashMap<String, String>) session.getAttribute("movieListRecord");
        //deleted if for checking if it exists
        HashMap<String, String> prevSession = new HashMap<>();

        prevSession.put("titlePrefix", titlePrefix);
        prevSession.put("genreID",genreID);
        prevSession.put("title",title);
        prevSession.put("year", year);
        prevSession.put("director", director);
        prevSession.put("star",star);
        prevSession.put("rowCT", rowCT);
        prevSession.put("sort", sort);
        prevSession.put("page", page);

        session.setAttribute("movieListRecord", prevSession);
        System.out.println(rowCT);
        int lm = 0;
        if (rowCT != null && (rowCT.equals("10") || rowCT.equals("25") || rowCT.equals("50") ||rowCT.equals("100"))){
            lm = Integer.valueOf(rowCT);
        }else{
            lm = 25;
        }
        int offs = 0;
        if (page != null && Integer.valueOf(page) > 0){
            offs = (Integer.valueOf(page)-1) * lm;
        }
        String subquery = "SELECT movie_result.id, movie_result.title, movie_result.year, movie_result.director, \n" +
                        "GROUP_CONCAT(DISTINCT g.name) AS genres_name, GROUP_CONCAT(DISTINCT g.id) AS genres_id, \n" +
                        "\n" +
                        "GROUP_CONCAT(DISTINCT star_order.id ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_id, \n" +
                        "GROUP_CONCAT(DISTINCT star_order.name ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_name, \n" +
                        "\n" +
                        "movie_result.rating\n" +
                        "\n" +
                        "FROM movie_result\n" +
                        "LEFT JOIN stars_in_movies sim ON sim.movieId = movie_result.id\n" +
                        "LEFT JOIN (SELECT s.id, s.name, count(*) as smct\n" +
                        "\t\tFROM stars s, stars_in_movies sim2, movie_result \n" +
                        "\t\tWHERE s.id IN (SELECT sim3.starId\n" +
                        "                                    FROM stars_in_movies sim3\n" +
                        "                                    WHERE sim3.movieId = movie_result.id) AND sim2.starId = s.id \n" +
                        "\t\tGROUP BY s.id\n" +
                        "\t\tORDER BY count(sim2.movieId) DESC\n" +
                        "\t\t) as star_order ON star_order.id = sim.starId\n" +
                        "LEFT JOIN genres_in_movies gm ON gm.movieId = movie_result.id\n" +
                        "LEFT JOIN genres g ON g.id = gm.genreId\n" +
                        "GROUP BY movie_result.id\n";
        List<String> titleSaleOrder = new ArrayList<>();
        List<String> upDownOrder = new ArrayList<>();
        if (sort != null){
            if (sort.equals("1") || sort.equals("2") || sort.equals("3") || sort.equals("4")){
                titleSaleOrder.add("title");
                titleSaleOrder.add("rating");
            }else if(sort.equals("5") || sort.equals("6") || sort.equals("7") || sort.equals("8")){
                titleSaleOrder.add("rating");
                titleSaleOrder.add("title");
            }
            if (sort.equals("1") || sort.equals("5")){
                upDownOrder.add("ASC");
                upDownOrder.add("ASC");
            }else if (sort.equals("2") || sort.equals("6")){
                upDownOrder.add("ASC");
                upDownOrder.add("DESC");
            }else if (sort.equals("3") || sort.equals("7")){
                upDownOrder.add("DESC");
                upDownOrder.add("ASC");
            }else if (sort.equals("4") || sort.equals("8")){
                upDownOrder.add("DESC");
                upDownOrder.add("DESC");
            }
//            else{
//                throw new Exception("Sort Info Incorrect");
//            }
        }
        else{
            titleSaleOrder.add("title");
            titleSaleOrder.add("rating");
            upDownOrder.add("ASC");
            upDownOrder.add("DESC");
        }
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement statement;
            PreparedStatement statement2;
            Statement statementOriginal = conn.createStatement();;
            ResultSet rs;
            ResultSet rs2;

            String query;
            String query2;

            if (titlePrefix != null){
                String checkType;
                String titlePrefixParsed;
                if (!titlePrefix.equals("*"))
                {
                    checkType = "LIKE";
                    titlePrefixParsed =  titlePrefix+'%';
                }else
                {
                    checkType = "REGEXP";
                    titlePrefixParsed = "^[^0-9A-Za-z]";
                }
//                System.out.println(titlePrefix);
//                System.out.println(titlePrefixParsed);
                String orderingStr = "";
                String orderingStr2 = "";
                if (titleSaleOrder.size() != 0){
                    if (titleSaleOrder.get(0).equals("title")) {
                        orderingStr += "ORDER BY m." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", r." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }else{
                        orderingStr += "ORDER BY r." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", m." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }
                }
                String of = "";
                if (offs != 0){
                    of = " OFFSET " + Integer.toString(offs);
                }
                query = "WITH movie_result AS \n" +
                        "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m\n"+
                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                        "WHERE m.title "+checkType+" ? \n" +
                        orderingStr+"\n"+"LIMIT " + Integer.toString(lm) + of +
                        ")\n" +
                        "\n" +
                        subquery +
                        orderingStr2;
                query2 = "SELECT COUNT(*) AS ct\n" +
                        "FROM movies m\n"+
                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                        "WHERE m.title "+checkType+" ? \n";
                statement = conn.prepareStatement(query);
                statement2 = conn.prepareStatement(query2);

                statement.setString(1, titlePrefixParsed);
                statement2.setString(1, titlePrefixParsed);
                rs = statement.executeQuery();
                rs2 = statement2.executeQuery();
            }
            else if (genreID != null)
            {
                String orderingStr = "";
                String orderingStr2 = "";
                if (titleSaleOrder.size() != 0){
                    if (titleSaleOrder.get(0).equals("title")) {
                        orderingStr += "ORDER BY m." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", r." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }else{
                        orderingStr += "ORDER BY r." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", m." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }
                }
                String of = "";
                if (offs != 0){
                    of = " OFFSET " + Integer.toString(offs);
                }
                query = "WITH movie_result AS \n" +
                        "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m \n" +
                        "LEFT JOIN ratings r ON r.movieId = m.id\n"+
                        "LEFT JOIN genres_in_movies gm ON gm.movieId = m.id\n" +
                        "LEFT JOIN genres g ON g.id = gm.genreId\n"+
                        "WHERE g.id = ?\n" +
                        orderingStr+"\n"+"LIMIT " + Integer.toString(lm) + of +
                        ")\n" +
                        "\n" +
                        "\n" +
                        subquery+
                        orderingStr2;
                query2 = "SELECT COUNT(*) AS ct\n" +
                        "FROM movies m \n" +
                        "LEFT JOIN ratings r ON r.movieId = m.id\n"+
                        "LEFT JOIN genres_in_movies gm ON gm.movieId = m.id\n" +
                        "LEFT JOIN genres g ON g.id = gm.genreId\n"+
                        "WHERE g.id = ?\n";
                statement = conn.prepareStatement(query);
                statement2 = conn.prepareStatement(query2);

                statement.setString(1, genreID);
                statement2.setString(1, genreID);
                rs = statement.executeQuery();
                rs2 = statement2.executeQuery();
            }
            else{ //if(star != null || title != null || director != null || year != null)
                String year_str = "";
                int year_num = 0;
                if (!year.equals("") && year != null) {
                    year_str = "m.year = ? AND";
                    year_num = Integer.parseInt(year);
                }

                String orderingStr = "";
                String orderingStr2 = "";
                if (titleSaleOrder.size() != 0){
                    if (titleSaleOrder.get(0).equals("title")) {
                        orderingStr += "ORDER BY m." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", r." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }else{
                        orderingStr += "ORDER BY r." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", m." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
                        orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);

                    }
                }
                String of = "";
                if (offs != 0){
                    of = " OFFSET " + Integer.toString(offs);
                }

                title = title.equals("")||title == null? "%": "%" + title + "%";
                director = director.equals("") || director == null? "%": "%" + director + "%";
//                    int year_num = Integer.valueOf(year);
                star = star.equals("") || star == null? "%": "%" + star + "%";

                String searchStar = "";
                if (!star.equals("")) {
                    searchStar = "AND m.id in (SELECT sim.movieId\n" +
                            "\t\t    FROM stars_in_movies sim, (SELECT stars.id\n" +
                            "\t\t\t\t\t\t\tFROM stars\n" +
                            "\t\t\t\t\t\t\tWHERE stars.name LIKE ?) AS result\n" +
                            "\t\t    WHERE sim.starId = result.id) ";
                }
                query = "WITH movie_result AS \n" +
                        "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m\n"+
                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                        "WHERE "+year_str+" m.title LIKE ? AND m.director LIKE ? "+searchStar+"\n" +
                        orderingStr+"\n"+"LIMIT " + Integer.toString(lm) + of +
                        ")\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        subquery +
                        orderingStr2;
                query2 = "SELECT COUNT(*) AS ct \n" +
                        "FROM movies m\n"+
                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                        "WHERE "+year_str+" m.title LIKE ? AND m.director LIKE ? "+searchStar+"\n";

                statement = conn.prepareStatement(query);
                statement2 = conn.prepareStatement(query2);
                if (!year.equals("")) {
                    statement.setInt(1, year_num);
                    statement.setString(2, title);
                    statement.setString(3, director);
                    statement2.setInt(1, year_num);
                    statement2.setString(2, title);
                    statement2.setString(3, director);
                    if (!star.equals("")) {
                        statement.setString(4, star);
                        statement2.setString(4, star);
                    }
                }else{
                    statement.setString(1, title);
                    statement.setString(2, director);
                    statement2.setString(1, title);
                    statement2.setString(2, director);
                    if (!star.equals("")) {
                        statement.setString(3, star);
                        statement2.setString(3, star);
                    }
                }


                rs = statement.executeQuery();
                rs2 = statement2.executeQuery();

            }
//            else if(star.equals("")){
//                String year_str = "";
//                int year_num = 0;
//                if (!year.equals("")) {
//                    year_num = Integer.parseInt(year);
//                    year_str = "m.year = ? AND";
//                }
//
//                title = title.equals("")? "%": "%" + title + "%";
//                director = director.equals("")? "%": "%" + director + "%";
////                    int year_num = Integer.valueOf(year);
//                query = "SELECT result2.id, result2.title, result2.year, result2.director, \n" +
//                        "GROUP_CONCAT(DISTINCT g.name) AS genres_name, GROUP_CONCAT(DISTINCT g.id) AS genres_id, \n" +
//                        "\n" +
//                        "GROUP_CONCAT(DISTINCT s.id ORDER BY s.name ASC) AS stars_id, \n" +
//                        "GROUP_CONCAT(DISTINCT s.name ORDER BY s.name ASC) AS stars_name, \n" +
//                        "\n" +
//                        "r.rating\n" +
//                        "\n" +
//                        "FROM\n" +
//                        "(SELECT m.id, m.title, m.year, m.director\n" +
//                        "FROM movies m\n" +
//                        "WHERE "+year_str+" m.title LIKE ? AND m.director LIKE ? ) AS result2, genres_in_movies gm, genres g, ratings r, stars_in_movies sim, stars s\n" +
//                        "WHERE gm.movieId = result2.id AND g.id = gm.genreId AND r.movieId = result2.id AND sim.movieId = result2.id AND s.id = sim.starId\n" +
//                        "GROUP BY result2.id";
//                statement = conn.prepareStatement(query);
//                if (!year.equals("")) {
//                    statement.setInt(1, year_num);
//                    statement.setString(2, title);
//                    statement.setString(3, director);
//                }else{
//                    statement.setString(1, title);
//                    statement.setString(2, director);
//                }
//
//
//                rs = statement.executeQuery();
//            }
//            else {
//                // Declare our statement
////                statementOriginal = conn.createStatement();
//
//                query = "WITH movie_result AS \n" +
//                        "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
//                        "FROM movies m\n" +
//                        "JOIN ratings r ON r.movieId = m.id\n" +
//                        "ORDER BY rating DESC LIMIT 20)\n" +
//                        "\n" +
//                        "\n" +
//                        subquery +
//                        "ORDER BY movie_result.rating DESC";
//
//                query2 = "SELECT COUNT(*) AS ct\n" +
//                        "FROM movies m\n" +
//                        "JOIN ratings r ON r.movieId = m.id\n";
//                statement = conn.prepareStatement(query);
//                statement2 = conn.prepareStatement(query2);
//                // Perform the query
//                rs = statement.executeQuery();
//                rs2 = statement2.executeQuery();
//            }



            JsonArray jsonArray = new JsonArray();
            System.out.println(rs);
            System.out.println(rs2);
            JsonObject metaData = new JsonObject();
            int ct = 0;
            if (rs2.next()) {
                 ct = rs2.getInt("ct");
            }
            metaData.addProperty("ct", ct);
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
                jsonObject.addProperty("movie_genres_id", movie_genres_id);
                jsonObject.addProperty("movie_stars_name", movie_stars_name);
                jsonObject.addProperty("movie_stars_id", movie_stars_id);

//                System.out.println(movie_stars_id);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
//            statementOriginal.close();

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
