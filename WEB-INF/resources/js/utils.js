
jQuery.event.props.push('dataTransfer');

window.onMobile = function() {
	var check = false;
	(function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))check = true})(navigator.userAgent||navigator.vendor||window.opera);
	return check;
}

function sizeFormat(bytes) {
    var exp = Math.log(bytes) / Math.log(1024) | 0;
    var result = (bytes / Math.pow(1024, exp)).toFixed(2);

    return result + ' ' + (exp == 0 ? 'Octets': 'KMGTPEZY'[exp - 1] + 'o');
}

function isDir(element) {
	return element && element.path && (element.size === undefined)
}

function isFile(element) {
	return element && element.name && !(element.size === undefined)
}

function isNameValid(name) {
	return !(!name || name == "" || name.indexOf('/') >= 0 || name.indexOf('\\') >= 0 || name.indexOf('"') >= 0 || name.indexOf("'") >= 0 || name.indexOf("<") >= 0 || name.indexOf(">") >= 0);
}

function isPathValid(name) {
	return !(!name || name == "" || name.indexOf('\\') >= 0 || name.indexOf('"') >= 0 || name.indexOf("'") >= 0 || name.indexOf("<") >= 0 || name.indexOf(">") >= 0);
}

var Class = function() {}

Class.extend = function(prop) {
	var _super = this.prototype;

    this.prototype.ignore = true;
    var prototype = new this();
    delete this.prototype.ignore;

    var _supers = {};

	for(var name in prop) {
		if(typeof prop[name] == 'function' && typeof _super[name] == 'function') {
			prototype[name] = (function(name, fn, _super) {
				return function() {
					this._super = _super;
					return fn.apply(this, arguments);
				}
			})(name, prop[name], _super[name]);
		} else
			prototype[name] = prop[name];
	}

    function Class() {
    	if (!this.ignore && this.init) {
    		this.init.apply(this, arguments);
    	}
    }

    Class.prototype = prototype;
    Class.prototype.constructor = Class;
    Class.extend = arguments.callee;

    return Class;
}

var CallbackHandler = Class.extend({
	init : function() {
		this.actions = {};
	},
	on : function(target, clbk, fireOnce) {
		if(this.actions[target] instanceof Array)
			this.actions[target].push(clbk);
		else
			this.actions[target] = [ clbk ];

		clbk._fireOnce = fireOnce || false;
		return this;
	},
	remove : function(target, toRemove) {
		if(this.actions[target] instanceof Array) {
			var index = this.actions[target].indexOf(toRemove);
			if(index >= 0)
				this.actions[target].splice(index, 1);
		}

		return this;
	},
	clear : function(target) {
		if(target) {
			if(this.actions[target] instanceof Array) {
				this.actions[target] = [];
			}
		} else
			this.actions = {};

		return this;
	},
	fireEvent : function(target) {
		var event_args = Array.prototype.slice.call(arguments, 1);
		var me = this;

		if(this.actions[target] instanceof Array) {
			for (var i = 0; i < this.actions[target].length; i++) {
				this.actions[target][i].apply(this, event_args);

				if(this.actions[target][i]._fireOnce)
					this.actions[target].remove(i--);
			}
		}
		return this;
	}
});

/*
var A = Class.extend({
	init: function(a, b) {
		this.a = a;
		this.b = b;
	},
	hello: function() {
		console.log("hello from A : " + this.a);
	},
	goodbye: function() {
		console.log("goodbye from A");
	}
})

var B = A.extend({
	init: function(a, b) {
		this.a = a + 1;
		this.b = b + 1;
	},
	hello: function() {
		this._super();
		console.log("hello from B : " + this.a);
	}
})

var C = B.extend({
	hello: function() {
		this._super();
		console.log("hello from C : " + this.a);
	}
})

var b = new B(1, 2);
var c = new C(1, 2);

b.hello();
b.goodbye();

c.hello();
c.goodbye();

console.log(c instanceof A); // true
console.log(b instanceof A); // true
console.log(b instanceof C); // false
*/



var Requests = CallbackHandler.extend({
	init : function(action) {
		this._super();
		this.actions = [];

		if(action)
			this.add(action);
	},
	add : function(action) {
		this.actions.push(action);

		return this;
	},
	proceed : function(action) {
		var me = this;

		if(action)
			this.add(action);

		var done = 0;
		var error = 0;
		var fail = 0;

		this.fireEvent('start', this.actions);
		var time = 0;

		this.actions.forEach(function(action) {
			setTimeout(function() {
				$.ajax({
					type: action.type || 'GET',
					url: endpoint + action.uri,
					dataType : 'json',
					processData : false,
					data : JSON.stringify(action.value || {}),
					contentType : 'application/json',
					success: function(data) {
						done++;

						me.fireEvent('success', done, error, fail, me, data.data);

						if((done + error + fail) >= me.actions.length)
							me.fireEvent('done', done, error, fail);
					},
					error: function(data) {
						error++;

						if(!data.responseJSON) {
							this.fail(data);
							return;
						}

						toastHandler.add(new Toast("Erreur " + data.responseJSON.data.status, data.responseJSON.data.message, "error"));
						
						me.fireEvent('error', done, error, fail, me, data.responseJSON.data);

						if((done + error + fail) >= me.actions.length)
							me.fireEvent('done', done, error, fail);
					},
					fail: function(data) {
						fail++;

						toastHandler.add(new Toast("Erreur ", "La requête a échouée", "error"));

						me.fireEvent('fail', done, error, fail, me);
						
						if((done + error + fail) >= me.actions.length)
							me.fireEvent('done', done, error, fail);
					}
				});
			}, time);

			time += 100;
		});

		return this;
	}
})

$.fn.draghover = function(options) {
  return this.each(function() {

    var collection = $(),
        self = $(this);

    self.on('dragenter', function(e) {
      if (collection.length === 0) {
        self.trigger('draghoverstart');
      }
      collection = collection.add(e.target);
    });

    self.on('dragleave drop', function(e) {
      collection = collection.not(e.target);
      if (collection.length === 0) {
        self.trigger('draghoverend');
      }
    });
  });
};