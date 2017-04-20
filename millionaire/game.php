<html>
	<meta content="text/html;charset=utf-8" http-equiv="Content-Type">
	<head>
		<title>Who Wants to be a Millionaire</title>

		<script>
			var name = <?php echo json_encode(isset($_POST["name"]) ? $_POST["name"] : "Anonymous"); ?>;
		</script>
		<script src="../jquery-1.12.2.min.js"></script>
		<script src="millionaire.js"></script>
	</head>
	<body>
		<link rel="stylesheet" href="game.css" type="text/css">
		<button class="walkButton" id="walkaway">Walk Away</button>
		<div id="game" style="position: relative;">
			<div id="levels">
				<br><br>
				<ul>
					<li><b>$1,000,000</b></li>
					<li>$500,000</li>
					<li>$250,000</li>
					<li>$125,000</li>
					<li>$64,000</li>
					<li><b>$32,000</b></li>
					<li>$16,000</li>
					<li>$8,000</li>
					<li>$4,000</li>
					<li>$2,000</li>
					<li><b>$1,000</b></li>
					<li>$500</li>
					<li>$300</li>
					<li>$200</li>
					<li>$100</li>
				</ul>
			</div>

			<div>
				<br><br>
				<p id="question">Loading questions...</p><br><br>
				<button class="ansButton" id=a>A.</button>
				&nbsp; &nbsp; &nbsp;
				<button class="ansButton" id=b>B.</button>
				<br><br><br>
				<button class="ansButton" id=c>C.</button>
				&nbsp; &nbsp; &nbsp;
				<button class="ansButton" id=d>D.</button>
				<br><br><br>
			</div>
			
			<div id="lifelines">
					<button id="fifty">Fifty-Fifty</button> 
					&nbsp; &nbsp;
					<button id="phone">Phone a Friend</button> 
					&nbsp; &nbsp;
					<button id="audience">Ask the Audience</button>
			</div>

				<div id="phone-screen">
					test
				</div>
	
				<div id="audience-screen">
					test
				</div>
		</div>
	</body>
</html>
