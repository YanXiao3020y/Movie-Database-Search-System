/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

// for the button of "Add to Cart"
function addToShoppingCart(movieId, rowMessageId)
{
    $.ajax("api/cart", {
        method: "POST",
        data: {id: movieId},
        success: function () {
            console.log("Success: Added to Shopping Cart!");
            alert("Success: Added to Shopping Cart!");
        },
        error: function() {
            console.log("Failure: Could not add to Shopping Cart");
            alert("Failure: Could not add to Shopping Cart");
        }
    });
}


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {

    console.log("handleResult: single-movie.js");
    console.log("resultData.length " + resultData.length);
    let link = jQuery("#movie_list");
    if (Object.keys(resultData[0]).length > 0){
        let sessionUrl = "index.html?";
        let titlePrefix = resultData[0]["titlePrefix"];
        let genreID = resultData[0]["genreID"];
        let title = resultData[0]["title"];
        let year = resultData[0]["year"];
        let director = resultData[0]["director"];
        let star = resultData[0]["star"];
        let rowCT = resultData[0]["rowCT"];
        let sort = resultData[0]["sort"];
        let page = resultData[0]["page"];

        //added
        let mtitle = resultData[0]["mtitle"];
        let titleID = resultData[0]["titleID"];
        //---------------

        if (titlePrefix != null){
            sessionUrl += "titlePrefix=" +titlePrefix;
        }else if (genreID != null){
            sessionUrl += "genreID=" + genreID;
        }else if(mtitle != null){
            sessionUrl += "mtitle=" + mtitle;
        }else if (titleID != null){
            sessionUrl += "titleID=" + titleID;
        }else{
            sessionUrl += "title=" + title + "&year=" + year + "&director=" + director + "&star=" + star;
        }
        if (rowCT != null){
            sessionUrl += "&rowCT=" + rowCT;
        }
        if (sort != null){
            sessionUrl += "&sort=" + sort;
        }
        if (page != null){
            sessionUrl += "&page=" + page;
        }
        link.append('<a href="' + sessionUrl + '">Movie List</a>');
    }
    else
    {
        link.append('<a href="index.html">Movie List</a>');
    }


    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Movie Name: " + resultData[1]["movie_title"] + "</p>" +
        "<p>( " + resultData[1]["movie_year"] + ")</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "detail_table_body"
    let movieTableBodyElement = jQuery("#detail_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th>" + resultData[1]["movie_director"] + "</th>";
    let genre_name = resultData[1]["movie_genres_name"];
    if (genre_name != null) {
        const array_genre_name = genre_name.split(",");
        const array_genre_id = resultData[1]["movie_genres_id"].split(",");
        if (array_genre_name.length != 0) {
            const ind = Math.min(3, array_genre_name.length)
            rowHTML += "<th>";
            for (let j = 0; j < ind - 1; j++) {
                rowHTML +=
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="index.html?genreID=' + array_genre_id[j] + '">'
                    + array_genre_name[j] + ', ' +    // display movie_name for the link text
                    '</a>';

            }
            rowHTML +=
                // Add a link to single-movie.html with id passed with GET url parameter
                '<a href="index.html?genreID=' + array_genre_id[ind - 1] + '">'
                + array_genre_name[ind - 1] +     // display movie_name for the link text
                '</a>' +
                "</th>";

        }else{
            rowHTML += "<th>N/A</th>";
        }
    }else{
        rowHTML += "<th>N/A</th>";
    }
    // rowHTML += "<th>" + resultData[1]["movie_genres_name"].split(',').join(', ') + "</th>";

    let stars_name = resultData[1]["movie_stars_name"];
    if (stars_name != null) {
        const array_stars_name = stars_name.split(",");
        // console.log(array_stars_name[0]);
        const array_stars_id = resultData[1]["movie_stars_id"].split(",");
        if (array_stars_name.length != 0) {
            const ind = array_stars_name.length;

            rowHTML +=
                "<th>";

            for (let j = 0; j < ind - 1; j++) {
                rowHTML +=
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-star.html?id=' + array_stars_id[j] + '">'
                    + array_stars_name[j] + ", " +    // display movie_name for the link text
                    '</a>';

            }
            rowHTML +=
                // Add a link to single-movie.html with id passed with GET url parameter
                '<a href="single-star.html?id=' + array_stars_id[ind - 1] + '">'
                + array_stars_name[ind - 1] +     // display movie_name for the link text
                '</a>' +
                "</th>";

        }
        else
            rowHTML += "<th>N/A</th>";
    }else{
        rowHTML += "<th>N/A</th>";
    }
    
    rowHTML += "<th>" + resultData[1]["movie_rating"] + "⭐️</th>";
    rowHTML += "<th>" + '<button onclick = addToShoppingCart(\''+ resultData[1]['movie_id'] + '\')' + ">" + "Add to Cart" + '</button>' + "</th>";

    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
    
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});