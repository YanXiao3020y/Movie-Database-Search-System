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

function handleInsertStarResult(resultDataString) {
    let resultData = JSON.parse(resultDataString);

    console.log("handle insert star response");
    console.log(resultData["status"]);

    let insert_star_message = jQuery("#insert_star_message");
    let rowHTML = ""
    if (resultData["status"] === "success")
    {
        rowHTML = "<p>" + resultData["message"] + '</p>';
    }
    else
    {
        rowHTML = "<p>Error adding new star</p>";
        console.log(resultData["errorMessage"]);
    }
    insert_star_message.append(rowHTML);

}

let star_form = $("#insert_star_form");
function submitLoginForm(formSubmitEvent) {
    console.log("insert star");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/insert-star", {
            method: "GET", // Setting request method
            data: star_form.serialize(),
            success: handleInsertStarResult // Setting callback function to handle data returned successfully by the MoviesServlet
        });
}

// Bind the submit action of the form to a handler function
star_form.submit(submitLoginForm);





