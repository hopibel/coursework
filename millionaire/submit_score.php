<?php
require_once('db_login.php');
$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$query = "insert into scores (name, score) values('" . mysqli_real_escape_string($conn, $_POST["name"]) . "','" . mysqli_real_escape_string($conn, $_POST["score"]) . "')";

if ($conn->query($query) === TRUE) {
	echo "Score submitted successfully";
} else {
	echo "Score submission failed: " . $conn->error . "\n";
}

$query = "delete from scores where id not in (select * from (select id from scores order by score desc limit 10) as temp)";
if ($conn->query($query) === TRUE) {
	echo "Excess scores deleted\n";
} else {
	echo "Failed to delete scores outside of top ten\n";
}

exit;
?>
