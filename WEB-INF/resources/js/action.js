Action = CallbackHandler.extend({
	init : function() {
		this._super();
	}
});

// =======================================================
//                     File creation
// =======================================================
CreateFileAction = Action.extend({
	proceed : function(element, name) {

	}
});

// =======================================================
//                   Directory creation
// =======================================================
CreateDirectoryAction = Action.extend({
	proceed : function(element, name) {

	}
});

// =======================================================
//                     Rename element
// =======================================================
RenameElementAction = Action.extend({
	proceed : function(element, name) {
		var me = this;

		// Construct request
		var requests = new Requests();

		if(isDir(element))
			requests.add({ type : 'POST', uri : 'api/dir' +  (element.name ? element.path + element.name + '/' : element.path), value : { action : "rename", name : name } });
		else
			requests.add({ type : 'POST', uri : 'api/file' +  element.path + element.name, value : { action : "rename", name : name } });

		// Bind & fire
		requests.on('done', function(done, error, fail) {
			toastHandler.add(new Toast("Fichier modifié", "Le fichier '" + element.name + "' a été renommé avec succès", "success"));

			me.fireEvent('done', done, error, fail, element);
		}).on('success', function(done, error, fail) {
			me.fireEvent('success', done, error, fail, element);
		}).on('error', function(done, error, fail) {
			me.fireEvent('error', done, error, fail, element);
		}).on('fail', function(done, error, fail) {
			me.fireEvent('fail', done, error, fail, element);
		}).proceed();
	}
})

// =======================================================
//                     Move element(s)
// =======================================================
MoveElementsAction = Action.extend({
	proceed : function(elements, destination) {
		var me = this;

		// Construct request
		var requests = new Requests();
		$.each(elements, function(UID, element) {
			if(isDir(element))
				requests.add({ type : 'POST', uri : 'api/dir' +  (element.name ? element.path + element.name + '/' : element.path), value : { action : "move", path : destination } });
			else
				requests.add({ type : 'POST', uri : 'api/file' +  element.path + element.name, value : { action : "move", path : destination } });
		});

		// Bind & fire
		requests.on('done', function(done, error, fail) {
			if(error + fail == 0) {
				toastHandler.add(new Toast("Déplacement effectué avec succès",
					"L'opération de déplacement a été effectuée avec succès",
					"success"));
			} else if(done + error + fail > 1) {
				toastHandler.add(new Toast("Déplacement effectué",
					"L'opération de déplacement a été effectuée, mais des erreurs sont survenues durant celle-ci",
					"warning"));
			}

			me.fireEvent('done', done, error, fail, elements);
		}).on('success', function(done, error, fail) {
			me.fireEvent('success', done, error, fail, elements);
		}).on('error', function(done, error, fail) {
			me.fireEvent('error', done, error, fail, elements);
		}).on('fail', function(done, error, fail) {
			me.fireEvent('fail', done, error, fail, elements);
		}).proceed();
	}
})

MoveElementAction = MoveElementsAction;

// =======================================================
//                    Delete element(s)
// =======================================================
DeleteElementsAction = Action.extend({
	proceed : function(elements) {
		var me = this;

		// Construct request
		var requests = new Requests();
		$.each(elements, function(UID, element) {
			if(isDir(element))
				requests.add({ type : 'DELETE', uri : 'api/dir' +  (element.name ? element.path + element.name + '/' : element.path) });
			else
				requests.add({ type : 'DELETE', uri : 'api/file' +  element.path + element.name });
		});

		// Bind & fire
		requests.on('done', function(done, error, fail) {
			if(error + fail == 0) {
				toastHandler.add(new Toast("Suppression effectué avec succès",
			      "L'opération de suppression a été effectuée avec succès",
			      "success"));
			} else if(done + error + fail > 1) {
				toastHandler.add(new Toast("Suppression effectué",
			      "L'opération de suppression a été effectuée, mais des erreurs sont survenues durant celle-ci",
			      "warning"));
			}
			
			me.fireEvent('done', done, error, fail, elements);
		}).on('success', function(done, error, fail) {
			me.fireEvent('success', done, error, fail, elements);
		}).on('error', function(done, error, fail) {
			me.fireEvent('error', done, error, fail, elements);
		}).on('fail', function(done, error, fail) {
			me.fireEvent('fail', done, error, fail, elements);
		}).proceed();
	}
})

DeleteElementAction = DeleteElementsAction;
