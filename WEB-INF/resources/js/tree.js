
// =======================================================
//                  Directory tree node
// =======================================================

var DirectoryTreeNode = CallbackHandler.extend({
	init : function(parent) {
		this._super();
		this.parent = parent;
		this.contained = [];
		this.developped = false;
		this.selected = false;
	},
	add : function(node) {
		if(this.contained.length <= 0)
			this.node.append(this.content); // Only append if the directory is not empty
		
		this.contained.push(node);
		this.content.append(node.node);
	},
	update : function(directory) {
		this.directory = directory;
		this.render();
	},
	render : function() {
		var me = this;
		var directory = this.directory;
		
		var li = $('<li rel="' + directory.UID + '" />')
		var button = $('<span />').addClass('developped');
		var ul = $('<ul style="display: none"/>');
		
		if(directory.name) {
			var name = $('<a href="#" > ' + directory.name + '</a>');
		} else {
			var name = $('<a href="#" class="root" > Racine </a>');
		}
		
		li.append(button).append(name);
		
		// Selection
		name.on('click', function(e) {
			me.select(e);

			e.preventDefault();
		});
		
		// Develop
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
			me.select(e);
			me.fireEvent('select_contextual', me.directory, me, e);
		
			e.preventDefault();
		});
		
		this.node = li;
		this.button = button;
		this.content = ul;
	},
	develop : function(clbk) {
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
	},
	shrink : function(clbk) {
		this.developped = false;
		
		this.button.addClass('developped');
		this.content.slideUp(function() {
			if(clbk)
				clbk();
		});
	},
	select : function(e, pushState) {
		this.selected = true;
		
		this.node.addClass('selected');
		this.fireEvent('select', this.directory, this, e, pushState);
		this.develop(); // Develop on selection
	},
	deselect : function(e) {
		this.selected = false;
		
		this.node.removeClass('selected');
		this.fireEvent('deselect', this.directory, this, e);
	}
});

// =======================================================
//                    Directory tree
// =======================================================

var DirectoryTree = CallbackHandler.extend({
	init : function(renderer) {
		this._super();

		this.renderer = renderer;
		this.nodes = [];
		this.selected = null;
	},
	render : function(data) {
		var me = this;

		var ul = $('<ul />');
		var root_node = new DirectoryTreeNode(null);

		root_node.update(data);
		root_node.on('select', function(element, node, e, pushState) {

			if(me.selected && me.selected != node)
				me.selected.deselect();

			me.selected = node;
			me.fireEvent('select', element, node, e, pushState);
		}).on('select_contextual', function(element, node, e) {
			me.fireEvent('select_contextual', element, node, e);
		});
		
		this._render_rec(root_node, data);
		this.renderer.empty().append(ul.append(root_node.node));
		
		this.nodes[null] = root_node;
	},
	_render_rec : function(parent, data) {
		var me = this;
		
		data.directories.forEach(function(directory) {
			var node = new DirectoryTreeNode(parent);
			node.update(directory);
			node.on('select', function(element, node, e) {

				if(me.selected && me.selected != node)
					me.selected.deselect();

				me.selected = node;
				me.fireEvent('select', element, node, e);
			}).on('select_contextual', function(element, node, e) {
				me.fireEvent('select_contextual', element, node, e);
			});
			
			parent.add(node);
			me.nodes[directory.UID] = node;
			
			if(directory.directories.length > 0) {
				me._render_rec(node, directory);
			}
		});
	},
	select_path : function(path, pushState) {
		if(!path || path == '/')
				this.select(null);

		for (node in this.nodes) {
			node = this.nodes[node];
			
			var path_test = node.directory.path + node.directory.name;
			
			if(path_test == path || (path_test + '/') == path) {
				node.select(pushState);
				break;
			}
		}
	},
	select : function(UID, pushState) {
		var node = this.nodes[UID];
		
		if(!node)
			return;
		
		node.select(pushState);
	},
	update : function(clbk) {
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
});
