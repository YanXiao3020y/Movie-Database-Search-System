
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.sql.*;
import java.sql.SQLException;

public class SAXParserXML extends DefaultHandler {

    FileOutputStream fileOutputStream;


    int mode; // 0:stars; 1:movies; 2:sim

    HashSet<String> existingStarsAll; //names
    HashMap<String, String> stagenameToID; //names, id

    HashSet<String> existingMoviesXML; //movie's id in xml
    HashMap<String, String> movieDTIMap; //[title, id] for all?
    HashMap<String, ArrayList> simTrack;

    //for parsing movie
    private String directorName;
    private String title;
    private int year;
    private String year_str;
    private List<String> genre;

    //for parsing star
    private String stageName;
    private int dob;
    private String dob_string;

    //for parsing sim
    private String dirfilm;
    private String fid;
    private String t;
    private String a;

    private DataSource dataSource;
    boolean incon;

//    int starsFound = 0;
    int starsNotFound = 0;
    int starsIncons = 0;
    int starsDub = 0;
    int starsInserted = 0;

    int moviesIncons = 0;
    int moviesDub = 0;
    int movieNotFound = 0;
    int moviesInserted = 0;

    int simInserted = 0;

    int castIncons = 0;
    Connection conn;
    private String tempVal;

    PreparedStatement statement_movie =null;
    PreparedStatement statement_sim =null;
    PreparedStatement statement_star =null;
    public SAXParserXML() {
        mode = 0;
        existingStarsAll = new HashSet();
        stagenameToID = new HashMap();
        existingMoviesXML = new HashSet();
        movieDTIMap = new HashMap();
        simTrack = new HashMap<>();

        directorName = null;
        title = null;
        year = -1;
        year_str = null;
        genre = null;

        stageName = null;
        dob = -1;
        dob_string = "";
        dirfilm = null;
        fid = null;
        t = null;
        a = null;

        incon = false;
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            conn = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        }catch (SQLException e) {
            e.printStackTrace();
        }catch(Exception d){
            d.printStackTrace();
        }

    }

    public void run() {
        parseDocument();
         printData();
    }

    private void parseDocument() {//still needs work-----------------------------------------------------------

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs


//            String query="insert into Movie (dirname, fid, t, year, release, cats) values(?,?,?,?,?,?)";
            String query_movie = "Call add_xml_movie(?, ?, ?, ?)";
            String query_star = "Call add_xml_star(?, ?)";
            String query_sim = "Call add_star_in_movie(?, ?)";
            try {
                //--------------------------------
                Statement stmt_star2 = conn.createStatement();
                String q_star2 = String.format("SELECT id, name from stars");
                ResultSet rs_star2 = stmt_star2.executeQuery(q_star2);
//                conn.commit();
                while(rs_star2.next()){ //movieDTIMap; //[director: [title, id]]
                    String nsearch = rs_star2.getString("name");
                    String idSearch = rs_star2.getString("id");
                    stagenameToID.put(nsearch, idSearch);
                    existingStarsAll.add(nsearch);
                }
                rs_star2.close();
                stmt_star2.close();
                //--------------------------------------
                Statement stmt_movie = conn.createStatement();
                String q_movie = String.format("SELECT id, title, director from movies");
                ResultSet rs_movie = stmt_movie.executeQuery(q_movie);

                while(rs_movie.next()){ //movieDTIMap; //[director: [title, id]]
                    String dSearch = rs_movie.getString("director");
                    String tSearch = rs_movie.getString("title");
                    String idSearch = rs_movie.getString("id");
//                    if (!movieDTIMap.containsKey(dSearch)) {
//                        movieDTIMap.put(dSearch, new HashMap<String, String>());
//                    }
                    movieDTIMap.put(tSearch, idSearch);
                }
                rs_movie.close();
                stmt_movie.close();

                conn.setAutoCommit(false);

                statement_movie = conn.prepareStatement(query_movie);
                statement_star = conn.prepareStatement(query_star);
                statement_sim = conn.prepareStatement(query_sim);
                sp.parse("actors63.xml", this);
                statement_star.executeBatch();
                conn.commit();
                try {
                    if(statement_star!=null)
                        statement_star.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                mode = 1;
                sp.parse("mains243.xml", this);
                statement_movie.executeBatch();
                conn.commit();
                try {
                    if(statement_movie!=null)
                        statement_movie.close();

                } catch(Exception e) {
                    e.printStackTrace();
                }
                //----------------------------------------------
                Statement stmt_star = conn.createStatement();
                String q_star = String.format("SELECT id, name from stars");
                ResultSet rs_star = stmt_star.executeQuery(q_star);
                conn.commit();
                while(rs_star.next()){ //movieDTIMap; //[director: [title, id]]
                    String nsearch = rs_star.getString("name");
                    String idSearch = rs_star.getString("id");
                    stagenameToID.put(nsearch, idSearch);
                }
                rs_star.close();
                stmt_star.close();
                //-----------
                Statement stmt_movie2 = conn.createStatement();
                String q_movie2 = String.format("SELECT id, title, director from movies");
                ResultSet rs_movie2 = stmt_movie2.executeQuery(q_movie2);
                conn.commit();
                while(rs_movie2.next()){ //movieDTIMap; //[director: [title, id]]
                    String dSearch = rs_movie2.getString("director");
                    String tSearch = rs_movie2.getString("title");
                    String idSearch = rs_movie2.getString("id");
//                    if (!movieDTIMap.containsKey(dSearch)) {
//                        movieDTIMap.put(dSearch, new HashMap<String, String>());
//                    }
                    movieDTIMap.put(tSearch, idSearch);
                }
                rs_movie2.close();
                stmt_movie2.close();

                //------------------------------------------------
                mode = 2;
                sp.parse("casts124.xml", this);
                statement_sim.executeBatch();
                conn.commit();
                try {
                    if(statement_sim!=null)
                        statement_sim.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(conn!=null)
                    conn.close();
            } catch(Exception e) {
                e.printStackTrace();
            }


        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        // System.out.println("No of Employees '" + myEmpls.size() + "'.");

        // Iterator<Employee> it = myEmpls.iterator();
        // while (it.hasNext()) {
        //     System.out.println(it.next().toString());
        // }
        System.out.println("Summary for Parsing Actors");
        System.out.println("Stars Inserted: " + starsInserted);
        System.out.println("Inconsistent Stars: " + starsIncons);
        System.out.println("Duplicate Stars: " + starsDub);
        System.out.println("\n--------------------\n");
        System.out.println("Summary for Parsing Main");
        System.out.println("Movies Inserted: " + moviesInserted);
        System.out.println("Inconsistent Movies: " + moviesIncons);
        System.out.println("Duplicate Movies: " + moviesDub);
        System.out.println("\n--------------------\n");
        System.out.println("Summary for Parsing Cast");
        System.out.println("Stars in Movies Inserted: "+ simInserted);
        System.out.println("Stars Not Found: "+ starsNotFound);
        System.out.println("Movies Not Found: "+ movieNotFound);
        System.out.println("Inconsistent Cast: "+ castIncons);

//        System.out.println(stagenameToID);
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {


            if (mode == 0) {
                if (qName.equalsIgnoreCase("actor")) {


                    if (stageName != null && !incon && !existingStarsAll.contains(stageName)) {
                        statement_star.setString(1, stageName);
                        if (dob == -1) {
                            statement_star.setNull(2, Types.INTEGER);
                        }else{
                            statement_star.setInt(2, dob);
                        }
                        statement_star.addBatch();
                        existingStarsAll.add(stageName);
                        starsInserted += 1;
                    } else if(existingStarsAll.contains(stageName)){
                        starsDub += 1;
                        System.out.println("Star Duplicated: stagename = " + stageName + ", dob = " + dob_string);
                    }
                    else if (incon || stageName != null) {
                        starsIncons += 1;
                        System.out.println("Star Inconsistent: stagename = " + stageName + ", dob = " + dob_string);
                    } else if (stageName == null) {
                        starsIncons += 1;
                        System.out.println("Star Inconsistent: stagename = null, dob = " + dob_string);
                    } else {
                        starsDub += 1;
                        System.out.println("Star Duplicated: stagename = " + stageName + ", dob = " + dob_string);
                    }
                    //--------------
                    stageName = null;
                    dob = -1;
                    dob_string = "";
                    incon = false;
                } else if (qName.equalsIgnoreCase("stagename")) {
                    tempVal = tempVal.strip();
                    stageName = tempVal;
                } else if (qName.equalsIgnoreCase("dob")) {
                    tempVal = tempVal.strip();
                    if (!tempVal.equals("")) {
                        try {
                            dob = Integer.parseInt(tempVal);
                        } catch (Exception e) {
                            incon = true;
                        }
                    }
                    dob_string = tempVal;
                }
            } else if (mode == 1) {
                if (qName.equalsIgnoreCase("directorfilms")) {
                    directorName = null;
                } else if (qName.equalsIgnoreCase("dirname")) {
                    tempVal = tempVal.strip();
                    directorName = tempVal;
                } else if (qName.equalsIgnoreCase("film")) {
                    boolean XMLcont = existingMoviesXML.contains(fid);
                    boolean dbcont = movieDTIMap.containsKey(title);

                    if (fid != null && title != null && year != -1 && genre != null && directorName != null && !incon && !XMLcont && !dbcont) {
                        statement_movie.setString(1, title);
//                        if (year == -1) {
//                            statement_movie.setInt(2, Types.INTEGER);
//                        }else {
                        statement_movie.setInt(2, year);
//                        }
                        statement_movie.setString(3, directorName);
                        statement_movie.setString(4, genre.get(0));
                        statement_movie.addBatch();
                        existingMoviesXML.add(fid);
                        moviesInserted += 1;
                    } else if ((XMLcont || dbcont) && !incon) {
                        System.out.println("Movie Duplicated: fid = " + fid + ", title = " + title + ", year = " + year_str + ", director = " + directorName + ", genre = " + genre);
                        moviesDub += 1;
                    } else {
                        System.out.println("Movie Inconsistent: fid = " + fid + ", title = " + title + ", year = " + year_str + ", director = " + directorName + ", genre = " + genre);
                        moviesIncons += 1;
                    }
                    //--------------------
                    fid = null;
                    title = null;
                    year = -1;
                    year_str = null;
                    genre = null;
                    incon = false;
                } else if (qName.equalsIgnoreCase("fid")) {
                    tempVal = tempVal.strip();
                    fid = tempVal;
                } else if (qName.equalsIgnoreCase("t")) {
                    tempVal = tempVal.strip();
                    title = tempVal;
                    if (tempVal.equalsIgnoreCase("NKT")) {
                        incon = true;
                    }
                } else if (qName.equalsIgnoreCase("year")) {
                    tempVal = tempVal.strip();
                    if (!tempVal.equals("")) {
                        try {
                            year = Integer.parseInt(tempVal);
                        } catch (Exception e) {
                            incon = true;
                        }
                    }
                    year_str = tempVal;
                } else if (qName.equalsIgnoreCase("cat")) {
                    tempVal = tempVal.strip();
                    if (genre == null) {
                        genre = new ArrayList();
                    }
                    genre.add(tempVal);
                }
            } else { //sim
                if (qName.equalsIgnoreCase("dirfilms")) {
                    dirfilm = null;
                } else if (qName.equalsIgnoreCase("m")) {

                    if (a == null || t == null || fid == null) {
                        castIncons += 1;
                        System.out.println("Cast Inconsistent: fid = " + fid + ", director = " + dirfilm + ", title = " + t + ", star = " + a);
                    } else if (!existingStarsAll.contains(a)) {
                        starsNotFound += 1;
                        System.out.println("Star Not Found: fid = " + fid + ", director = " + dirfilm + ", title = " + t + ", star = " + a);
                    } else if (!existingMoviesXML.contains(fid)) {
                        movieNotFound += 1;
                        System.out.println("Movie Not Found: fid = " + fid + ", director = " + dirfilm + ", title = " + t + ", star = " + a);
                    } else {
                        String nameId = stagenameToID.get(a);

                        if (movieDTIMap.containsKey(t)){
                            String filmId = movieDTIMap.get(t);
                            if (simTrack.containsKey(filmId)){
                                if (!simTrack.get(filmId).contains(nameId)){
                                    statement_sim.setString(1, nameId);
                                    statement_sim.setString(2, filmId);
                                    statement_sim.addBatch();
                                    simTrack.get(filmId).add(nameId);
                                    simInserted += 1;
                                }
                                else{
                                    System.out.println("Data Already Exist in stars_in_movies: fid = " + fid + ", director = " + dirfilm + ", title = " + t + ", star = " + a);
                                }
                            }else{
                                statement_sim.setString(1, nameId);
                                statement_sim.setString(2, filmId);
                                statement_sim.addBatch();
                                simTrack.put(filmId, new ArrayList<>());
                                simTrack.get(filmId).add(nameId);
                                simInserted += 1;
                            }

                        }else{
                            System.out.println("Movie Not Found in DataBase: fid = " + fid + ", director = " + dirfilm + ", title = " + t + ", star = " + a);
                            movieNotFound += 1;
                        }


                    }
                    //--------------------------
                    fid = null;
                    t = null;
                    a = null;
                } else if (qName.equalsIgnoreCase("is")) {
                    tempVal = tempVal.strip();
                    dirfilm = tempVal;
                } else if (qName.equalsIgnoreCase("f")) {
                    tempVal = tempVal.strip();
                    fid = tempVal;
                } else if (qName.equalsIgnoreCase("t")) {
                    tempVal = tempVal.strip();
                    t = tempVal;
                } else if (qName.equalsIgnoreCase("a")) {
                    tempVal = tempVal.strip();
                    if (tempVal.equals("sa")){
                        incon = true;
                    }else{
                        a = tempVal;
                    }

                }
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
//        File file = new File("unique1.txt");
            FileOutputStream fileOutputStream = new FileOutputStream("unique2.txt");
            PrintStream printStream = new PrintStream(fileOutputStream);
            // Create a PrintStream that writes to the FileOutputStream
//            PrintStream printStream = new PrintStream(fileOutputStream);
            // Redirect System.out to the PrintStream
            System.setOut(printStream);

            SAXParserXML spe = new SAXParserXML();
            spe.run();

            printStream.close();
            fileOutputStream.close();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}
