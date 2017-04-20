<?php
require_once("db_login.php");

$conn = new mysqli($servername, $username, $password, $database);
if ($conn->connect_error)
{
	die("Connection failed: " . $conn->connect_error);
}

switch ($_POST["action"]) {
case "search":
	$query = "select * from questions where 1";
	if (!isset($_POST["ignore-diff"]) && !empty($_POST["difficulty"])) {
		$query .= " and difficulty='" . mysqli_real_escape_string($conn, $_POST["difficulty"]) . "'";
	}
	if (!empty($_POST["id"])) {
		$query .= " and id='" . mysqli_real_escape_string($conn, $_POST["id"]) . "'";
	}
	if (!empty($_POST["question"])) {
		$query .= " and question='" . mysqli_real_escape_string($conn, $_POST["question"]) . "'";
	}
	if (!empty($_POST["a"])) {
		$query .= " and a='" . mysqli_real_escape_string($conn, $_POST["a"]) . "'";
	}
	if (!empty($_POST["b"])) {
		$query .= " and b='" . mysqli_real_escape_string($conn, $_POST["b"]) . "'";
	}
	if (!empty($_POST["c"])) {
		$query .= " and c='" . mysqli_real_escape_string($conn, $_POST["c"]) . "'";
	}
	if (!empty($_POST["d"])) {
		$query .= " and d='" . mysqli_real_escape_string($conn, $_POST["d"]) . "'";
	}
	if (!isset($_POST["ignore-answer"]) && !empty($_POST["answer"])) {
		$query .= " and answer='" . mysqli_real_escape_string($conn, $_POST["answer"]) . "'";
	}
	$query .= " order by id";
	$result = $conn->query($query);
	$rows = array();
	while ($r = mysqli_fetch_assoc($result)) {
		$rows[] = $r;
	}
	echo json_encode($rows);

	break;
case "insert":
	$query = "insert into questions (difficulty, question, a, b, c, d, answer)";
	$val = array();
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["difficulty"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["question"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["a"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["b"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["c"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["d"]) . "'";
	$val[] = "'" . mysqli_real_escape_string($conn, $_POST["answer"]) . "'";
	$query .= " values(" . join(",", $val) . ")";
	if ($conn->query($query) === TRUE) {
		echo '{"message":"Record successfully inserted", "id": ' . mysqli_insert_id($conn) . '}';
	} else {
		echo '{"message":"Error inserting record: "' . $conn->error.  ', "id":"0"}';
	}
	break;
case "update":
	$query = "update questions set ";
	$val = array();
	if (!empty($_POST["difficulty"])) {
		$val[] = "difficulty='" . mysqli_real_escape_string($conn, $_POST["difficulty"]) . "'";
	}
	if (!empty($_POST["question"])) {
		$val[] = "question='" . mysqli_real_escape_string($conn, $_POST["question"]) . "'";
	}
	if (!empty($_POST["a"])) {
		$val[] = "a='" . mysqli_real_escape_string($conn, $_POST["a"]) . "'";
	}
	if (!empty($_POST["b"])) {
		$val[] .= "b='" . mysqli_real_escape_string($conn, $_POST["b"]) . "'";
	}
	if (!empty($_POST["c"])) {
		$val[] .= "c='" . mysqli_real_escape_string($conn, $_POST["c"]) . "'";
	}
	if (!empty($_POST["d"])) {
		$val[] .= "d='" . mysqli_real_escape_string($conn, $_POST["d"]) . "'";
	}
	if (!empty($_POST["answer"])) {
		$val[] .= "answer='" . mysqli_real_escape_string($conn, $_POST["answer"]) . "'";
	}
	$query .= join(",", $val) . " where id='" . $_POST["id"] . "'";
	if ($conn->query($query) === TRUE) {
		echo "Record updated successfully";
	} else {
		echo "Error updating record: " . $conn->error;
	}
	break;
case "delete":
	$query = "delete from questions where id='" . $_POST["id"] . "'";
	if ($conn->query($query) === TRUE) {
		echo "Record deleted successfully";
	} else {
		echo "Error deleting record: " . $conn->error;
	}
	break;
}

$conn->close();
?>
