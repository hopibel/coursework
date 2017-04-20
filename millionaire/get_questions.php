<?php
require_once('db_login.php');
$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$query = "(select * from questions where difficulty='easy' order by rand() limit 5)
union (select * from questions where difficulty='medium' order by rand() limit 5)
union (select * from questions where difficulty='hard' order by rand() limit 5)";

$result = $conn->query($query);
$rows = array();
while ($r = mysqli_fetch_assoc($result)) {
	$rows[] = $r;
}
echo json_encode($rows);
?>
