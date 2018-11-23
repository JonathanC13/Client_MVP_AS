
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
//$result = $db->query("SELECT * FROM floor_collection_0");

//$mysqli->query("SELECT * FROM floor_collection_0");

// Prepare a select statement
$query = "SELECT * FROM img_collection";
//$stmt = $mysqli->prepare($query);

// execute the statement
//$stmt->execute();

// close statement
//$stmt->close();

//

// SELECT
if ($result = $mysqli->query($query)) {

    // an array for the floor_rows
    $response["door_icons"] = array();

    // floor_collection_0 table columns
    //  idfloor | nm_floor | or_floor | img_path | iddoor
    //output a row here
    while ($row = $result->fetch_row()){
      //printf("id: %s, name: %s\n", $row[0], $row[1] );
      $icon_rows = array();

      $icon_rows["path_img"] = $row[2];

      // push single floor into final response array
      array_push($response["door_icons"], $icon_rows);
    }

    // on success, tag it
    $response["success"] = 1;

    echo json_encode($response);

} else {
  // no floors found
    $response["success"] = 0;
    $response["message"] = "No icons found";

    // echo no users JSON
    echo json_encode($response);
}

?>
