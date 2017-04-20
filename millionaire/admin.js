$(document).ready(function() {
	// Kill the form submit so we can handle it per-button
	var form = $("#form");
	$(form).submit(function() { return false; });

	$("#form input[type=submit]").on("click", function() {
		var action = "none"
		switch ($(this).val()) {
		case "Search":
			action = "search";
			break;
		case "Insert":
			action = "insert";
			break;
		case "Update":
			action = "update";
			break;
		case "Delete":
			if (confirm("Are you sure you want to delete this question?") == true) {
				action = "delete";
			} else {
				update_status("Delete aborted");
				return;
			}
			break;
		}
		
		$.post($(form).attr('action'), $(form).serialize() + "&action=" + action, function(response){
			switch (action) {
			case "search": search(response); break;
			case "insert": insert(response); break;
			case "update": update_status(response); break;
			case "delete": delete_button(response); break;
			}
      },"text");
	});

	$("#clear").on("click", function() {
		insert_mode();
	});

	insert_mode();
	update_status("Ready");
});

function insert_mode() {
	$("input").prop("disabled", false);
	$("input").prop("readonly", false);
	$("#update, #delete").prop("disabled", true);
}

function search(response) {
	var data = JSON.parse(response);
	if (data.length == 1) {
		load(data[0]);
		return;
	}
	var tr;
	var table = $("table#table tbody");
	$(table).html("");
	for (var i = 0; i < data.length; ++i) {
		tr = $('<tr>');
		tr.append("<td>" + data[i].id + "</td>");
		tr.append("<td>" + data[i].difficulty + "</td>");
		tr.append("<td>" + data[i].question + "</td>");
		tr.append("<td>" + data[i].a + "</td>");
		tr.append("<td>" + data[i].b + "</td>");
		tr.append("<td>" + data[i].c + "</td>");
		tr.append("<td>" + data[i].d + "</td>");
		tr.append("<td>" + data[i].answer + "</td>");
		tr.append("</tr>");
		$(table).append(tr);
	}
	$(table).find("tr").on("click", function() {
		var cols = $(this).find("td").map(function() {return $(this).text();}).get();
		load({
			id: cols[0],
			difficulty: cols[1],
			question: cols[2],
			a: cols[3],
			b: cols[4],
			c: cols[5],
			d: cols[6],
			answer: cols[7]
		});
	});
}

function insert(data) {
	var response = JSON.parse(data);
	$("input[name='id']").val(response.id);
	update_mode();
	update_status(response.message);
}

function load(data) {
	$("select[name='difficulty']").val(data.difficulty);
	$("input[name='id']").val(data.id);
	$("input[name='question']").val(data.question);
	$("input[name='a']").val(data.a);
	$("input[name='b']").val(data.b);
	$("input[name='c']").val(data.c);
	$("input[name='d']").val(data.d);
	$("input[name='answer']").val(data.answer);

	update_mode();
}

function update_mode() {
	$("input").prop("disabled", false);
	$("input").prop("readonly", false);
	$("#search, #insert").prop("disabled", true);
	$("input[name='id']").prop("readonly", true);
}

function update_status(response) {
	$("#status").text(response);
	$("#status").toggle();
	$("#status").fadeIn("slow");
}

function delete_button(response) {
	$("#clear").click();
	update_status(response);
}
