
// =======================================================
//                   Contextual menu
// =======================================================

function ContextualMenu(folder, tre0) {
	var me = this;
	this.tree = tree;
	this.folder = folder;
	this.content = $('<div id="context_menu"></div>');
	
	this.content.mouseleave(function() {
		me.content.fadeOut(200);
	});
}

ContextualMenu.prototype.display = function(elements, x, y) {
	var infos = $('<ul></ul>');
	var actions = $('<ul></ul>');
	var me = this;

	elements.forEach(function(element) {
		if(element.info) {
			infos.append($('<li class="info"><span>' + element.name + '</span><span>' + element.value + '</span></li>'));
		} else if(element.button) {
			var action = $('<li><span class="glyphicon glyphicon-' + element.icon + '" />' + element.name + '</li>');
			action.on('click', function(e) {
				if(element.action)
					element.action(e);
				me.content.fadeOut(200);
			});
			actions.append(action);
		}
	});
	
	this.content.empty().append(infos).append(actions);
	$('body').append(this.content);
	
	var height = this.content.height();
	var window_height = $(window).height();
	
	var width = this.content.width();
	var window_width = $(window).width();
	
	if(y + height > window_height)
		y = window_height - height;
	
	if(x + width > window_width)
		x = window_width - width;
	
	this.content.css({ left : x - 5, top : y - 5});
	this.content.fadeIn(200);
}