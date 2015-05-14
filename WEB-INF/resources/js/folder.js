
// =======================================================
//                    Directory content
// =======================================================

var Folder = CallbackHandler.extend({
	init : function(renderer) {
		this._super();
		this.renderer = renderer;
		this.in_update = false;
		this.selected = [];
	},
	prepare_update : function() {
		this.in_update = true;
		this.selected = {};
		this.renderer.addClass('blur');
	},
	finish_update : function() {
		this.in_update = false;
		this.renderer.removeClass('blur');

		this.fireEvent('update', this.element);
	},
	update : function(element, parent) {
		var me = this;
		var link;
		
		this.prepare_update();

		if(element.UID == null)
			link = endpoint + 'api/dir/';
		else
			link = endpoint + 'api/dir-id/' + element.UID;
			
		$.ajax({
			type: 'GET',
			url: link,
			dataType : 'json',
			headers : authHeader('vuzi', '1234'),
			success: function(data) {
				me.render(data.data, parent);
				me.finish_update();
			},
			error: function(data, e) {

				// No response JSON
				if(!data.responseJSON) {
					this.fail(data);
					return;
				}
				
				var pop = new Pop_up("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error");
				pop.display();
				me.finish_update();
			},
			fail: function(data) {
				console.log(data);
				
				var pop = new Pop_up("Erreur ", "La requête a échouée", "error");
				pop.display();
				me.finish_update();
			}
		});
	},
	render : function(element, parent) {
		this.element = element;
		
		var me = this;
		var h1 = $('<h1> Contenu de ' + (element.name == null ? '/' : element.path  + element.name) + '</h1>');
		var ul = $('<ul />');
		
		// '..' folder
		if(element.UID != null && parent) {
			new Icon(ul).on('click', function(element, icon, e) {
				me.fireEvent('select', element, event);
			}).on('click_context', function(element, icon, e) {
				me._action_context(element, e);
			}).on('select', function(element, icon, e) {
				me._action_select(element, e);
			}).update(parent, 'parent', '...');
		}
		
		// Directories
		element.directories.forEach(function(directory) {
			new Icon(ul).on('click', function(element, icon, e) {
				me.fireEvent('select', element, event);
			}).on('click_context', function(element, icon, e) {
				me._action_context(element, e);
			}).on('select', function(element, icon, e) {
				me._action_select(element, e);
			}).update(directory, 'folder');
		});
		
		// Files
		element.files.forEach(function(file) {
			new Icon(ul).on('click', function(element, icon, e) {
				me.fireEvent('select', element, event);
			}).on('click_context', function(element, icon, e) {
				me._action_context(element, e);
			}).on('select', function(element, icon, e) {
				me._action_select(element, e);
			}).update(file);
		});
		
		this.renderer.empty().append(h1).append(ul);
	},
	_action_select : function(element, event) {
		if(!this.in_update) {
			if(this.selected[element.UID])
				delete this.selected[element.UID];
			else 
				this.selected[element.UID] = element;
		}
	},
	_action_context : function(element, event) {
		if(!this.in_update) {
			if(Object.keys(this.selected).length > 1 && this.selected[element.UID])
				this.fireEvent('select_context', this.selected, event);
			else
				this.fireEvent('select_context', element, event);
		}
	}
})


// =======================================================
//                          Icon
// =======================================================

var Icon = CallbackHandler.extend({
	init : function(renderer) {
		this._super();
		this.selected = false;
		this.renderer = renderer;
	},
	generateTypeIcon : function(mimeType, filename, path) {
		var icons_dir = "resources/style/icons/";
		var icon_src = "default.png";
		var me = this;

		switch (mimeType) {
			case 'application/x-troff-msvideo':
			case 'video/avi' :
			case 'video/msvideo' :
			case 'video/x-msvideo' :
				// Avi video
				icon_src = "avi.png";
				break;
			case 'text/csv' :
				// CSV file
				icon_src = "csv.png";
				break;
			case 'application/msword' :
			case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' :
			case 'application/vnd.openxmlformats-officedocument.wordprocessingml.template' :
				// Doc
				icon_src = "doc.png";
				break;
			case 'parent':
				icon_src = "parent.png";
				break;
			case 'folder':
				// Folder
				icon_src = "folder.png";
				break;
			case 'image/jpeg' :
				// Jpeg
				icon_src = "jpg.png";
				break;
			case 'audio/mpeg' :
			case 'audio/mp3' :
				// MP3
				icon_src = "mp3.png";
				break;
			case 'application/pdf' :
				// PDF
				icon_src = "pdf.png";
				break;
			case 'image/png' :
				// PNG
				icon_src = "png.png";
				break;
			case 'application/vnd.openxmlformats-officedocument.presentationml.presentation' :
			case 'application/vnd.openxmlformats-officedocument.presentationml.slideshow' :
			case 'application/vnd.openxmlformats-officedocument.presentationml.template' :
				// Ppt
				icon_src = "ppt.png";
				break;
			case 'application/x-rar-compressed' :
				// rar
				icon_src = "rar.png";
				break;
			case 'text/plain' :
				// txt
				icon_src = "txt.png";
				break;
			case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' :
			case 'application/vnd.openxmlformats-officedocument.spreadsheetml.template' :
				// xls
				icon_src = "xls.png";
				break;
			case 'application/zip' :
				// Zip
				icon_src = "zip.png";
				break;
		}

		var icon_path = endpoint + icons_dir + icon_src;
		this.image = $('<div style="background-image: url(' + icon_path + ');"/>');

		if(mimeType.match("^image")) {
			var image_path = endpoint + 'api/file-bin' + path + filename;

			this.image.on('mouseover', function() {
				me.image.css('background-image', 'url(' + image_path + ')');
			});
			this.image.on('mouseout', function() {
				me.image.css('background-image', 'url(' + icon_path + ')');
			});

			return $('<a href="#"></a>').
						append($('<figure class="image" ></figure>').
							append(this.image).
							append($('<figcaption>' + filename + '</figcaption>')));
		} else {
			return $('<a href="#"></a>').
						append($('<figure></figure>').
							append(this.image).
							append($('<figcaption>' + filename + '</figcaption>')));
		}
	},
	update : function(element, type, name) {
		this.element = element;

		var me = this;
		var icon = this.generateTypeIcon(type || element.type, name || element.name, element.path);

		icon.on(window.onMobile() ? 'click' : 'dblclick', function(e) {
			me.fireEvent('click', me.element, me, e);
			e.preventDefault();
		});

		// Context menu
		icon.bind('contextmenu', function(e) {
			me.fireEvent('click_context', me.element, me, e);
			e.preventDefault();
		});
		
		// Select
		icon.on('click', function(e) {
			icon.toggleClass('selected');

			if(!me.selected) {
				me.fireEvent('select', me.element, me, e);
				me.selected = true;
			}
			e.preventDefault();
		});
		
		this.renderer.append($('<li />').append(icon));
	}
})
