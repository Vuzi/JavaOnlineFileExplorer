
// =======================================================
//                    Directory renderer
// =======================================================
var FolderRenderer = CallbackHandler.extend({
	init : function(renderer, size) {
		this._super();
		this.renderer = renderer;
		this.size = size || "normalView";
	},
	render : function() {},
	add : function(element, type, name, selectable) {},
})

// =======================================================
//                    Directory renderer
// =======================================================
var FolderTableRenderer = CallbackHandler.extend({
	init : function(renderer, size) {
		this._super();
		this.renderer = renderer;
		this.size = size || "normalView";
	},
	render : function() {
		this.rendered = $('<table class="' + this.size + '"></table>');
		this.rendered.append($('<tr><th><p></p></th><th><p>Nom</p></th><th><p>Type</p></th><th><p>Création</p></th><th><p>Modification</p></th><th><p>Taille</p></th></tr>'));
	},
	generateTypeIcon : function(mimeType, filename, path, size) {
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
			case 'video/mp4' :
				icon_src = "mp4.png";
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
		var image = $('<img src="' + icon_path + '" alt="icon" />');

		return image;
	},
	add : function(element, type, name, selectable) {
		var me = this;
		var icon = this.generateTypeIcon(type || element.type, name || element.name, element.path, element.size);
		var line = $('<tr draggable="true"></tr>');

		line.append($('<td class="icon"></td>').append($('<p></p>').append(icon)));
		line.append($('<td><p>' + (name || element.name) + '</p></td>'));
		line.append($('<td><p>' + (type || element.type) + '</p></td>'));

		if(element.UID) {
			line.append($('<td><p>' + (element.modificationDate ? new Date(element.modificationDate).toLocaleString() : new Date(element.edit).toLocaleString() )+ '</p></td>'));
			line.append($('<td><p>' + (element.creationDate ? new Date(element.creationDate).toLocaleString() : new Date(element.creation).toLocaleString() )+ '</p></td>'));
			line.append($('<td><p>' + (element.size ? sizeFormat(element.size) : '-') + '</p></td>'));
		} else {
			line.append($('<td><p> - </p></td>'));
			line.append($('<td><p> - </p></td>'));
			line.append($('<td><p> - </p></td>'));
		}

		line.on('dragstart', function(e) {
			me.fireEvent('dragstart', element, icon, e);
		}).on('dragend', function(e) {
			me.fireEvent('dragend', element, icon, e);
		}).on('dragover', function(e) {
			e.preventDefault();  
			e.stopPropagation();
			icon.addClass('drag_over', element, icon, e);
		}).on('dragleave', function(e) {
			icon.removeClass('drag_over');
		}).on('drop', function(e) {
			icon.removeClass('drag_over');
			me.fireEvent('drop', element, icon, e);
		});

		line.on(window.onMobile() ? 'click' : 'dblclick', function(e) {
			me.fireEvent('click', element, line, e);
		});

		// Context menu
		line.bind('contextmenu', function(e) {
			me.fireEvent('click_context', element, line, e);
			e.preventDefault();
		});
		
		// Select
		line.on('click', function(e) {
			if(selectable !== false) {
				line.toggleClass('selected');

				if(!me.selected) {
					me.fireEvent('select', element, line, e);
					me.selected = true;
				} else {
					me.fireEvent('select', element, line, e);
					me.selected = false;
				}
			}
		});
		
		this.rendered.append(line);
	}
})

// =======================================================
//                  Directory icon renderer
// =======================================================
var FolderIconRenderer = FolderRenderer.extend({
	generateTypeIcon : function(mimeType, filename, path, size) {
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
			case 'video/mp4' :
				icon_src = "mp4.png";
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
		var image = $('<div class="image"></div>');
		image.css('background-image', 'url("' + icon_path + '")');

		if(mimeType.match("^image")) {
			var image_path = endpoint + 'api/file-bin' + path + filename;

			if (size <= 1048576){
				image.css('background-image', 'url("' + image_path + '")');
				image.addClass('preview');
			} else {
				image.on('mouseover', function() {
					image.css('background-image', 'url("' + image_path + '")');
				});
				image.on('mouseout', function() {
					image.css('background-image', 'url("' + icon_path + '")');
				});
			}
			
			return $('<a href="javascript:void(0)" draggable="true"></a>').
						append($('<figure draggable="true"></figure>').
							append($('<div class="image-wrapper" ></div>').append(image)).
							append($('<figcaption>' + filename + '</figcaption>')));
		} else {
			return $('<a href="javascript:void(0)" draggable="true" ></a>').
						append($('<figure draggable="true"></figure>').
							append($('<div class="image-wrapper" ></div>').append(image)).
							append($('<figcaption>' + filename + '</figcaption>')));
		}
	},
	render : function() {
		this.rendered = $('<ul class="' + this.size + '"></ul>');
	},
	add : function(element, type, name, selectable) {
		var me = this;
		var icon = this.generateTypeIcon(type || element.type, name || element.name, element.path, element.size);

		icon.on('dragstart', function(e) {
			me.fireEvent('dragstart', element, icon, e);
		}).on('dragend', function(e) {
			me.fireEvent('dragend', element, icon, e);
		}).on('dragover', function(e) {
			e.preventDefault();  
			e.stopPropagation();
			icon.addClass('drag_over', element, icon, e);
		}).on('dragleave', function(e) {
			icon.removeClass('drag_over');
		}).on('drop', function(e) {
			icon.removeClass('drag_over');
			me.fireEvent('drop', element, icon, e);
		});

		icon.on(window.onMobile() ? 'click' : 'dblclick', function(e) {
			me.fireEvent('click', element, icon, e);
		});

		// Context menu
		icon.bind('contextmenu', function(e) {
			me.fireEvent('click_context', element, icon, e);
			e.preventDefault();
		});
		
		// Select
		icon.on('click', function(e) {
			if(selectable !== false) {
				icon.toggleClass('selected');

				if(!me.selected) {
					me.fireEvent('select', element, icon, e);
					me.selected = true;
				} else {
					me.fireEvent('select', element, icon, e);
					me.selected = false;
				}
			}
		});
		
		this.rendered.append($('<li />').append(icon));
	}
})


// =======================================================
//                    Directory content
// =======================================================

var Folder = CallbackHandler.extend({
	init : function(renderer, folderRenderer) {
		this._super();
		this.renderer = renderer;
		this.folderRenderer = folderRenderer || new FolderIconRenderer();
		this.folderRenderer.renderer = this.renderer;
		this.in_update = false;
		this.selected = [];
		this.dragged = [];
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
	refresh : function() {
		this.prepare_update();
		this.render(this.element, this.parent);
		this.finish_update();
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
				var pop = new Toast("Erreur ", "La requête a échouée", "error");
				pop.display();
				me.finish_update();
			}
		});
	},
	render : function(element, parent, search) {
		this.element = element;
		this.parent = parent;
		
		var me = this;
		var h1 = (search ? $('<h1> Résultat de la recherche "' + search.search + '" dans "' + search.path + '" </h1>')
			             : $('<h1> Contenu de ' + (element.name == null ? '/' : element.path  + element.name) + '</h1>'));

		this.folderRenderer.clear();
		this.folderRenderer.render();
		
		// '..' folder
		if(element.UID != null && parent) {
			this.folderRenderer.add(parent, 'parent', '...', false);
		}
		
		// Directories
		element.directories.forEach(function(directory) {
			me.folderRenderer.add(directory, 'folder');
		});
		
		// Files
		element.files.forEach(function(file) {
			me.folderRenderer.add(file);
		});

		// Add drag & drop icon
		
		// Handle events
		this.folderRenderer.on('click', function(element, icon, e) {
			if(!this.in_update) 
				me.fireEvent('select', element, e);
		}).on('click_context', function(element, icon, e) {
			if(!this.in_update) 
				me._action_context(element, icon, e);
		}).on('select', function(element, icon, e) {
			if(!this.in_update) 
				me._action_select(element, icon, e);
		}).on('deselect', function(element, icon, e) {
			if(!this.in_update) 
				me._action_select(element, icon, e);
		}).on('dragstart', function(element, icon, e) {
			if(!this.in_update) 
				me._action_drag_start(element, icon, e);
		}).on('dragend', function(element, icon, e) {
			if(!this.in_update) 
				me._action_drag_stop(element, icon, e);
		}).on('drop', function(element, icon, e) {
			if(!this.in_update) 
				me._action_drop(element, icon, e);
		});

		// Render element
		this.renderer.empty().append(h1).append(this.folderRenderer.rendered);
	},
	isDragging : function() {
		return this.dragged.length != 0
	},
	_action_drag_start : function(element, icon, event) {
		var me = this;

		if(Object.keys(this.selected).length > 1 && this.selected[element.UID]) {
			this.dragged = []
			$.each(this.selected, function(UID, element) {
				me.dragged.push(element);
			});
			this.renderer.addClass('dragging');
		} else {
			// Direct download
			if(isFile(element)) {
    			event.dataTransfer.setData("DownloadURL", "application/octet-stream:" + element.name + ":" + endpoint + "api/file-bin" + element.path + element.name);
			}
			
			this.dragged = [ element ];
			icon.addClass('dragged');
		}
	},
	_action_drag_stop : function(element, icon, event) {
		if(this.selected[element.UID]) {
			this.renderer.removeClass('dragging');
		} else {
			icon.removeClass('dragged');
		}
		this.dragged = [];
	},
	_action_drop : function(element, icon, event) {
		var me = this;
		if(isDir(element) && this.dragged.length > 0) {
			startLoading("Déplacement de resources en cours...");

			// Bind & launch action
			new MoveElementsAction().on('done', function(done, error, fail) {
				stopLoading();
				me.fireEvent('need_update', me.element);
			}).proceed(this.dragged, element.name ? element.path + element.name + '/' : '/');
		}
	},
	_action_select : function(element, icon, event) {
		if(this.selected[element.UID]) {
			delete this.selected[element.UID];
		} else { 
			this.selected[element.UID] = element;
		}
	},
	_action_context : function(element, icon, event) {
		if(Object.keys(this.selected).length > 1 && this.selected[element.UID])
			this.fireEvent('select_context', this.selected, event);
		else
			this.fireEvent('select_context', element, event);
	}
})
