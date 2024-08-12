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
import java.io.File;
import java.io.FileWriter;
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
@WebServlet(name = "FullTextServlet", urlPatterns = "/api/fulltext")
public class FullTextServlet extends HttpServlet {
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
        long startTimeTS = System.nanoTime();
        long elapsedTime = 0;
        response.setContentType("application/json"); // Response mime type
        String mtitle = request.getParameter("mtitle");
        String titleID = request.getParameter("titleID");
        String rowCT = request.getParameter("rowCT");
        String sort = request.getParameter("sort");
        String page = request.getParameter("page");
        HttpSession session = request.getSession();
//        HashMap<String, String> prevSession = (HashMap<String, String>) session.getAttribute("movieListRecord");

        long s1 = 0;
        long s2 = 0;
        long s3 = 0;
        long s4 = 0;

        HashMap<String, String> prevSession = new HashMap<>();

        prevSession.put("mtitle",mtitle);
        prevSession.put("titleID", titleID);
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
//        String subquery = "SELECT movie_result.id, movie_result.title, movie_result.year, movie_result.director, \n" +
//                "GROUP_CONCAT(DISTINCT g.name) AS genres_name, GROUP_CONCAT(DISTINCT g.id) AS genres_id, \n" +
//                "\n" +
//                "GROUP_CONCAT(DISTINCT star_order.id ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_id, \n" +
//                "GROUP_CONCAT(DISTINCT star_order.name ORDER BY star_order.smct DESC, star_order.name ASC) AS stars_name, \n" +
//                "\n" +
//                "movie_result.rating\n" +
//                "\n" +
//                "FROM movie_result\n" +
//                "LEFT JOIN stars_in_movies sim ON sim.movieId = movie_result.id\n" +
//                "LEFT JOIN (SELECT s.id, s.name, count(*) as smct\n" +
//                "\t\tFROM stars s, stars_in_movies sim2, movie_result \n" +
//                "\t\tWHERE s.id IN (SELECT sim3.starId\n" +
//                "                                    FROM stars_in_movies sim3\n" +
//                "                                    WHERE sim3.movieId = movie_result.id) AND sim2.starId = s.id \n" +
//                "\t\tGROUP BY s.id\n" +
//                "\t\tORDER BY count(sim2.movieId) DESC\n" +
//                "\t\t) as star_order ON star_order.id = sim.starId\n" +
//                "LEFT JOIN genres_in_movies gm ON gm.movieId = movie_result.id\n" +
//                "LEFT JOIN genres g ON g.id = gm.genreId\n" +
//                "GROUP BY movie_result.id\n";
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
            PreparedStatement statementS;
            PreparedStatement statementG;
            Statement statementOriginal = conn.createStatement();;
            ResultSet rs;
            ResultSet rs2;
            ResultSet rsS;
            ResultSet rsG;

            String query;
            String query2;




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

            int ct = 0;

            String titleParsed = "";
            String[] titleArray = mtitle.split(" ");
            if (titleArray.length > 0) {
                titleParsed += "+" + titleArray[0] + "*";
                for (int i = 1; i < titleArray.length; i++) {
                    titleParsed += " +" + titleArray[i] + "*";
                }
            }
            int stepsAllotted = Math.floorDiv(mtitle.length(), 3);
            String titleParsed2 = "%" + mtitle + "%";
            query = "SELECT m.id, m.title, m.year, m.director, r.rating \n" +
                    "FROM movies m \n" +
                    "LEFT JOIN ratings r ON r.movieId = m.id \n" +
                    "WHERE match(m.title) against ( ? in boolean mode) or m.title LIKE ? \n" +
                    orderingStr + "\n LIMIT " + Integer.toString(lm) + of;
//            or ed(lower(m.title), lower(?)) < ?


//                    "WITH movie_result AS \n" +
//                    "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
//                    "FROM movies m\n" +
//                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
//                    "WHERE match(m.title) against ( ? in boolean mode)\n" +
//                    orderingStr + "\n" + "LIMIT " + Integer.toString(lm) + of +
//                    ")\n" +
//                    "\n" +
//                    subquery +
//                    orderingStr2;

            query2 = "SELECT COUNT(*) AS ct\n" +
                    "FROM movies m\n" +
                    "WHERE match(m.title) against ( ? in boolean mode) or m.title LIKE ?\n";
//            or ed(lower(m.title), lower(?)) < ?

            statement = conn.prepareStatement(query);
            statement2 = conn.prepareStatement(query2);

            statement.setString(1, titleParsed);
            statement2.setString(1, titleParsed);

//            statement.setString(3, mtitle);
//            statement.setInt(4,stepsAllotted);
            statement.setString(2, titleParsed2);
//            statement2.setString(3, mtitle);
//            statement2.setInt(4, stepsAllotted);
            statement2.setString(2, titleParsed2);

            long startTime = System.nanoTime();
            rs = statement.executeQuery();
            rs2 = statement2.executeQuery();
            long endTime = System.nanoTime();
            elapsedTime = endTime - startTime;
            s1 += endTime - startTime;
            if (rs2.next()) {
                ct = rs2.getInt("ct");
            }
            //add-------------------------------------------
//            if (ct == 0){
//                int stepsAllotted = Math.floorDiv(mtitle.length(), 3);
//                String titleParsed2 = "%" + mtitle + "%";
//                query = "SELECT m.id, m.title, m.year, m.director, r.rating\n" +
//                        "FROM movies m\n" +
//                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
//                        "WHERE ed(lower(m.title), lower(?)) < ? or m.title LIKE ?\n" +
//                        orderingStr + "\n" + "LIMIT " + Integer.toString(lm) + of;
////                        "WITH movie_result AS \n" +
////                        "(SELECT m.id, m.title, m.year, m.director, r.rating\n" +
////                        "FROM movies m\n" +
////                        "LEFT JOIN ratings r ON r.movieId = m.id\n" +
////                        "WHERE ed(lower(m.title), lower(?)) < ? or m.title LIKE ?\n" +
////                        orderingStr + "\n" + "LIMIT " + Integer.toString(lm) + of +
////                        ")\n" +
////                        "\n" +
////                        subquery +
////                        orderingStr2;
//                query2 = "SELECT COUNT(*) AS ct\n" +
//                        "FROM movies m\n" +
//                        "WHERE ed(lower(m.title), lower(?)) < ? or m.title LIKE ?\n";
//
//                statement = conn.prepareStatement(query);
//                statement2 = conn.prepareStatement(query2);
//
//                statement.setString(1, mtitle);
//                statement.setInt(2,stepsAllotted);
//                statement.setString(3, titleParsed2);
//                statement2.setString(1, mtitle);
//                statement2.setInt(2, stepsAllotted);
//                statement2.setString(3, titleParsed2);
////                    rs = statement.executeQuery();
////                    rs2 = statement2.executeQuery();
//
//                long startTime2 = System.nanoTime();
//                rs = statement.executeQuery();
//                rs2 = statement2.executeQuery();
//                long endTime2 = System.nanoTime();
//                elapsedTime += endTime2 - startTime2;
//                s2 += endTime2 - startTime2;
//
//
//                if (rs2.next()) {
//                    ct = rs2.getInt("ct");
//                }
//            }
            JsonArray jsonArray = new JsonArray();
//            System.out.println(rs);
//            System.out.println(rs2);
            JsonObject metaData = new JsonObject();

            metaData.addProperty("ct", ct);
            jsonArray.add(metaData);

            String queryS = "SELECT s.id as stars_id, s.name as stars_name \n" +
                    "        FROM stars s, stars_in_movies sim2  \n" +
                    "        WHERE s.id IN (SELECT sim3.starId \n" +
                    "                       FROM stars_in_movies sim3 \n" +
                    "                       WHERE sim3.movieId = ?) \n" +
                    "\t\tAND sim2.starId = s.id  \n" +
                    "        GROUP BY s.id \n" +
                    "        ORDER BY count(sim2.movieId) DESC \n" +
                    "\tLIMIT 3";
            statementS = conn.prepareStatement(queryS);
            String queryG = "SELECT g.name as genres_name, g.id as genres_id\n" +
                    "\tFROM genres g, genres_in_movies gim\n" +
                    "\tWHERE gim.movieId = ? and gim.genreId = g.id\n" +
                    "\tGROUP BY g.id\n" +
                    "\tORDER BY g.name\n" +
                    "\tLIMIT 3";
            statementG = conn.prepareStatement(queryG);
            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");


                statementS.setString(1, movie_id);
                long startTimeS = System.nanoTime();
                rsS = statementS.executeQuery();
                long endTimeS = System.nanoTime();
                elapsedTime += endTimeS - startTimeS;
                s3 += endTimeS - startTimeS;

                String movie_stars_name = "";
                String movie_stars_id = "";
                if (rsS.next()){
                    movie_stars_id += rsS.getString("stars_id");
                    movie_stars_name += rsS.getString("stars_name");
                }
                while (rsS.next()){
                    movie_stars_id += "," + rsS.getString("stars_id");
                    movie_stars_name += "," + rsS.getString("stars_name");
                }

                statementG.setString(1, movie_id);
                long startTimeG = System.nanoTime();
                rsG = statementG.executeQuery();
                long endTimeG = System.nanoTime();
                elapsedTime += endTimeG - startTimeG;
                s4 += endTimeG - startTimeG;

                String movie_genres_name = "";
                String movie_genres_id = "";
                if (rsG.next()){
                    movie_genres_id += rsG.getString("genres_id");
                    movie_genres_name += rsG.getString("genres_name");
                }
                while (rsG.next()){
                    movie_genres_id += "," + rsG.getString("genres_id");
                    movie_genres_name += "," + rsG.getString("genres_name");
                }




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
            System.out.println("result num: "+jsonArray.size());
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
        long endTimeTS = System.nanoTime();
        long elapsedTimeTS = endTimeTS - startTimeTS;
        String path = request.getServletContext().getRealPath("/");
        File file = new File(path+"\\log.txt");
        FileWriter fr = new FileWriter(file, true);
//        fr.write("TJ: " + elapsedTime + " TS: " + elapsedTimeTS + "\t s1: "+ s1 + "s2: "+ s2 + " s3 "+ s3 + " s4 " + s4 +"\n");
        fr.write("TJ: " + elapsedTime + " TS: " + elapsedTimeTS + "\n");

        fr.close();
        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
