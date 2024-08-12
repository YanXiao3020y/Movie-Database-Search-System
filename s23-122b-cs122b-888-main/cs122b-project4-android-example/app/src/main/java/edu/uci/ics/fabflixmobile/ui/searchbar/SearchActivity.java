package edu.uci.ics.fabflixmobile.ui.searchbar;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;
//import com.google.gson.JsonObject;
import edu.uci.ics.fabflixmobile.R;


import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    private EditText searchText;
    private String search_info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_main_page);

        Intent cache = getIntent();
        String searchString = cache.getStringExtra("movie_title");
        searchText = findViewById(R.id.search);
        if (searchString != null){
            searchText.setText(searchString);
        }
        search_info = searchText.getText().toString();

        final Button searchButton = findViewById(R.id.search_button);

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> searchMovie());

    }
    @SuppressLint("SetTextI18n")
    void searchMovie(){
//        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        Log.d("search", search_info);

        searchText = findViewById(R.id.search);
        search_info = searchText.getText().toString();
        //Complete and destroy login activity once successful
        finish();
        // initialize the activity(page)/destination
        Intent MovieListPage = new Intent(SearchActivity.this, MovieListActivity.class);
        MovieListPage.putExtra("movie_title", search_info);
        MovieListPage.putExtra("page", "1");
        // activate the list page.
        startActivity(MovieListPage);

    }

}
