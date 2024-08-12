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


console.log("Metadata");
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => handleMetadataResult(resultData)
});

function handleMetadataResult(resultData) {
    console.log("metadata");
    let databaseNameElement = jQuery("#database_name");
    databaseNameElement.append("<p>Metadata of Moviedb Database</p>");


    let metadataTableBodyElement = jQuery("#metadata_table_body");

    for (let i = 0; i < resultData.length; i++) {
        console.log(i + resultData[i]["attributes"]);

        let rowHTML = "<p style='text-align: center;'><i>" + resultData[i]["name"] + "</i></p>";
        let attributes = resultData[i]["attributes"];

        for (let j = 0; j < attributes.length; j++) {
            rowHTML += "<p style='text-align: center;'>" + attributes[j]["field"] + " " + attributes[j]["type"] + "</p>";
        }

        rowHTML += "<br><br>";
        metadataTableBodyElement.append(rowHTML);
    }


    // for (let i = 0; i < resultData.length; i++)
    // {
    //     let rowHTML = "<p style='text-align: center;'>" + resultData[i]["name"] + "</p>";
    //     rowHTML += "<table><tbody>";
    //     let attributes = resultData[i]["attributes"];
    //     for (let j = 0; j < attributes.length; j++)
    //     {
    //         rowHTML += '<thread>';
    //         rowHTML += "<tr>" + attributes[j]["field"] + "</tr>";
    //         rowHTML += "<tr>" + attributes[j]["type"]  + "</tr>";
    //         rowHTML += '</thread>';
    //     }
    //     rowHTML += "</tbody></table>";
    //     rowHTML += "<br>"
    //     metadataTableBodyElement.append(rowHTML);
    // }
}


