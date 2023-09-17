$(function(){	
	// Callback functions for editmode
	$('button.editmode, a.editmode').on('editpre', function(){
		console.log('editpre');		
	});
	$('button.editmode, a.editmode').on('editduring', function(){
		console.log('editduring');	
	});
	$('button.editmode, a.editmode').on('editdone', function(){
		console.log('editdone');	
	});
		
	// Callback function for view submit	
	$('button.viewmode, a.viewmode').on('viewsubmitpre', function(){
		console.log('viewsubmitpre');
	});
	$('button.viewmode, a.viewmode').on('viewsubmitduring', function(){
		console.log('viewsubmitduring');
	});
	$('button.viewmode, a.viewmode').on('viewsubmitdone', function(){
		console.log('viewsubmitdone');
	});
	
	
	// Callback function for view cancel
	$('button.viewmode, a.viewmode').on('viewcancelpre', function(){
		console.log('viewcancelpre');
	});
	$('button.viewmode, a.viewmode').on('viewcancelduring', function(){
		console.log('viewcancelduring');
	});
	$('button.viewmode, a.viewmode').on('viewcanceldone', function(){
		console.log('viewcanceldone');
	});
	
	
	// Project specific page loading init function		
	$('body').on('appinit', function(){
		// Archive a live record
		$('a#archivebutton').on('click', function() {
			console.log('clicked archive...');
			$.post($(this)[0].href, "", function(data, textStatus, jqXHR){
				$.postOnSuccess(data, textStatus, jqXHR);
			});
			return false;
		});
		
		// Restore a archive record
		$('a#restorebutton').on('click', function() {
			console.log('clicked archive...');
			$.post($(this)[0].href, "", function(data, textStatus, jqXHR){
				$.postOnSuccess(data, textStatus, jqXHR);
			});
			return false;
		});
		
		// Clean feedback message section on click cancel button 
		$('a#headercancelbutton').on('click', function() {
			$('p.navbar-text').empty();
		});
		
		// if copy button available, then we assume it is not a new request button
		if(!$('#copybutton').get(0)){
			$('#updatebutton').trigger('click');
			console.log('trigger update button..');
		} 
		else {
			viewmode();
		}
	});

});


$.postOnSuccess = function(data, textStatus, jqXHR, callback) {
//	console.log("success: " + JSON.stringify(data));
//	console.log("relaod type: " + (typeof data.reloads));
//	console.log("jqXHR: " + JSON.stringify(jqXHR));
	
	if (typeof data.redirect == 'string') {
		// Handle redirects
		var redirect = location.href;
		if (data.redirect.indexOf('://') != -1) {
			console.log("absolute redirect");
		} else {
			console.log("relative redirect");
			// XXX: Shall we use <base href...> from the current page?
			var href = location.href.split('/');
			var redirect = href[0] + '//' + href[2] + '/' + href[3] + data.redirect;
		}
		console.log("redirect to: " + redirect);
		location.href = redirect;
		
	} else if (typeof data.reloads == 'object') {
		// Handle reloads
		console.log("handle reloads: " + data.reloads);
		
		for (key in data.reloads) {
			console.log(key + " => " + data.reloads[key]);
			
			var url = data.reloads[key];			
			if (url.indexOf('://') != -1) {
				console.log("absolute redirect");
			} else {
				console.log("relative redirect");
				// XXX: Shall we use <base href...> from the current page?
				var href = location.href.split('/');
				var url= href[0] + '//' + href[2] + '/' + href[3] + url;
			}		
			
			var targetdiv = $('#' + key).parent();
			targetdiv.load(url + ' #' + key, function() {
				console.log('after reloading section...');
				targetdiv.trigger('reloaded');
				
				// Added callback functionality for postOnSuccess
				callback();
			});
			
		}
		
	} else if (typeof data.errors == 'object') {
		// Handle errors
		var errorText = '';
		for (key in data.errors) {
			console.log(key + " => " + data.errors[key]);
			// TODO: make better error display
			// errorText += key + ": " + data.errors[key] + '; ';
			errorText += data.errors[key] + '; ';
		}
		alert(errorText);
		$('p.navbar-text').html(errorText);
		$('p.navbar-text').effect("highlight", {}, 1000);
	}
};

$.postOnError = function(jqXHR, textStatus, errorThrown, callback) {
	console.log("error: " + textStatus + " ~ " + errorThrown + " ~ " + jqXHR.responseText);
	
	// Added callback functionality for postOnError
	callback();
};
