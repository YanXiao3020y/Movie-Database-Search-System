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

function handleInsertMovieResult(resultDataString) {
    let resultData = JSON.parse(resultDataString);

    console.log("handle insert movie response");
    console.log(resultData["status"]);

    let insert_movie_message = jQuery("#insert_movie_message");
    let rowHTML = ""
    if (resultData["status"] === "success")
    {
        rowHTML = "<p>" + resultData["message"] + '</p>';
    }
    else
    {
        rowHTML = "<p>Success! Error: Duplicated movie!</p>";
        console.log(resultData["errorMessage"]);
    }
    insert_movie_message.append(rowHTML);

}

let movie_form = $("#insert_movie_form");
function submitLoginForm(formSubmitEvent) {
    console.log("insert movie");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/insert-movie", {
            method: "GET", // Setting request method
            data: movie_form.serialize(),
            success: handleInsertMovieResult // Setting callback function to handle data returned successfully by the MoviesServlet
        });
}

// Bind the submit action of the form to a handler function
movie_form.submit(submitLoginForm);





