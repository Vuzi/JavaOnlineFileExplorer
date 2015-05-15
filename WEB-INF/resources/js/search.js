
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
	},
	render : function() {
		this.content = $('<ul></ul>');
		this.renderer.append(this.content);
	},
	add : function(values, results) {
		var me = this;

		var li = $('<li><span class="glyphicon glyphicon-search" href="#"></span>' + values.search + ' (' + (results ? results.length : 0) + ')</li>');
		this.content.prepend(li);

		// Only keep 10 elements
		if(this.results.length > 10) {
			this.results.pop().li.remove();
		}

		var element = {
			li : li,
			value : values,
			results : results
		};
		this.results.unshift(element);

		li.on('click', function(e) {
			me.fireEvent('select', element, e);
		});

		this.deselect(); // TODO
		li.addClass('selected');
	},
	select : function(result) {
		// TODO
	},
	deselect : function(result) {
		this.content.children().removeClass('selected');
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
		headers : authHeader('vuzi', '1234'),
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