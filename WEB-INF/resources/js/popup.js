
// =======================================================
//                      Toast handler
// =======================================================
// 
ToastHandler = Class.extend({
	init : function(renderer) {
		this.messages = [];
		this.renderer = renderer;
	},
	add : function(toast) {
		// Place in the queue
		this.messages.push(toast);

		if(this.messages.length == 1) {
			// Display now
			this._display(this.messages[0]);
		}

		return this;
	},
	_update : function() {
		if(this.messages.length <= 0)
			return;

		this._display(this.messages[0]);
	},
	_display : function(toast) {
		var me = this;

		toast.display(this.renderer);
		toast.on('dissmiss', function() {
			me.messages.splice(me.messages.indexOf(toast), 1);
			me._update();
		});
	}
})

// =======================================================
//                      Pop up base class
// =======================================================

var PopUp = CallbackHandler.extend({
	init : function(title, message) {
		this._super();
		this.title = title;
		this.message = message;
	},
	render : function() {
		this.content = $('<div class="pop_up bouncy"></div>');
		this.content.append($('<h2></h2>').append(this.title));
		this.content.append($('<p></p>').append(this.message));

		return this.content;
	},
	display : function() {
		this.render();

		pop_up_back.fadeIn();
		global.addClass('blur');
		this.content.addClass('bounce');
		pop_up_back.append(this.content);

		this.fireEvent('display', this);
	},
	dissmiss : function(timer) {
		timer = timer || 0;
		var me = this;

		setTimeout(function() {
			me.content.removeClass('bounce');
			me.fireEvent('dissmiss', me);

			setTimeout(function() {
				me.content.remove();
			}, 1000);

			setTimeout(function() {
				global.removeClass('blur');
				pop_up_back.fadeOut();
			}, 300);
		}, timer || 0);
	}
});

// =======================================================
//                      Toast
// =======================================================

var Toast = CallbackHandler.extend({
	init : function(title, message, type, timer) {
		this._super();
		this.title = title;
		this.message = message;
		this.type = type ||'info';

		if(!timer) {
			if(this.type == 'info' || this.type == 'success')
				this.timer = 2000;
			else
				this.timer = 4000;
		} else
			this.timer = timer;
	},
	display : function(render) {
		var me = this;

		this.render();
		this.content.addClass('bounce');
		render.append(this.content);

		this.fireEvent('display', this);

		setTimeout(function() {
			me.dissmiss();
		}, this.timer);
	},
	render : function() {
		this.content = $('<div class="toast bouncy-bottom"></div>');
		this.content.append($('<h2></h2>').append(this.title));
		this.content.append($('<p></p>').append(this.message));
		this.content.addClass(this.type);
	},
	dissmiss : function() {
		var me = this;

		me.content.removeClass('bounce');
		me.fireEvent('dissmiss', this);

		setTimeout(function() {
			me.content.remove();
		}, 1000);
	}
});

// =======================================================
//                   Cancelable window
// =======================================================

var PopUpCancelable = PopUp.extend({
	render : function() {
		var me = this;
		this.cancelable = true;
		this.cancel_button = $("<a href='javascript:void(0)' class='close'><img src='" + endpoint + "resources/style/icons/close_button.png'/>");
		
		this.cancel_button.on('click', function(e) {
			me.fireEvent('cancel', me, e);
			me.dissmiss();
		});

		this._super().append(this.cancel_button);

		this.content.on('keyup', function(e) {
			if(me.cancelable && e.keyCode == 27) {
				me.fireEvent('cancel', me, e);
				me.dissmiss();
			}
		});

		return this.content;
	},
	toggle_dissmiss : function() {
		this.cancel_button.toggle();
		this.cancelable = false;
	}
});

// =======================================================
//                     Image preview
// =======================================================
// 
var ImagePreviewWindow = PopUpCancelable.extend({
	init : function(file) {
		this.img = $('<img src="' + endpoint + 'api/file-bin' + file.path + file.name + '" />');
		this.a = $('<a href="' + endpoint + "api/file-bin" + file.path + file.name + '"></a>');

		this._super(file.name, this.a.append(this.img));
	}
});


// =======================================================
//                Directory creation
// =======================================================

var DirectoryCreationWindow = PopUpCancelable.extend({
	init : function(directory) {
		var me = this;

		this.parent = directory;
		var path = directory.name ? directory.path + directory.name + '/' : directory.path;

		var message = $('<p></p>');
		var dir_path = $('<input class="field" type="text" value="' + path + '" disabled="disabled"></input>');
		this.dir_name = $('<input class="field" type="text" placeholder="Nom du dossier"></input>');
		this.submit = $('<input type="submit" value="Créer"></input>');

		message.append($('<span class="field-name">Dossier : </span>')).append(this.dir_name).append($('<br/>')).
		append($('<span class="field-name">Chemin : </span>')).append(dir_path).append($('<br/>')).append(this.submit);

		this._super("Création dossier", message);

		// Enter pressed
		this.dir_name.on('keypress', function(e) {
			if(e.which == 13) {
				me.action();
			}
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		this._super();
		this.dir_name.focus();
	},
	action : function() {
		var dir_name = this.dir_name.val().trim();
		var me = this;
		
		if(!isNameValid(dir_name)) {
			toastHandler.add(new Toast("Impossible de créer le dossier", "Le nom '" + dir_name + "' n'est pas valide", "error"));
			return;
		}

		this.dissmiss();
		startLoading("Création de dossier en cours...");
		
		// Bind & launch action
		new CreateDirectoryAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done');
		}).proceed(this.parent, dir_name);
	}
})

// =======================================================
//                  Directory renaming
// =======================================================

var DirectoryRenamingWindow = PopUpCancelable.extend({
	init : function(directory) {
		var me = this;

		this.directory = directory;
		this.path = directory.name ? directory.path + directory.name + '/' : directory.path;
		
		var message = $('<div></div>');
		this.dir_name = $('<input class="field" type="text" value="' + directory.name + '" placeholder="Nom du fichier"></input>');
		this.dir_path = $('<input class="field" type="text" value="' + directory.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Modifier le nom"></input>');
		
		message.append('<span class="field-name">Nom : </span>').append(this.dir_name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.dir_path).append('<br/>').append(this.submit);

		this._super("Changement de nom dossier", message);

		// On enter
		this.dir_name.on('keypress', function(e) {
			if(e.which == 13) {
				me.action();
			}
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		this._super();
		this.dir_name.focus();
	},
	action : function() {
		var dir_name = this.dir_name.val().trim();
		var me = this;

		if(!isNameValid(dir_name)) {
			toastHandler.add(new Toast("Impossible de changer le nom", "Le nom '" + dir_name + "' n'est pas valide", "error"));
			return;
		}

		this.dissmiss();
		startLoading("Renommage de dossier en cours...");
		
		// Bind & launch action
		new RenameElementAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done');
		}).proceed(this.directory, dir_name);
	}
});

// =======================================================
//                    File creation
// =======================================================

var FileCreationWindow = PopUpCancelable.extend({
	init : function(directory) {
		var me = this;

		this.parent = directory;
		this.path = directory.name ? directory.path + directory.name + '/' : directory.path;
		
		var message = $('<div></div>');
		this.file = $('<input type="file" name="file" hidden="true"/>');
		this.label = $('<label class="input_file">Parcourir</label>');
		this.file_name = $('<input class="field" type="text" placeholder="Nom du fichier"></input>');
		this.dir_path = $('<input class="field" type="text" value="' + this.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Envoyer"></input>');
		
		message.append('<span class="field-name">Fichier : </span>').append(this.label.append(this.file)).append('<br/>').
		append('<span class="field-name">Nom : </span>').append(this.file_name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.dir_path).append('<br/>').append(this.submit);

		this._super("Upload fichier", message);

		// File change
		this.file.on('change', function() {
			me.file_name.val(me.file.val().replace(/.*(\/|\\)/, ''));
		});

		// On enter
		this.file_name.on('keypress', function(e) {
			if(e.which == 13) {
				me.action();
			}
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		this._super();
		this.file_name.focus();
	},
	action : function() {
		var file_name = this.file_name.val().trim();
		var me = this;

		if(!this.file.val()) {
			toastHandler.add(new Toast("Impossible d'envoyer le fichier", "Aucun fichier sélectionné", "error"));
			return;
		}

		if(!isNameValid(file_name)) {
			toastHandler.add(new Toast("Impossible d'envoyer le fichier", "Le nom '" + file_name + "' n'est pas valide", "error"));
			return;
		}

		this.dissmiss();
		startLoading("Upload fichier...");
		
		var file = this.file[0].files[0];

		new CreateFileAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done');
		}).proceed(this.parent, file, file_name);
	}
})

// =======================================================
//                     File renaming
// =======================================================

var FileRenamingWindow = PopUpCancelable.extend({
	init : function(file) {
		var me = this;

		this.file = file;
		this.path = file.name ? file.path + file.name + '/' : file.path;
		
		var message = $('<div></div>');
		this.file_name = $('<input class="field" type="text" value="' + file.name + '" placeholder="Nom du fichier"></input>');
		this.file_path = $('<input class="field" type="text" value="' + file.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Modifier le nom"></input>');
		
		message.append('<span class="field-name">Nom : </span>').append(this.file_name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.file_path).append('<br/>').append(this.submit);

		this._super("Changement de nom fichier", message);

		// On enter
		this.file_name.on('keypress', function(e) {
			if(e.which == 13) {
				me.action();
			}
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		this._super();
		this.file_name.focus();
	},
	action : function() {
		var file_name = this.file_name.val().trim();
		var file_path = this.file_path.val().trim();
		var me = this;

		if(!isNameValid(file_name)) {
			toastHandler.add(new Toast("Impossible de changer le nom", "Le nom '" + file_name + "' n'est pas valide", "error"));
			return;
		}

		this.dissmiss();
		startLoading("Renommage de fichier en cours...");
		
		// Bind & launch action
		new RenameElementAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done');
		}).proceed(this.file, file_name);

	}
})

// =======================================================
//                    Resource generic
// =======================================================

var ResourceGenericWindow = PopUpCancelable.extend({
	init_info : function(elements) {
		var me = this;

		if(elements.name) {
			this.elements = {};
			this.elements[elements.UID] = elements;
		} else {
			this.elements = elements;
		}
		
		// Prepare values
		this.dir_nb = 0;
		this.file_nb = 0;
		this.total_nb = 0;

		this.me = null;
		this.names = '';

		$.each(this.elements, function(UID, element) {
			if(isDir(element))
				me.dir_nb++;
			else
				me.file_nb++;

			me.total_nb++;

			if(me.names != '')
				me.names += ', ';

			me.names += element.name;

			if(!me.path)
				me.path = element.path || '/';
		});

		// Define name to use
		if(this.dir_nb >= 1 && this.file_nb == 0)
			this.title = "dossier" + (this.dir_nb > 1 ? 's' : '');
		else if(this.file_nb >= 1 && this.dir_nb == 0)
			this.title = "fichier" + (this.file_nb > 1 ? 's' : '');
		else
			this.title = "fichiers & dossiers";
	}
});

// =======================================================
//                    Resource move
// =======================================================

var ResourceMoveWindow = ResourceGenericWindow.extend({
	init : function(elements) {
		var me = this;

		this.init_info(elements);

		// Construct view
		var message = $('<div></div>');
		var name = $('<input class="field" type="text" disabled="disabled" value="' + this.names + '" placeholder="Nom ' + this.title + '"></input>');
		this.path_field = $('<input class="field" type="text" value="' + this.path + '" ></input>');
		var tree_div = $('<div class="tree"></div>');
		this.submit = $('<input type="submit" value="Déplacer le' + (this.total_nb > 1 ? 's' : '') + ' ' + this.title + '"></input>');

		message.append('<span class="field-name">Nom' + (this.total_nb > 1 ? 's' : '') + ' : </span>').append(name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.path_field).append('<br/>').append(tree_div).append(this.submit);

		this.tree = new DirectoryTree(tree_div);
		this.tree.on('select', function(element, node, e, ignorePushState) {
			me.path_field.val(element.name ? element.path + element.name + '/' : '/');
		});
		
		this._super("Déplacement " + this.title, message);
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		var me = this;
		this._super();
		this.tree.update(me.path);
	},
	action : function() {
		var me = this;
		var requests = new Requests();
		var path = this.path_field.val().trim();

		// Test name
		if(!isPathValid(path)) {
			toastHandler.add(new Toast("Impossible de déplacer le fichier", "Le chemin '" + path + "' n'est pas valide", "error"));
			return;
		}
		
		this.dissmiss();
		startLoading("Déplacement de resources en cours...");

		// Bind & launch action
		new MoveElementsAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done', done, error, fail);
		}).proceed(this.elements, path);
	}
})

// =======================================================
//                  Resource deletion
// =======================================================

var ResourceDeletionWindow = ResourceGenericWindow.extend({
	init : function(elements) {
		var me = this;

		this.init_info(elements);

		// Construct view
		var message = $('<p>Êtes vous certain de vouloir supprimer le' + (this.total_nb > 1 ? 's' : '') + ' ' + this.title + ' ' + this.names + ' ?</p>');
		this.submit = $('<input type="submit" value="Supprimer" style="display: inline-block; margin-right: 10px;"></input>');
		this.cancel = $('<input type="submit" value="Annuler" style="display: inline-block;"></input>');
		
		message.append($('<div style="text-align: center;"></div>').append(this.submit).append(this.cancel));

		this._super("Suppression " + this.title, message);
		
		// Cancel button
		this.cancel.on('click', function(e) {
			me.dissmiss();
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});
	},
	display : function() {
		this._super();
		this.cancel.focus();
	},
	action : function() {
		var me = this;

		this.dissmiss();
		startLoading("Suppression de resources en cours...");
		
		// Bind & launch action
		new DeleteElementsAction().on('done', function(done, error, fail) {
			stopLoading();

			me.fireEvent('done', done, error, fail);
		}).proceed(this.elements);
	}
})