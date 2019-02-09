<?php
/*
  * WAMPServer to host PHP to connect to SQL sever management studio server.
  *   `- Needed:  + SQLsrv driver (since driver for mySQL is different). To configure, in php.ini place "extension = 72_x64.dll"
  *               + ODBC update
  * NOT WORKING, just use mySQL

  // Using default drivers for mySQL lib for PHP
    `- this works`

  */
/**
 * A class file to connect to database
 * USE: $db = new DB_CONNECT(); // creating class object(will open database connection)
 */

    /**
     * Function to connect with database
     */
    function connect($choose_server) {
        // import database connection variables

        require_once __DIR__ . '/db_config.php';

/*
        if($serverNm == "10.0.2.2"){
          $serverName = "localhost";
        } else {
          $serverName = $serverNm;
        }
*/
        if($choose_server == "LOGIN"){
          $serverName = DB_SERVER_LOGINS;
        } else if($choose_server == "FLOOR"){
          $serverName = DB_SERVER_FLOORS;
        }

        // Connecting to mysql server and select database
        $con = mysqli_connect($serverName, DB_USER, DB_PASSWORD, DB_DATABASEFLOORS);


        // Check connection
        if (mysqli_connect_errno())
        {
        echo "Failed to connect to MySQL: " . mysqli_connect_error();
        }

        // returing connection cursor
        return $con;
    }

    function close($curr){
      mysqli_close($curr);
    }


?>
