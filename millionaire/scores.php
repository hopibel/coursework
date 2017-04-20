<html>
	<meta content="text/html;charset=utf-8" http-equiv="Content-Type">
	<head>
		<title>High Scores - Who Wants to be a Millionaire</title>
		<link rel="stylesheet" href="scores.css" type="text/css">
	</head>
	<body>
		<br><br>
		<h1 align="center">HIGH SCORES</h1>
		<br><br>
		<table>
<?php
require_once('db_login.php');
$conn = new mysqli($servername, $username, $password, $database);

if ($conn->connect_error) {
	die("Connection failed: " . $conn->connect_error);
}

$query = "select * from scores order by score desc, name";
$result = $conn->query($query);
$rows = array();
while ($r = mysqli_fetch_assoc($result)) {
	$rows[] = $r;
}
echo <<<EOF
<tr class='header'>
<td class='rank'>Rank</td>
<td class='name'>Name</td>
<td class='score'>Score</td>
</tr>
EOF;

for ($i = 0; $i < count($rows); ++$i) {
$j = $i + 1;
echo <<<EOF
<tr>
	<td class="rank">$j.</td>
	<td class="name">{$rows[$i]["name"]}</td>
	<td class="score">{$rows[$i]["score"]}</td>
</tr>
EOF;
}
?>
		</table>
	</body
</html
