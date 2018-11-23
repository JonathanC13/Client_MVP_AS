<?php

/*
 * Following code will list all the products
 */

// array for JSON response
$response = array();

// include db connect class
//require_once __DIR__ . '/db_connect.php';
include 'db_connect.php';

// connecting to db
$mysqli = connect();

// get all products from products table
$query = "SELECT * FROM door_collection_0";

// check for empty result
if ($result = $mysqli->query($query)) {
    // looping through all results
    // products node
    $response["arr_doors"] = array();

    while ($row = $result->fetch_row()){
        // temp user array
        $doors = array();
        $doors["iddr"] = $row[0];
        $doors["nm_door"] = $row[1];
        $doors["iddoor"] = $row[2];
        $doors["MarginLeft"] = $row[3];
        $doors["MarginTop"] = $row[4];
        $doors["MarginRight"] = $row[5];
        $doors["MarginBot"] = $row[6];
        $doors["IP_door"] = $row[7];

        // push single product into final response array
        array_push($response["arr_doors"], $doors);
    }
    // success
    $response["success"] = 1;

    // echoing JSON response
    echo json_encode($response);
} else {
    // no products found
    $response["success"] = 0;
    $response["message"] = "No products found";

    // echo no users JSON
    echo json_encode($response);
}
?>
