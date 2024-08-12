/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {


        // String[] array_movie_genres_name = movie_genres_name.split(","); //split string to array
        // int size_movie_genres_name = Math.min(3, array_movie_genres_name.length); //find min size
        // movie_genres_name = "";
        // for(int i=0; i<size_movie_genres_name-1; i++)
        // movie_genres_name += array_movie_genres_name[i] + ", ";
        // movie_genres_name += array_movie_genres_name[size_movie_genres_name-1];


        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        // rowHTML +=
        //     "<th>" +
        //     // Add a link to single-movie.html with id passed with GET url parameter
        //     '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
        //     + resultData[i]["movie_title"] +     // display movie_name for the link text
        //     '</a>' +
        //     "</th>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_name for the link text
            '</a>' +
            "</th>";
        // rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres_name"] + "</th>";

        let stars_name = resultData[i]["movie_stars_name"];
        const array_stars_name = stars_name.split(",");
        // console.log(array_stars_name[0]);
        const array_stars_id = resultData[i]["movie_stars_id"].split(",");
        if (array_stars_name.length != 0) {
            const ind = Math.min(3, array_stars_name.length)

            rowHTML +=
                "<th>"

            for (let j = 0; j < ind-1; j++) {
                rowHTML +=
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-star.html?id=' + array_stars_id[j] + '">'
                    + array_stars_name[j] + ", " +    // display movie_name for the link text
                    '</a>';

            }
            rowHTML +=
                // Add a link to single-movie.html with id passed with GET url parameter
                '<a href="single-star.html?id=' + array_stars_id[ind-1] + '">'
                + array_stars_name[ind-1] +     // display movie_name for the link text
                '</a>' +
                "</th>";

        }

        rowHTML += "<th>" + resultData[i]["movie_rating"] +  "⭐</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top2Movies", // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});