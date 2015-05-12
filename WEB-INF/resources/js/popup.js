
// =======================================================
//                      Pop up handler
// =======================================================

function Pop_up(title, message, type) {
	this.title = title;
	this.message = message;
	this.type = type || 'info';
}

Pop_up.prototype.display = function() {
	var div = $('<div class="pop_up ' + this.type + ' bouncy"></div>');
	div.append('<h2>' + this.title + '</h2>');
	div.append('<p>' + this.message + '</p>');
	
	pop_up_back.fadeIn();
	global.addClass('blur');
	div.addClass('bounce');
	pop_up_back.append(div);
	
	setTimeout(function() {
		div.removeClass('bounce');

		setTimeout(function() {
			div.slideUp(200, function() {
				div.remove();
				if(pop_up_back.is(':empty')) {
					global.removeClass('blur');
					pop_up_back.fadeOut();
				}
			});
		}, 200);
	}, 2000);
}

// =======================================================
//                      Wiondow handler
// =======================================================

function Pop_up_window(title, content) {
	this.title = title;
	this.content = content;
}

Pop_up_window.prototype.close = function() {
	var div = this.div;
	
	div.removeClass('bounce');

	setTimeout(function() {
		div.slideUp(200, function() {
			div.remove();
			if(pop_up_back.is(':empty')) {
				global.removeClass('blur');
				pop_up_back.fadeOut();
			}
		});
	}, 200);
}

Pop_up_window.prototype.toggle_dissmiss = function() {
	this.dissmiss.toggle();
}

Pop_up_window.prototype.display = function() {
	var me = this;
	var div = $('<div class="pop_up bouncy"></div>');
	var dissmiss = $("<a href='#' class='close'><img src='" + endpoint + "resources/style/icons/close_button.png'/>");
	div.append('<h2>' + this.title + '</h2>');
	div.append(dissmiss);
	div.append(this.content);
	this.div = div;
	this.dissmiss = dissmiss;
	
	pop_up_back.fadeIn();
	global.addClass('blur');
	div.addClass('bounce');
	pop_up_back.append(div);
	
	div.on('keyup', function(e) {
		if(e.which == 27) {
			me.close();
		}
	});

	dissmiss.on('click', function(e) {
		me.close();
	});
	
	pop_up_back.fadeIn();
	global.addClass('blur');
	pop_up_back.append(div);
}