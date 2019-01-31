/*
	jQuery-GetPath v0.01, by Dave Cardwell. (2007-04-27)
	
	http://davecardwell.co.uk/javascript/jquery/plugins/jquery-getpath/
	
	Copyright (c)2007 Dave Cardwell. All rights reserved.
	Released under the MIT License.
	
	
	Usage:
	var path = $('#foo').getPath();
*/

jQuery.fn.extend({
	getPath: function( path , parent) {
		"use strict";
		if ( typeof parent == 'undefined' || parent == null){
			parent = 'html'
		}
		// The first time this function is called, path won't be defined.
		if ( typeof path == 'undefined' || path == null ) path = '';

		// If this element is <html> we've reached the end of the path.
		if (this.is(parent))
			return path;

		// Add the element name.
		if(this.get(0) == undefined){
			console.log(this)
		}
		var cur = this.get(0).nodeName.toLowerCase();
		if(cur != 'body'){
			// Determine the IDs and path.
			var id = this.attr('id'),classAttr = this.attr('class');

			// Add the #id if there is one.
			if ( typeof id != 'undefined' ){
				cur += '#' + id;
			}

			// Add any classes.
			
			if ( classAttr != undefined && typeof classAttr != 'undefined' && classAttr.trim() != ''){
				classAttr = classAttr.trim()
				cur += '.' + classAttr.split(/[\s\n]+/).join('.');
			}
		}
		// Recurse up the DOM.
		return this.parent().getPath( ' > ' + cur + path , parent );
	},


	getPathWithoutClass: function(path , parent){
		"use strict";
		if ( typeof parent == 'undefined' || parent == null){
			parent = 'html'
		}
		// The first time this function is called, path won't be defined.
		if ( typeof path == 'undefined' || path == null ) path = '';

		// If this element is <html> we've reached the end of the path.
		if (this.is(parent))
			return path;

		// Add the element name.
		var cur = this.get(0).nodeName.toLowerCase();

		// Determine the IDs and path.
		var id    = this.attr('id')


		// Add the #id if there is one.
		if ( typeof id != 'undefined' )
			cur += '#' + id;

		// Recurse up the DOM.
		return this.parent().getPathWithoutClass( ' > ' + cur + path , parent );
	}
});
