<?php
$serverName = "localhost"; // Π.χ. "localhost\SQLEXPRESS"
$connectionOptions = array(
    "Database" => "Pelatologio",
    "Uid" => "sa",
    "PWD" => "admin",
	"CharacterSet" => "UTF-8"
);

// Σύνδεση με MSSQL
$conn = sqlsrv_connect($serverName, $connectionOptions);
if (!$conn) {
    die(print_r(sqlsrv_errors(), true));
}
?>
