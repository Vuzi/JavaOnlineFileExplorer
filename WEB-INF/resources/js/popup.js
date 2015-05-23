
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

			setTimeout(function() {
				me.content.slideUp(200, function() {
					me.content.remove();
					me.fireEvent('dissmiss', me);

					if(pop_up_back.is(':empty')) {
						global.removeClass('blur');
						pop_up_back.fadeOut();
					}
				});
			}, 200);
		}, timer || 0);
	}
});

// =======================================================
//                      Toast
// =======================================================

var Toast = PopUp.extend({
	init : function(title, message, type) {
		this._super(title, message);
		this.type = type ||'info';
	},
	render : function() {
		return this._super().addClass(this.type);
	},
	display : function(timer) {
		this._super();
		this.dissmiss(timer || 2000);
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
//                Cancelable action window
// =======================================================

var PopUpAction = PopUpCancelable.extend({
	_show_loading : function() {
		// Show a loading animation
		this.content.empty().append('<div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader-label">Chargement...</div>');
		this.toggle_dissmiss();
	},
	action : function(type, URI, values) {
		var me = this;

		this._show_loading();

		// Perform the request
		$.ajax({
			type: type,
			url: endpoint + URI,
			dataType : 'json',
			processData : false,
			data : JSON.stringify(values),
			contentType : 'application/json',
			headers : authHeader('vuzi', '1234'),
			success: function(data) {
				me.dissmiss();
				me.fireEvent('success', me, data.data);
			},
			error: function(data) {

				if(!data.responseJSON) {
					this.fail(data);
					return;
				}

				var pop = new Toast("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error");
				pop.display();
				
				me.fireEvent('error', me, data.responseJSON.data);
				me.dissmiss();
			},
			fail: function(data) {
				var pop = new Toast("Erreur ", "La requête a échouée", "error");
				pop.display();

				me.fireEvent('fail', me);
				me.dissmiss();
			}
		});
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

var DirectoryCreationWindow = PopUpAction.extend({
	init : function(directory) {
		var me = this;

		this.path = directory.name ? directory.path + directory.name + '/' : directory.path;

		var message = $('<p></p>');
		this.dir_name = $('<input class="field" type="text" placeholder="Nom du dossier"></input>');
		this.dir_path = $('<input class="field" type="text" value="' + this.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Créer"></input>');

		message.append($('<span class="field-name">Dossier : </span>')).append(this.dir_name).append($('<br/>')).
		append($('<span class="field-name">Chemin : </span>')).append(this.dir_path).append($('<br/>')).append(this.submit);

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

		// On success
		this.on('success', function() {
			var toast = new Toast("Création du dossier effectuée", "Le dossier '" + me.dir_name.val() + "' a été crée avec succès", "success");
			toast.display();
		});
	},
	display : function() {
		this._super();
		this.dir_name.focus();
	},
	action : function() {
		var dir_name = this.dir_name.val().trim();
		var dir_path = this.dir_path.val().trim();
		var me = this;
		
		if(!dir_name || dir_name == "" || dir_name.indexOf('/') >= 0 || dir_name.indexOf('"') >= 0 || dir_name.indexOf("'") >= 0) {
			new Toast("Impossible de créer le dossier", "Le nom '" + dir_name + "' n'est pas valide", "error").display();
			return;
		}

		this._super('PUT', 'api/dir' + dir_path + dir_name, {});
	}
})

// =======================================================
//                  Directory renaming
// =======================================================

var DirectoryRenamingWindow = PopUpAction.extend({
	init : function(directory) {
		var me = this;

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

		// On success
		this.on('success', function() {
			var toast = new Toast("Dossier modifié", "Le dossier '" + me.dir_name.val() + "' a été renommé avec succès", "success");
			toast.display();
		});
	},
	display : function() {
		this._super();
		this.dir_name.focus();
	},
	action : function() {
		var dir_name = this.dir_name.val().trim();
		var dir_path = this.dir_path.val().trim();
		var me = this;

		if(!dir_name || dir_name == "" || dir_name.indexOf('/') >= 0 || dir_name.indexOf('"') >= 0 || dir_name.indexOf("'") >= 0) {
			new Pop_up("Impossible de changer le nom", "Le nom '" + dir_name + "' n'est pas valide", "error").display();
			return;
		}

		this._super('POST', 'api/dir' + this.path, { action : "rename", name : dir_name });
	}
});

// =======================================================
//                    File creation
// =======================================================

var FileCreationWindow = PopUpAction.extend({
	init : function(directory) {
		var me = this;

		this.path = directory.name ? directory.path + directory.name + '/' : directory.path;
		
		var message = $('<div></div>');
		this.file = $('<input type="file" name="file" hidden="true"/>');
		this.label = $('<label class="input_file">Parcourir</label>');
		this.dir_name = $('<input class="field" type="text" placeholder="Nom du fichier"></input>');
		this.dir_path = $('<input class="field" type="text" value="' + this.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Envoyer"></input>');
		
		message.append('<span class="field-name">Fichier : </span>').append(this.label.append(this.file)).append('<br/>').
		append('<span class="field-name">Nom : </span>').append(this.dir_name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.dir_path).append('<br/>').append(this.submit);

		this._super("Upload fichier", message);

		// File change
		this.file.on('change', function() {
			me.dir_name.val(me.file.val().replace(/.*(\/|\\)/, ''));
		});

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

		// On success
		this.on('success', function() {
			var toast = new Toast("Fichier uploadé", "Le fichier '" + me.dir_name.val() + "' a été uploadé avec succès", "success");
			toast.display();
		});
	},
	display : function() {
		this._super();
		this.dir_name.focus();
	},
	action : function() {
		var dir_name = this.dir_name.val().trim();
		var dir_path = this.dir_path.val().trim();
		var me = this;

		if(!this.file.val()) {
			new Pop_up("Impossible d'envoyer le fichier", "Aucun fichier sélectionné", "error").display();
			return;
		}

		if(!dir_name || dir_name == "" || dir_name.indexOf('/') >= 0 || dir_name.indexOf('"') >= 0 || dir_name.indexOf("'") >= 0) {
			new Pop_up("Impossible d'envoyer le fichier", "Le nom '" + dir_name + "' n'est pas valide", "error").display();
			return;
		}

		this.content.empty().append('<div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader-label">Chargement...</div>');
		this.toggle_dissmiss();
		
		var file = this.file[0].files[0];
		var formData = new FormData();
		
		formData.append(dir_name, file, file.name);
		
		var xhr = new XMLHttpRequest();
		xhr.onerror = function(error) {
			var toast = new Toast("Erreur ", "La requête a échouée", "error");
			toast.display();

			me.fireEvent('fail', me);
			me.dissmiss();
		}
		xhr.onload = function () {
			var val = JSON.parse(xhr.responseText);

			if (xhr.status === 200) {
				me.fireEvent('success', me, val || {});
				me.dissmiss();
			} else {
				var val = JSON.parse(xhr.responseText);
				
				if(val && val.data && val.data.message) {
		    		var toast = new Toast("Erreur " + val.data.status, val.data.message, "error");
					me.fireEvent('error', me, val.data);
				} else {
		    		var toast = new Toast("Erreur ", "La requête a échouée", "error");
					me.fireEvent('error', me, {});
				}

				toast.display();
				me.dissmiss();
			}
		};
		xhr.open('POST', endpoint + 'api/file-bin' + dir_path, true);
		xhr.setRequestHeader("Authorization", authHeaderValue('vuzi', '1234'));
		xhr.send(formData);
	}
})

// =======================================================
//                   File deletion
// =======================================================

var FileDeletionWindow = PopUpAction.extend({
	init : function(file) {
		var me = this;

		this.path = file.name ? file.path + file.name + '/' : file.path;
		
		var message = $('<p>Êtes vous certain de vouloir supprimer le fichier "' + file.name + '" ?</p>');
		this.submit = $('<input type="submit" value="Supprimer" style="display: inline-block; margin-right: 10px;"></input>');
		this.cancel = $('<input type="submit" value="Annuler" style="display: inline-block;"></input>');
		
		message.append($('<div style="text-align: center;"></div>').append(this.submit).append(this.cancel));
		
		this._super("Suppression fichier", message);

		// Cancel button
		this.cancel.on('click', function(e) {
			me.dissmiss();
		});
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});

		// On success
		this.on('success', function() {
			var toast = new Toast("Suppression du fichier effectuée", "Le fichier '" + file.name + "' a été supprimé avec succès", "success");
			toast.display();
		});
	},
	display : function() {
		this._super();
		this.cancel.focus();
	},
	action : function() {
		this._super('DELETE', 'api/file' + this.path, {});
	}
})

// =======================================================
//                    Resource generic
// =======================================================

var ResourceGenericWindow = PopUpAction.extend({
	init_info : function(elements) {
		var me = this;

		console.log(elements);

		if(elements.name) {
			this.elements = {};
			this.elements[elements.UID] = elements;
		} else {
			this.elements = elements;
		}

		console.log(this.elements);

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
		this.file_name = $('<input class="field" type="text" disabled="disabled" value="' + this.names + '" placeholder="Nom ' + this.title + '"></input>');
		this.file_path = $('<input class="field" type="text" value="' + this.path + '" ></input>');
		var tree_div = $('<div class="tree"></div>');
		this.submit = $('<input type="submit" value="Déplacer le' + (this.total_nb > 1 ? 's' : '') + ' ' + this.title + '"></input>');

		message.append('<span class="field-name">Nom' + (this.total_nb > 1 ? 's' : '') + ' : </span>').append(this.file_name).append('<br/>').
		append('<span class="field-name">Chemin : </span>').append(this.file_path).append('<br/>').append(tree_div).append(this.submit);

		this.tree = new DirectoryTree(tree_div);
		this.tree.on('select', function(element, node, e, ignorePushState) {
			me.file_path.val(element.name ? element.path + element.name + '/' : '/');
		});
		
		this._super("Déplacement " + this.title, message);
		
		// Submit button
		this.submit.on('click', function(e) {
			me.action();
		});

		// On success
		this.on('done', function(done, error, fail) {
			if(error + fail == 0)
				new Toast("Déplacement effectué avec succès",
					      "L'opération de déplacement a été effectuée avec succès",
					      "success").display()
			else
				new Toast("Déplacement effectué",
					      "L'opération de déplacement a été effectuée, mais des erreurs sont survenues durant celle-ci",
					      "warning").display(3500);
		});
	},
	display : function() {
		var me = this;
		this._super();
		this.tree.update(function() {
			me.tree.select_path(me.path);
		});
	},
	action : function() {
		var me = this;
		var requests = new Requests();
		var file_path = this.file_path.val().trim();

		if(!file_path || file_path == "" || file_path.indexOf('"') >= 0 || file_path.indexOf("'") >= 0) {
			new Pop_up("Impossible de déplacer le fichier", "Le chemin '" + file_path + "' n'est pas valide", "error").display();
			return;
		}

		this._show_loading();

		$.each(this.elements, function(UID, element) {
			if(isDir(element))
				requests.add({ type : 'POST', uri : 'api/dir' +  (element.name ? element.path + element.name + '/' : element.path), value : { action : "move", path : file_path } });
			else
				requests.add({ type : 'POST', uri : 'api/file' +  element.path + element.name, value : { action : "move", path : file_path } });
		});

		requests.on('done', function(done, error, fail) {
			me.dissmiss();
			me.fireEvent('done', done, error, fail);
		}).proceed();
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

		// On success
		this.on('done', function(done, error, fail) {
			if(error + fail == 0)
				new Toast("Suppression effectué avec succès",
					      "L'opération de suppression a été effectuée avec succès",
					      "success").display()
			else
				new Toast("Suppression effectué",
					      "L'opération de suppression a été effectuée, mais des erreurs sont survenues durant celle-ci",
					      "warning").display(3500);
		});
	},
	display : function() {
		this._super();
		this.cancel.focus();
	},
	action : function() {
		var me = this;
		var requests = new Requests();

		this._show_loading();

		$.each(this.elements, function(UID, element) {
			if(isDir(element))
				requests.add({ type : 'DELETE', uri : 'api/dir' +  (element.name ? element.path + element.name + '/' : element.path) });
			else
				requests.add({ type : 'DELETE', uri : 'api/file' +  element.path + element.name });
		});

		requests.on('done', function(done, error, fail) {
			me.dissmiss();
			me.fireEvent('done', done, error, fail);
		}).proceed();
	}
})