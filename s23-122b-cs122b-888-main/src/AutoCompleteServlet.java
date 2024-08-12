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
@WebServlet(name = "AutoCompleteServlet", urlPatterns = "/api/autocomplete")
public class AutoCompleteServlet extends HttpServlet {
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
        String autotitle = request.getParameter("autotitle");

        HttpSession session = request.getSession();
        HashMap<String, String> prevSession = (HashMap<String, String>) session.getAttribute("movieListRecord");

        prevSession = new HashMap<>();

        prevSession.put("autotitle",autotitle);


        session.setAttribute("movieListRecord", prevSession);
//        System.out.println(rowCT);
//        int lm = 0;
//        if (rowCT != null && (rowCT.equals("10") || rowCT.equals("25") || rowCT.equals("50") ||rowCT.equals("100"))){
//            lm = Integer.valueOf(rowCT);
//        }else{
//            lm = 25;
//        }
//        int offs = 0;
//        if (page != null && Integer.valueOf(page) > 0){
//            offs = (Integer.valueOf(page)-1) * lm;
//        }
        String subquery = "SELECT movie_result.id, movie_result.title, movie_result.year, movie_result.director, \n" +
                "GROUP_CONCAT(DISTINCT g.name) AS genres_name, GROUP_CONCAT(DISTINCT g.id) AS genres_id, \n" +
                "\n" +
                "GROUP_CONCAT(DISTINCT star_order.id ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_id, \n" +
                "GROUP_CONCAT(DISTINCT star_order.name ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_name, \n" +
                "\n" +
                "movie_result.rating\n" +
                "\n" +
                "FROM movie_result\n" +
                "JOIN stars_in_movies sim ON sim.movieId = movie_result.id\n" +
                "JOIN (SELECT s.id, s.name, count(*) as smct\n" +
                "\t\tFROM stars s, stars_in_movies sim2, movie_result \n" +
                "\t\tWHERE s.id IN (SELECT sim3.starId\n" +
                "                                    FROM stars_in_movies sim3\n" +
                "                                    WHERE sim3.movieId = movie_result.id) AND sim2.starId = s.id \n" +
                "\t\tGROUP BY s.id\n" +
                "\t\tORDER BY count(sim2.movieId) DESC\n" +
                "\t\t) as star_order ON star_order.id = sim.starId\n" +
                "JOIN genres_in_movies gm ON gm.movieId = movie_result.id\n" +
                "JOIN genres g ON g.id = gm.genreId\n" +
                "GROUP BY movie_result.id\n";
//        List<String> titleSaleOrder = new ArrayList<>();
//        List<String> upDownOrder = new ArrayList<>();
//        if (sort != null){
//            if (sort.equals("1") || sort.equals("2") || sort.equals("3") || sort.equals("4")){
//                titleSaleOrder.add("title");
//                titleSaleOrder.add("rating");
//            }else if(sort.equals("5") || sort.equals("6") || sort.equals("7") || sort.equals("8")){
//                titleSaleOrder.add("rating");
//                titleSaleOrder.add("title");
//            }
//            if (sort.equals("1") || sort.equals("5")){
//                upDownOrder.add("ASC");
//                upDownOrder.add("ASC");
//            }else if (sort.equals("2") || sort.equals("6")){
//                upDownOrder.add("ASC");
//                upDownOrder.add("DESC");
//            }else if (sort.equals("3") || sort.equals("7")){
//                upDownOrder.add("DESC");
//                upDownOrder.add("ASC");
//            }else if (sort.equals("4") || sort.equals("8")){
//                upDownOrder.add("DESC");
//                upDownOrder.add("DESC");
//            }
////            else{
////                throw new Exception("Sort Info Incorrect");
////            }
//        }
//        else{
//            titleSaleOrder.add("title");
//            titleSaleOrder.add("rating");
//            upDownOrder.add("ASC");
//            upDownOrder.add("DESC");
//        }
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


//            String orderingStr = "";
//            String orderingStr2 = "";
//            if (titleSaleOrder.size() != 0){
//                if (titleSaleOrder.get(0).equals("title")) {
//                    orderingStr += "ORDER BY m." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", r." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
//                    orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
//
//                }else{
//                    orderingStr += "ORDER BY r." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", m." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
//                    orderingStr2 += "ORDER BY movie_result." + titleSaleOrder.get(0) + " " + upDownOrder.get(0) + ", movie_result." + titleSaleOrder.get(1) + " " + upDownOrder.get(1);
//
//                }
//            }
//            String of = "";
//            if (offs != 0){
//                of = " OFFSET " + Integer.toString(offs);
//            }
            String titleParsed = "";
            String [] titleArray = autotitle.split(" ");
            if (titleArray.length >0) {
                titleParsed += "+" + titleArray[0] + "*";
                for (int i = 1; i < titleArray.length; i++) {
                    titleParsed += " +" + titleArray[i] + "*";
                }
            }
            query = "SELECT m.id, m.title, m.year\n" +
                    "FROM movies m\n"+
//                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                    "WHERE match(m.title) against ( ? in boolean mode)\n" +
                    "ORDER BY m.title\n" +
                    "LIMIT 10";
//                    orderingStr+"\n"+"LIMIT " + Integer.toString(lm) + of +
//                    ")\n" +
//                    "\n" +
//                    subquery +
//                    orderingStr2;
            query2 = "SELECT COUNT(*) AS ct\n" +
                    "FROM movies m\n"+
                    "WHERE match(m.title) against ( ? in boolean mode)\n" +
                    "ORDER BY m.title\n" +
                    "LIMIT 10";
//                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
//                    "WHERE match(m.title) against ( ? in boolean mode)\n";
            statement = conn.prepareStatement(query);
            statement2 = conn.prepareStatement(query2);

            statement.setString(1, titleParsed);
            statement2.setString(1, titleParsed);
            rs = statement.executeQuery();
            rs2 = statement2.executeQuery();
            int ct = 0;
            if (rs2.next()) {
                ct = rs2.getInt("ct");
            }
            if (ct == 0){
                int stepsAllotted = Math.floorDiv(autotitle.length(), 3);
                String titleParsed2 = "%" + autotitle + "%";
                query = "SELECT m.id, m.title, m.year\n" +
                        "FROM movies m\n"+
//                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                        "WHERE ed(lower(m.title), lower(?)) < ? or m.title LIKE ?" +
                        "ORDER BY m.title\n" +
                        "LIMIT 10";
                statement = conn.prepareStatement(query);
                statement.setString(1, autotitle);
                statement.setInt(2,stepsAllotted);
                statement.setString(3, titleParsed2);
                rs = statement.executeQuery();
            }



            JsonArray jsonArray = new JsonArray();
            System.out.println(rs);
//            System.out.println(rs2);
//            JsonObject metaData = new JsonObject();
//            int ct = 0;
//            if (rs2.next()) {
//                ct = rs2.getInt("ct");
//            }
//            metaData.addProperty("ct", ct);
//            jsonArray.add(metaData);

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
//                String movie_director = rs.getString("director");
//                String movie_rating = rs.getString("rating");
//                String movie_genres_name = rs.getString("genres_name");
//                String movie_genres_id = rs.getString("genres_id");
//                String movie_stars_name = rs.getString("stars_name");
//                String movie_stars_id = rs.getString("stars_id");



                // Create a JsonObject based on the data we retrieve from rs
//                JsonObject jsonObject = new JsonObject();
//                jsonObject.addProperty("movie_id", movie_id);
//                jsonObject.addProperty("movie_title", movie_title);
//                jsonObject.addProperty("movie_year", movie_year);
                jsonArray.add(generateJsonObject(movie_id, movie_title, movie_year));


//                System.out.println(movie_stars_id);

//                jsonArray.add(jsonObject);
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
    private static JsonObject generateJsonObject(String movieID, String title, String year) {
        JsonObject jsonObject = new JsonObject();
        String movieResult = title + " (" + year +") ";
        jsonObject.addProperty("value", movieResult);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
