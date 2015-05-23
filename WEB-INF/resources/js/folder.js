
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
	updateFrom : function(elements, values) {
		var tmp = { files : [], directories : [] };

		elements.forEach(function(element) {
			if(element.size)
				tmp.files.push(element);
			else
				tmp.directories.push(element);
		});

		this.render(tmp, null, values);
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
				
				var pop = new Toast("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error");
				pop.display();
				me.finish_update();
			},
			fail: function(data) {
				console.log(data);
				
				var pop = new Toast("Erreur ", "La requête a échouée", "error");
				pop.display();
				me.finish_update();
			}
		});
	},
	render : function(element, parent, search) {
		this.element = element;
		
		var me = this;
		var h1 = (search ? $('<h1> Résultat de la recherche "' + search.search + '" dans "' + search.path + '" </h1>')
			             : $('<h1> Contenu de ' + (element.name == null ? '/' : element.path  + element.name) + '</h1>'));
		var ul = $('<ul />');
		
		// '..' folder
		if(element.UID != null && parent) {
			this._render_event(new Icon(ul)).update(parent, 'parent', '...', false);
		}
		
		// Directories
		element.directories.forEach(function(directory) {
			me._render_event(new Icon(ul)).update(directory, 'folder');
		});
		
		// Files
		element.files.forEach(function(file) {
			me._render_event(new Icon(ul)).update(file);
		});
		
		this.renderer.empty().append(h1).append(ul);
	},
	_render_event : function(icon) {
		var me = this;
		return icon.on('click', function(element, icon, e) {
				me.fireEvent('select', element, e);
			}).on('click_context', function(element, icon, e) {
				me._action_context(element, e);
			}).on('select', function(element, icon, e) {
				me._action_select(icon, e);
			}).on('deselect', function(element, icon, e) {
				me._action_select(icon, e);
			}).on('dragstart', function(element, icon, e) {
				me._action_drag_start(icon, e);
			}).on('dragend', function(element, icon, e) {
				me._action_drag_stop(icon, e);
			});
	},
	_action_drag_start : function(icon, event) {
		if(!this.in_update) {
			if(this.selected[icon.element.UID]) {
				event.dataTransfer.setData("value", this.selected);
				event.dataTransfer.setData("multiple", true);

				$.each(this.selected, function(key, value) {
					value.image.css('opacity', '0.5');
				});
			} else {
				event.dataTransfer.setData("value", icon);
				event.dataTransfer.setData("multiple", false);
				icon.image.css('opacity', '0.5');
			}
		}
	},
	_action_drag_stop : function(icon, event) {
		if(!this.in_update) {
			if(this.selected[icon.element.UID]) {
				$.each(this.selected, function(key, value) {
					value.image.css('opacity', 1);
				});
			} else {
				icon.image.css('opacity', 1);
			}
		}
	},
	_action_select : function(icon, event) {
		if(!this.in_update) {
			if(this.selected[icon.element.UID]) {
				delete this.selected[icon.element.UID];
			} else { 
				this.selected[icon.element.UID] = icon;
			}
		}
	},
	_action_context : function(element, event) {
		if(!this.in_update) {
			if(Object.keys(this.selected).length > 1 && this.selected[element.UID]) {
				var values = {};

				$.each(this.selected, function(UID, node) {
					values[UID] = node.element;
				});

				this.fireEvent('select_context', values, event);
			} else
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
		this.image = $('<div class="image" style="background-image: url(' + icon_path + ');"/>');


		if(mimeType.match("^image")) {
			var image_path = endpoint + 'api/file-bin' + path + filename;

			if (this.element.size <= 1048576){
				this.image.css('background-image', 'url(' + image_path + ')');
				this.image.addClass('preview');
			} else {
				this.image.on('mouseover', function() {
					me.image.css('background-image', 'url(' + image_path + ')');
				});
				this.image.on('mouseout', function() {
					me.image.css('background-image', 'url(' + icon_path + ')');
				});
			}
			

			return $('<a href="javascript:void(0)" draggable="true"></a>').
						append($('<figure draggable="true"></figure>').
							append($('<div class="image-wrapper" ></div>').append(this.image)).
							append($('<figcaption>' + filename + '</figcaption>')));
		} else {
			return $('<a href="javascript:void(0)" draggable="true" ></a>').
						append($('<figure draggable="true"></figure>').
							append($('<div class="image-wrapper" ></div>').append(this.image)).
							append($('<figcaption>' + filename + '</figcaption>')));
		}
	},
	update : function(element, type, name, selectable) {
		this.element = element;

		var me = this;
		var icon = this.generateTypeIcon(type || element.type, name || element.name, element.path);

		icon.on('dragstart', function(e) {
			me.fireEvent('dragstart', me.element, me, e);
		}).on('dragend', function(e) {
			me.fireEvent('dragend', me.element, me, e);
		}).on('dragover', function(e) {
			icon.addClass('drag_over');
		}).on('dragleave', function(e) {
			icon.removeClass('drag_over');
		});

		icon.on(window.onMobile() ? 'click' : 'dblclick', function(e) {
			me.fireEvent('click', me.element, me, e);
		});

		// Context menu
		icon.bind('contextmenu', function(e) {
			me.fireEvent('click_context', me.element, me, e);
			e.preventDefault();
		});
		
		// Select
		icon.on('click', function(e) {
			if(selectable !== false) {
				icon.toggleClass('selected');

				if(!me.selected) {
					me.fireEvent('select', me.element, me, e);
					me.selected = true;
				} else {
					me.fireEvent('select', me.element, me, e);
					me.selected = false;
				}
			}
		});
		
		this.renderer.append($('<li />').append(icon));
	}
})
