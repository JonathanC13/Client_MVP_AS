<?php
//$par_url = 'http://localhost:8080/door_app_images/';
$par_url = 'http://';
$folder_img = '/door_app_images/';

// app file directory
//$img_folder = $_GET["string1"]; // need to get from android
//$img_folder = '/data/user/0/com.example.jonathan.client_mvp/files/images/';

function download(){

  if(isset($_POST['folder'])){

    $img_folder = $_POST['folder'];
    // include db connect class
    //require_once __DIR__ . '/db_connect.php';
    include 'db_connect.php';

    $par_url .= DB_SERVER . ':' . DB_PORT . folder_img;

    // connecting to db
    $mysqli = connect();

    // <img_colleciton>
    // FIrst download images from img_collection
    // idimg | nm_img | path_img
    // Prepare a select statement
    $query = "SELECT * FROM img_collection";

    if ($result = $mysqli->query($query)) {
      while ($row = $result->fetch_row()){
        // set url for each image
        $full_url = $par_url;
        $full_url .= $row[2];

        // download the image to app image folder
        $save_loc = $img_folder;
        $save_loc .= $row[2];
        echo $save_loc;
        //echo $full_url;
        //echo $save_loc;
        file_put_contents($save_loc, file_get_contents($full_url));
      }
    }

    // <img_collection>

    // <floor collection images>
    //  idfloor | nm_floor | or_floor | img_path | iddoor
    $query = "SELECT * FROM floor_collection_0";

    if ($result = $mysqli->query($query)) {
      while ($row = $result->fetch_row()){

        // set url for each image
        $full_url = $par_url;
        $full_url .= $row[3];

        // download the image to app image folder
        $save_loc = $img_folder;
        $save_loc .= $row[3];
        echo $save_loc;
        //echo $full_url;
        //echo $save_loc;
        file_put_contents($save_loc, file_get_contents($full_url));
      }
    }

    close($mysqli);
    // </floor collection images>
  }
}

?>
