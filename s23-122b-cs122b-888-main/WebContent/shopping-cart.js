
function addQuantity(movieTitle)
{
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, action: "add"},
    });
    location.reload();
}

function subtractQuantity(movieTitle)
{
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, action: "subtract"}
    });
    location.reload();
}

function deleteItem(movieTitle)
{
    $.ajax("api/modify-cart", {
        method: "POST",
        data: {title: movieTitle, action: "delete"}
    });
    location.reload();
}

function handleSessionData(resultDataString) {
    // let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataString);
    console.log(resultDataString["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataString["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataString["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataString["previousItems"]);
}

function handleCartArray(resultArray) {
    console.log(resultArray);
    let cart_list = jQuery("#cart_list");
    // change it to html list
    let rowHTML = "";
    for (let i = 0; i < resultArray.length; i++) {
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultArray[i]["title"] + "</th>";

        let next_disable = "disabled";
        if (resultArray[i]["quantity"] > 1)
            next_disable = "";

        rowHTML += "<th>" +
            "<button  display: inline-block' onclick=\"subtractQuantity('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\""+ next_disable + ">-</button>\n";
        rowHTML += "<p  style='color: red; display: inline-block'> " + resultArray[i]["quantity"] + "  </p>"; //not sure why space or \t does not work???
        rowHTML +=
            "<button onclick=\"addQuantity('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\">+</button>\n" +
            "</th>";

        rowHTML += "<th>" +
            "<button  onclick=\"deleteItem('" + resultArray[i]['title'] + "', '" +
            "added_to_cart" + i + "')\">Delete</button>\n" +
            "</th>";

        rowHTML += "<th>" + "$" + parseInt(resultArray[i]["price"])  + "</th>";
        rowHTML += "<th>" + "$" + parseInt(resultArray[i]["price"])*parseInt(resultArray[i]["quantity"]) + "</th>";

        rowHTML += "</tr>";
    }

    //display total price
    rowHTML += "<tr>";
    rowHTML += "<th></th><th></th><th></th><th></th>";
    rowHTML += "<th>";
    let total = 0;
    for (let i = 0; i < resultArray.length; i++)
        total += parseInt(resultArray[i]["price"]) * parseInt(resultArray[i]["quantity"]);
    rowHTML += "$" + total; // add total value to the rowHTML
    rowHTML += "</th>";
    rowHTML += "</tr>";


    // clear the old array and show the new array in the frontend
    cart_list.html("");
    cart_list.append(rowHTML);
    let proceedPayment = jQuery("#proceedPayment");
    let next_disable = "disabled";
    if (resultArray[0]["quantity"] > 1)
        next_disable = "";
    proceedPayment.append("<button><a href=\"payment.html?totalPrice="+ total + "\" class=\"button\" style=\"font-size: x-large\""+ next_disable +">Proceed to Payment</a></button>\n")

}

$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});