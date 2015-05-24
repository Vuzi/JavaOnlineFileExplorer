
// =======================================================
//                  Search list element
// =======================================================

var SearchListElement = CallbackHandler.extend({
	init : function(renderer, value, results) {
		this._super();

		this.renderer = renderer;
		this.selected = false;

		this.value = value;
		this.results = results;
	},
	update : function() {
		this.render();
		return this;
	},
	render : function() {
		var me = this;

		this.content = $('<li><span class="glyphicon glyphicon-search" href="#"></span>' + (this.value ? this.value.search : '-') + ' (' + (this.results ? this.results.length : 0) + ')</li>');
		this.renderer.prepend(this.content);
		this.content.on('click', function(e) {
			me.select(e);
		});
		return this;
	},
	select : function(e) {
		if(!this.selected) {
			this.fireEvent('select', this, this.results, e);
			this.content.addClass('selected');
			this.selected = true;
		}
		return this;
	},
	deselect : function(e) {
		if(this.selected) {
			this.fireEvent('deselect', this, this.results, e);
			this.content.removeClass('selected');
			this.selected = false;
		}
		return this;
	}
})

// =======================================================
//                     Search list
// =======================================================

var SearchList = CallbackHandler.extend({
	init : function(renderer) {
		this._super();

		this.renderer = renderer;
		this.results = [];
		this.selected = null;
		this.max_size = 10;
	},
	update : function() {
		this.render();
		return this;
	},
	render : function() {
		this.content = $('<ul></ul>');
		this.renderer.append(this.content);
		return this;
	},
	add : function(value, results) {
		var me = this;

		var node = new SearchListElement(this.content, value, results);
		node.on('select', function(node, result, e) {
			if(me.selected)
				me.selected.deselect();

			me.selected = node;
			me.fireEvent('select', node, result, e);
		});

		// Only keep 10 elements
		if(this.results.length > 10) {
			this.results.pop().content.remove();
		}

		this.results.unshift(node);
		node.update().select();
		return this;
	},
	deselect: function() {
		if(this.selected)
			this.selected.deselect();
		this.selected = null;
		return this;
	}
})

var lastPath = '/';
var performSearch = function(value, tree, folder, searchList) {

	// To Handle
	var values = {
		search : value,
		element : 'both',
		recursive : true,
		order : 'name_asc'
	};

	// URI
	var URI = 'api/search' + (tree.selected ? ( tree.selected.directory.name ? tree.selected.directory.path + tree.selected.directory.name : '/') : lastPath);

	// Prepare
	if(tree.selected) {
		lastPath = ( tree.selected.directory.name ? tree.selected.directory.path + tree.selected.directory.name : '/');
		tree.deselect();
	}
	folder.prepare_update();
	tree.nodes[null].shrink();

	// Perform the request
	$.ajax({
		type: 'POST',
		url: endpoint + URI,
		dataType : 'json',
		processData : false,
		data : JSON.stringify(values),
		contentType : 'application/json',
		success: function(data) {
			values.path = lastPath;
			
			folder.updateFrom(data.data, values);
			folder.finish_update();

			searchList.add(values, data.data);
		},
		error: function(data) {

			if(!data.responseJSON) {
				this.fail(data);
				return;
			}

			var pop = new Toast("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error");
			pop.display();
		},
		fail: function(data) {
			var pop = new Toast("Erreur ", "La requête a échouée", "error");
			pop.display();
		}
	});
}