let credit_card_form = $("#credit_card_form");

/**
 * Handle the data returned by creditCardServlet
 * @param resultDataString jsonObject
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
function handleCreditCardResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle credit card response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If credit card succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success")
        window.location.replace("confirmation.html");
    else
    {
        // If credit card fails, the web page will display
        // error messages on <div> with id "credit_card_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#credit_card_error_message").text(resultDataJson["message"]);
    }


}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
// let payment = getParameterByName("totalPrice")
function submitCreditCardForm(formSubmitEvent) {
    console.log("submit credit card form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the credit card form to the data sent by POST request
            data: credit_card_form.serialize(),
            success: handleCreditCardResult
        }
    );
}


let totalPrice = getParameterByName("totalPrice")
console.log("totalPrice: " + totalPrice);

let finalPayment = jQuery('#finalPayment');
let rowHTML = "";
rowHTML += "Final Payment: $";
rowHTML += totalPrice;
// clear the old array and show the new array in the frontend
finalPayment.html("");
finalPayment.append(rowHTML);
console.log("rowHTML: " + rowHTML);
console.log("finalPayment: " + finalPayment);

// Bind the submit action of the form to a handler function
credit_card_form.submit(submitCreditCardForm);

