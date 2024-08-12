
function handleResult(resultData) {

    console.log("handleResult: confirmation.js");
    console.log("resultData.length " + resultData.length);
    console.log("resultData " + resultData);

    let bodyElement = jQuery("#info_table_body");

    // tableElement.append("<p>Customer Id: " + resultData[0]["customerId"] + "</p>"); //???
    for (let i = 0; i < resultData.length; i++)
    {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["saleId"] + "</th>" ;
        rowHTML += "<th>" + resultData[i]["title"] + "</th>" ;
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>" ;
        rowHTML += "<th>" + "$" + resultData[i]["price"] + "</th>" ;
        rowHTML += "<th>" + "$" + resultData[i]["total"] + "</th>" ;
        rowHTML += "</tr>";
        bodyElement.append(rowHTML);
    }
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<th></th> <th></th> <th></th> <th></th>"
    let total = 0;
    for (let i = 0; i < resultData.length; i++)
        total +=  resultData[i]["total"];
    rowHTML += "<th>" + "$" + total + "</th>"
    rowHTML += "</tr>";
    bodyElement.append(rowHTML);

}

function handleErrorResult(resultData) {
    console.log(resultData);
}
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "POST",// Setting request method
    url: "api/confirmation", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData), // Setting callback function to handle data returned successfully by the SingleStarServlet
    error: (resultData) => handleErrorResult(resultData)
});

