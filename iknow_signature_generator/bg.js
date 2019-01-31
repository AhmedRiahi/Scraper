// MUTATION OBSERVER

var observer = new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
        for(var i=0;i<mutation.addedNodes.length;i++){
        	var child = mutation.addedNodes[i];
        	if(!$(child).hasClass('iknow_dismiss_observer')){
			//	$(child).remove();
			}
		}
    });
});

var cookies = []
chrome.runtime.onMessage.addListener(function(msg, sender, sendResponse) {
     cookies = msg;
     console.log(cookies)
});
var config = { attributes: false, childList: true, characterData: true,subtree: true }
observer.observe(document,config);



// SCRIPT VARIABLES
var ContentListenerType = {
	TITLE 		:{
		id		: 1,
		text	:'TITLE'
	},
	DESCRIPTION :{
		id		: 2,
		text	:'DESCRIPTION'
	},
	DATE 		:{
		id		: 3,
		text	:'DATE'
	},
	LINK 		:{
		id		: 4,
		text	:'LINK'
	},
	CONTAINER 	:{
		id		: 5,
		text	:'CONTAINER'
	},
	ELEMENT 	:{
		id		: 6,
		text	:'ELEMENT'
	}
};

var context = {
	descriptor 		: {
		name 				: '',
		url 				: '',
		contentListeners 	: []
	},
	selectedElement 		: null,
	selectedMenuContainer 	: null,
	selectedMenuElement 	: null,
	pauseSelection 			: false
}



$(document).ready(function(){
	iknowApp.gui.drawIknowMenu();
	initListeners();
	$('a').attr('href','#')
});


function initListeners(){
	$(document).keyup(function(e){
		if(e.altKey){
			switch(e.keyCode){
				case 13:
					// ASCII('ENTER')=13
					if($('#iknow_menu').is(':visible')){
						$('#iknow_menu').hide();
						context.pauseSelection = false;
					}else{
						$('#iknow_menu').show();
						context.pauseSelection = true;
					}
				break;

				case 32:
					// ASCII('SPACE')=32
					context.pauseSelection = !context.pauseSelection;
				break;

				case 80:
					// ASCII('P')=80
					iknowApp.gui.selectParent();
				break;
			}
		}
		
	});

	$("*").on('mouseenter',function(e){
		iknowApp.gui.removeSelectedElement(e.target);
		iknowApp.gui.selectElement(e.target);
	});

	$("*").on('mouseout',function(e){
		iknowApp.gui.removeSelectedElement(e.target);
	});
}

var iknowApp = {};
iknowApp.gui = {};
iknowApp.generator = {};
iknowApp.utils = {};

// ---------------- GUI FUNCTIONS -----------------

iknowApp.gui = {
	menu : 	"\
		<div id='iknow_menu' class='iknow_dismiss_observer'>\
			<div>\
				<p class='title first'></p>\
			</div>\
			<div>\
				<p class='title'>Add new listeners</p>\
				<div id='iknow_new_listeners'>\
					<input type='text' placeholder='Name' id='iknow_listener_name'/>\
					<select id='iknow_sct'></select>\
					<br>\
					<button id='iknow_add_listener'>Add Listener</button>\
					<button id='iknow_delete_listener'>Delete Listener</button>\
					<button id='iknow_generate_listener'>Generate Descriptor</button>\
				</div>\
			</div>\
			<div>\
				<p class='title'>Containers</p>\
				<ul id='iknow_containers'></ul>\
			</div>\
			<div>\
				<p class='title'>Elements</p>\
				<ul id='iknow_elements'></ul>\
			</div>\
			<div>\
				<p class='title'>Items</p>\
				<ul id='iknow_items'></ul>\
			</div>\
			<div>\
				<p class='title'>Properties</p>\
				<div id='iknow_item_properties'>\
					<span id='cl_matches'></span> Matching elements.<br>\
					<textarea id='cl_signature'></textarea><br>\
					<button id='updateSignatureButton'>Update signature</button>\
				</div>\
			</div>\
			<div>\
				<p class='title'>Descriptor</p>\
				<textarea id='iknow_descriptor'></textarea>\
			</div>\
			<div>\
				<p class='title last'></p>\
			</div>\
		</div>\
	",

	drawIknowMenu : function(visible){
		$("body").append(this.menu);
		$.each(ContentListenerType,function(i,val){
			subItem = "<option class='iknow_dismiss_observer' value='"+val.id+"'>"+val.text+"</option>";
			$("#iknow_sct").append(subItem);	
		});

		if(typeof visible == 'undefined' || visible ==  false){
			$('#iknow_menu').hide();
		}

		$('#iknow_add_listener').on('click',function(){
			iknowApp.gui.addContentListener();
		});

		$('#iknow_delete_listener').on('click',function(){
			iknowApp.generator.removeDescriptorContentListener(context.selectedMenuItemId)
			iknowApp.gui.refreshIknowMenu();
		});


		$('#iknow_generate_listener').on('click',function(){
			iknowApp.generator.generateDescriptor();
		});

		$("#updateSignatureButton").on('click',function(){
			var newSignature = $("#cl_signature").val();
			var cl = iknowApp.generator.getCLByLocalId(context.selectedMenuItemId)
			for(var i=0; i <cl.signatures.length;i++){
				if(cl.signatures[i].signatureType=="CSS_REFERENCE_SELECTOR"){
					cl.signatures[i].value=newSignature
				}
			}
		})

		$(function() {
			$( "#iknow_menu" ).draggable();
		});
	},

	deleteIknowMenu : function(){
		$('#iknow_menu').remove();
	},

	refreshIknowMenu : function(){
		// cleaning menu elements
		$('#iknow_containers').html('')
		$('#iknow_elements').html('')
		$('#iknow_items').html('')

		// adding menu elements
		$.each(context.descriptor.contentListeners,function(i,cl){
			switch(cl.webContentType.id){
				case ContentListenerType.CONTAINER.id:
					iknowApp.gui.addMenuItem($('#iknow_containers'),cl);
				break;

				case ContentListenerType.ELEMENT.id:
					iknowApp.gui.addMenuItem($('#iknow_elements'),cl);
				break;

				case ContentListenerType.TITLE.id:
				case ContentListenerType.DATE.id:
				case ContentListenerType.DESCRIPTION.id:
				case ContentListenerType.LINK.id:
					iknowApp.gui.addMenuItem($('#iknow_items'),cl);
				break;
			}
		});
	},

	addMenuItem : function(panel,cl,signatureTypeID){
		var li = '<li class="iknow_dismiss_observer iknow_item iknow_last" data-id="'+cl.localId+'">'+cl.name+'</li>';
		$(panel).append(li);
		$('.iknow_last').on('click',function(){
			iknowApp.gui.selectMenuItem($(this),cl.webContentType.id);
		});
		$('.iknow_last').removeClass('iknow_last');
	},

	selectElement : function(elem){
		if (!context.pauseSelection){
			context.selectedElement = elem;
			$(context.selectedElement).addClass('iknow_hovered_element');
		}
	},

	removeSelectedElement : function(elem,forcedMode){
		forcedMode = typeof forcedMode != 'undefined' ? forcedMode : false
		if (!context.pauseSelection || forcedMode){
			$(context.selectedElement).removeClass('iknow_hovered_element');
			$(elem).removeClass('iknow_hovered_element');
			context.selectedElement = null;
		}
	},

	selectMenuItem : function(elem,clType){
		$('#iknow_containers .iknow_item.selected_menu_item').removeClass('selected_menu_item');
		/*switch(clType){
			case ContentListenerType.CONTAINER.id:
				$('#iknow_containers .iknow_item.selected_menu_item').removeClass('selected_menu_item');
			break;

			case ContentListenerType.ELEMENT.id:
				$('#iknow_elements .iknow_item.selected_menu_item').removeClass('selected_menu_item');
			break;

			case ContentListenerType.TITLE.id:
			case ContentListenerType.DATE.id:
			case ContentListenerType.DESCRIPTION.id:
			case ContentListenerType.LINK.id:
				$('#iknow_items .iknow_item.selected_menu_item').removeClass('selected_menu_item');
			break;
		}*/
		$(elem).addClass('selected_menu_item');
		iknowApp.gui.drawMenuItemProperties(elem)
	},

	drawMenuItemProperties : function(element){
		console.log(element)
		var localId = $(element).data("id")
		context.selectedMenuItemId = localId;
		var cl = iknowApp.generator.getCLByLocalId(localId)
		console.log(cl)
		for(var i=0; i <cl.signatures.length;i++){
			if(cl.signatures[i].signatureType=="CSS_REFERENCE_SELECTOR"){
				var signature = cl.signatures[i].value.substring(2,cl.signatures[i].value.length);
				var clMatchers = document.querySelectorAll(signature)
				$("#cl_signature").val(signature)
				$("#cl_matches").html(clMatchers.length)
				$('.matching_cl').removeClass('matching_cl')
				$(clMatchers).each(function(i,matcher){
					$(matcher).addClass('matching_cl')
				})
			}
		}
		//console.log(document.querySelector())
	},

	selectParent : function(){
		if($(context.selectedElement).prop('tagName').toLowerCase() != 'body'){
			$(context.selectedElement).removeClass('iknow_hovered_element');
			$(context.selectedElement).parent().addClass('iknow_hovered_element');
			context.selectedElement  = $(context.selectedElement).parent();
		}
	},

	addContentListener : function(){
		var name 	= $('#iknow_listener_name').val();
		var value 	= $('#iknow_sct').val();
		var clType 	= $.map(ContentListenerType,function(val){
			if (val.id == value){
				return val;
			}
		})[0];
		var refersToLocalID = null;
		//check referal item
		switch(clType.id){
			case ContentListenerType.CONTAINER.id:
				// nothing to do : container do not have referal item
			break;

			case ContentListenerType.ELEMENT.id:
				// we must select referal container
				if($('#iknow_containers .selected_menu_item').length > 0){
					refersToLocalID = $('#iknow_containers .selected_menu_item').data('id');
				}else{
					alert('You must select container referal');
					return;
				}
			break;

			default:
				// we must select referal element
				if($('#iknow_elements .selected_menu_item').length > 0){
					refersToLocalID = $('#iknow_elements .selected_menu_item').data('id');
				}else{
					alert('You must select element referal');
					return;
				}
			break;
		}

		// pre generation : cleaning
		this.deleteIknowMenu();
		this.clearPreviewHints();
		$('.iknow_hovered_element').removeClass('iknow_hovered_element')

		// generation process
		iknowApp.generator.addDescriptorContentListener(name,clType,refersToLocalID);

		// post generation : restoring graphics
		this.drawIknowMenu(true);
		this.refreshIknowMenu();
		this.refreshPreviewHints();
		this.removeSelectedElement(context.selectedElement,true)
		$('#iknow_listener_name').val('');
	},


	refreshPreviewHints : function(){
		$(context.descriptor.contentListeners).each(function(i,cl){
			switch(cl.webContentType.id){
				case ContentListenerType.CONTAINER.id:
					$(cl.DOM).addClass('iknow_preview_container');
				break;

				case ContentListenerType.ELEMENT.id:
					$(cl.DOM).addClass('iknow_preview_element');
				break;

				default:
				// not yet implemented
				break;
			}
		});
	},

	clearPreviewHints : function(){
		$('.iknow_preview_container').removeClass('iknow_preview_container');
		$('.iknow_preview_element').removeClass('iknow_preview_element');
	}
}




// ---------------- DESCRIPTOR GENERATION FUNCTIONS -----------------

iknowApp.generator = {

	clNumber : 0,
	generateDOMSignature : function(domElement,refersTo){
		var signatures = [];

		// DOM ID
		if ($(domElement).attr('id') != null){
			var signature = {
				'type'			: 'domSignatureModel',
				'signatureType'	: 'DOM_ID',
				'value'			: $(domElement).attr('id')
			}
			signatures.push(signature);
		}

		// DOM REFERENCE INDEX
		/*if ( referesTo != null ){
			var signature = {
				'type'	: 'DOM_REFERENCE_INDEX',
				'value'	: $(domElement).index($(referesTo))
			}
			signatures.push(signature);
		}*/

		// DOM CLASS
		if ($(domElement).attr('class') != null && $(domElement).attr('class') != ''){
			var signature = {
				'type'			: 'domSignatureModel',
				'signatureType'	: 'DOM_CLASS',
				'value'			: $(domElement).attr('class')
			}
			signatures.push(signature);
		}

		// CSS REFERENCE SELECTOR
		var signature = {
			'type'			: 'domSignatureModel',
			'signatureType'	: 'CSS_REFERENCE_SELECTOR',
			'value'			: $(domElement).getPath(null,refersTo)
		}
		signatures.push(signature);

		// CSS SELECTOR
		var signature = {
			'type'			: 'domSignatureModel',
			'signatureType'	: 'CSS_SELECTOR',
			'value'			: $(domElement).getPath()
		}
		signatures.push(signature);

		// CSS SELECTOR
		var signature = {
			'type'			: 'domSignatureModel',
			'signatureType'	: 'XPATH_SELECTOR',
			'value'			: $(domElement).getPathWithoutClass()
		}
		signatures.push(signature);

		return signatures;
	},

	addDescriptorContentListener : function(name,clType,referesTo){
		if( !context.pauseSelection || context.selectedElement == null ){
			alert("You must select an element before creating content listener");
			return;
		}

		var refersToDOM = null;
		// get refersTo DOM 
		if (referesTo != null){
			refersToDOM = iknowApp.utils.getContentListenerByLocalID(referesTo).DOM;
		}
		

		var signatures = this.generateDOMSignature(context.selectedElement,refersToDOM);

		var contentListener = {
			'localId'		: this.clNumber,
			'name' 			: name,
			'url'			: window.location.href,
			'signatures'	: signatures,
			'webContentType': clType,
			'refersToLocal'	: referesTo,
			'DOM'			: context.selectedElement
		}

		context.descriptor.contentListeners.push(contentListener);
		this.clNumber++;
		iknowApp.gui.refreshIknowMenu();
	},

	removeDescriptorContentListener : function(localId){
		for(var i=0;i < context.descriptor.contentListeners.length;i++){
			if(context.descriptor.contentListeners[i].localId == localId){
				context.descriptor.contentListeners.splice(i,1)
			}
		}
	},

	getCLByLocalId : function(localId){
		for(var i=0; i<context.descriptor.contentListeners.length;i++){
			if(context.descriptor.contentListeners[i].localId == localId){
				return context.descriptor.contentListeners[i];
			}
		}
	},


	generateDescriptor : function(){
		console.log('---- DESCRIPTOR ----')
		console.log(context.descriptor);
		this.prepareDescriptor();
		var cookiesList = [];
		if(cookies!=undefined && cookies.cookies!=undefined){
			for(var i=0;i<cookies.cookies.length;i++){
				cookiesList.push({
					name : cookies.cookies[i].name,
					value : cookies.cookies[i].value
				})
			}
		}
		context.descriptor.cookies = cookiesList;
		$('#iknow_descriptor').val(JSON.stringify(context.descriptor));

	},


	prepareDescriptor : function(){
		$(context.descriptor.contentListeners).each(function(i,cl){
			delete cl.DOM;
			cl.webContentType = cl.webContentType.text; 
		});
	}
}




// ---------------- UTILS FUNCTIONS -----------------

iknowApp.utils = {

	getContentListenerByLocalID : function(localId){
		for(var i=0; i < context.descriptor.contentListeners.length ; i++){
			if(context.descriptor.contentListeners[i].localId == localId ){
				return context.descriptor.contentListeners[i];
			}
		}
	}
}