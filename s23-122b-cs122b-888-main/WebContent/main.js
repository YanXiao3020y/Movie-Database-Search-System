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
let search_form = $("#search_bar");
let ft_search_form = $("#ft_search_bar");
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {


    // populate the star info h3
    // find the empty h3 body by id "star_info"



    console.log("handleResult: populating genre list from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let genreElement = jQuery("#genre_list");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let genreHTML = "";
        genreHTML += '<a href="index.html?genreID=' + resultData[i]["genre_id"] + '"><p>' + resultData[i]["genre_name"] + '</p></a>';
        genreElement.append(genreHTML);
    }

    console.log("handleResult: populating alphabet list");
    let alphabetElement = jQuery("#alphabet_list");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 65; i < 91; i++) {
        let alphabetHTML = "";
        let temp = String.fromCharCode(i);
        alphabetHTML += '<a href="index.html?titlePrefix=' + temp + '"><p>' + temp + '</p></a>';
        alphabetElement.append(alphabetHTML);
    }
    alphabetElement.append('<br>');
    for (let i = 48; i < 58; i++) {
        let alphabetHTML = "";
        let temp = String.fromCharCode(i);
        alphabetHTML += '<a href="index.html?titlePrefix=' + temp + '"><p>' + temp + '<p/></a>';
        alphabetElement.append(alphabetHTML);
    }
    alphabetElement.append('<br>');
    let alphabetHTML = "";
    alphabetHTML += '<a href="index.html?titlePrefix=*"><p>*</p></a>';
    alphabetElement.append(alphabetHTML);

}

function search(searchEvent){
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    searchEvent.preventDefault();
    let title = document.getElementsByName('movie_title')[0].value;
    let year = document.getElementsByName('year')[0].value;
    let director = document.getElementsByName('director')[0].value;
    let star = document.getElementsByName('star')[0].value;
    window.location.href = "index.html?title=" + title + "&year=" + year + "&director=" + director + "&star=" +star;
    // $.ajax(
    //
    //     "api/main", {
    //         method: "GET",
    //         // Serialize the login form to the data sent by POST request
    //         data: search_form.serialize(),
    //         // success: handleLoginResult
    //     }
    // );
}
function fullText(searchEvent){
    console.log("submit full text search form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    searchEvent.preventDefault();
    let title = document.getElementsByName('full_text_title')[0].value;

    window.location.href = "index.html?mtitle=" + title;

}

//----------------------------------------------------------------------------
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here
    let retrievedData = sessionStorage.getItem("autocomplete");
    let result;
    if (retrievedData != null){
        let jsonData = JSON.parse(retrievedData);
        // console.log("into the 1 if")
        // console.log(retrievedData)
        // console.log("jsonData[query]: " + JSON.stringify(jsonData[query]))
        if (jsonData[query] !== undefined){
            // console.log("into the 2 if")
            console.log("query existing in cache")
            var myJsonString = JSON.stringify(jsonData[query]);
            let jData = JSON.parse(myJsonString);
            console.log("Used Suggested List (from cache):" + JSON.stringify(jData));
            doneCallback( { suggestions: jData } );
            return;
        }

    }
    // console.log("out the 1 if")

    console.log("sending AJAX request to backend Java Servlet")

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?autotitle=" + escape(query),
        "datatype": "json",
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")
    console.log("type:" + typeof(data))
    // console.log("data:" + data[0]["value"])

    var myJsonString = JSON.stringify(data);
    // parse the string into JSON
    var jsonData = JSON.parse(myJsonString);
    // console.log(jsonData)
    console.log("Used Suggested List (from ajax):" + JSON.stringify(jsonData));


    // TODO: if you want to cache the result into a global variable you can do it here
    let retrievedData = sessionStorage.getItem("autocomplete");
    if (retrievedData == null){
        retrievedData = {};
    }else{
        retrievedData = JSON.parse(retrievedData);
    }
    retrievedData[query] = jsonData;
    sessionStorage.setItem("autocomplete", JSON.stringify(retrievedData));
    // {"autocomplete":{query:data}}
    // {query:data}
    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"])
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieID"];
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars:3
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
    window.location.href = "index.html?mtitle=" + query;
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})
//----------------------------------------------------------------------------
search_form.submit(search);
ft_search_form.submit(fullText);
/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
// let titleID = getParameterByName('title');
// let genreID = getParameterByName('genre');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/main", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});