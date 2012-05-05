/**
 * Glue JavaScript library.
 */
GX = {}

/**
 * CRUD functions.
 */
GX.crud = {
	
	/**
	 *
	 */
	chooseRadio: function(radioGroupName) {
		var selectedValue = null
		var radioGroup = document.getElementById(radioGroupName)
		var len = radioGroup.length
		if (len != undefined) {
			for (var i = 0; i < len; i++) {
				if (radioGroup[i].checked) {
					selectedValue = radioGroup[i].value
				}
			}
		}
		// TODO Generate link: glueCrudService.asyncAssociate(domainA: arg.domain, idA: arg.domainId, propertyA: arg.property, domainB: propertyType, idB: "selectedValue")
	},
	
	/**
	 *
	 */
	switchCheckbox: function(successDiv, failureDiv) {
		return
		// TODO Generate link: glueCrudService.asyncSwitchAssociation(domainA: arg.domain, idA: arg.domainId, propertyA: arg.property, domainB: propType, idB: p.id)
		/*new Ajax.Updater({success: successDiv, failure: failureDiv},
			'/xxx/glue/edit',
			{
				asynchronous: true,
				evalScripts: true,
				parameters: 'domain=&domainId=&type=&mode=edit&update_success=&update_failure=&nextAction_controller=&nextAction_action=&nextAction_params='
			})
		return false*/
	}
	
}

/**
 * HTML rendering.
 */
GX.html = {
	
	/**
	 *
	 */
	setFocus: function(id) {
		document.getElementById(id).focus()
	},
	
	/**
	 *
	 */
	toggleDisplay: function(id) {
		var e = document.getElementById(id)
		e.style.display = e.style.status == "none" ? "" : "none"
	},
	
	/*
	 *
	 */
	showSpinner: function() {
		$('gxMainSpinner').show();
	},
	
	/*
	 *
	 */
	hideSpinner: function() {
		$('gxMainSpinner').hide();
	}
	
}

/**
 * File uploads.
 */
GX.fileupload = {
	
	/**
	 *
	 */
	frame: function(c) {
		var n = 'f' + Math.floor(Math.random() * 99999);
		var d = document.createElement('DIV');
		d.innerHTML = '<iframe style="display:none" src="about:blank" id="' + n + '" name="' + n + '" onload="GX.fileupload.loaded(\'' + n +'\')"></iframe>';
		document.body.appendChild(d);
		var i = document.getElementById(n);
		if (c && typeof(c.onComplete) == 'function') {
			i.onComplete = c.onComplete;
		}
		return n;
	},

	/**
	 *
	 */
	form: function(f, name) {
		f.setAttribute('target', name);
	},

	/**
	 *
	 */
	submit: function(f, c) {
		GX.fileupload.form(f, GX.fileupload.frame(c));
		if (c && typeof(c.onStart) == 'function') {
			return c.onStart();
		} else {
			return true;
		}
	},
	
	/**
	 *
	 */
	loaded: function(id) {
		var i = document.getElementById(id);
		if (i.contentDocument) {
			var d = i.contentDocument;
		} else if (i.contentWindow) {
			var d = i.contentWindow.document;
		} else {
			var d = window.frames[id].document;
		}
		if (d.location.href == "about:blank") {
			return;
		}
		if (typeof(i.onComplete) == 'function') {
			i.onComplete(d.body.innerHTML);
		}
	},
	
	/**
	 *
	 */
	startCallback: function() {
		return true;
	},
	
	/**
	 *
	 */
	completeCallback: function(response) {
		$('crud_panel_left').innerHTML = response;
		return true;
	}
	
}

// Register prototype global events
Ajax.Responders.register({
	onLoading: function() {
		GX.html.showSpinner();
	},
	onComplete: function() {
		if (!Ajax.activeRequestCount) {
			GX.html.hideSpinner();
		}
	}
});
