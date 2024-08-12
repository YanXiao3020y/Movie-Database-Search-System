package edu.uci.ics.fabflixmobile.ui.single_movie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import edu.uci.ics.fabflixmobile.R;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.searchbar.SearchActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import com.google.gson.JsonObject;


import javax.xml.transform.Result;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity{

//    https://ec2-18-221-109-181.us-east-2.compute.amazonaws.com:8443/cs122b-project1-api-example/index.html?mtitle=good&page=2
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
        setContentView(R.layout.activity_single_movie);
        final TextView title = findViewById(R.id.title1);
        final TextView year = findViewById(R.id.year1);
        final TextView rating = findViewById(R.id.rating1);
        final TextView director = findViewById(R.id.director1);
        final TextView genres = findViewById(R.id.genres1);
        final TextView stars = findViewById(R.id.stars1);
        final Button back_btn = findViewById(R.id.back_button2);
        back_btn.setOnClickListener(view -> redirect());
        Intent cache = getIntent();
        mtitle = cache.getStringExtra("movie_title");
        page = cache.getStringExtra("page");
        String id = cache.getStringExtra("id");
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest singleMovieReq = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id="+id,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        Log.d("data.num", ""+jsonArray.length());
                        JSONObject movieItem = jsonArray.getJSONObject(1);
//                        String movie_id = movieItem.getString("movie_id");
                        String movie_title = movieItem.getString("movie_title");
                        String movie_year = movieItem.getString("movie_year");
                        String movie_director = movieItem.getString("movie_director");
                        String movie_rating = movieItem.getString("movie_rating");
                        String movie_genres_name = movieItem.getString("movie_genres_name");
                        String movie_stars_name = movieItem.getString("movie_stars_name");
                        title.setText(movie_title);
                        year.setText(movie_year);
                        director.setText("Director: " + movie_director);
                        rating.setText(movie_rating);
                        genres.setText("Genres: "+movie_genres_name);
                        stars.setText("Stars: " + movie_stars_name);



                    }catch(Exception e){
                        Log.d("error.json",e.toString());
                        redirect();
                    }
                },
                error ->{
                    redirect();
                }
        );

        queue.add(singleMovieReq);



    }
    @SuppressLint("SetTextI18n")
    public void redirect() {
        finish();
        // initialize the activity(page)/destination
        Intent ResultPage = new Intent(SingleMovieActivity.this, MovieListActivity.class);
        ResultPage.putExtra("movie_title", mtitle);
        ResultPage.putExtra("page", page);
        // activate the list page.
        startActivity(ResultPage);
    }
}
