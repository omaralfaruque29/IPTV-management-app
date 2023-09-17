$(function() {
			var sampleTags = new Array();
			$("#tagList option").each(function() {
				sampleTags.push($(this).val());
			});

			$('#singleFieldTags2').tagit({
				availableTags : sampleTags
			});
			
			$('.singleFieldTags2').tagit({
			});
			

			//-------------------------------
			// Preloading data in markup
			//-------------------------------
			$('#myULTags').tagit({
				availableTags : sampleTags, // this param is of course optional. it's for autocomplete.
				// configure the name of the input field (will be submitted with form), default: item[tags]
				itemName : 'item',
				fieldName : 'tags'
			});

			//-------------------------------
			// Tag events
			//-------------------------------
			var eventTags = $('#eventTags');
			var addEvent = function(text) {
				$('#events_container').append(text + '<br></br>');

			};

			eventTags.tagit({
				availableTags : sampleTags,
				beforeTagAdded : function(evt, ui) {
					if (!ui.duringInitialization) {
						addEvent('beforeTagAdded: '
								+ eventTags.tagit('tagLabel', ui.tag));
					}
				},
				afterTagAdded : function(evt, ui) {
					if (!ui.duringInitialization) {
						addEvent('afterTagAdded: '
								+ eventTags.tagit('tagLabel', ui.tag));
					}
				},
				beforeTagRemoved : function(evt, ui) {
					addEvent('beforeTagRemoved: '
							+ eventTags.tagit('tagLabel', ui.tag));
				},
				afterTagRemoved : function(evt, ui) {
					addEvent('afterTagRemoved: '
							+ eventTags.tagit('tagLabel', ui.tag));
				},
				onTagClicked : function(evt, ui) {
					addEvent('onTagClicked: '
							+ eventTags.tagit('tagLabel', ui.tag));
				},
				onTagExists : function(evt, ui) {
					addEvent('onTagExists: '
							+ eventTags.tagit('tagLabel', ui.existingTag));
				}
			});

			//-------------------------------
			// Read-only
			//-------------------------------
			$('#readOnlyTags').tagit({
				readOnly : true
			});

			//-------------------------------
			// Tag-it methods
			//-------------------------------
			$('#methodTags').tagit({
				availableTags : sampleTags
			});

			//-------------------------------
			// Allow spaces without quotes.
			//-------------------------------
			$('#allowSpacesTags').tagit({
				availableTags : sampleTags,
				allowSpaces : true
			});

			//-------------------------------
			// Remove confirmation
			//-------------------------------
			$('#removeConfirmationTags').tagit({
				availableTags : sampleTags,
				removeConfirmation : true
			});

		});