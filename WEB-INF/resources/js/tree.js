
// =======================================================
//                  Directory tree node
// =======================================================

function DirectoryTreeNode(parent, tree) {
	this.tree = tree;
	this.parent = parent;
	this.contained = [];
	this.is_developped = false;
	this.is_selected = false;
}

DirectoryTreeNode.prototype.add = function(node) {
	if(this.contained.length <= 0)
	this.node.append(this.content); // Only append if the directory is not empty
	
	this.contained.push(node);
	this.content.append(node.node);
}

DirectoryTreeNode.prototype.update = function(directory) {
	this.directory = directory;
	this.render();
}

DirectoryTreeNode.prototype.render = function() {
	var me = this;
	var directory = this.directory;
	
	var li = $('<li rel="' + directory.UID + '" />')
	var button = $('<span />').addClass('developped');
	var ul = $('<ul style="display: none"/>');
	
	if(directory.name) {
		var name = $('<a href="#" > ' + directory.name + '</a>');
	} else {
		var name = $('<a href="#" class="root" > Root </a>');
	}
	
	li.append(button).append(name);
	
	name.on('click', function(e) {
		me.tree.select(directory.UID);

		e.preventDefault();
	});
	
	button.on('click', function(e) {
		if(button.is(e.target)) {
			if(me.developped)
				me.shrink();
			else
				me.develop(); // Develop self
		}
	});
	
	// Context menu
	name.bind('contextmenu', function(e) {
		me.tree.select(directory.UID);
		me.tree._action_context(directory, e);
		
		e.preventDefault();
	});
	
	this.node = li;
	this.button = button;
	this.content = ul;
}

DirectoryTreeNode.prototype.develop = function(clbk) {
	this.developped = true;
	
	var me = this;
	var f = function() {
		me.button.removeClass('developped');
		me.content.slideDown(function() {
			if(clbk)
				clbk();
		});
	};
	
	if(this.parent)
		this.parent.develop(f); // Develop parent before, then self
	else
		f(); // Only develop self
}

DirectoryTreeNode.prototype.shrink = function(clbk) {
	this.developped = false;
	
	this.button.addClass('developped');
	this.content.slideUp(function() {
		if(clbk)
			clbk();
	});
}

DirectoryTreeNode.prototype.select = function(ignorePushState) {
	this.selected = true;
	
	ignorePushState = ignorePushState || false;
	
	if(this.tree.selected) {
		this.tree.selected.deselect();
	}
	
	if(!ignorePushState) {
		if(this.directory.name)
			window.history.pushState(this.directory, "Title", endpoint + '~' + this.directory.path + this.directory.name);
		else
			window.history.pushState(this.directory, "Title", endpoint + '~/');
	}
	
	this.node.addClass('selected');

	this.tree.selected = this;         // Register as the selected node 
	this.tree._action(this.directory); // Actions

	this.develop(); // Develop on selection
}

DirectoryTreeNode.prototype.deselect = function() {
	this.selected = false;
	
	this.tree.selected = null;
	this.node.removeClass('selected');
}

// =======================================================
//                    Directory tree
// =======================================================

function DirectoryTree(renderer, action, action_context) {
	this.renderer = renderer;
	this.action = action;
	this.action_context = action_context;
	this.nodes = [];
	this.selected = null;
}

DirectoryTree.prototype.render = function(data) {
	var ul = $('<ul />');
	var root_node = new DirectoryTreeNode(null, this);
	root_node.update(data);
	
	this._render_rec(root_node, data);
	this.renderer.empty().append(ul.append(root_node.node));
	
	this.nodes[null] = root_node;
}

DirectoryTree.prototype._render_rec = function(parent, data) {
	var me = this;
	
	data.directories.forEach(function(directory) {
		var node = new DirectoryTreeNode(parent, me);
		node.update(directory);
		
		parent.add(node);
		me.nodes[directory.UID] = node;
		
		if(directory.directories.length > 0) {
			me._render_rec(node, directory);
		}
	});
}

DirectoryTree.prototype.select_path = function(path, pushState) {
	if(!path || path == '/')
	this.select(null, pushState);
	
	
	for (node in this.nodes) {
		node = this.nodes[node];
		
		var path_test = node.directory.path + node.directory.name;
		
		if(path_test == path || (path_test + '/') == path) {
			node.select(pushState);
			break;
		}
		
	}
}

DirectoryTree.prototype.select = function(UID, pushState) {
	var node = this.nodes[UID];
	
	if(!node)
		return;
	
	node.select(pushState);
}

DirectoryTree.prototype._action = function(element, event) {
	if(this.action)
		this.action(element, this.selected, event);
}

DirectoryTree.prototype._action_context = function(element, event) {
	if(this.action_context)
		this.action_context(element, this.selected, event);
}

DirectoryTree.prototype.update = function(clbk) {
	var me = this;
	$.ajax({
		type: 'GET',
		url: endpoint + 'api/tree',
		dataType : 'json',
		headers : authHeader('vuzi', '1234'),
		success: function(data) {
			me.render(data.data);
			
			if(me.selected)
				me.select(me.selected.directory.UID);
			else
				me.select(null);
			
			if(clbk)
				clbk(me);
		},
		error: function(data) {
			console.log(data);
			var pop = new Pop_up("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error");
			pop.display();
		},
		fail: function(data) {
			console.log(data);
			var pop = new Pop_up("Erreur ", "La requête a échouée", "error");
			pop.display();
		}
	});
}
