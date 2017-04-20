var Millionaire = function(name) {
	this.current = 0;
	this.player = name;
	this.questions;
	this.used = {
		fifty: false,
		fifty_prev: false,
		phone: false,
		audience: false
	};
	this.money = [100, 200, 300, 500, 1000, 2000, 4000, 8000, 16000, 32000, 64000, 125000,
		250000, 500000, 1000000];
};

Millionaire.prototype = {
	start: function() {
		$.ajax({
			url: "get_questions.php",
			dataType: "json",
			context: this,
			success: function(data) {
				this.questions = data;
				this.display_question();
			}
		});
	},

	display_question: function() {
		$("#levels ul li").css("background-color","transparent");
		$("#levels ul li").eq(14-this.current).css("background-color","#ff9900");

		$("#question").text(this.questions[this.current].question);
		$("#a").text("A. " + this.questions[this.current].a);
		$("#b").text("B. " + this.questions[this.current].b);
		$("#c").text("C. " + this.questions[this.current].c);
		$("#d").text("D. " + this.questions[this.current].d);

		if(this.used.fifty_prev == true) {
			var that = this;
			$(".ansButton").off();
			$("#a").on("click", function() {that.check("a")});
			$("#b").on("click", function() {that.check("b")});
			$("#c").on("click", function() {that.check("c")});
			$("#d").on("click", function() {that.check("d")});
			this.used.fifty_prev = false;
		}
	},

	check: function(answer) {
		if(answer == this.questions[this.current].answer) {
			if(this.current == 14) {
				++this.current;
				this.end(this.current);
			} else if (this.current < 14) {
				this.display_question(++this.current);
			}
		} else {
			alert("You lose. Correct answer: " + this.questions[this.current].answer);
			var round = 0;
			if (this.current >= 10) {
				round = 10;
			} else if (this.current >= 5) {
				round = 5;
			}
			this.end(round);
		}
	},

	fifty: function() {
		if(this.used.fifty) return;
		var items = $(".ansButton");
		for(var i = 0; i < items.length; ++i) {
			if($(items[i]).attr("id") === this.questions[this.current].answer) {
				items.splice(i, 1);
				break;
			}
		}
		items.splice(Math.floor(Math.random()*items.length), 1);
		for(var i = 0; i < items.length; ++i) {
			$(items[i]).text("X");
			$(items[i]).off();
		}
		this.used.fifty = true;
		this.used.fifty_prev = true;
	},

	phone: function() {
		//easy: 100
		//medium: 75
		//hard: 50
		if(this.used.phone) return;
		var phone = $("#phone-screen");
		var choice = "abcd";
		if(this.current >= 10) {
			if(Math.random() < 1/3) {
				phone.text("The answer is " + this.questions[this.current].answer.toUpperCase());
			} else {
				phone.text("The answer is " + choice.charAt(Math.floor(Math.random()*choice.length)).toUpperCase());
			}
		} else if(this.current >= 5) {
			if(Math.random() < 2/3) {
				phone.text("The answer is " + this.questions[this.current].answer.toUpperCase());
			} else {
				phone.text("The answer is " + choice.charAt(Math.floor(Math.random()*choice.length)).toUpperCase());
			}
		} else {
			phone.text("The answer is " + this.questions[this.current].answer.toUpperCase());
		}
		phone.css({"visibility":"visible"});
		this.used.phone = true;
	},

	audience: function() {
		/*
		easy: 70-90%
		medium: 30-50%
		hard: 25-30%
		instead of removing the correct answer from the choices, we adjust the probability "X"
		such that X+(1-X)*.25 = desired probability
		*/

		if(this.used.audience) return;

		var votes = {
			a: 0,
			b: 0,
			c: 0,
			d: 0
		};
		var choice = "abcd";
		var audience = $("#audience-screen");
		for(var i = 0; i < 100; ++i) {
			if(this.current >= 10) {
				if(Math.random() < ((25+Math.random()*5)-25)/75) {
					++votes[this.questions[this.current].answer];
				} else {
					++votes[choice.charAt(Math.floor(Math.random()*choice.length))];
				}
			} else if(this.current >= 5) {
				if(Math.random() < ((30+Math.random()*20)-25)/75) {
					++votes[this.questions[this.current].answer];
				} else {
					++votes[choice.charAt(Math.floor(Math.random()*choice.length))];
				}
			} else {
				if(Math.random() < ((70+Math.random()*20)-25)/75) {
					++votes[this.questions[this.current].answer];
				} else {
					++votes[choice.charAt(Math.floor(Math.random()*choice.length))];
				}
			}
		}
		audience.css({"visibility":"visible"});
		audience.html("A: " + votes.a + "%<br>"
			+ "B: " + votes.b + "%<br>"
			+ "C: " + votes.c + "%<br>"
			+ "D: " + votes.d + "%");
	},

	end: function(round) {
		var score = 0;
		if (round > 0) {
			score = this.money[round - 1];
			$.ajax({
				url: "submit_score.php",
				dataType: "json",
				method: "post",
				data: {
					"name": name,
					"score": score,
				},
			});
		}

		alert("You won $" + score);
		location.replace("scores.php");
	}
};

$(document).ready(function(){
	var game = new Millionaire(name);

	$("#game button").off();
	$("#a").on("click", function() {game.check("a")});
	$("#b").on("click", function() {game.check("b")});
	$("#c").on("click", function() {game.check("c")});
	$("#d").on("click", function() {game.check("d")});

	$("#fifty").on("click", function() {game.fifty()});
	$("#phone").on("click", function() {game.phone()});
	$("#audience").on("click", function() {game.audience()});

	$("#phone-screen").on("click", function() {$("#phone-screen").toggle()});
	$("#audience-screen").on("click", function() {$("#audience-screen").toggle()});

	$("#walkaway").on("click", function() {game.end(game.current)});

	game.start();
});
