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
    function connect() {
        // import database connection variables

        require_once __DIR__ . '/db_config.php';

        // Connecting to mysql server and select database
        $con = mysqli_connect(DB_SERVER, DB_USER, DB_PASSWORD, DB_DATABASE);


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
