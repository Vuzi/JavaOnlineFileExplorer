
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
		this.cancel_button = $("<a href='#' class='close'><img src='" + endpoint + "resources/style/icons/close_button.png'/>");
		
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
	action : function(type, URI, values) {
		var me = this;

		// Show a loading animation
		var tmp = this.content.contents();
		this.content.empty().append('<div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader"></div><div class="loader-label">Chargement...</div>');
		this.toggle_dissmiss();

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
		this.dir_name = $('<input type="text" placeholder="Nom du dossier"></input>');
		this.dir_path = $('<input type="text" value="' + this.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Créer"></input>');

		message.append($('<span>Dossier : </span>')).append(this.dir_name).append($('<br/>')).
		append($('<span>Chemin : </span>')).append(this.dir_path).append($('<br/>')).append(this.submit);

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
//                   Directory deletion
// =======================================================

var DirectoryDeletionWindow = PopUpAction.extend({
	init : function(directory) {
		var me = this;

		this.path = directory.name ? directory.path + directory.name + '/' : directory.path;
		
		var message = $('<p>Êtes vous certain de vouloir supprimer le dossier "' + this.path + '" ?</p>');
		this.submit = $('<input type="submit" value="Supprimer" style="display: inline-block; margin-right: 10px;"></input>');
		this.cancel = $('<input type="submit" value="Annuler" style="display: inline-block;"></input>');
		
		message.append($('<div style="text-align: center;"></div>').append(this.submit).append(this.cancel));
		
		this._super("Suppression dossier", message);

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
			var toast = new Toast("Suppression du dossier effectuée", "Le dossier '" + this.path + "' a été supprimé avec succès", "success");
			toast.display();
		});
	},
	display : function() {
		this._super();
		this.cancel.focus();
	},
	action : function() {
		this._super('DELETE', 'api/dir' + this.path, {});
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
		this.dir_name = $('<input type="text" value="' + directory.name + '" placeholder="Nom du fichier"></input>');
		this.dir_path = $('<input type="text" value="' + directory.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Modifier le nom"></input>');
		
		message.append('<span>Nom : </span>').append(this.dir_name).append('<br/>').
		append('<span>Chemin : </span>').append(this.dir_path).append('<br/>').append(this.submit);

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
		this.dir_name = $('<input type="text" placeholder="Nom du fichier"></input>');
		this.dir_path = $('<input type="text" value="' + this.path + '" disabled="disabled"></input>');
		this.submit = $('<input type="submit" value="Envoyer"></input>');
		
		message.append('<span>Fichier : </span>').append(this.label.append(this.file)).append('<br/>').
		append('<span>Nom : </span>').append(this.dir_name).append('<br/>').
		append('<span>Chemin : </span>').append(this.dir_path).append('<br/>').append(this.submit);

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

