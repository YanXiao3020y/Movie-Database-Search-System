package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.searchbar.SearchActivity;
import edu.uci.ics.fabflixmobile.ui.single_movie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "ec2-18-221-109-181.us-east-2.compute.amazonaws.com";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "cs122b_project1_api_example_war";
//    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    private String mtitle;
    private String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        final Button backButton = findViewById(R.id.back_button);
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        //assign a listener to call a function to handle the user request when clicking a button
        backButton.setOnClickListener(view -> redirect());
//        if (pg == null || pg == "1"){
//            prev_disable = " disabled";
//            pg = "1";
//        }
//        let max = parseInt(resultData[0]["ct"]);
//        let pg_num = parseInt(pg);
//        if (row_num * pg_num >= max){
//            next_disable = " disabled";
//        }
//        // let content = "<button name=\"prev\" onclick=\"prev()\""+prev_disable+">Prev</button>" + "<h5>"+pg +"</h5>"+
//        //     "<button name=\"next\" onclick=\"next()\""+next_disable+">Next</button>\n";
//        let content = "<button name=\"prev\" onclick=\"prev()\""+prev_disable+" style=\"display:inline-block;\">Prev</button>" +
//                "<h5 style=\"display:inline-block; margin: 0 10px;\">"+pg +"</h5>"+
//                "<button name=\"next\" onclick=\"next()\""+next_disable+" style=\"display:inline-block;\">Next</button>\n";




        // TODO: this should be retrieved from the backend server
        final ArrayList<Movie> movies = new ArrayList<>();
//        movies.add(new Movie("The Terminal", (short) 2004));
//        movies.add(new Movie("The Final Season", (short) 2007));
        //-------------------------------------------------------------------------------------------
        Intent currentIntent = getIntent();
        mtitle = currentIntent.getStringExtra("movie_title");
        page = currentIntent.getStringExtra("page");
        final TextView pageNum = findViewById(R.id.pageNum);
        pageNum.setText(page);
        final Button prev_btn = findViewById(R.id.prev_button);
        final Button next_btn = findViewById(R.id.next_button);
        prev_btn.setOnClickListener(view -> prev());
        next_btn.setOnClickListener(view -> next());

        if (page.equals("1")){
            prev_btn.setClickable(false);
        }
        else{
            prev_btn.setClickable(true);
        }

//        "mtitle", mtitle);
//        params.put("rowCT", "10");
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/fulltext?mtitle="+mtitle+"&rowCT=10&page="+page,
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject countObj = jsonArray.getJSONObject(0);
                        String totalCount = countObj.getString("ct");
                        Log.d("data.row", "" + jsonArray.length());
                        if (10 * Integer.parseInt(page)>=Integer.parseInt(totalCount)){
                            next_btn.setClickable(false);
                        }else{
                            next_btn.setClickable(true);
                        }
                        for (int i = 1; i < jsonArray.length(); i++) {
                            JSONObject movieItem = jsonArray.getJSONObject(i);
                            String movie_id = movieItem.getString("movie_id");
                            String movie_title = movieItem.getString("movie_title");
                            String movie_year = movieItem.getString("movie_year");
                            String movie_director = movieItem.getString("movie_director");
                            String movie_rating = movieItem.getString("movie_rating");
                            String movie_genres_name = movieItem.getString("movie_genres_name");
                            String genresStr = "";
                            if (movie_genres_name != null) {
                                String[] array_genre_name = movie_genres_name.split(",");
                                if (array_genre_name.length != 0) {
                                    int ind = Math.min(3, array_genre_name.length);
                                    for (int j = 0; j < ind - 1; j++) {
                                        genresStr += array_genre_name[j] + ", ";

                                    }
                                    genresStr += array_genre_name[ind-1];

                                }else{
                                    genresStr += "N/A";
                                }
                            }else{
                                genresStr += "N/A";
                            }
                            String movie_stars_name = movieItem.getString("movie_stars_name");
                            String starString = "";
                            if (movie_stars_name != null) {
                                String[] array_star_name = movie_stars_name.split(",");
                                if (array_star_name.length != 0) {
                                    int ind = Math.min(3, array_star_name.length);
                                    for (int j = 0; j < ind - 1; j++) {
                                        starString += array_star_name[j] + ", ";

                                    }
                                    starString += array_star_name[ind-1];

                                }else{
                                    starString += "N/A";
                                }
                            }else{
                                starString += "N/A";
                            }
                            movies.add(new Movie( movie_id,  movie_title, movie_year,
                                    genresStr,  starString,  movie_director, movie_rating));

                        }


                        Log.d("search.success", response);
                        //Complete and destroy login activity once successful



                    } catch (JSONException e) {
                        String error = e.toString();

                        System.out.println(e.toString());
                        Log.d("error:", e.toString());
                        finish();
                        // initialize the activity(page)/destination
                        Intent SearchPage = new Intent(MovieListActivity.this, SearchActivity.class);
                        // activate the list page.
                        startActivity(SearchPage);
                    }
                    MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                    ListView listView = findViewById(R.id.list);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        //TODO: redirect ???
                        Movie movie = movies.get(position);
                        @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s, %s",
                                position, movie.getName(), movie.getYear());
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        finish();
                        // initialize the activity(page)/destination
                        Intent SinglePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                        SinglePage.putExtra("movie_title", mtitle);
                        SinglePage.putExtra("page", page);
                        SinglePage.putExtra("id", movie.getMovieId());
                        // activate the list page.
                        startActivity(SinglePage);
                    });


                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                    finish();
                    // initialize the activity(page)/destination
                    Intent SearchPage = new Intent(MovieListActivity.this, SearchActivity.class);
                    // activate the list page.
                    startActivity(SearchPage);
                })
        {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("mtitle", mtitle);
                params.put("rowCT", "10");
                return params;
            }
        };
        queue.add(searchRequest);
        //--------------------------------------------------------------------------------------

    }

    @SuppressLint("SetTextI18n")
    public void redirect() {
        finish();
        // initialize the activity(page)/destination
        Intent SearchPage = new Intent(MovieListActivity.this, SearchActivity.class);
        SearchPage.putExtra("movie_title", mtitle);

        // activate the list page.
        startActivity(SearchPage);
    }

    @SuppressLint("SetTextI18n")
    public void prev() {
        finish();
        Intent resultPage = new Intent(MovieListActivity.this, MovieListActivity.class);
        resultPage.putExtra("movie_title", mtitle);
        page = ""+(Integer.parseInt(page)-1);
        resultPage.putExtra("page", page);
        startActivity(resultPage);
    }

    @SuppressLint("SetTextI18n")
    public void next() {
        finish();
        Intent resultPage = new Intent(MovieListActivity.this, MovieListActivity.class);
        resultPage.putExtra("movie_title", mtitle);
        page = ""+(Integer.parseInt(page)+1);
        resultPage.putExtra("page", page);
        startActivity(resultPage);
    }
}