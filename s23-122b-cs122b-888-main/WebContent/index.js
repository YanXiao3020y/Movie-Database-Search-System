/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
let update_form = $("#update_form");
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
function parseHTML(){
    let urlString = "";
    let titlePrefix = getParameterByName('titlePrefix');
    urlString += titlePrefix != null? "titlePrefix=" +titlePrefix:"";
    let genreID = getParameterByName('genreID');
    urlString += genreID != null? "genreID=" +genreID: "";
    //added----------------------------------------
    let mtitle = getParameterByName('mtitle');
    urlString += mtitle != null? "mtitle=" +mtitle: "";
    let titleID = getParameterByName('titleID');
    urlString += titleID != null? "titleID=" +titleID: "";
    //added---------------------------------------
    if (titlePrefix == null && genreID == null && mtitle == null && titleID == null){
        let title = getParameterByName('title');
        urlString += title != null? "title=" +title:"title=";
        let year = getParameterByName('year');
        urlString += year != null? "&year=" +year:"&year=";
        let director = getParameterByName('director');
        urlString += director != null? "&director=" +director:"&director=";
        let star = getParameterByName('star');
        urlString += star != null? "&star=" +star:"&star=";

    }
    return urlString
}
function parseMetaData(){
    let mpp_str = getParameterByName('rowCT');
    let order_str = getParameterByName('sort');
// if (mpp_str =! null) {
//     document.getElementById("mpp").value = mpp_str;
//     document.getElementById("sort").value = order_str;
// }
    let extra = "";
    extra += mpp_str != null? "&rowCT=" +mpp_str:"";

    extra += order_str != null? "&sort=" +order_str: "";
    return extra;
}
function parsePageInfo(){
    let page = "";
    let pageNum = getParameterByName('page');
    page += pageNum != null? "&page=" +pageNum:"";
    return page;

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
function prev(){
    let urlSub = parseHTML();
    let meta = parseMetaData();
    let page = getParameterByName("page");
    let pageCheck = 1;
    if (page != null) {
        pageCheck = parseInt(page);
    }
    if (pageCheck != 1) {
        pageCheck -= 1;
        let pageNum = pageCheck.toString();
        window.location.href = "index.html?" + urlSub + meta + "&page=" + pageNum;
    }

}
function next(){
    let urlSub = parseHTML();
    let meta = parseMetaData();
    let page = getParameterByName("page");
    let pageCheck = 1;
    if (page != null) {
        pageCheck = parseInt(page);
    }
    pageCheck += 1;
    let pageNum = pageCheck.toString();
    window.location.href = "index.html?" + urlSub + meta + "&page=" + pageNum;


}
function handleMovieResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");
    console.log(resultData.length);

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 1; i <  resultData.length; i++) {


        // String[] array_movie_genres_name = movie_genres_name.split(","); //split string to array
        // int size_movie_genres_name = Math.min(3, array_movie_genres_name.length); //find min size
        // movie_genres_name = "";
        // for(int i=0; i<size_movie_genres_name-1; i++)
        // movie_genres_name += array_movie_genres_name[i] + ", ";
        // movie_genres_name += array_movie_genres_name[size_movie_genres_name-1];


        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
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

        let genre_name = resultData[i]["movie_genres_name"];
        if (genre_name != null) {
            const array_genre_name = genre_name.split(",");
            const array_genre_id = resultData[i]["movie_genres_id"].split(",");
            if (array_genre_name.length != 0) {
                const ind = Math.min(3, array_genre_name.length);
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
        // rowHTML += "<th>" + resultData[i]["movie_genres_name"] + "</th>";//still need work???

        let stars_name = resultData[i]["movie_stars_name"];
        if (stars_name != null) {
            const array_stars_name = stars_name.split(",");
            // console.log(array_stars_name[0]);
            const array_stars_id = resultData[i]["movie_stars_id"].split(",");
            if (array_stars_name.length != 0) {
                const ind = Math.min(3, array_stars_name.length)

                rowHTML += "<th>";

                for (let j = 0; j < ind - 1; j++) {
                    rowHTML +=
                        // Add a link to single-movie.html with id passed with GET url parameter
                        '<a href="single-star.html?id=' + array_stars_id[j] + '">'
                        + array_stars_name[j] + ", " +    // display movie_name for the link text
                        '</a>';

                }//----------
                rowHTML +=
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-star.html?id=' + array_stars_id[ind - 1] + '">'
                    + array_stars_name[ind - 1] +     // display movie_name for the link text
                    '</a>' +
                    "</th>";

            }else
            {
                rowHTML += "<th>N/A</th>";
            }
        }else{
            rowHTML += "<th>N/A</th>";
        }

        // rowHTML += "<th>" + resultData[i]["movie_stars_name"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] +  "⭐</th>";
        rowHTML += "<th>" + '<button onclick = addToShoppingCart(\''+ resultData[i]['movie_id'] + '\')' + ">" + "Add to Cart" + '</button>' + "</th>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    const page = ''
    let pnButton = jQuery("#prevNext");
    let pg = getParameterByName("page");
    let row = getParameterByName("rowCT");
    let row_num = row != null? parseInt(row): 25;
    let prev_disable = "";
    let next_disable = "";
    if (pg == null || pg == "1"){
        prev_disable = " disabled";
        pg = "1";
    }
    let max = parseInt(resultData[0]["ct"]);
    let pg_num = parseInt(pg);
    if (row_num * pg_num >= max){
        next_disable = " disabled";
    }
    // let content = "<button name=\"prev\" onclick=\"prev()\""+prev_disable+">Prev</button>" + "<h5>"+pg +"</h5>"+
    //     "<button name=\"next\" onclick=\"next()\""+next_disable+">Next</button>\n";
    let content = "<button name=\"prev\" onclick=\"prev()\""+prev_disable+" style=\"display:inline-block;\">Prev</button>" +
        "<h5 style=\"display:inline-block; margin: 0 10px;\">"+pg +"</h5>"+
        "<button name=\"next\" onclick=\"next()\""+next_disable+" style=\"display:inline-block;\">Next</button>\n";

    pnButton.append(content);
}



/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
//This is for movie/genre index
let head = "api/movies?"

let urlSubstring = parseHTML();

let sortInfo = parseMetaData();


let pageInfo = parsePageInfo();

function submitUpdateForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    let moviesPerPage = $('#mpp').val();
    // document.getElementById("mpp").value = moviesPerPage;
    let order = $('#order').val();
    // document.getElementById("order").value = order;
    urlSubstring += "&rowCT=" + moviesPerPage;
    urlSubstring += "&sort="+ order;
    window.location.href = "index.html?" + urlSubstring;
    // $.ajax(urlString, {
    //     method: "GET",
    //     // data: {title: movieTitle, action: "delete"}
    // });
    // location.reload();
}
update_form.submit(submitUpdateForm);

// console.log(urlString);
//=" + titleID + "&genre=" + genreID

let mtitle = getParameterByName("mtitle");
let titleID = getParameterByName("titleID");
if (mtitle != null){
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/fulltext?mtitle=" + mtitle + sortInfo + pageInfo, // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}else if(titleID != null){
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/fulltext?titleID=" + titleID + sortInfo + pageInfo, // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
else {


// Makes the HTTP GET request and registers on success callback function handleMovieResult
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: head + urlSubstring + sortInfo + pageInfo, // Setting request url, which is mapped by MoviesServlet in MoviesServlet.java
        success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}
