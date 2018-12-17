<?php

    $correct_flag = 0;

    if(isset($_POST['ip']) && isset($_POST['user']) && isset($_POST['pass'])){

      $serIP = (string)$_POST['ip'];

      if($serIP == "10.0.2.2"){
        $serIP = "localhost";
      }
      //$serIP = "localhost";
      define('SERVERNAME', $serIP);

      $user = $_POST['user'];
      $pass = $_POST['pass'];
      //$user = "James01";
      //$pass = "pass2";

      // include db connect class
      require_once __DIR__ . '/db_config.php';

      $tb_employee = TB_EMPLOYEETABLE;

      $mysqli = mysqli_connect(SERVERNAME, DB_USER, DB_PASSWORD, DB_DATABASEEMPLOYEE);

      // connecting to db
      //$mysqli = connect();

      // Could do multiple queries for each level of validation. DB has user, using where -> get password to compare -> if all valid, get card number
      // For now just pull all rows in order and compare. Impact performance, but by using stored procedures it prevents injection.

      $query = "SELECT * FROM $tb_employee";
      //echo $query;
      // Check if non empty, != 0 is true in PHP
      if ($result = $mysqli->query($query)) {
        while($row = $result->fetch_row()){
          if(strcmp($row[0], $user) == 0){
            // When user is matched, compare the given password
            if(strcmp($row[1], $pass) == 0){
              // if password correct
              // an array for the floor_rows
              $response["cardinfo"] = array();

              $cardSeq = array();
              $cardSeq['card'] = $row[2];

              array_push($response["cardinfo"], $cardSeq);

              // on success, tag it
              $response["success"] = 1;

              $correct_flag = 1;
              echo json_encode($response);

              break;

            } else {
              // password not correct
              $response["success"] = 0;
              echo json_encode($response);
              break;
            }
          }
        }
      } else {
        // query Failed
        $response["success"] = 0;
        echo json_encode($response);
      }

      mysqli_close($mysqli);
    }

    if($correct_flag == 0){

      $response["success"] = 0;
      echo json_encode($response);
    }


?>
