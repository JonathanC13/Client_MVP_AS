
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
$query = "SELECT * FROM floor_collection_0";
//$stmt = $mysqli->prepare($query);

// execute the statement
//$stmt->execute();

// close statement
//$stmt->close();

//

// SELECT
if ($result = $mysqli->query($query)) {

    // an array for the floor_rows
    $response["arr_floor_rows"] = array();

    // floor_collection_0 table columns
    //  idfloor | nm_floor | or_floor | img_path | iddoor
    //output a row here
    while ($row = $result->fetch_row()){
      //printf("id: %s, name: %s\n", $row[0], $row[1] );
      $floor_rows = array();

      $floor_rows["idfloor"] = $row[0];
      $floor_rows["nm_floor"] = $row[1];
      $floor_rows["or_floor"] = $row[2];
      $floor_rows["img_path"] = $row[3];
      $floor_rows["iddoor"] = $row[4];

      // push single floor into final response array
      array_push($response["arr_floor_rows"], $floor_rows);
    }

    // on success, tag it
    $response["success"] = 1;

    echo json_encode($response);

} else {
  // no floors found
    $response["success"] = 0;
    $response["message"] = "No floors found";

    // echo no users JSON
    echo json_encode($response);
}

close($mysqli);
/*
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all results
    // products node
    $response["products"] = array();

    while ($row = mysql_fetch_array($result)) {
        // temp user array
        $product = array();
        $product["idFloor"] = $row["idFloor"];
        $product["Name"] = $row["Name"];
        $product["FloorOrder"] = $row["FloorOrder"];
        $product["Image_floor"] = $row["Image_floor"];
        $product["DoorID"] = $row["DoorID"];

        // push single product into final response array
        array_push($response["products"], $product);
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
*/

?>
