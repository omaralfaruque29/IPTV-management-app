/**
 @module Metaboot
 */
/**
 * @class Date Picker
 */

/**
 * Initialises datepicker widget on any inputs with class of input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate
 * @method datepickerInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @param {object} m4language Required global variable from screen specific config.js. Both the datepicker plugin and moment.js have language formats settings that are loaded for each plugin respectively from the config file.
 * @param {object} m4dateformat Required global variable from screen specific config.js. Both the datepicker plugin and moment.js have seperate date format settings that are loaded for each plugin respectively from the config file.
 * @version
 * @author Tom Yeldham
 */

//DATEPICKERS

function datepickerInitialisation(target) {

    // Initialize datepickers with the DD-MMM-YYYY format (note that the addon uses dd-M-yyyy to generate that format)
    $('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate', target).each(function() {
        var datepickerformat;
        if ($(this).data('date-format') === undefined) {
            datepickerformat = m4dateformat.datepickers;
        }
        else {
            datepickerformat = $(this).data('date-format');
        }
        $(this).datepicker({
            orientation: "auto",
            format: datepickerformat,
            autoclose: false,
            language: "m4"
        }).on("changeDate", function() {
            // Then add validation triggers on the changedate event.
            if ($(this).is('input.con-startdate, input.con-enddate')) {
                var datepair = $(this).data("datepair");
                if ($("input[data-datepair=" + datepair + "]").not('#' + $(this).attr('id') + '').val() !== "" && $('div.datepicker:visible').length > 0) {
                    $(this).valid();
                }
            }
            else {
                if ($('div.datepicker:visible').length > 0) {
                    $(this).valid();
                }
            }
            if ($(this).parents('div#flightstable')) {
                //Specific event for the booking screen flight modal datepickers
                $(this).trigger('nowdodatepickerstuff');
            }
            if ($('div.datepicker:visible').length > 0) {
                $(this).focus();
            }
        }).on('show', function() {
            // Make sure that on datepicker opening, the selected date is correctly shown
            $(this).datepicker('update');
        }).on("blur", function() {
            var momentformat = "";
            if ($(this).data('moment-format') === undefined) {
                momentformat = m4dateformat.moment;
            }
            else {
                momentformat = $(this).data('moment-format');
            }
            if ($('div.datepicker:visible').length < 1 && ($(this).val() !== "" || $(this).data("beenactive") === "Y")) {
                var thisval = $(this).val();
                var m4date;
                //Get the numbers from the value
                var numbersinvalue = thisval.replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()a-zA-Z]/g, '');
                //Check for value existance and the presence of at least 2 digits
                if (!$(this).valid()) {
                    if (thisval.length > 0 && numbersinvalue.length >= 1) {
                        //If so make pull the letters from the value
                        var lettersinvalue = thisval.replace(/[\.-\/#!$�%\^& \*;:{}=\-_`~()0-9]/g, '');
                        //Make an array of the words, split at comma's
                        var wordarray = lettersinvalue.split(",");
                        var datepickermatcher = 0;
                        var wordarraycounter = 0;
                        //If there are contents in the array
                        if (lettersinvalue.length > 0) {
                            //Loop each one
                            $.each(wordarray, function() {
                                var strippedword = wordarray[wordarraycounter].replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()]/g, '');
                                //Capitalise the first letter of each item
                                var capitalisedword = capitaliseFirstLetter(strippedword);
                                wordarray[wordarraycounter] = capitalisedword;
                                //If the contents match the global variable months or days
                                if ($.inArray(wordarray[wordarraycounter], m4language.moment.monthsShort) !== -1) {
                                    //Increase the ticker
                                    datepickermatcher++;
                                    if ($.inArray(wordarray[wordarraycounter], m4language.moment.weekdaysShort) !== -1)
                                    {
                                        datepickermatcher++;
                                    }
                                }
                                wordarraycounter++;
                            });
                        }
                        //If there are no matches
                        if (datepickermatcher < 1) {
                            // Check if theres any letters left in the value
                            if (thisval.replace(/[\.-\/#!$�%\^& \*;:{}=\-_`~()0-9]/g, '').length === 0) {
                                var strippedval = thisval.replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()]/g, '');
                                //Check for ddmmyy format
                                if (strippedval.length === 6) {
                                    var typedyears = strippedval.substr(4);
                                    //If ddmmyy then check if the years were typed as 00
                                    if (typedyears === "00") {
                                        //If so then set the years as 2000 
                                        var daysandmonths = thisval.substring(0, 4);
                                        thisval = daysandmonths + "2000";
                                    }
                                }
                                //Set the date variable using the created string in the format it was produced in
                                m4date = moment(thisval, "DD-MM-YYYY");
                            }
                            else {
                                // Call Fire validation error
                                $(this).valid();
                                console.log('fire error');
                                return false;
                            }
                        }
                        else {
                            // if there are 2 matches, then it is using ddd, DD-MMM-YYYY format
                            if (datepickermatcher === 2) {
                                m4date = moment(thisval, "ddd, DD-MMM-YYYY");
                            }
                            else {
                                m4date = moment(thisval, "DD-MMM-YYYY");
                            }
                        }
                        var dyears = m4date.format("YYYY");
                        // If no years are typed into the field
                        if (dyears === "0000") {
                            //Sets the years to the current year
                            var today = moment().format("YYYY-MM-DD");
                            var fulldate = m4date.year(moment().format("YYYY"));
                            var usablefulldate = fulldate.format("YYYY-MM-DD");
                            //If the full date has occured this year, then guess next year
                            if (moment(usablefulldate).isBefore(today)) {
                                usablefulldate = moment(usablefulldate).add('years', 1);
                            }
                            //Format the date with the global date format
                            var finaldate = moment(usablefulldate).format(momentformat);
                            $(this).val(finaldate);
                        }
                        else {
                            //Checks if it is a 4 character date that has been typed
                            if (dyears.charAt(0) === "0") {
                                //if not then creates variables for the year 10 years from now, the last 2 values of that year and of the typed date
                                var tenyearsahead = moment().add("years", 10).format("YYYY");
                                var trimmedtenyearsahead = tenyearsahead.substr(2);
                                var trimmedyears = dyears.substr(2);
                                var correctedyear;
                                // if the last 2 digits of the typed year value is greater than those of the date 10 years from now, then assume that the date is meant to be in the past and set to 19+these digits
                                if (trimmedyears > trimmedtenyearsahead) {
                                    correctedyear = 19 + trimmedyears;
                                }
                                else {
                                    //otherwise guess 20..
                                    correctedyear = 20 + trimmedyears;
                                }
                                //use this calculated year value to combine with the rest of the typed date to form a final date
                                var usabled = m4date.format(momentformat);
                                var trimmed = usabled.substring(0, usabled.length - 4);
                                var finaldate2 = trimmed + correctedyear;
                                // set the final date as the value and make the datepicker update it's location to this date'
                                $(this).val(finaldate2);
                            }
                            else {
                                var justformattheirdate = m4date.format(momentformat);
                                $(this).val(justformattheirdate);
                            }
                        }
                        $(this).datepicker('update');
                        if ($(this).parents('div#flightstable')) {
                            //Specific event for the booking screen flight modal datepickers
                            $(this).trigger('nowdodatepickerstuff');
                        }
                        $(this).valid();
                    }
                    else {
                        if ($(this).data("beenactive") === "Y") {
                            $(this).valid();
                        }
                    }
                }
            }
        }).on("focus", function() {
            if ($(this).val() !== "") {
                $(this).data("beenactive", "Y");
            }
        });
    });

    $('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate', target).attr('autocomplete', 'off');
    var datepickerdateformat = m4dateformat.moment;

    var twohundredyearsago = moment().subtract('years', 200).format(datepickerdateformat);
    var twohundredfromnow = moment().add('years', 200).format(datepickerdateformat);
    var defaultstartdatepickerdate = twohundredyearsago;
    var defaultenddatepickerdate = twohundredfromnow;
    if ($('.con-startdate').data('date-limit') !== undefined) {
        defaultstartdatepickerdate = $(this).data('date-limit');
    }
    if ($('.con-enddate').data('date-limit') !== undefined) {
        defaultenddatepickerdate = $(this).data('date-limit');
    }
    $('.con-startdate', target).datepicker('setStartDate', defaultstartdatepickerdate);
    $('.con-enddate', target).datepicker('setEndDate', defaultenddatepickerdate);

    // On blur of a startdate field, if it's paired enddate field doesn't yet have a value, then set it a week ahead from the selected startdate
    $('.con-startdate', target).on('blur', function() {
        var momentformat = "";
        if ($(this).data('moment-format') === undefined) {
            momentformat = m4dateformat.moment;
        }
        else {
            momentformat = $(this).data('moment-format');
        }
        var datepair = $(this).data("datepair");
        var adddays = $(this).data("adddays");
        var otherdate = $("input[data-datepair=" + datepair + "]").not(".con-startdate");
        var otherval = otherdate.val();
        var thisval = $(this).val();
        if (thisval !== "" && otherval === "") {
            var d = moment(thisval, momentformat);
            var oneweekfromthen;
            if (typeof adddays !== 'undefined') {
                oneweekfromthen = d.add('days', adddays).format(momentformat);
            }
            else {
                oneweekfromthen = d.add('days', 7).format(momentformat);
            }
            otherdate.val(oneweekfromthen);
            otherdate.datepicker('update');
        }
    });

    $('.con-enddate', target).on('blur', function() {
        var durationpairvalue = $(this).data('durationpair');
        var durationtarget = $("input[data-durationpair=" + durationpairvalue + "]").not(".con-enddate, .con-startdate");
        if (durationtarget.length > 0) {
            var momentformat = "";
            if ($(this).data('moment-format') === undefined) {
                momentformat = m4dateformat.moment;
            }
            else {
                momentformat = $(this).data('moment-format');
            }
            var datepair = $(this).data("datepair");
            var otherdate = $("input[data-datepair=" + datepair + "]").not(".con-enddate");
            var fromdate = otherdate.val();
            if (fromdate !== '') {
                var todate = $(this).val();
                var momentedfromdate = moment(fromdate, momentformat);
                var momentedtodate = moment(todate, momentformat);
                var days = momentedtodate.diff(momentedfromdate, 'days');
                durationtarget.val(days);
            }
        }
    });

    $('input[data-durationpair]:not(".con-enddate, .con-startdate")', target).on('blur', function() {
        var durationpairvalue = $(this).data('durationpair');
        var fromdateinput = $("input[data-durationpair=" + durationpairvalue + "].con-startdate");
        var todateinput = $("input[data-durationpair=" + durationpairvalue + "].con-enddate");
        var momentformat = "";
        if ($(this).data('moment-format') === undefined) {
            momentformat = m4dateformat.moment;
        }
        else {
            momentformat = $(this).data('moment-format');
        }
        var fromdate = fromdateinput.val();
        if (fromdate !== '') {
            var momentedfromdate = moment(fromdate, momentformat);
            var daystoadd = $(this).val();
            var todate = momentedfromdate.add('days', daystoadd).format(momentformat);
            todateinput.val(todate);
        }
    });

    $('span.add-on', target).has('i.icon-calendar').on('click', function() {
        var datepickerfield = $(this).parents('div.input-append').find('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate');
        datepickerfield.datepicker('show');
        datepickerfield.focus();
    });

}


/* $id$ */
/**
 * @class Drag-Drop Tables
 */

/**
 * Used to initialise drag drop functionality on all forms of draggable/sortable and droppable tables
 * @method draggableTableInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 * @return false On all buttons to prevent form submission by default
 */

// DRAGGABLE TABLES

function draggableTableInitialisation(target) {

    var fixHelper = function(e, ui) {
        ui.children().each(function() {
            $(this).width($(this).width());
            $(this).height($(this).height());
        });
        return ui;
    };

    //Initializes dragging on sortable table rows
    $('table.con-sortable tbody', target).sortable({
        handle: ".draghandle",
        helper: fixHelper
    });

    //Initializes droppable tbody
    $("table.con-droppable tbody", target).sortable({
        revert: true,
        helper: fixHelper,
        opacity: 0.5,
        handle: ".draghandle",
        // On drop, if it's a new row
        stop: function(event, ui) {
            if (ui.item.hasClass("newrow")) {
                var thisid = $(this).parents("table").attr("id");
                var i = $("#" + thisid + " tbody tr:visible").length;
                //Clone the hidden template row in the tbody
                $("#" + thisid + " tbody tr:first").clone().find(":input").each(function() {
                    // set the id and name to be unique
                    $(this).attr({
                        'id': function(_, id) {
                            return id + i;
                        },
                        'name': function(_, name) {
                            return name + i;
                        },
                        'value': ''
                    });
                    //and append the row to the tbody after the dropped row
                }).end().insertAfter(ui.item).addClass("float").removeClass("nodisplay").addClass("newrow").attr({
                    'id': function(_, id) {
                        return id + i;
                    }
                });
                var validationtarget = $("#" + thisid + " tr.newrow");
                // Clear dropped row
                ui.item.remove();
                // Run validation initialization over the new row.
                addvalidation(validationtarget);
                $("#" + thisid + " tr.newrow").find(" :input").each(function() {
                    $(this).bind("change", function() {
                        $(this).valid();

                    });
                });
                $("#" + thisid + " tr.newrow").removeClass("newrow");
                $("#" + thisid + " tr.placeholder").remove();
                adjustscroll();
            }
        }
    });

    // Initializes draggable rows and pairs them to their droppable tbody through the data-tablepair attribute
    $("table.con-draggable tr", target).each(function() {
        var tablepair = $(this).parents("table").data("tablepair");
        var pairedtable = $("table.con-droppable[data-tablepair=" + tablepair + "]").attr("id");
        var pairedtablebody = $("#" + pairedtable + " tbody");
        var options = {
            connectToSortable: pairedtablebody,
            helper: 'clone',
            placeholder: "ui-state-highlight",
            handle: ".draghandle",
            opacity: 0.5
        };
        $(this).draggable(options);
    });

    //  For droppable tables, if all actual rows are deleted, then produce a placeholder drop location row.
    $("table.con-droppable tbody button.deleterow", target).on("click", function() {
        var thisid = $(this).parents("table").attr("id");
        var i = $("#" + thisid + " tbody tr:visible").length;
        if (i < 2) {
            var colcount = $("#" + thisid + " thead th").length;
            $("#" + thisid + " tbody").append("<tr class='placeholder'><td colspan=" + colcount + "><p>Drop rows here</p></td></tr>");
        }
    });

}


/* $id$ */
/**
 * @class Email Modal
 */

/**
 * Initialises various buttons used in the email modals
 * @method emailModalInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function emailModalInitialisation(target) {

    $('.replyorforward', target).on('click', function() {
        $(this).parents('div.modal').find('input:disabled').attr('disabled', false);
        $(this).parents('div.modal').find('button[type=submit], div.fileupload-buttonbar button, div.fileinput-button').show();
        $(this).parents('div.modal').find('.fileupload').fileupload('enable');
    });

    $('.viewemailmodal', target).on('shown', function() {
        var cc = $(this).find('input[id$="cc"]');
        var bcc = $(this).find('input[id$="bcc"]');
        if (cc.attr('value') !== "" || cc.val() !== "") {
            cc.parents('div.control-group.nodisplay').show();
        }
        else {
            cc.parents('div.control-group.nodisplay').hide();
        }
        if (bcc.attr('value') !== "" || bcc.val() !== "") {
            bcc.parents('div.control-group.nodisplay').show();
        }
        else {
            bcc.parents('div.control-group.nodisplay').hide();
        }
        $(this).find('div.modal-body :input').not('div.fileupload-buttonbar button, div.fileinput-button').attr('disabled', "disabled");
        $(this).find('button[type=submit], div.fileupload-buttonbar button, div.fileinput-button').hide();
        $(this).find('.fileupload').fileupload('disable');
        disablerte($(this));
    });

    $('.emailstable tbody td:has("i.star")', target).on('click', function() {
        $(this).find('i.star').toggleClass('icon-star-empty');
    });

}


/* $id$ */
/**
 * @class Field Toggles
 */

/**
 * Initialises the various forms of visual toggling of elements .clickchangetrigger for radio controls, .checkboxcontrolled for checkboxes, .showhide for generic elements, .sectionbartoggle for section headerbar elements
 * @method fieldToggleInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @return   false on all functions here to prevent generic form submission
 * @version
 * @author Tom Yeldham
 */
function fieldToggleInitialisation(target) {

    // On load makes inputs paired to a, currently checked, checkbox to be visible
    $('div.controls label.radio input:checked, div.controls label.checkbox input:checked', target).parents('label').next(':input:hidden, div:has("input[data-search-url]")').show();

    $('select.onlyoneshow option.onlyonehide', target).on('click', function() {

        var parentid = $('select.onlyoneshow').attr('id');
        $('.' + parentid + 'hide').css("display", "none");
    });

    //  On change event for keyboard controls
    $('.clickchangetrigger', target).on('change', function(event) {
        var thisgroup = $(this).data('clickchangegroup');
        var thispair = $(this).data('clickchangepair');
        // Hides all non trigger elements in group of fields, then shows the one that pairs to the clicked element.
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').hide();
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').parents('div.controls').removeClass('error');
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').removeClass('error');
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').parents('div.controls').find('div.help-block').remove();
        $('[data-clickchangegroup=' + thisgroup + '][data-clickchangepair*=' + thispair + ']').show();
        event.stopImmediatePropagation();
        return false;
    });

    //  Same duplicated functionality for click events. Solves some weird bug related to clicking radios to my recolection
    $('.clickchangetrigger', target).on('click', function() {
        var thisgroup = $(this).data('clickchangegroup');
        var thispair = $(this).data('clickchangepair');
        // Hides all non trigger elements in group of fields, then shows the one that pairs to the clicked element.
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').hide();
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').parents('div.controls').removeClass('error');
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').removeClass('error');
        $('[data-clickchangegroup=' + thisgroup + ']').not('.clickchangetrigger').parents('div.controls').find('div.help-block').remove();
        $('[data-clickchangegroup=' + thisgroup + '][data-clickchangepair*=' + thispair + ']').show();
    });

    // Used for checkboxes to toggle elements in and out. Clears all validation on hide.
    $('.checkboxcontrolled', target).on('change', function() {
        var targetinput = $('[data-toggledby="' + this.id + '"]');
        var targetinputid = targetinput.attr('id');
        if (targetinput.next().is($('div.help-block').has('[for="' + targetinputid + '"]'))) {
            targetinput.next().remove();
        }
        targetinput.removeClass('error');
        targetinput.toggle();
        adjustscroll();
        return false;
    });

    // Simple toggle function
    $('.showhide', target).on("change, click", function() {
        $('.' + this.id).toggle();
        adjustscroll();
        if ($(this).parents('table#accomtable')) {
            $(this).find('i.icon-chevron-right, i.icon-chevron-down').toggleClass("icon-chevron-right").toggleClass("icon-chevron-down");
        }
        return false;
    });

    // Sectionbar toggle function
    $('.sectionbartoggle', target).on("click", function() {
        $(this).siblings().toggle();
        $(this).find('i.icon-chevron-right, i.icon-chevron-down').toggleClass("icon-chevron-right").toggleClass("icon-chevron-down");
        adjustscroll();
        return false;
    });

}


/* $id$ */
/**
 * @class In Row Edit
 */

/**
 * Initialises buttons and keyboard nav on in grid tables
 * @method inrowEditTableInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @param {object} submitSetting Required global variable from screen specific searchconfig.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a submit-settings data attribute to it containing the key of the set that is required. These settings are passed to the generic inroweditsubmit function. See examples below.
 * @version
 * @author Tom Yeldham
 */
function inrowEditTableInitialisation(target) {

    $.each($('table.gridtable', target), function() {
        $(this).find(':input').unbind();
        var id = $(this).attr('id');
        var markup = $(this).parent().html();
        localStorage.setItem(id, markup);
        setuptablenav($(this));
    });

    // Initialise Keyboard control for tablecells. Allows keyboard navigation around the cells if set up correctl

    $('.gridtableeditbutton', target).on('click', function() {
        $(this).parents('table').find(':disabled').removeAttr('disabled');
        $(this).parents('table').find('.gridtablenewbutton, .gridtableconfirmchangesbutton, .gridtablecancelchangesbutton').show();
        $(this).hide();
         return false;
    });
    
    $('.gridtablenewbutton', target).on('click', function() {
        var tbody = $(this).parents('table').find('tbody');
        tbody.find('tr:last-child').clone().addClass('cloneofrow').appendTo(tbody);
        var newrow = tbody.find('.cloneofrow');
        var newrowinputs = newrow.find(':input');
        reidrowinputs($(this).parents('form'));
        newrow.find('.error').removeClass('error');
        newrow.find('div.help-block').remove();
        metaboot(newrow);
        editmode(newrow);
        setuptablenav(newrow);
        $.each(newrowinputs, function() {
            $(this).val("");
            $(this).removeClass('error');
        });
        newrow.removeClass('cloneofrow');
        var thisid = $(this).parents("table").attr("id");
        var i = $("#" + thisid + " tbody tr:visible").length;
        if (i >= 2) {
            $("table.gridtable tbody button.deleterow").show();
        }
        $(this).parents('table').trigger('cleanandvalidate');
        return false;
    });

    $('.gridtableconfirmchangesbutton', target).on('click', function() {
        if ($(this).parents('form').valid()) {
            var submitsettings = $(this).data('submit-settings');
            var submitconfig = submitSetting[submitsettings];
            inroweditsubmit(submitconfig);
        }
    });

    $('.gridtablecancelchangesbutton', target).on('click', function() {
        var parentform = $(this).parents('table.gridtable').parent();
        var parenttableid = $(this).parents('table.gridtable').attr('id');

        // remove the rows in the specified table before we start
        parentform.empty();
        // get the table from local storage using the id of the table as the key, append it to the table and then initialise it
        var tablemarkup = localStorage.getItem(localStorage.key(findIndexOfKey(parenttableid)));
        parentform.append(tablemarkup);
        inrowEditTableInitialisation(parentform);
        metaboot(parentform);
        editmode(parentform);
        addvalidation(parentform);


        // either use an ajax call to refresh or maybe get it from local storage
        //            $(this).parents('table').find('tbody :input').attr('disabled', 'disabled');
        //            $(this).parents('table').find('.gridtablenewbutton, .gridtableconfirmchangesbutton, .gridtablecancelchangesbutton').hide();
        //            $(this).parents('table').find('.gridtablenewbutton').show()
    });

    $('table.gridtable', target).on('cleanandvalidate', function() {
        $.each($(this).find(':input').not('button'), function() {
            $(this).rules("remove");
            $(this).siblings('div.help-block').not('div.help-block:has(span[for="' + $(this).attr('id') + '"])').find('span').attr('for', $(this).attr('id'));
        });
        $(this).find('td div.help-block:empty').remove();
        addvalidation($(this));
    });
}


/* $id$ */
/**
 * @class Inputs
 */

/**
 * Initialises general functionality on various form elements
 * @method inputInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function inputInitialisation(target) {

    /**
     * Generalised tooltip intialisation function. Used primarily for input add-ons on elements that have helper tooltips, ie datepickers
     * @method initialiseaddontooltips
     * @param {Jquery object} targetelement Required parameter used to target the initialisation on the desired element.
     * @param {string} message Required parameter. Sets the tooltip message for that addon.
     * @version
     * @author Tom Yeldham
     */
    function initialiseaddontooltips(targetelement, message) {
        var parentelement = targetelement.parents('div.control-group, td');
        targetelement.tooltip({
            title: message,
            animation: true,
            trigger: 'hover',
            delay: {
                show: 600,
                hide: 250
            },
            placement: function(tip, element) {
                var $element, above, actualHeight, actualWidth, below, boundBottom, boundLeft, boundRight, boundTop, elementAbove, elementBelow, elementLeft, elementRight, isWithinBounds, left, pos, right;
                isWithinBounds = function(elementPosition) {
                    return boundTop < elementPosition.top && boundLeft < elementPosition.left && boundRight > (elementPosition.left + actualWidth) && boundBottom > (elementPosition.top + actualHeight);
                };
                $element = $(element);
                pos = $.extend({}, $element.offset(), {
                    width: element.offsetWidth,
                    height: element.offsetHeight
                });
                actualWidth = 283;
                actualHeight = 117;
                boundTop = $(document).scrollTop();
                boundLeft = $(document).scrollLeft();
                boundRight = boundLeft + $(window).width();
                boundBottom = boundTop + $(window).height();
                elementAbove = {
                    top: pos.top - actualHeight,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementBelow = {
                    top: pos.top + pos.height,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementLeft = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left - actualWidth
                };
                elementRight = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left + pos.width
                };
                above = isWithinBounds(elementAbove);
                below = isWithinBounds(elementBelow);
                left = isWithinBounds(elementLeft);
                right = isWithinBounds(elementRight);
                if (above) {
                    return "top";
                } else {
                    if (below) {
                        return "bottom";
                    } else {
                        if (left) {
                            return "left";
                        } else {
                            if (right) {
                                return "right";
                            } else {
                                return "right";
                            }
                        }
                    }
                }
            },
            container: parentelement
        }).on('show', function(e) {
            e.stopPropagation();
        }).on('hidden', function(e) {
            e.stopPropagation();
        });
    }

    $(":input[data-valid='postcode']", target).on('keypress', function() {
        $(this).val($(this).val().toUpperCase());
    });

// Initializes tooltips
    $('[rel=tooltip] ,[data-toggle=tooltip]', target).each(function() {
        initialiseaddontooltips($(this));
    });
    $("input[data-valid='distance'], input[data-valid='weight'], input[data-valid='dims'], input[data-valid='currencytogbp']").each(function() {
        var type = $(this).data('valid');
        var message = "";
        switch (type) {
            case "distance":
                message = "For miles -> km conversion, just subfix mi. Hover here to see distance in miles.";
                break;
            case "weight" :
                message = "For pound -> kilo conversion, just subfix lb.  Hover here to see distance in lbs.";
                break;
            case "dims" :
                message = "For inch -> cm conversion, just subfix in. Hover here to see the distance in inches.";
                break;
            case "currencytogbp" :
                message = "For conversion to GBP, just subfix gbp. Hover here to see the amount in USD.";
                break;
        }
        initialiseaddontooltips($(this).parents('div.input-append').find('span.add-on'), message);
    });
    $('span.add-on').has('i.icon-calendar').each(function() {
        initialiseaddontooltips($(this), 'Click here or press "?" to open the datepicker. Alternatively type your date into the field');
    });
    $('input.con-dobfield').parents('div.input-append').find('span.add-on').each(function() {
        initialiseaddontooltips($(this), 'D.o.B fields require a full DD-MM-YYYY formatted date');
    });
    $('span.add-on').has('i.icon-time').each(function() {
        initialiseaddontooltips($(this), 'Time fields accept 3 or 4 digit strings. Delimiters will be added if not provided');
    });
    $('span.add-on').has('i.icon-search').each(function() {
        initialiseaddontooltips($(this), 'Type into the field to search');
    });
    $('.con-dobfield', target).on('blur', function() {
        if ($(this).valid() && $(this).val().length > 0) {
            var digits = $(this).val().replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()a-zA-Z]/g, '');
            var days = digits.substring(0, 2);
            var months = digits.substring(2, 4);
            var years = digits.substring(4, 8);
            $(this).val(days + "-" + months + "-" + years);
        }
    });
    $('.con-timepicker', target).on('blur', function() {
        var typedtime = $(this).val();
        var typedigits = typedtime.replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()a-zA-Z]/g, '');
        var noofdigits = typedigits.length;
        if (typedtime.length > 0) {
            if (noofdigits > 0 && typedtime.replace(/[\.,-\/#!$�%\^& \*;:{}=\-_`~()0-9]/g, '').length === 0) {
                if (noofdigits === 1 || noofdigits === 2) {
                    typedigits = typedigits + "00";
                }
                if (typedigits.length === 3) {
                    typedigits = "0" + typedigits;
                }
                $(this).val(typedigits);
                if ($(this).valid()) {
                    var hours = typedigits.substring(0, 2);
                    var mins = typedigits.substring(2, 4);
                    if ((hours === "00" && mins === "00") || (hours <= "23" && mins <= "59")) {
                        $(this).val(hours + ":" + mins);
                    }
                }
                else {
                    $(this).val(typedtime);
                    $(this).valid();
                }
            }
            else {
                $(this).valid();
            }
        }
    });
    $("input:text").focus(function() {
        $(this).select();
    }).mouseup(function(e) {
        e.preventDefault();
    });
    // Pairs 2 fields to have the same value as one another. Not complete
    $('[data-valuepair]', target).on('change', function() {
        var pair = $(this).data('valuepair');
        var value = $(this).val();
        $('[data-valuepair=' + pair + ']').val(value).valid();
    });
    //  Stops text-ares from being able to be made smaller than their initialized width.
    $('textarea', target).each(function() {
        var width = $(this).width();
        $(this).css('min-width', width);
    });
    // Initializes popovers
    $('[rel=popover], [data-toggle=popover]', target).each(function() {
        var parentelement = $(this).parents('div.control-group, td');
        $(this).popover({
            animation: true,
            placement: function(tip, element) {
                var $element, above, actualHeight, actualWidth, below, boundBottom, boundLeft, boundRight, boundTop, elementAbove, elementBelow, elementLeft, elementRight, isWithinBounds, left, pos, right;
                isWithinBounds = function(elementPosition) {
                    return boundTop < elementPosition.top && boundLeft < elementPosition.left && boundRight > (elementPosition.left + actualWidth) && boundBottom > (elementPosition.top + actualHeight);
                };
                $element = $(element);
                pos = $.extend({}, $element.offset(), {
                    width: element.offsetWidth,
                    height: element.offsetHeight
                });
                actualWidth = 283;
                actualHeight = 117;
                boundTop = $(document).scrollTop();
                boundLeft = $(document).scrollLeft();
                boundRight = boundLeft + $(window).width();
                boundBottom = boundTop + $(window).height();
                elementAbove = {
                    top: pos.top - actualHeight,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementBelow = {
                    top: pos.top + pos.height,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementLeft = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left - actualWidth
                };
                elementRight = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left + pos.width
                };
                above = isWithinBounds(elementAbove);
                below = isWithinBounds(elementBelow);
                left = isWithinBounds(elementLeft);
                right = isWithinBounds(elementRight);
                if (above) {
                    return "top";
                } else {
                    if (below) {
                        return "bottom";
                    } else {
                        if (left) {
                            return "left";
                        } else {
                            if (right) {
                                return "right";
                            } else {
                                return "right";
                            }
                        }
                    }
                }
            },
            trigger: 'hover',
            delay: {
                show: 600,
                hide: 250
            },
            html: true,
            container: parentelement
        }).on('show', function(e) {
            e.stopPropagation();
        }).on('hidden', function(e) {
            e.stopPropagation();
        });
    });
    $('[data-toggle=autowidthpopover]', target).each(function() {
        var parentelement = $(this).parents('div.control-group, td');
        $(this).popover({
            animation: true,
            placement: function(tip, element) {
                var $element, above, actualHeight, actualWidth, below, boundBottom, boundLeft, boundRight, boundTop, elementAbove, elementBelow, elementLeft, elementRight, isWithinBounds, left, pos, right;
                isWithinBounds = function(elementPosition) {
                    return boundTop < elementPosition.top && boundLeft < elementPosition.left && boundRight > (elementPosition.left + actualWidth) && boundBottom > (elementPosition.top + actualHeight);
                };
                $element = $(element);
                pos = $.extend({}, $element.offset(), {
                    width: element.offsetWidth,
                    height: element.offsetHeight
                });
                actualWidth = 283;
                actualHeight = 117;
                boundTop = $(document).scrollTop();
                boundLeft = $(document).scrollLeft();
                boundRight = boundLeft + $(window).width();
                boundBottom = boundTop + $(window).height();
                elementAbove = {
                    top: pos.top - actualHeight,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementBelow = {
                    top: pos.top + pos.height,
                    left: pos.left + pos.width / 2 - actualWidth / 2
                };
                elementLeft = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left - actualWidth
                };
                elementRight = {
                    top: pos.top + pos.height / 2 - actualHeight / 2,
                    left: pos.left + pos.width
                };
                above = isWithinBounds(elementAbove);
                below = isWithinBounds(elementBelow);
                left = isWithinBounds(elementLeft);
                right = isWithinBounds(elementRight);
                if (above) {
                    return "top";
                } else {
                    if (below) {
                        return "bottom";
                    } else {
                        if (left) {
                            return "left";
                        } else {
                            if (right) {
                                return "right";
                            } else {
                                return "right";
                            }
                        }
                    }
                }
            },
            trigger: 'hover',
            delay: {
                show: 600,
                hide: 250
            },
            html: true,
            container: parentelement,
            template: '<div class="popover in autowidth"><div class="arrow"></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"><p></p></div></div></div>'
        }).on('show', function(e) {
            e.stopPropagation();
        }).on('hidden', function(e) {
            e.stopPropagation();
        });
    });
}


/* $id$ */
/**
 * @class Metaboot
 */

function metaboot(target) {
    inrowEditTableInitialisation(target);
    viewEditModeEventHandlingInitialisation(target);
    multiselectInitialisation(target);
    datepickerInitialisation(target);
    inputInitialisation(target);
    fieldToggleInitialisation(target);
    pageInitialisation(target);
    navInitialisation(target);
    oldInroweditTableInitialisation(target);
    draggableTableInitialisation(target);
    searchSuggestInitialisation(target);
    tableInitialisation(target);
    uploaderInitialisation(target);
    rteInitialisation(target);
    modalsInitialisation(target);
    emailModalInitialisation(target);

    adjustscroll();
}

$(document).ready(function(){
    setvalidator();
    metaboot($('body'));
    addvalidation($('body'));
    $('body').trigger('appinit');
});


/* $id$ */
/**
 * @class Modals
 */

/**
 * All the modal Initialisation code
 * @method modals
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function modalsInitialisation(target) {

    //Locks modal scroll in place when a modal is fired from within another modal
    $('div.modal:not(".bootstrap-wysihtml5-insert-link-modal")', target).on('shown', function() {
        $('body').css('overflow', 'hidden');
    }).on('hidden', function(e) {
        $('body').css('overflow', 'auto');
    });

    // Clears remotely loaded content on modal close
    $('div.modal:not(".bootstrap-wysihtml5-insert-link-modal")').on('hidden', function() {
        $(this).removeData('modal');
    });





    /**
     * Sets adjustmodalbodyminheight event listener on all modals and the triggers for it
     * @method modalHeightAdjustmentInitialisation
     * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
     * @version
     * @author Tom Yeldham
     */

    //MODAL HEIGHT ADJUSTMENT

    function modalHeightAdjustmentInitialisation(target) {

        $(window).resize(function() {
            if ($('div.modal').is(':visible')) {
                $('div.modal:visible').find('.modal-body').trigger('adjustmodalbodyminheight');
            }
        });


        // On modal show fire custom event
        $('div.modal:not(".bootstrap-wysihtml5-insert-link-modal")', target).on('shown', function() {
            $(this).find('.modal-body').trigger('adjustmodalbodyminheight');
        });

        // Which causes the modal to calculate the visible space on the screen and adjust itself vertically to fill it correctly
        $('div.modal-body', target).on('adjustmodalbodyminheight', function() {
            var headerheight = $(this).parents('div.modal').find('div.modal-header:visible').height();
            var footerheight = $(this).parents('div.modal').find('div.modal-footer:visible').height();
            var viewportHeight = $(window).height();
            var combinedheights = viewportHeight - (headerheight + footerheight) - 80;
            var negativetop = (viewportHeight / 2) - 20;
            $(this).parents('div.modal').css('margin-top', '-' + negativetop + 'px');
            $(this).css('max-height', '' + combinedheights + 'px');
        });


    }



    /**
     * Initialises various functionality for all modal dismiss buttons.
     * @method modalCloseInitialisation
     * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
     * @version
     * @author Tom Yeldham
     */

    //MODAL CLOSE

    function modalCloseInitialisation(target) {

        $('div.modal-body :input').on('change', function() {
            var modalclosebutton = $(this).parents('div.modal').find('div.modal-footer button.modalclose');
            if (modalclosebutton.data('firewarning') !== "Y") {
                modalclosebutton.data('firewarning', 'Y');
            }
        });

        $('div.modal-footer button.modalclose', target).on('click', function() {
            if ($(this).data('firewarning') === "Y") {
                modalclose($(this).parents('div.modal').attr('id'));
            }
            else {
                $(this).parents('div.modal').modal('hide');
            }
        });

        $('div.modal:not(".bootstrap-wysihtml5-insert-link-modal")', target).on('shown', function() {
            $(this).find('div.modal-footer button.modalclose').data('firewarning', "");
        });

        function modalclose(targetmodalid) {
            var r = confirm("Close without saving?");
            if (r === true)
            {
                $('#' + targetmodalid + '').modal('hide');
                $('#' + targetmodalid + '').find('tr.template-download, .fileuploaderlog, #modalsubmitlog').remove();
            }
            else
            {
                return false;
            }
        }

        // Selects modal cancel buttons in any modals with an id containing add or new, resets the contents of the modal
        $('div.modal[id*="add"], div.modal[id*="new"]', target).on('shown', function() {
            $(this).parents('div.modal').find(':input').val('');
            $(this).parents('div.modal').find('.nodisplay').hide();
            $(this).parents('div.modal').find('div.error').removeClass('error');
            $(this).parents('div.modal').find('div.help-block').remove();
        });

    }





    /**
     * Initialises functionality on modal submit buttons. Like search suggests accepts a global parameter in the form of submit settings, which are set up in the same js as the search suggest settings.
     * @method modalSubmitInitialisation
     * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
     * @param {object} submitSetting Required global variable from screen specific searchconfig.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a submit-settings data attribute to it containing the key of the set that is required. See examples below.
     * @version
     * @author Tom Yeldham
     */


    //MODAL SUBMIT

    function modalSubmitInitialisation(target) {

        // Make non booking modal modals reset themselves when closed
        $('div.modal:not("div.bookingmodal, .bootstrap-wysihtml5-insert-link-modal")', target).on('hidden', function() {
            $(this).find(':input').val("");
            $(this).find('.error').removeClass('error');
            $(this).find('div.help-block').remove();
            $(this).find(':input:checked').attr('checked', false);
            $(this).find("select").val([]);
            $(this).find('ul.qq-upload-list li').remove();
            $(this).find('div.alert-error').remove();
        });

        $('div.modal-footer button[data-submit-settings]', target).on('click', function(event) {
            event.preventDefault();
            var submitsettings = $(this).data('submit-settings');
            var submitconfig = submitSetting[submitsettings];
            if ($(this).parents('div.modal').find('div.roombox:visible').length > 0) {
                var associatedform = $(this).attr('form');
                if ($('form#' + associatedform + '').valid()) {
                    if ($(this).parents('div.modal').find('div.roomheader.selected').length > 0) {
                        // Submit code to be added by Dhaka
                        submitsettings = $(this).data('submit-settings');
                        submitconfig = submitSetting[submitsettings];
                        modalsubmit(submitconfig);
                    }
                    else {
                        if ($(this).parents('div.modal-footer').find('div.roomerrorbox').length === 0) {
                            $(this).parents('div.modal-footer').append('<div class="alert alert-info roomerrorbox"><strong>Uh oh...</strong>Please price and select at least 1 item</div>');
                        }
                    }
                }
            }
            else {
                if (submitconfig === undefined) {
                    submitconfig = {
                        url: $(this).parents('form').attr('action'),
                        data: $(this).parents('form').serializeArray(),
                        type: $(this).parents('form').attr('method')
                    };
                }
                if ($(this).is('.dontvalidate')) {
                    modalsubmit(submitconfig);
                }
                else {
                    if ($(this).parents('form').length > 0 && $(this).parents('form').valid()) {
                        modalsubmit(submitconfig);
                    }
                    else {
                        var associatedform2 = $(this).parents('div.modal').find('form:visible');
                        var invalid = 0;
                        $(associatedform2).each(function() {
                            if ($(this).valid() === false) {
                                invalid++;
                            }
                        });
                        if (invalid === 0) {
                            modalsubmit(submitconfig);
                        }
                    }
                }
            }
        });

    }

    modalHeightAdjustmentInitialisation(target);
    modalCloseInitialisation(target);
    modalSubmitInitialisation(target);
}


/* $id$ */
/**
 * @class Multi-select
 */

/**
 * Initialises multiselect widget on any inputs with class of con-multiselect
 * @method multiselectInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function multiselectInitialisation(target) {

    // Initializes multiselects
    $('select.con-multiselect:not("[data-limit]")', target).multiselect({
        selectedList: 20,
        selectedText: "# of # selected",
        noneselectedtext: "Select as many as required",
        beforeopen: function() {
            if ($('div.modal:visible').length > 0) {
                var multiselectpopup = $('div.ui-multiselect-menu').has('ul.ui-multiselect-checkboxes li label input[name*="' + $(this).attr('id') + '"]');
                multiselectpopup.css('z-index', '1060');
            }
        },
        position: {
            my: 'left bottom',
            at: 'left top',
            collision: "flip flip"
        }
    }).multiselectfilter();

    $('select.con-multiselect[data-limit], select.combinablemultiselect', target).multiselect({
        selectedList: 20,
        selectedText: "# of # selected",
        noneselectedtext: "Select as many as required",
        uncheckAllText: "",
        checkAllText: "",
        beforeopen: function() {
            if ($('div.modal:visible').length > 0) {
                var multiselectpopup = $('div.ui-multiselect-menu').has('ul.ui-multiselect-checkboxes li label input[name*="' + $(this).attr('id') + '"]');
                multiselectpopup.css('z-index', '1060');
            }
        },
        position: {
            my: 'left bottom',
            at: 'left top',
            collision: "flip flip"
        }
    }).multiselectfilter();

    $('select.con-multiselect[data-limit]', target).on('change', function(event, ui) {
        var thisid = $(this).attr('id');
        var replacementoptions = $('ul.ui-multiselect-checkboxes li label input[name*="' + thisid + '"]');
        var currentlyselected = replacementoptions.filter('[aria-selected="true"]');
        var currentlyselectedno = currentlyselected.length;
        var maxselectable = $(this).data('limit');
        if (maxselectable !== "") {
            if (currentlyselectedno >= maxselectable) {
                replacementoptions.not(currentlyselected).attr('disabled', 'disabled');
            }
            else {
                replacementoptions.removeAttr('disabled');
            }
        }
    });

}


/* $id$ */
/**
 * @class Navigation
 */

/**
 * Initialises keyboard navigation for all fields and pageheaders
 * @method navInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */

function navInitialisation(target) {

    // Keyboard navigation controls from fields
    $(':input', target).on("keydown", function(event) {

        if ($('div.datepicker:visible').length < 1) {
            var offsetheight = -($("div.subnav").height() + 40);

            if (event.ctrlKey && event.which === 38) {
                $.scrollTo($(this).parents('section').find('div.page-header'), 1200, {
                    offset: offsetheight
                });
                $(this).parents('section').find('div.page-header').focus();
            }
            else if (event.ctrlKey && event.which === 40) {
                if ($(this).parents('section').next('section').length > 0) {
                    $.scrollTo($(this).parents('section').next('section').find('div.page-header'), 1200, {
                        offset: offsetheight
                    });
                    $(this).parents('section').next('section').find('div.page-header').focus();
                }
                else if ($(this).parents('form#mainform').next('section').length > 0) {
                    $.scrollTo($(this).parents('form#mainform').next('section').find('div.page-header'), 1200, {
                        offset: offsetheight
                    });
                    $(this).parents('form#mainform').next('section').find('div.page-header').focus();
                }
            }
        }
    });

    // Keyboard navigation controls on section bars
    $('div.page-header', target).on("keydown", function(event) {

        var offsetheight = -($("div.subnav").height() + 42);

        if (event.ctrlKey && event.which === 40) {
            if ($(this).parents('section').next('section').length > 0) {
                $.scrollTo($(this).parents('section').next('section').find('div.page-header'), 1200, {
                    offset: offsetheight
                });
                $(this).parents('section').next('section').find('div.page-header').focus();
            }
            else if ($(this).parents('form#mainform').next('section').length > 0) {
                $.scrollTo($(this).parents('form#mainform').next('section').find('div.page-header'), 1200, {
                    offset: offsetheight
                });
                $(this).parents('form#mainform').next('section').find('div.page-header').focus();
            }
        }
        if (event.ctrlKey && event.which === 37) {
            if ($(this).siblings().is(':visible')) {
                $(this).siblings().css("display", "none");
                $(this).find('i.icon-chevron-right, i.icon-chevron-down').toggleClass("icon-chevron-right").toggleClass("icon-chevron-down");
                adjustscroll();
            }
        }
        if (event.ctrlKey && event.which === 39) {
            var that = $(this).siblings();
            if (that.is(':hidden')) {
                that.css("display", "inline-block");
                $(this).find('i.icon-chevron-right, i.icon-chevron-down').toggleClass("icon-chevron-right").toggleClass("icon-chevron-down");
                that.find(":input:visible:enabled:first").focus();
                adjustscroll();
            }
        }
        if (event.ctrlKey && event.which === 38) {
            if ($(this).parents('section').prev('section').length > 0) {
                $.scrollTo($(this).parents('section').prev('section').find('div.page-header'), 1200, {
                    offset: offsetheight
                });
                $(this).parents('section').prev('section').find('div.page-header').focus();
            }
            else if ($(this).parents('section').prev('form#mainform').length > 0) {
                $.scrollTo($(this).parents('section').prev('form#mainform').find('section').last().find('div.page-header'), 1200, {
                    offset: offsetheight
                });
                $(this).parents('section').prev('form#mainform').find('section').last().find('div.page-header').focus();
            }
        }
    });

}


/* $id$ */
/**
 * @class Old Tables
 */

/**
 * Used to initialise visual functionality to in row edit functionality for old inline edittable tables
 * @method oldInroweditTableInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @param {object} submitSetting Required global variale, set up in the screen specific config js. Paired to the row submits by use of data-submit settings attribute.
 * @version
 * @author Tom Yeldham
 * @return false On all buttons to prevent form submission by default
 * @deprecated
 */

// OLD INLINE EDIT TABLES

//No submit code at all, only visual currently

function oldInroweditTableInitialisation(target) {
    
    $('table tr button[data-submit-settings]', target).on('click', function(){
		var href = location.href.split('/');
		// Prevent to submit the form
		$(this).parents('form').on('submit', false);

		if ($(this).parents('tr').find(':input').valid()) {
			var submitsettings = $(this).data('submit-settings');
			var tableformid = $(this).parents('form').attr('id');
			var submitconfig = submitSetting[submitsettings];
			if (submitconfig === undefined) submitconfig = {};
			var submitaction = $(this).data('submit-action');            

			if (submitaction !== undefined) {
				// XXX: Shall we use <base href...> from the current page?
				if (submitconfig.url === undefined) submitconfig.url = href[0] + '//' + href[2] + $(this).parents('form').attr('action') + '/' + submitaction;
			
				submitconfig.data = $(this).parents('tr').find(':input').serializeArray();				
				submitconfig.type = 'POST';
				submitconfig.formid = tableformid; 
			}
			
			inroweditsubmit(submitconfig);
		}
	});

    // Used for in row editting table row edit buttons. Shoes shadowbox, floats row about it, enables fields, focuses the first one and toggles the edittable buttons in
    $('.edittable', target).on("click", function() {
        $(this).parents('tr').addClass('float');
        $(this).parents('tr').find(':input').attr('disabled', false);
        $(this).parents('tr').find(" :input:visible:first").focus();
        $(this).parents('tr').find("button").not('.hideuntiltablecollapse').toggle();
        $('button.edittable, .include, button.addbutton').attr('disabled', true);
        sortoutaddons($(this).parents('tr'));
        return false;
    });

    // Used for in row editting table row confirm and cancel buttons. Hides shadowbox, removes row's z-index, locks fields, removes all validation errors and toggles the edittable buttons out
    $('.locktable', target).on("click", function() {
        $(this).parents('tr').removeClass('float');
        $(this).parents('tr').find('.error').removeClass('error');
        $(this).parents('tr').find('div.help-block').remove();
        $(this).parents('tr').find("button").not('.hideuntiltablecollapse').toggle();
        $(this).parents('tr').find(':input').attr('disabled', true);
        $('button.edittable, .include, button.addbutton').attr('disabled', false);
        sortoutaddons($(this).parents('tr'));
        return false;
    });


    // Reveals new row in in row edittable tables. Does the same as edittable buttons
    $('.addnewrow', target).on("click", function() {
        $(this).parents('table').find('tr:hidden:last').addClass('float').show();
        $(this).parents('table').find('tr:hidden:last').find(" :input:visible:first").focus();
        $('button.edittable, .include, button.addbutton').attr('disabled', true);
        return false;
    });

    // Hides new row in in row edittable tables. Does the same as viewtable buttons
    $('.hidenewrow', target).on("click", function() {
        $(this).parents('tr').find('.error').removeClass('error');
        $(this).parents('tr').find('div.help-block').remove();
        $(this).parents('tr').removeClass('float').hide();
        $(this).parents('tr').find(" :input").val('');
        $('button.edittable, .include, button.addbutton').attr('disabled', false);
        return false;
    });

}


/* $id$ */
/**
 * @class Page Init
 */

/**
 * Standard page element functionality initialisation
 * @method pageInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */

// PAGE INIT

function pageInitialisation(target) {
	
    $('div.modal', target).on('loaded', function() {
        setvalidator();
        metaboot($(this).find('.modal-body'));
        editmode($(this).find('.modal-body'));
        addvalidation($(this).find('.modal-body'));
        console.log('just ran the boot on the new content');
    });


    // Sets arrow on section bars correctly depending on if they are hidden or not on load
    $('div.page-header').each(function() {
        if ($(this).siblings('div.row:visible').length < 1) {
            $(this).find('i.icon-chevron-down').toggleClass("icon-chevron-right").toggleClass("icon-chevron-down");
        }
    });


    // Initialise scrollspy
    $('body').scrollspy({
        target: '#subnavscroll',
        offset: 50
    });

    $('#optionstabs', target).tab();


    // Uses the position attribute of the subnav scrollspy bar to determine if screen is being displayed on a tablet sized screen. If not then pad form so that the fixed headerbar doesnt over lap the contents
    if ($('div.subnav', target).css('position') === 'fixed') {

        $('form#mainform').css('padding-top', ($("div.subnav").height()) + "px");
    } else {

        $('form#mainform').css('padding-top', "30px");
    }

    var scrolloffset = 80;
    // Makes the scrollspy tabs scroll the page to the correct location
    $('#subnavscroll li a', target).on('click', function(event) {
        var href = $(this).attr('href');
        if ($('div.subnav').css('position') === 'fixed') {
            event.preventDefault();
            window.location.hash = href;
            scrollBy(0, -scrolloffset);
        } else {
            event.preventDefault();
            window.location.hash = href;
            scrollBy(0, -10);
        }
    });

}


/* $id$ */
/**
 * @class Rich Text Editors
 */

/**
 * Initialises all rte textareas on the page
 * @method rteInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function rteInitialisation(target) {

    $('textarea.rte', target).each(function() {
        var parentelement = $(this).parent();
        $(this).wysihtml5({
            "image": false,
            "stylesheets": false,
            "events": {
                "load": function() {
                	if($('#copybutton').get(0)){
                    	disablerte(parentelement);
                    }
                }
            }
        });
    });

    $('.bootstrap-wysihtml5-insert-link-modal').on('shown', function(e) {
        e.stopPropagation();
    }).on('hidden', function(e) {
        e.stopPropagation();
    });

    $('.disablerte', target).on('click', function() {
        var targettexteditorcontainer = $(this).parents('div.modal');
        disablerte(targettexteditorcontainer);
    });

    $('.enablerte', target).on('click', function() {
        var targettexteditorcontainer = $(this).parents('div.modal');
        enablerte(targettexteditorcontainer);
    });

}

function loadintorte(targetrte, datatoload) {
    targetrte.data('wysihtml5').editor.setValue(datatoload);
}



/* $id$ */
/**
 * @class Search Suggests
 */

/**
 *  Used to initialise searchsuggest fields
 *  @method searchSuggestInitialisation
 *  @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 *  @param {object} searchSuggestSetting Required global variable from screen specific searchconfig.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a search-settings data attribute to it containing the key of the set that is required. See examples below.
 *  @version
 *  @author Tom Yeldham
 *  @return false For multi search suggests
 *  
 *  @example <input type="text" autocomplete="off" placeholder="Search" class="typeahead search-query input-large" data-search-settings="navbarsearch">
 * 
 *  The header navbar has the above mark up 
 * 
 *  The data-search-settings attribute value is what is used to pair this particular field to the desired set of parameters in searchconfig.js. We can see it has a value of navbarsearch, which corresponds to
 *
 *  @example  
 *    "navbarsearch": {
 *        // Set the url to your service and the searchid to that of the type service you require from that service
 *        url: "http://maf.dev.m4.net/searchSuggest-1.0/newSearchSuggestServlet",
 *        dataType: "jsonp",
 *        dataparams: [{
 *            key:"id", value:"PROPERTY1"
 *        }],
 *        success: function( responseData, resultsList) {              
 *            for(i=0;i<responseData.length;i++){
 *                record = {};
 *                record["key"] = responseData[i].key;
 *                if($.isArray(responseData[i].value)){
 *                    record["value"] = [];
 *                    for(j=0;
 *                        j<responseData[i].value.length;
 *                        j++){
 *                        record["value"][j] = responseData[i].value[j];
 *                    }
 *                }
 *                resultsList.push(record)
 *            }
 *        },
 renderItem: function (ul, data) {
 *            return $("<li class='columnautocomplete'></li>")
 *            .data("item.autocomplete", data)
 *            //Format the below line to contain the desired amount of columns and ensure that you are selecting the desired array value for each column
 *            .append("<a><div class='col'>" + data.value[0] + "</div><div class='col'>" + data.value[1] + "</div></a>")
 *            .appendTo(ul);
 *        }             
 *    },  
 *
 *  url, dataparans and dataType are all compulsary. dataparams must be an array of key value pairs formatted as in the example. beforesend, complete, success, error and select are all optional if you wish to alter the default behaviour of the plugin which is configured to work with the service I have been testing against, by using a different function.
 *  If the searchsuggest is required to display tabular data, then you must add a renderItem parameter in your config for that key. It obviously has to fit with the format of resultsList array that you are providing to the response callback.
 *  They should be configured to conform as the jQuery ajax() settings of the same name. See the following link for full documentation http://api.jquery.com/jQuery.ajax/.
 *
 *  If additional parameters are required it is possible to completely override the template and provide a unique complete autocomplete initilaise for that searchsuggest by giving a search config key a parameter of uniqueinitialise. This should be a full initialise function and obviously shouldnt include any other parameters.
 *
 *  An optional 'heading' object can be sent in the JSON. This can be picked up in the success function and formatted in the renderItem function. See above.
 *   
 *  Example JSON:
 *  <code> { headings: ['Place','City'], values: [{"value":["Africa EMB","Cape Town"],"key":"AFRICAEMB"},{"value":["Arthurs Seat","Cape Town"],"key":"ARS"}] }; </code>
 */

//SEARCHES

function searchSuggestInitialisation(target) {

    /*
     * Need to make it workable it for all kind of searches
     * without distroubing the current searches
     * 
    $(".typeahead", target).each(function() {
        var settingid = $(this).data('search-settings');
        var uniqueinitialise = searchSuggestSetting[settingid].uniqueinitialise;
        var searchurl = searchSuggestSetting[settingid].url;
        var searchdataparams = searchSuggestSetting[settingid].dataparams;
        var searchdatatype = searchSuggestSetting[settingid].dataType;
        var searchsuccess = searchSuggestSetting[settingid].success;
        var searcherror = searchSuggestSetting[settingid].error;
        var searchselect = searchSuggestSetting[settingid].select;
        var renderItem = searchSuggestSetting[settingid].renderItem;
        var beforesend = searchSuggestSetting[settingid].beforesend;
        var complete = searchSuggestSetting[settingid].complete;
        if (uniqueinitialise !== undefined) {
            $(this).uniqueinitialise();
        }
        else {
            if (beforesend === undefined) {
                beforesend = function() {
                };
            }
            if (complete === undefined) {
                complete = function() {
                };
            }
            if (searchdatatype === undefined) {
                searchdatatype = "json";
            }
            $(this).autocomplete({
                minLength: 1,
                delay: 150,
                autoFocus: true,
                messages: {
                    noResults: '',
                    results: function() {
                    }
                },
                position: {
                    my: "left top",
                    at: "left bottom",
                    collision: "flip"},
                source:
                        function(request, response) {

                            var data = {
                                search: request.term
                            };
                            // Make global var parameter an array of key value pairs [{a, 1},{b. 2}]
                            for (var searchparamcounter = 0; searchparamcounter < searchdataparams.length; searchparamcounter++) {
                                var param = searchdataparams[searchparamcounter];
                                var key = param.key;
                                var val = param.value;
                                data[key] = val;
                            }

                            $.ajax({
                                complete: complete(),
                                beforeSend: beforesend(),
                                async: true,
                                url: searchurl,
                                dataType: searchdatatype,
                                data: data,
                                success:
                                        function(responseData) {
                                            var resultsList = [];
                                            if (searchsuccess === undefined) {
                                                for (var i = 0; i < responseData.length; i++) {
                                                    var record = {};
                                                    record.key = responseData[i].key;
                                                    record.value = "";
                                                    if ($.isArray(responseData[i].value)) {
                                                        for (var j = 0; j < responseData[i].value.length; j++) {
                                                            record.value += responseData[i].value[j];
                                                            if (j < (responseData[i].value.length - 1))
                                                                record.value += ", ";
                                                        }
                                                    } else {
                                                        record.value = responseData[i].value;
                                                    }
                                                    resultsList.push(record);
                                                }
                                            }
                                            else {
                                                searchsuccess(responseData, resultsList);
                                            }
                                            response(resultsList);
                                        },
                                error: function(data) {
                                    if (searcherror === undefined) {
                                        alert("We're sorry, something went wrong.");
                                    }
                                    else {
                                        searcherror(data);
                                    }
                                }
                            });
                        },
                select: function(event, ui) {
                    if (searchselect === undefined) {
                        if ($(event.currentTarget).find('li:has("a.ui-state-focus")').hasClass('addnew')) {
                            var targetmodal = $(event.currentTarget).find('li.addnew div').attr('data-target');
                            $(targetmodal).modal('show');
                        }
                        else {
                            $(this).siblings(".typeahead-val").val(ui.item.key);
                            $(this).parent().siblings(".typeahead-val").val(ui.item.key);
                            if ($(this).parent().is("form"))
                                $(this).parents("form").submit();
                        }
                    }
                    else {
                        searchselect(event, ui);
                    }
                }
            });
            if (renderItem !== undefined) {
                $(this).data("autocomplete")._renderItem = renderItem;
            }
        }
    });
    */
    
    $(".typeahead", target).autocomplete({
        minLength: 1,
        delay: 150,
        autoFocus: true,
        messages: {
            noResults: function(){
                 $(this).siblings(".no-search-results").html(" No search results");
            },
            results: function(amount){
                console.log("total records: " + amount);
                $(".no-search-results").html("");
            }
        },
        source: function( request, response ) {
            var searchUrl   = this.element.data('search-url');
            var id          = this.element.data('search-id');
            var settings    = this.element.data('search-settings');
            var elementId   = $(this).attr('id');

            var search = {};
            console.log("search term: " + this.term);
            if (this.term == "?") {
                this.term = encodeURIComponent(this.term);
            }
            search.search = this.term;
            search.id = id;
            search.callback = elementId;

            var data = [];

            searchSuggest = $.getJSON(searchUrl + this.term +".json",
                function(responseData){
                    var search_config = searchSuggestSetting[settings];
                    if (typeof search_config != 'undefined' && search_config.table == "Y"){
                        for(i=0;i<responseData.length;i++){
                            record = {};
                            record.key = responseData[i].key;                           
                            if($.isArray(responseData[i].value)){
                                record.value = "";
                                for(j=0;j<responseData[i].value.length;j++){
                                    record.value += "<span class='faketd'>"+responseData[i].value[j]+"</span>";
                                }
                                record.value += "";
                            }
                        }
                        data.push(record);
                    }
                    else{                       
                        for(var i=0;i<responseData.length;i++){
                            var record = {};
                            record.key = responseData[i].key;
                            record.value = "";
                            if($.isArray(responseData[i].value)){
                                for(j=0;j<responseData[i].value.length;j++){
                                    record.value += responseData[i].value[j];
                                    if(j < (responseData[i].value.length -1)) {
                                        record.value += search_config.element;
                                    }
                                }
                            }
                            else{
                                record.value = responseData[i].value;
                            }
                            data.push(record);
                        }
                    }
                    response(data);
                }); 
        },
        select: function( event, ui ) {
            $(this).siblings(".typeahead-val").val(ui.item.key);
            $(this).parent().siblings(".typeahead-val").val(ui.item.key);
            // TODO: Not all search-suggest ends up with immediate form submission!
            if($(this).parent().is("form")) {
                $(this).parents("form").attr("action", $(this).parents("form").attr("action") + ui.item.key); 
                $(this).parents("form").submit();
            }
        //return false;
        }
    });

}


/* $id$ */
/**
 * @class Tables
 */

/**
 * Generic table initialisation set up
 * @method tableInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function tableInitialisation(target) {

    // Sets table action column to be 60px across if buttons are present in it
    $('tr td:last-child button.btn:visible', target).each(function() {
        if ($(this).parents('table').attr('id') === "routecoststoggletable") {
            if ($(this).parents('table').find('tr th:last-child input.typeahead').length > 0) {
                $(this).parents('table').find('tr th:last-child input.typeahead').css("margin-right", "-60px");
            }
        }
        else {
            if ($(this).siblings('button.btn:visible').length > 0) {
                $(this).parents('table').find('th:last-child').css('width', '60px');
            }
            else {
                if ($(this).parents('table').is('.gridtable')) {
                    $(this).parents('table').find('th:last-child').css('width', '60px');
                }
                else {
                    if ($(this).parents('table').is('#accomtable')) {
                        $(this).parents('table').find('th:last-child').css('width', '100px');
                    }
                    else {
                        $(this).parents('table').find('th:last-child').css('width', '30px');
                    }
                }
            }
        }
    });
    $('.tablerowtoggles', target).on('change', function() {
        var table = $(this).parents('div.control-group').data('tabletotoggle');
        var target = $(this).data('toggle');
        $(this).parents('section').find('table#' + table + ' tr[data-toggle="' + target + '"]').toggle();
    });


    // Set up for responsive tables
    $('table:has(thead)', target).not('#itinery').each(function() {
        var thcount = $(this).find('th').length;
        for (var i = 1; i < thcount + 1; i++) {
            var thisid = $(this).attr("id");
            var headerdata = $("#" + thisid + " thead th:nth-child(" + i + ")").text();
            $("#" + thisid + " tbody tr td:nth-child(" + i + ")").attr('data-headerdetails', headerdata);
        }
    });

    //  For droppable tables, if all actual rows are deleted, then produce a placeholder drop location row.
    $("button.deleterow", target).on("click", function() {
        var thisid = $(this).parents("table").attr("id");
        var firetherest = "N";
        if ($(this).parents("table").is('.inrowedittable')) {
            firetherest = "Y";
        }
        $(this).parents('tr').remove();
        $('#backgroundPopup2').hide();
        if (firetherest === "Y") {
            var i = $("#" + thisid + " tbody tr:visible").length;
            if (i === 1) {
                $("table.inrowedittable tbody button.deleterow").hide();
            }
            reidrowinputs($('#' + thisid + '').parents('form'));
            $('#' + thisid + '').trigger('cleanandvalidate');
        }
        adjustscroll();
    });

}

/* Set the defaults for DataTables initialisation */
$.extend(true, $.fn.dataTable.defaults, {
    "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
    "sPaginationType": "bootstrap",
    "oLanguage": {
        "sLengthMenu": "_MENU_ records per page",
        "sSearch": "Filter records:"
    }
});


/* Default class modification */
$.extend($.fn.dataTableExt.oStdClasses, {
    "sWrapper": "dataTables_wrapper form-inline"
});

$.extend($.fn.dataTableExt.oSort, {
    "title-string-pre": function(a) {
        return a.match(/title="(.*?)"/)[1].toLowerCase();
    },
    "title-string-asc": function(a, b) {
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },
    "title-string-desc": function(a, b) {
        return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
});

jQuery.extend(jQuery.fn.dataTableExt.oSort, {
    "title-numeric-pre": function(a) {
        var x = a.match(/data-sort-value="*(-?[0-9\.]+)/)[1];
        return parseFloat(x);
    },
    "title-numeric-asc": function(a, b) {
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },
    "title-numeric-desc": function(a, b) {
        return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
});

$.extend($.fn.dataTableExt.oSort, {
    "date-uk-pre": function(a) {
        var b = $(a).text();
        var ukDatea = b.split('-');
        return (ukDatea[2] + ukDatea[1] + ukDatea[0]) * 1;
    },
    "date-uk-asc": function(a, b) {
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },
    "date-uk-desc": function(a, b) {
        return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
});

/* Create an array with the values of all the input boxes in a column */
$.fn.dataTableExt.afnSortData['dom-text'] = function(oSettings, iColumn)
{
    var aData = [];
    $('td:eq(' + iColumn + ')', oSettings.oApi._fnGetTrNodes(oSettings)).each(function() {
        aData.push($(this).text());
    });
    return aData;
};

/* API method to get paging information */
$.fn.dataTableExt.oApi.fnPagingInfo = function(oSettings)
{
    return {
        "iStart": oSettings._iDisplayStart,
        "iEnd": oSettings.fnDisplayEnd(),
        "iLength": oSettings._iDisplayLength,
        "iTotal": oSettings.fnRecordsTotal(),
        "iFilteredTotal": oSettings.fnRecordsDisplay(),
        "iPage": oSettings._iDisplayLength === -1 ?
                0 : Math.ceil(oSettings._iDisplayStart / oSettings._iDisplayLength),
        "iTotalPages": oSettings._iDisplayLength === -1 ?
                0 : Math.ceil(oSettings.fnRecordsDisplay() / oSettings._iDisplayLength)
    };
};


/* Bootstrap style pagination control */
$.extend($.fn.dataTableExt.oPagination, {
    "bootstrap": {
        "fnInit": function(oSettings, nPaging, fnDraw) {
            var oLang = oSettings.oLanguage.oPaginate;
            var fnClickHandler = function(e) {
                e.preventDefault();
                if (oSettings.oApi._fnPageChange(oSettings, e.data.action)) {
                    fnDraw(oSettings);
                }
            };

            $(nPaging).addClass('pagination').append(
                    '<ul>' +
                    '<li class="prev disabled"><a href="#">&larr; ' + oLang.sPrevious + '</a></li>' +
                    '<li class="next disabled"><a href="#">' + oLang.sNext + ' &rarr; </a></li>' +
                    '</ul>'
                    );
            var els = $('a', nPaging);
            $(els[0]).bind('click.DT', {
                action: "previous"
            }, fnClickHandler);
            $(els[1]).bind('click.DT', {
                action: "next"
            }, fnClickHandler);
        },
        "fnUpdate": function(oSettings, fnDraw) {
            var iListLength = 5;
            var oPaging = oSettings.oInstance.fnPagingInfo();
            var an = oSettings.aanFeatures.p;
            var i, ien, j, sClass, iStart, iEnd, iHalf = Math.floor(iListLength / 2);

            if (oPaging.iTotalPages < iListLength) {
                iStart = 1;
                iEnd = oPaging.iTotalPages;
            }
            else if (oPaging.iPage <= iHalf) {
                iStart = 1;
                iEnd = iListLength;
            } else if (oPaging.iPage >= (oPaging.iTotalPages - iHalf)) {
                iStart = oPaging.iTotalPages - iListLength + 1;
                iEnd = oPaging.iTotalPages;
            } else {
                iStart = oPaging.iPage - iHalf + 1;
                iEnd = iStart + iListLength - 1;
            }

            for (i = 0, ien = an.length; i < ien; i++) {
                // Remove the middle elements
                $('li:gt(0)', an[i]).filter(':not(:last)').remove();

                // Add the new list items and their event handlers
                for (j = iStart; j <= iEnd; j++) {
                    sClass = (j == oPaging.iPage + 1) ? 'class="active"' : '';
                    $('<li ' + sClass + '><a href="#">' + j + '</a></li>')
                            .insertBefore($('li:last', an[i])[0])
                            .bind('click', function(e) {
                        e.preventDefault();
                        oSettings._iDisplayStart = (parseInt($('a', this).text(), 10) - 1) * oPaging.iLength;
                        fnDraw(oSettings);
                    });
                }

                // Add / remove disabled classes from the static elements
                if (oPaging.iPage === 0) {
                    $('li:first', an[i]).addClass('disabled');
                } else {
                    $('li:first', an[i]).removeClass('disabled');
                }

                if (oPaging.iPage === oPaging.iTotalPages - 1 || oPaging.iTotalPages === 0) {
                    $('li:last', an[i]).addClass('disabled');
                } else {
                    $('li:last', an[i]).removeClass('disabled');
                }
            }
        }
    }
});


/*
 * TableTools Bootstrap compatibility
 * Required TableTools 2.1+
 */
if ($.fn.DataTable.TableTools) {
    // Set the classes that TableTools uses to something suitable for Bootstrap
    $.extend(true, $.fn.DataTable.TableTools.classes, {
        "container": "DTTT btn-group",
        "buttons": {
            "normal": "btn include",
            "disabled": "disabled include"
        },
        "collection": {
            "container": "DTTT_dropdown dropdown-menu include",
            "buttons": {
                "normal": "include",
                "disabled": "disabled include"
            }
        },
        "print": {
            "info": "DTTT_print_info modal"
        },
        "select": {
            "row": "active"
        }
    });

    // Have the collection use a bootstrap compatible dropdown
    $.extend(true, $.fn.DataTable.TableTools.DEFAULTS.oTags, {
        "collection": {
            "container": "ul",
            "button": "li",
            "liner": "a"
        }
    });
}


/* Table initialisation */
$(document).ready(function() {
    $('table.datatabled').not('.con-draggable').each(function() {
        var defaultsortcol = $(this).find('th[data-defaultsort]');
        var updown = defaultsortcol.data("defaultsort");
        var defaultcolumnlocation = $($(this).find('th')).index(defaultsortcol);
        if (defaultsortcol.length < 1) {
            defaultcolumnlocation = 1;
            updown = "asc";
        }
        var columncount = $(this).find('th').length;
        var finalcolumncells = $(this).find('tbody tr td:nth-child(' + columncount + ')');
        var cellarray = [];
        var i = 0;
        if ($(this).find('tbody td :input:disabled').not('button').length > 0) {
            for (i = 0; i < columncount; i++) {
                cellarray.push({
                    "sSortDataType": "dom-text"
                });
            }
        }
        else {
            for (i = 0; i < columncount; i++) {
                cellarray.push(null);
            }
        }
        if ($(this).find('th[data-datesort="Y"]').length > 0) {
            var datecolumn = $(this).find('th[data-datesort="Y"]');
            var datecolumnindex = $($(this).find('th')).index(datecolumn);
            cellarray[datecolumnindex] = {
                "sType": "date-uk"
            };
        }
        if ($(this).find('th[data-datasort="Y"]').length > 0) {
            var selectedheader = $(this).find('th[data-datasort="Y"]');
            $.each(selectedheader, function() {
                var selectedheaderindex = $($(this).parents('table').find('th')).index($(this));
                cellarray[selectedheaderindex] = {
                    "sType": "title-numeric"
                };
            });
        }
        if ($(this).is('#emailstable')) {
            cellarray[0] = {
                "bSortable": false
            };
        }
        if (finalcolumncells.find('button.btn').length > 0) {
            cellarray[columncount - 1] = {
                "bSortable": false
            };
        }
        if ($(this).is('#servicestable')) {
            $(this).dataTable({
                "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                "sPaginationType": "bootstrap",
                "oLanguage": {
                    "sLengthMenu": "_MENU_ services per page",
                    "sSearch": "Filter returned services:"
                },
                "aaSorting": [[defaultcolumnlocation, updown]],
                "aoColumns": cellarray
            });
        }
        else {
            if ($(this).find('tbody tr:visible').length > 10) {
                $(this).dataTable({
                    "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records per page"
                    },
                    "aaSorting": [[defaultcolumnlocation, updown]],
                    "aoColumns": cellarray
                });
            }

            else {
                $(this).dataTable({
                    "sDom": "<'row'<'span6'l><'span6'f>r>t<'row'<'span6'i><'span6'p>>",
                    "sPaginationType": "bootstrap",
                    "oLanguage": {
                        "sLengthMenu": "_MENU_ records per page"
                    },
                    "aaSorting": [[defaultcolumnlocation, updown]],
                    "aoColumns": cellarray
                });
            }
        }
    });
});


/* $id$ */
/**
 * @class Uploaders
 */

/**
 * Initialises all uploader elements on the page
 * @method uploaderInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */

function uploaderInitialisation(target) {
    $('.fileupload').each(function() {
        $(this).fileupload({
            dropZone: $(this),
            autoUpload: true,
            sequentialUploads: true
        }).bind('fileuploadfailed', function(e, data) {
            bindremoveuploaditem($(this).find('tr.template-download:has("span.label-important")').not('.metabooted'));
            $(this).find('tr.template-download').addClass('metabooted');
        }).bind('fileuploadcompleted', function(e, data) {
            uploaderhiddenfields($(this));
        });
    });

    $(document).bind('drop dragover', function(e) {
        e.preventDefault();
    });

    function bindremoveuploaditem(target) {
        $('.deleteuploaditem', target).on('click', function() {
            var parentform = $(this).parents('form');
            if ($(this).parents('tr.template-download').find('span.label-important').length > 0) {
                $(this).parents('tr.template-download').remove();
                uploaderhiddenfields(parentform);
            }
        });
    }
}

/**
 * Deletes existing hidden fields for the successful uploads, then generates a hidden field for each successful upload for the uploader and sets it's value to the text content of the respective upload.
 * @method uploaderhiddenfields
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */

function uploaderhiddenfields(target) {
    var parentelement = target.parents('section');
    if(target.parents('div.modal')){
        parentelement = target.parents('div.modal');
    }
    var uploaderlogtemplate = parentelement.find('.fileuploaderlogtemplate');
    parentelement.find('.fileuploaderlog').remove();
    var successfuluploads = target.find('tr.template-download').not(':has("span.label-important")');
    var counter = 0;
    $.each(successfuluploads, function() {
        var uploadname = $(this).text();
        var strippeduploadname = uploadname.replace(/\s/g, "");
        uploaderlogtemplate.clone().addClass('newfileuploaderlog').removeClass('fileuploaderlogtemplate').attr('name', 'upload[' + counter + ']').after(uploaderlogtemplate);
        var newuploadlog = parentelement.find('.newfileuploaderlog');
        newuploadlog.val(strippeduploadname).addClass('fileuploaderlog').removeClass('newfileuploaderlog');
        counter++;
    });
}


/* $id$ */
/**
 * @class Utils
 */


/**
 * Binds the click event of any .alertremoval elements to remove the elements parent div.alert. Used for close buttons in dynamically generated alert boxes
 * @method alertremoval
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function alertremoval(target) {
    $('.alertremoval', target).on('click', function() {
        $(this).parents('div.alert').remove();
    });
}

/**
 * Disables any wysiwyg editors and hides their respective toolbars.
 * @method disablerte
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function disablerte(target) {
    $('ul.wysihtml5-toolbar', target).parent().find("iframe").contents().find('body.wysihtml5-editor').attr('contenteditable', 'false');
    $('ul.wysihtml5-toolbar', target).parent().find("iframe").addClass('disabled');
    $('ul.wysihtml5-toolbar', target).hide();
}

/**
 * Enables any wysiwyg editors and shows their respective toolbars.
 * @method enablerte
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */
function enablerte(target) {
    $('ul.wysihtml5-toolbar', target).parent().find("iframe").contents().find('body.wysihtml5-editor').attr('contenteditable', 'true');
    $('ul.wysihtml5-toolbar', target).parent().find("iframe").removeClass('disabled');
    $('ul.wysihtml5-toolbar', target).show();
}

/**
 * Extend jquery load to included a loaded event
 * @method extendqjueryload
 * @version
 */

(function() {
    $.fn.jqueryLoad = $.fn.load;

    $.fn.load = function(url, params, callback) {
        var $this = $(this);
        var cb = $.isFunction(params) ? params : callback || $.noop;
        var wrapped = function(responseText, textStatus, XMLHttpRequest) {
            cb(responseText, textStatus, XMLHttpRequest);
            $this.trigger('loaded');
        };

        if ($.isFunction(params)) {
            params = wrapped;
        } else {
            callback = wrapped;
        }

        $this.jqueryLoad(url, params, callback);

        return this;
    };
})();

/**
 * Takes a string and capitalises the first character.
 * @method capitaliseFirstLetter
 * @param {object} string Required parameter. The string which you wish to capitalise.
 * @version
 * @author Tom Yeldham
 */
function capitaliseFirstLetter(string)
{
    return string.charAt(0).toUpperCase() + string.slice(1);
}


/**
 * Initialises the language settings for both the datepicker plugin and moment.js.
 * @method setdatepickers
 * @param {object} m4language Required global variable from screen specific config.js. Both the datepicker plugin and moment.js have seperate language formats that are loaded for each plugin respectively from the config file.
 * @version
 * @author Tom Yeldham
 */
(function($) {
    $.fn.datepicker.dates.m4 = m4language.datepickers;
    moment.lang('m4', m4language.moment);
}(jQuery));

/**
 * Used to check passenger age for prepopulating booking modal page 1's using the data provided in the passenger table.
 * @method getAge
 * @param {object} dateString Required string. function splits the string at - delimiters and caluclates the age based on the current date.
 * @version
 * @author Tom Yeldham
 */
function getAge(dateString) {

    var dates = dateString.split("-");
    var d = new Date();

    var userday = dates[0];
    var usermonth = dates[1];
    var useryear = dates[2];

    var curday = d.getDate();
    var curmonth = d.getMonth() + 1;
    var curyear = d.getFullYear();

    var age = curyear - useryear;

    if ((curmonth < usermonth) || ((curmonth == usermonth) && curday < userday)) {

        age--;

    }

    return age;
}

/**
 * Basic ajax call that is configured using submitSetting global variable
 *
 * @method modalsubmit
 * @param {object} submitconfig  Required global variable from screen specific config.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a submit-settings data attribute to it containing the key of the set that is required. See examples below.
 * @version
 * @author Tom Yeldham
 *
 *
 * @example
 * 
 *          <button class="btn btn-primary submit" data-submit-settings="modal1submit" form="modal1form" type="submit" aria-hidden="true">Save changes</button>
 *
 * Assume that an modal's submit button has the above mark up
 *
 * The data-submit-settings attribute value is what is used to pair this particular field to the desired set of parameters in config.js. We can see it has a value of table1submit, which corresponds to
 *
 * @example
 * 
 *          "modal1submit": {
 *              // Set the url to your service and the searchid to that of the type service you require from that service
 *              url: "http://maf.dev.m4.net/searchSuggest-1.0/newSearchSuggestServlet",
 *              type: "POST",
 *              data: $(this).parents('div.modal').find('form:visible :input').not('button').serializeArray(),
 *              error: function(){
 *                  alert('Test alternate alert')
 *              }
 *          },
 *
 * url, data, type are all compulsary. beforesend, complete, success, error and select are all optional if you wish to alter the default behaviour of the plugin which is configured to work with the service I have been testing against, by using a different function.
 * They should be configured to conform as the jQuery ajax() settings of the same name. See the following link for full documentation http://api.jquery.com/jQuery.ajax/.
 *
 * If additional parameters are required it is possible to completely override the template and provide a unique ajax function for that table by giving a submitconfig key a parameter of unique. This should be a full ajax request function and obviously shouldnt include any other parameters.
 */
function modalsubmit(submitconfig) {
    //Generic submit function which will accept parameters from a config file like search. To be extended as required by Dhaka.
    var submitdata = submitconfig.data;
    var targeturl = submitconfig.url;
    var submittype = submitconfig.type;
    var beforesend = submitconfig.beforesend;
    var cache = submitconfig.cache;
    var complete = submitconfig.complete;
    var unique = submitconfig.unique;
    var success_provided = submitconfig.success;
    var error = submitconfig.error;
    if (unique !== undefined) {
        unique();
    }
    else {        
        success = function() {
            $('div.modal:visible').modal('hide');            
            if(success_provided) success_provided();            
        };
        
        if (error === undefined) {
            error = function() {

            };
        }
        if (beforesend === undefined) {
            beforesend = function() {
            };
        }

        if (cache === undefined) {
            cache = false;
        }
        if (complete === undefined) {
            complete = function() {
            };
        }
        $.ajax({
            cache: cache,
            complete: complete,
            beforeSend: beforesend,
            type: submittype,
            url: targeturl,
            data: submitdata,
            success: function() {
                if ($('#modalsubmitlog').length > 0) {
                    $('#modalsubmitlog').remove();
                }
                success();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if ($('#modalsubmitlog').length > 0) {
                    $('#modalsubmitlog').remove();
                }
                $('div.modal-body:visible').prepend('<div id="modalsubmitlog" class="alert alert-error"> <strong>The following error has occured: ' + errorThrown + '</strong></div>');
                alertremoval($('#modalsubmitlog'));
                error();
            }
        });
    }
}


/**
 * Basic ajax call that is configured using submitSetting global variable. Has the exact same parameter as the modal submit call, however as it is related to the main page submit the error and success functions have default error message related actions as well.
 *
 * @method mainpagesubmit
 * @param {object} submitconfig  Required global variable from screen specific config.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a submit-settings data attribute to it containing the key of the set that is required. See examples below.
 * @version
 * @author Tom Yeldham
 *
 *
 * @example
 * 
 *          <a class="submit viewmode updatebutton addbutton" id="headerconfirmbutton" data-submit-settings="mainpagesubmit">Confirm</a>
 *
 * The booking screen header confirm button has the above mark up
 *
 * The data-submit-settings attribute value is what is used to pair this particular field to the desired set of parameters in config.js. We can see it has a value of table1submit, which corresponds to
 *
 * @example
 * 
 *   "mainpagesubmit": {
 *       // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
 *       url: deployspecificurlsection + "newSearchSuggestServlet",
 *       type: "POST",
 *       data: $('form#mainform').find(':input').not('button').serializeArray(),
 *       error: function() {
 *           console.log('Test mainform submit error');
 *       },
 *       success: function() {
 *           console.log('Test mainform submit success');
 *       }
 *   },
 *
 * url, data, type are all compulsary. beforesend, complete, success, error and select are all optional if you wish to alter the default behaviour of the plugin which is configured to work with the service I have been testing against, by using a different function.
 * They should be configured to conform as the jQuery ajax() settings of the same name. See the following link for full documentation http://api.jquery.com/jQuery.ajax/.
 *
 * If additional parameters are required it is possible to completely override the template and provide a unique ajax function for that table by giving a submitconfig key a parameter of unique. This should be a full ajax request function and obviously shouldnt include any other parameters.
 */

function mainpagesubmit(submitconfig) {
    //Generic submit function which will accept parameters from a config file like search. To be extended as required by Dhaka.
    var submitdata;
    var targeturl;
    var submittype;
    var beforesend;
    var cache;
    var complete;
    var unique;
    var success;
    var error;
    if (submitconfig !== undefined) {
        submitdata = submitconfig.data;
        targeturl = submitconfig.url;
        submittype = submitconfig.type;
        beforesend = submitconfig.beforesend;
        cache = submitconfig.cache;
        complete = submitconfig.complete;
        unique = submitconfig.unique;
        success = submitconfig.success;
        error = submitconfig.error;
        
    } else {
        submitdata = $('form#mainform').serialize();
        targeturl = $('form#mainform')[0].action;
        submittype = 'POST';
    }
    
    if (unique !== undefined) {
        unique();
    }
    else {
        if (success === undefined) {
            success = function() {
            };
        }
        if (error === undefined) {
            error = function() {
            };
        }
        if (beforesend === undefined) {
            beforesend = function() {
            };
        }

        if (cache === undefined) {
            cache = false;
        }
        if (complete === undefined) {
            complete = function() {
            };
        }
        $.ajax({
            complete: complete,
            beforeSend: beforesend,
            type: submittype,
            url: targeturl,
            data: submitdata,
            success: function(data, textStatus, jqXHR) {            	
            	if ($('#mainformsubmitlog').length > 0) {
                    $('#mainformsubmitlog').remove();
                }
            	
            	if (data.success) {
                    $('div.nav-collapse p.navbar-text').remove();
                    $('div.nav-collapse').prepend('<p class="navbar-text span3">Successfully submitted</p>');
                    $('div.nav-collapse p.navbar-text').effect("pulsate", {times: 1}, 1000);
                    $('div.nav-collapse p.navbar-text').delay(4000).fadeOut('slow');
                    
                    $.postOnSuccess(data, textStatus, jqXHR);
                    success();
            	}
            	else {
            		// Handle errors
            		var errorText = '';
            		for (key in data.errors) {
            			console.log(key + " => " + data.errors[key]);
            			errorText += data.errors[key] + '; ';
            		}
            		
            		$('#mainform').prepend('<div id="mainformsubmitlog" class="alert alert-error"> The following error has occured: <strong>' + errorText + '</strong></div>');
                    alertremoval($('#mainformsubmitlog'));
                    $('div.nav-collapse p.navbar-text').remove();
                    $('div.nav-collapse').prepend('<p class="navbar-text span4 navbar-error"><b>An error occured!</b> Check the log</p>');
                    $('div.nav-collapse p.navbar-text').effect("pulsate", {times: 1}, 1000);
                    error();
            	}
            },
            error: function(jqXHR, textStatus, errorThrown) {
                if ($('#mainformsubmitlog').length > 0) {
                    $('#mainformsubmitlog').remove();
                }
                $('#mainform').prepend('<div id="mainformsubmitlog" class="alert alert-error"> <strong>The following error has occured: ' + errorThrown + '</strong></div>');
                alertremoval($('#mainformsubmitlog'));
                $('div.nav-collapse p.navbar-text').remove();
                $('div.nav-collapse').prepend('<p class="navbar-text span4 navbar-error"><b>An error occured!</b> Check the log</p>');
                $('div.nav-collapse p.navbar-text').effect("pulsate", {times: 1}, 1000);
                error();
            }
        });
    }
}



/**
 * Basic ajax call that is configured using submitSetting global variables
 * @method inroweditsubmit
 * @param {object} submitconfig  Required global variable from screen specific config.js. Contains a series of sets of parameters, each with a unique key. Each input is paired to the desired set of parameters by attaching a submit-settings data attribute to it containing the key of the set that is required. See examples below.
 * @version
 * @author Tom Yeldham
 *
 * @example
 * 
 *      <button id="newedittableconfirmchangesbutton" class="btn btn-mini-icon pull-right inrowedittableconfirmchangesbutton" data-submit-settings="table1submit"><i class="icon-ok"></i></button>
 *
 * Assume that an inrowedittable submit button has the above mark up.
 *
 * The data-submit-settings attribute value is what is used to pair this particular field to the desired set of parameters in config.js. We can see it has a value of table1submit, which corresponds to
 *
 * @example
 * 
 *      "table1submit": {
 *          // Set the url to your service and the searchid to that of the type service you require from that service
 *          url: "http://maf.dev.m4.net/searchSuggest-1.0/newSearchSuggestServlet",
 *          type: "POST",
 *          data: $(this).parents('table').find('tbody :input').not('button').serializeArray(),
 *          error: function(){
 *              alert('Test alternate alert')
 *          }
 *      },
 *
 *
 * url, data, type are all compulsary. beforesend, complete, success, error and select are all optional if you wish to alter the default behaviour of the plugin which is configured to work with the service I have been testing against, by using a different function.
 * They should be configured to conform as the jQuery ajax() settings of the same name. See the following link for full documentation http://api.jquery.com/jQuery.ajax/.
 *
 * If additional parameters are required it is possible to completely override the template and provide a unique for that table by giving a submit config key a parameter of unique. This should be a full ajax request function and obviously shouldnt include any other parameters.
 */

function inroweditsubmit(submitconfig) {
    //Generic submit function which will accept parameters from a config file like search. To be extended as required by Dhaka.
    var submitdata = submitconfig.data;
    var targeturl = submitconfig.url;
    var submittype = submitconfig.type;
    var beforesend = submitconfig.beforesend;
    var cache = submitconfig.cache;
    var complete = submitconfig.complete;
    var unique = submitconfig.unique;
    var success_provided = submitconfig.success;
    var error = submitconfig.error;
    var parentform = submitconfig.formid; 
    var allowsubmit = submitconfig.allowsubmit;    
    if (unique !== undefined) {
        unique();
    }
    else {        
        success = function(returneddata) {
            if (success_provided) success_provided();
        };        
        if (error === undefined) {
            error = function() {
                alert('Sorry, something went wrong while trying to submit your changes');
            };
        }
        if (beforesend === undefined) {
            beforesend = function() {
            };
        }

        if (cache === undefined) {
            cache = false;
        }
        if (complete === undefined) {
            complete = function() {
            };
        }
        
        if (allowsubmit ? allowsubmit() : true) {
            $.ajax({
                cache: cache,
                complete: complete,
                beforeSend: beforesend,
                type: submittype,
                url: targeturl,
                data: submitdata,
                success: function(data, textStatus, jqXHR) {
                	$(".modal-body").parents("div").unbind("*");
                	
                	$.postOnSuccess(data, textStatus, jqXHR, function(){
                		if (data.success) {
	                        success(data);
                		}
                		$('button.edittable, .include, button.addbutton').attr('disabled', false);
						$('p.navbar-text').empty();
                    });
                    return false;
                },
                error: function(jqXHR, textStatus, errorThrown){
                    $.postOnError(jqXHR, textStatus, errorThrown, function(){
                        error();
                    });
                }
            });
        }        
    }
}



/**
 * Initialises keyboard nav for grid tables
 * @method setuptablenav
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @return false
 * @version
 * @author Tom Yeldham
 */
function setuptablenav(target) {
    $('td :input:not("button")', target).on('keydown', function(event) {
        var rowlength = $(this).parents('tr').children('td').length;
        var rowcount = $(this).parents('tbody').find('tr').length;
        var column = $(this).parents('tr').children('td').index($(this).parents('td'));
        var row = $(this).parents('tbody').find('tr').index($(this).parents('tr'));
        var parsedcolumn = parseInt(column);
        var parsedrow = parseInt(row);
        parsedcolumn++;
        parsedrow++;
        var lastinputcell = parseInt(rowlength) - 1;
        //ENTER
        if (event.shiftKey && event.which === 13) {
            event.preventDefault();
            $(this).parents('table').find('button.gridtableconfirmchangesbutton').trigger('click');
        }
        //DELETE
        if (event.shiftKey && event.which === 46) {
            event.preventDefault();
            if ($(this).parents('tr').find('button.deleterow:visible').length > 0) {
                var targetcell;
                if (parsedrow !== rowcount) {
                    targetcell = $(this).parents('tr').nextAll("tr:visible").first().find("td:nth-child(" + parsedcolumn + ") :input");
                }
                else {
                    targetcell = $(this).parents('tr').prevAll("tr:visible").first().find("td:nth-child(" + parsedcolumn + ") :input");
                }
                $(this).parents('tr').find('button.deleterow:visible').trigger('click');
                targetcell.focus();
            }
        }
        //BACKSPACE
        if (event.shiftKey && event.which === 8) {
            event.preventDefault();
            $(this).parents('table').find('button.gridtablecancelchangesbutton').trigger('click');
        }
        //DOWN
        if (event.shiftKey && event.which === 40) {
            event.preventDefault();
            if (parsedrow !== rowcount) {
                $(this).parents('tr').nextAll("tr:visible").first().find("td:nth-child(" + parsedcolumn + ") :input").focus();
            }
            else {
                $(this).parents('table').find('button.gridtablenewbutton').trigger('click');
                $(this).parents('tr').nextAll("tr:visible").first().find("td:nth-child(" + parsedcolumn + ") :input").focus();
            }
            return false;
        }
        //LEFT
        if (event.shiftKey && event.which === 37) {
            event.preventDefault();
            if (parsedcolumn === 1 && row > 0) {
                $(this).parents('tr').prevAll("tr:visible").first().find("td:nth-child(" + lastinputcell + ") :input").focus();
            }
            else {
                $(this).parent('td').prev().children("input").focus();
            }
            return false;
        }
        //RIGHT
        if (event.shiftKey && event.which === 39) {
            event.preventDefault();
            if (lastinputcell === parsedcolumn && parsedrow !== rowcount) {
                $(this).parents('tr').nextAll("tr:visible").first().find("td:first:has(':input') :input").focus();
            }
            else {
                $(this).parent('td').next().children("input").focus();
            }
            return false;
        }
        //UP
        if (event.shiftKey && event.which === 38) {
            event.preventDefault();
            if (row > 0) {
                $(this).parents('tr').prevAll("tr:visible").first().find("td:nth-child(" + parsedcolumn + ") :input").focus();
            }
            return false;
        }
    });
}


function sortoutaddons(target){
    $('span.add-on', target).each(function (){
        if ($(this).prevAll(':input:first').is(":disabled")){
            $(this).addClass("disabledaddon");
        }
        else{
            $(this).removeClass("disabledaddon");
        }
    });
}



/**
 * Enables form elements and shows various other elements within a target area. Also shows viewmode buttons and hides editmode buttons. Called on contents that need to be edittable straight away or to toggle off the effects for viewmode()
 * @method editmode
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @param {string} param2 Optional parameter additionally show elements with a certain class.
 * @return false
 * @version
 * @author Tom Yeldham
 */

function editmode(target, param2) {
    // Hides navbar editmode buttons, shows selected navbar buttons for this mode
    $('li', target).has('.editmode, .hidebuttons').hide();
    $('li', target).has('.' + param2).show();
    $('.available, a.remove-all, .action, .ui-datepicker-trigger, a.help, .hiddentable, button.underlay, .draghandlecontainer, .draghandle', target).show();
    $('button.edittable, button.addbutton, .include, .notesbutton, .ui-datepicker-trigger, button.underlay, li, button.ui-multiselect', target).attr('disabled', false);
    // Shows the add-ons (ie icons on the end of, various fields
    enablerte(target);
    sortoutaddons(target);
    $('.fileupload', target).fileupload('enable');
    return false;
}

/**
 * Disables form elements and hides various other elements within a target area. Also hides viewmode buttons and shows editmode buttons. Usually called on load of a new record or to toggle off the effects for editmode()
 * @method viewmode
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @return false
 * @version
 * @author Tom Yeldham
 */

function viewmode(target) {
    $('.available, a.remove-all, .action, a.help, .hiddentable, button.underlay, .draghandlecontainer, .draghandle', target).hide();
    $('li', target).has('a.editmode, a.hidebuttons').show();
    $('li', target).has('a.viewmode, a.submit').hide();
    $('button.edittable, button.addbutton, .include, .notesbutton, button.underlay, button.ui-multiselect', target).attr('disabled', true);
    $('button.underlay', target).has('div.largeservice').show();
    $('#mainformsubmitlog').remove();
    disablerte(target);
    sortoutaddons(target);
    $('.fileupload', target).fileupload('disable');
    return false;
}


/**
 * Adjusts the scroll for the subnavbar. Called on any function that causes a change in vertical layout
 * @method adjustscroll
 * @version
 * @author Tom Yeldham
 */

// Adjusts the scroll for the subnavbar. Called on any function that causes a change in vertical layout
function adjustscroll() {
    $('[data-spy="scroll"]').each(function() {
        var $spy = $(this).scrollspy('refresh');
    });
}

/**
 * Generic function to make sure that when edittable table rows are added or deleted, the cells have their id's and names updated to ensure that there are no duplicates.
 * @method reidrowinputs
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @version
 * @author Tom Yeldham
 */

function reidrowinputs(target) {
    var tbody = target.find('table.gridtable tbody');
    var tablerows = tbody.find('tr');
    var no = 1;
    $.each(tablerows, function() {
        var rowinputs = $(this).find(':input');
        var copiedrowid = $(this).attr('id');
        var correctrownoid = copiedrowid.replace(/[0-9]/g, '' + no + '');
        $(this).attr('id', correctrownoid);
        var newrownewid = $(this).attr('id');
        $.each(rowinputs, function() {
            var thisid = $(this).attr('id');
            if (thisid) {
                var elementid = thisid.replace(copiedrowid, '');
                $(this).attr('id', '' + newrownewid + elementid + '');
            }
            var thisname = $(this).attr('name');
            if (thisname) {
                var elementname = thisname.replace(copiedrowid, '');
                $(this).attr('name', '' + newrownewid + elementname + '');
            }
        });
        no++;
    });
}

var findIndexOfKey = function(searchKey) {
    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        if (key === searchKey)
            return i;
    }
    return -1;
};


/* $id$ */
/**
 * @class view&editEventHandlers
 */

/**
 * Initialises .viewmode and .editmode buttons in the header. Takes global variables of editcallbacks and viewcallbacks from screen specific editviewmodecallbacks.js
 * @method viewEditModelEventHandlingInitialisation
 * @param {Jquery object} target Required parameter to localise the area of the DOM that the script selectors will initialise over.
 * @param {object} editcallbacks Required global variable found in the screen specific editviewmodecallbacks.js file. Used to tie ajax calls and additional funcitonality into 3 events (editpre, editduring, editdone) fired upon the .editmode button click in the headerbar.
 * @param {object} viewcallbacks Required global variable found in the screen specific editviewmodecallbacks.js file. Used to tie ajax calls and additional funcitonality into 3 events (viewpre, viewduring, viewdone) fired upon the .viewmode button click in the headerbar.
 * @version
 * @author Tom Yeldham
 *
 * @example 
 *      var editcallbacks = {
 *          editpre:  function (that){
 *              console.log('editpre')
 *          },
 *          editduring: function (that){
 *              console.log('editduring')
 *          },
 *          editdone: function (that){
 *              console.log('editdone')
 *          }
 *      }; 
 *      var viewcallbacks = {
 *          viewpre:  function (that){
 *              console.log('viewpre')
 *          },
 *          viewduring: function (that){
 *              console.log('viewduring')
 *          },
 *          viewdone: function (that){
 *              console.log('viewdone')
 *          }
 *      }; 
 */

// VIEW/MODE EVENT HANDLING

function viewEditModeEventHandlingInitialisation(target) {

    $('button.editmode, a.editmode', target).on("click", function(event){
        //genericpreloadstuff();
        $(this).trigger('editpre');
        $(this).trigger('preclickstuffdone');
    });

    $('button.editmode, a.editmode', target).on("preclickstuffdone", function(event){
        //genericduringstuff();
        $(this).trigger('editduring');
        $(this).trigger('duringstuffdone');
    });

    $('button.editmode, a.editmode', target).on("duringstuffdone", function(event){
        var param2 = $(this).attr('id');
        editmode(target, param2);
        $(this).trigger('editdone');
    });

    $('button.viewmode, a.viewmode', target).on("click", function(event) {
        if ($('.gridtableconfirmchangesbutton:visible').length > 0) {
            alert('Please submit or cancel your table changes first');
        }
        else {
            if ($(this).hasClass('submit')) {
                var targetform;
                if ($(this).is('#headerconfirmbutton')) {
                    targetform = $('form#mainform');
                }
                if ($(this).is('[form]')) {
                    targetform = $(this).attr('form');
                }
                if ($(this).data('targetform')) {
                    targetform = $(this).data('targetform');
                }
                if (targetform !== "") {
                    if (targetform.valid()) {
                        //genericpreloadstuff();
                        $(this).trigger('viewsubmitpre');
                        $(this).trigger('preclicksubmitstuffdone');
                    }
                }
            }
            else {
                $(this).trigger('viewcancelpre');
                $(this).trigger('preclickcancelstuffdone');
                $('.error').removeClass('error');
                $('div.help-block').remove();
                viewmode(target);                
            }            
        }
        
    });

    $('button.viewmode, a.viewmode', target).on("preclicksubmitstuffdone", function(event) {
        //genericduringstuff();
        var submitsettings = $(this).data('submit-settings');
        
        var submitconfig = submitSetting[submitsettings];
        mainpagesubmit(submitconfig);
        $(this).trigger('viewsubmitduring');
    });

    $('button.viewmode, a.viewmode', target).on("duringsubmitstuffdone", function(event) {
        viewmode(target);
        $(this).trigger('viewsubmitdone');
    });

    $('button.viewmode, a.viewmode', target).on("preclickcancelstuffdone", function(event) {
        //genericduringstuff();
        $(this).trigger('viewcancelduring');
        $(this).trigger('duringcancelstuffdone');
    });

    $('button.viewmode, a.viewmode', target).on("duringcancelstuffdone", function(event) {
        viewmode(target);
        $(this).trigger('viewcanceldone');
    });

}



function setvalidator(target) {

    function roundNumber(num, dec) {
        var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
        return result;
    }

    // Setting where/how the validation errors are displayed for different types of controls in different formats

    $("#mainform, div.modal form", target).not('div.modal form:has("table.inrowedittable")').each(function() {
        $(this).validate({
            onkeyup: function(element) {
                if (!$(element).is('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate, input.con-timepicker, input.con-dobfield')) {
                    if (!this.checkable(element) && (element.name in this.submitted || !this.optional(element))) {
                        this.element(element);
                    }
                }
            },
            onfocusout: function(element) {
                if (!$(element).is('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate, input.con-timepicker, input.con-dobfield')) {
                    if (!this.checkable(element) && (element.name in this.submitted || !this.optional(element))) {
                        this.element(element);
                    }
                }
            },
            errorElement: "span",
            wrapper: "div", // a wrapper around the error message

            //Error message mark up
            errorPlacement: function(error, element) {
                if ($(element).data('toggledby')) {
                    element.after(error);
                    error.addClass('help-block error');  // add a class to the wrapper
                    error.css('margin', '0');

                }
                else if ($(element).siblings().hasClass('radio')) {
                    element.after(error);
                    error.addClass('help-block error');  // add a class to the wrapper
                    error.css('margin', '0');

                }
                else if ($(element).is('.con-startdate, .con-enddate')) {
                    element.parents('div.controls').find('span.error').remove();
                    element.parents('div.controls').append(error);
                    error.addClass('help-block error');  // add a class to the wrapper
                    error.css('margin', '0');

                }
                else if ($(element).parent().hasClass('input-append')) {
                    element.parent().append(error);
                    error.addClass('help-block error');  // add a class to the wrapper
                    error.css('margin', '0');

                }
                else {
                    element.parents('div.controls').append(error);
                    error.addClass('help-block');  // add a class to the wrapper
                    error.css('margin', '0');
                }
            },
            //Where error class is applied to the element
            highlight: function(element, error) {
                if ($(element).data('toggledby')) {
                    $(element).addClass("error");
                }
                else if ($(element).siblings().hasClass('radio')) {
                    $(element).addClass("error");
                }
                else if ($(element).is('.con-startdate, .con-enddate')) {
                    var datepair = $(element).data("datepair");
                    $("input[data-datepair=" + datepair + "]").parent().addClass("error");
                }
                else if ($(element).parent().hasClass('input-append')) {
                    $(element).parent().addClass("error");
                }
                else {
                    $(element).addClass("error");
                    $(element).parents("div.controls").addClass("error");
                }
            },
            unhighlight: function(element, error) {
                if ($(element).data('toggledby')) {
                    $(element).removeClass("error");
                }
                else if ($(element).siblings().hasClass('radio')) {
                    $(element).removeClass("error");
                }
                else if ($(element).is('.con-startdate, .con-enddate')) {
                    var datepair = $(element).data("datepair");
                    $("input[data-datepair=" + datepair + "]").parent().removeClass("error");
                }
                else if ($(element).parent().hasClass('input-append')) {
                    $(element).parent().removeClass("error");
                }
                else {
                    $(element).removeClass("error");
                    $(element).parents("div.controls")
                            .removeClass("error");
                }
            },
            focusInvalid: false
        });
    });

    //Different for table cells because of different mark up

    $("form:has(table)", target).not('#mainform').each(function() {
        $(this).validate({
            onkeyup: function(element, event) {
                if (event.which === 16 && this.elementValue(element) === '') {
                    return;
                }
                else {
                    if (!$(element).is('input.con-datepicker, input.con-startdate, input.con-enddate, input.con-futuredate, input.con-pastdate')) {
                        if (!this.checkable(element) && (element.name in this.submitted || !this.optional(element))) {
                            this.element(element);
                        }
                    }
                }
            },
            errorElement: "span",
            wrapper: "div", // a wrapper around the error message
            errorPlacement: function(error, element) {
                var width = $(element).width();
                var elementid = element.attr('id');
                var elementparentcell = $('#' + elementid).parents('td');
                error.appendTo(elementparentcell);
                error.addClass('help-block');  // add a class to the wrapper
                error.css('margin', '0');
                error.css('width', width);
            },
            showErrors: function(errorMap, errorList) {
                for (var i = 0; errorList[i]; i++) {
                    var element = this.errorList[i].element;
                    //solves the problem with brute force
                    //remove existing error label and thus force plugin to recreate it
                    //recreation == call to errorplacement function
                    this.errorsFor(element).remove();
                }
                this.defaultShowErrors();
            },
            highlight: function(element, error) {
                $(element).addClass("error");
                $(element).parents("td").addClass("error");
            },
            unhighlight: function(element, error) {
                $(element).removeClass("error");
                $(element).parents("td").removeClass("error");
            },
            focusInvalid: false
        });
    });

    //Setting the default error messages for the defaultvalidation rules

    jQuery.extend(jQuery.validator.messages, {
        required: "This field is required.",
        remote: "Please fix this field.",
        email: "Please enter a valid email address.",
        url: "Please enter a valid URL.",
        date: "Please enter a valid date.",
        dateISO: "Please enter a valid date (ISO).",
        number: "Please enter a valid number.",
        digits: "Whole numbers only",
        creditcard: "Please enter a valid credit card number.",
        equalTo: "Please enter the same value again.",
        accept: "Please enter a value with a valid extension.",
        maxlength: jQuery.validator.format("Please enter no more than {0} characters."),
        minlength: jQuery.validator.format("Please enter at least {0} characters."),
        rangelength: jQuery.validator.format("Please enter a value between {0} and {1} characters long."),
        range: jQuery.validator.format("Please enter a value between {0} and {1}."),
        max: jQuery.validator.format("Please enter a value less than or equal to {0}."),
        min: jQuery.validator.format("Please enter a value greater than or equal to {0}.")
    });

    //    jQuery.validator.addMethod("require_from_group", function(value, element, options) {
    //        var numberRequired = options[0];
    //        var selector = options[1];
    //        var fields = $(selector);
    //        var filled_fields = fields.filter(function() {
    //            // it's more clear to compare with empty string
    //            return $(this).val() != ""; 
    //        });
    //        // we will mark only first empty field as invalid
    //        if (filled_fields.length < numberRequired) {
    //            fields.focus();
    //            return false;
    //        }
    //        else {       
    //            fields.focus();
    //            return true;
    //        }
    //    // {0} below is the 0th item in the options field
    //    }, jQuery.format("Please fill out at least {0} of these fields."));

    //    jQuery.validator.addMethod("require_from_group", function(value, element, options) {
    //        var elems = $(element).parents('form').find(selector);
    //        var numberRequired = options[0];
    //        var selector = options[1];
    //        //Look for our selector within the parent form
    //        var validOrNot = $(selector, element.form).filter(function() {
    //            // Each field is kept if it has a value
    //            return $(this).val();
    //        // Set to true if there are enough, else to false
    //        }).length >= numberRequired;
    //        if(!$(element).data('being_validated')) {
    //            var fields = $(selector, element.form);
    //            var validator = this;
    //            fields.each(function(){
    //                validator.valid(this);
    //            });
    //        }
    //        return validOrNot;
    //    // {0} below is the 0th item in the options field
    //    }, jQuery.format("Please fill out at least {0} of these fields."));
    //
    //
    //    $(":input[data-required='grouprequired']").each(function() {    
    //        var reqno = $(this).data("numberrequired");
    //        var group = $(this).data("requiredgroup");
    //        $(this).rules('add', {
    //            require_from_group: [reqno,"[data-requiredgroup="+group+"]"]
    //        });
    //    });  


    //Adding custom rules to the validator

    jQuery.validator.addMethod("positivenumber", function(value, element) {
        return this.optional(element) || /^(?!(?:^[-+]?[0.]+(?:[Ee]|$)))(?!(?:^-))(?:(?:[+-]?)(?=[0123456789.])(?:(?:(?:[0123456789]+)(?:(?:[.])(?:[0123456789]*))?|(?:(?:[.])(?:[0123456789]+))))(?:(?:[Ee])(?:(?:[+-]?)(?:[0123456789]+))|))$/.test(value);
    }, "Can't be a negative");


    jQuery.validator.addMethod("decimalplaces", function(value, element, param) {
        regex = new RegExp("^[0-9]*(\.[0-9]{1," + param + "})?$");
        return this.optional(element) || regex.test(value);
    }, "This has been set up wrong, sorry!");

    jQuery.validator.addMethod("cvc", function(value, element) {
        return this.optional(element) || /^\d{3,4}$/.test(value);
    }, "Please enter 3 digit cvc code");

    jQuery.validator.addMethod("price", function(value, element) {
        return this.optional(element) || /^[0-9]*(\.[0-9]{1,2})?$/.test(value);
    }, "Prices can only have 2 decimal places");

    jQuery.validator.addMethod("threedecimalplaceprice", function(value, element) {
        return this.optional(element) || /^[0-9]*(\.[0-9]{1,3})?$/.test(value);
    }, "Prices can only have 3 decimal places");

    jQuery.validator.addMethod("alpha", function(value, element) {
        return this.optional(element) || /^\s*[a-zA-Z,\s]+\s*$/.test(value);
    }, "Letters only please");

    jQuery.validator.addMethod("code", function(value, element) {
        return this.optional(element) || /^[A-Za-z0-9]+$/.test(value);
    }, "Alphanumeric only");

    jQuery.validator.addMethod("postcode", function(value, element) {
        return this.optional(element) || /^([A-PR-UWYZ]([0-9]{1,2}|([A-HK-Y][0-9]|[A-HK-Y][0-9]([0-9]|[ABEHMNPRV-Y]))|[0-9][A-HJKS-UW])\ [0-9][ABD-HJLNP-UW-Z]{2}|(GIR\ 0AA)|(SAN\ TA1)|(BFPO\ (C\/O\ )?[0-9]{1,4})|((ASCN|BBND|[BFS]IQQ|PCRN|STHL|TDCU|TKCA)\ 1ZZ))$/.test(value);
    }, "Sorry we don't know where that is");

    jQuery.validator.addMethod("zipcode", function(value, element) {
        return this.optional(element) || /^[0-9]{5}(-[0-9]{4})?$/.test(value);
    }, "Sorry we don't know where that is");


    jQuery.validator.addMethod("ukmobile", function(value, element) {
        return this.optional(element) || /^((07|00447|\+447)\d{9}|(08|003538|\+3538)\d{8,9})$/.test(value);
    }, "Please enter a valid UK mobile number");

    jQuery.validator.addMethod("creditcard", function(value, element) {
        return this.optional(element) || /^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\d{3})\d{11})$/.test(value);
    }, "Please enter a valid credit card number.");

    jQuery.validator.addMethod("ukphonenumber", function(value, element) {
        return this.optional(element) || /^(((\+44\s?\d{4}|\(?0\d{4}\)?)\s?\d{3}\s?\d{3})|((\+44\s?\d{3}|\(?0\d{3}\)?)\s?\d{3}\s?\d{4})|((\+44\s?\d{2}|\(?0\d{2}\)?)\s?\d{4}\s?\d{4}))(\s?\#(\d{4}|\d{3}))?$/.test(value);
    }, "Please enter a valid UK phone number");

    jQuery.validator.addMethod("usphonenumber", function(value, element) {
        return this.optional(element) || /^1?[-\. ]?(\(\d{3}\)?[-\. ]?|\d{3}?[-\. ]?)?\d{3}?[-\. ]?\d{4}$/.test(value);
    }, "Please enter a valid US phone number");

    jQuery.validator.addMethod("help", function(value, element) {
        return this.optional(element) || /[^?]$/.test(value);
    }, "Generic help message");

    jQuery.validator.addMethod("regex", function(value, element, param) {
        regex = new RegExp(param);
        return this.optional(element) || regex.test(value);
    }, "This has been set up wrong, sorry!");


    jQuery.validator.addMethod("percentage", function(value, element) {
        return this.optional(element) || /^0*(100(\.00?)?|[0-9]?[0-9](\.[0-9][0-9]?)?)%?$/.test(value);
    }, "Between 0 and 100, with up to 2 decimal places");


    $.validator.addMethod("enddate", function(value, element) {
        var datepair = $(element).data("datepair");
        if ($("input[data-datepair=" + datepair + "]").not(".con-enddate").val() !== "") {
            var otherdate = $("input[data-datepair=" + datepair + "]").not(".con-enddate");
            var otherdateid = otherdate.attr('id');
            $("span.error[for='" + otherdateid + "']").hide();
            var startdatevalue = otherdate.val();
            return moment(startdatevalue, "DD-MMM-YYYY") <= moment(value, "DD-MMM-YYYY");
        }
        return true;
    }, "Can't leave before you arrive");

    $.validator.addMethod("startdate", function(value, element) {
        var datepair = $(element).data("datepair");
        if ($("input[data-datepair=" + datepair + "]").not(".con-startdate").val() !== "") {
            var otherdate = $("input[data-datepair=" + datepair + "]").not(".con-startdate");
            var otherdateid = otherdate.attr('id');
            $("span.error[for='" + otherdateid + "']").hide();
            var enddatevalue = otherdate.val();
            return moment(enddatevalue, "DD-MMM-YYYY") >= moment(value, "DD-MMM-YYYY");
        }
        return true;
    }, "Can't leave before you arrive");

    $.validator.addMethod("formatdate", function(value, element) {
        return this.optional(element) || m4dateformat.regex.test(value);
    }, "Please enter a valid date format or use the datepicker by clicking the calendar icon");

    $.validator.addMethod("customformatdate", function(value, element) {
        var customstring = $(element).data("custom-date-format-regex");
        var customregex = RegExp(customstring);
        return this.optional(element) || customregex.test(value);
    }, "Custom format error message");

    $.validator.addMethod("lastdate", function(value) {
        return moment("01-01-2030", "DD-MMM-YYYY") > moment(value, "DD-MMM-YYYY");
    }, "Can't be after 2020");

    $.validator.addMethod("earliestdate", function(value) {
        return moment("01-01-1800", "DD-MMM-YYYY") < moment(value, "DD-MMM-YYYY");
    }, "Can't be before 1800");

    $.validator.addMethod("aftertoday", function(value) {
        var rightnow = moment();
        return moment(rightnow, "DD-MMM-YYYY") <= moment(value, "DD-MMM-YYYY");
    }, "Has to be in the future");

    $.validator.addMethod("beforetoday", function(value) {
        var rightnow = moment();
        return moment(rightnow, "DD-MMM-YYYY") >= moment(value, "DDMMYYYY");
    }, "Has to be in the past");

    $.validator.addMethod("time", function(value, element) {
        return this.optional(element) || /^([01]?[0-9]|2[0-3]):?[0-5][0-9]$/i.test(value);
    }, "Please enter a valid time (1:20, 01:20)");

    $.validator.addMethod("dob", function(value, element) {
        return this.optional(element) || m4dateformat.dobregex.test(value);
    }, m4dateformat.dobregexmessage);

    $.validator.addMethod("email", function(value, element) {
        var emailregexp = new RegExp("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
        return this.optional(element) || emailregexp.test(value);
    }, "Please enter a valid email address");

}

//Function to add validation rules to elements within the target

function addvalidation(target) {
    function roundNumber(num, dec) {
        var result = Math.round(num * Math.pow(10, dec)) / Math.pow(10, dec);
        return result;
    }

    //Function to add validation rules to elements within the target


    //Explanation
    $(":input[data-valid='integer']", target).each(function() {
        //variables set to check for any validation overrides (data-attributes) on the element
        var maxlength = $(this).data("maxlength");
        var max = $(this).data("max");
        var min = $(this).data("min");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var maxmessage = $(this).data("maxmessage");
        var minmessage = $(this).data("minmessage");
        //Each option applies the validation rules defined for that option to the element, here digits, maxlength and max
        $(this).rules("add", {
            digits: true,
            maxlength: 6,
            max: 999999
        });
        //Series of checks to see if any of the variables exist and if so to apply the validation overrides to the element
        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (max !== undefined) {
            $(this).rules("add", {
                max: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    max: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });


    $(":input[data-valid='requiredcheckboxes']", target).each(function() {
        var reqno = $(this).data("numberrequired");
        $(this).rules('add', {
            required: true,
            minlength: 1,
            messages: {
                minlength: "Please select 1 or more options"
            }
        });
        if (reqno !== undefined) {
            $(this).rules("add", {
                minlength: reqno,
                messages: {
                    minlength: "Please select " + reqno + " or more options"
                }
            });
        }
    });

    $(":input[data-valid='cvc']", target).each(function() {
        $(this).rules('add', {
            cvc: true
        });
    });

    $(":input[data-valid='time']", target).each(function() {
        $(this).rules('add', {
            time: true
        });
    });

    $(":input[data-valid='creditcard']", target).each(function() {
        $(this).rules('add', {
            creditcard: true
        });
    });

    $(":input.con-datepicker:not([data-date-format])", target).each(function() {
        $(this).rules('add', {
            formatdate: true
        });
    });

    $(":input.con-datepicker[data-date-format]", target).each(function() {
        $(this).rules('add', {
            customformatdate: true
        });
    });

    $(":input.con-enddate", target).each(function() {
        $(this).rules('add', {
            formatdate: true,
            enddate: true,
            lastdate: true,
            earliestdate: true
        });
    });

    $(":input.con-startdate", target).each(function() {
        $(this).rules('add', {
            formatdate: true,
            startdate: true,
            earliestdate: true,
            lastdate: true
        });
    });

    $(":input.con-futuredate", target).each(function() {
        $(this).rules('add', {
            formatdate: true,
            lastdate: true,
            aftertoday: true
        });
    });

    $(":input.con-pastdate", target).each(function() {
        $(this).rules('add', {
            formatdate: true,
            earliestdate: true,
            beforetoday: true
        });
    });


    $(":input[data-valid='customdecimalplaces']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var max = $(this).data("max");
        var min = $(this).data("min");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var maxmessage = $(this).data("maxmessage");
        var minmessage = $(this).data("minmessage");
        var decimalplaces = $(this).data("decimalplaces");
        $(this).rules("add", {
            decimalplaces: 2,
            maxlength: 9,
            max: 999999999,
            messages: {
                decimalplaces: "Only accepts " + decimalplaces + " decimal places"
            }
        });
        if (decimalplaces !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }

        if (maxlength !== undefined) {
            $(this).rules("add", {
                decimalplaces: decimalplaces
            });
        }
        if (max !== undefined) {
            $(this).rules("add", {
                max: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    max: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });

    $(":input[data-valid='price']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var max = $(this).data("max");
        var min = $(this).data("min");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var maxmessage = $(this).data("maxmessage");
        var minmessage = $(this).data("minmessage");
        $(this).rules("add", {
            price: true,
            maxlength: 9,
            max: 999999999
        });

        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (max !== undefined) {
            $(this).rules("add", {
                max: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    max: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });

    $(":input[data-valid='threedecimalplaceprice']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var max = $(this).data("max");
        var min = $(this).data("min");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var maxmessage = $(this).data("maxmessage");
        var minmessage = $(this).data("minmessage");
        $(this).rules("add", {
            threedecimalplaceprice: true,
            maxlength: 9,
            max: 999999999
        });

        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (max !== undefined) {
            $(this).rules("add", {
                max: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    max: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });

    $(":input[data-valid='number']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var max = $(this).data("max");
        var min = $(this).data("min");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var maxmessage = $(this).data("maxmessage");
        var minmessage = $(this).data("minmessage");
        $(this).rules("add", {
            number: true,
            maxlength: 9,
            max: 999999999
        });

        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (max !== undefined) {
            $(this).rules("add", {
                max: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    max: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });

    $(":input[data-valid='percentage']", target).each(function() {
        $(this).rules("add", {
            percentage: true
        });
    });

    $(":input[data-valid='cheque']", target).each(function() {
        $(this).rules("add", {
            digits: true,
            maxlength: 6,
            minlength: 6,
            messages: {
                minlength: "Cheque numbers need to be 6 digits",
                maxlength: "Cheque numbers need to be 6 digits"
            }
        });
    });

    $(":input[data-valid='age']", target).each(function() {
        $(this).rules("add", {
            digits: true,
            maxlength: 3,
            minlength: 1,
            max: 120,
            min: 0,
            messages: {
                minlength: "You have to have been born",
                maxlength: "This seems high",
                max: "This seems high",
                min: "You have to have been born"
            }
        });
    });

    $(":input[data-valid='pax']", target).each(function() {
        $(this).rules("add", {
            digits: true,
            maxlength: 2,
            minlength: 1,
            min: 1,
            messages: {
                minlength: "Surely someone wants to go?",
                maxlength: "Only 99 per booking"
            }
        });
    });

    $(":input[data-valid='infants']", target).each(function() {
        $(this).rules("add", {
            digits: true,
            max: 8,
            minlength: 0,
            messages: {
                max: "That seems like too many"
            }
        });
    });

    $(":input[data-valid='children']", target).each(function() {
        $(this).rules("add", {
            digits: true,
            maxlength: 2,
            minlength: 0,
            messages: {
                maxlength: "That seems like too many"
            }
        });
    });

    $(":input[data-valid='adults']", target).each(function() {
        var min = $(this).data("min");
        var minmessage = $(this).data("minmessage");
        $(this).rules("add", {
            digits: true,
            maxlength: 2,
            minlength: 1,
            min: 1,
            messages: {
                maxlength: "That seems like too many",
                min: "Surely someone wants to go?"
            }
        });
        if (min !== undefined) {
            $(this).rules("add", {
                min: min
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    min: minmessage
                }
            });
        }
    });

    $(":input[data-valid='distance']", target).each(function() {
        $(this).rules("add", {
            help: true,
            positivenumber: true,
            maxlength: 8,
            messages: {
                positivenumber: "Positive numbers only. For miles -> km conversion, just subfix mi",
                help: "For miles -> km conversion, just subfix mi"
            }
        });
    });

    $(":input[data-valid='distance']").keyup(function() {
        if ($(this).is(":input[value$='mi']")) {
            var value = $(this).val().replace(/[^\d\.]/g, '') * 1.609344;
            var roundednumber = roundNumber(value, 2);
            $(this).val(roundednumber);
        }
        var milesvalue = $(this).val().replace(/[^\d\.]/g, '') / 1.609344;
        var roundedmiles = roundNumber(milesvalue, 2);
        $(this).parents('div.input-append').find('span.add-on').attr("data-original-title", "Or " + roundedmiles + " miles. For miles -> km conversion, just subfix mi");
    });

    $(":input[data-valid='weight']", target).each(function() {
        $(this).rules("add", {
            help: true,
            positivenumber: true,
            maxlength: 8,
            messages: {
                positivenumber: "Positive numbers only. For pound -> kilo conversion, just subfix lb",
                help: "For pound -> kilo conversion, just subfix lb"
            }
        });
    });

    $(":input[data-valid='weight']", target).keyup(function() {
        if ($(this).is(":input[value$='lb']")) {
            var value = $(this).val().replace(/[^\d\.]/g, '') * 0.453592;
            var roundednumber = roundNumber(value, 2);
            $(this).val(roundednumber);
        }
        var lbsvalue = $(this).val().replace(/[^\d\.]/g, '') / 0.453592;
        var roundedlbs = roundNumber(lbsvalue, 2);
        $(this).parents('div.input-append').find('span.add-on').attr("data-original-title", "Or " + roundedlbs + " lbs. For pound -> kilo conversion, just subfix lb");
    });


    $(":input[data-valid='dims']", target).each(function() {
        $(this).rules("add", {
            help: true,
            positivenumber: true,
            maxlength: 8,
            messages: {
                positivenumber: "Positive numbers only. For inch -> cm conversion, just subfix in",
                help: "For inch -> cm conversion, just subfix in"
            }
        });
    });

    $(":input[data-valid='dims']", target).keyup(function() {
        if ($(this).is(":input[value$='in']")) {
            var value = $(this).val().replace(/[^\d\.]/g, '') * 2.54;
            var roundednumber = roundNumber(value, 2);
            $(this).val(roundednumber);
        }
        var invalue = $(this).val().replace(/[^\d\.]/g, '') / 2.54;
        var roundedins = roundNumber(invalue, 2);
        $(this).parents('div.input-append').find('span.add-on').attr("data-original-title", "Or " + roundedins + " inches. For inch -> cm conversion, just subfix in");
    });

    $(":input[data-valid='currencytogbp']", target).keyup(function() {
        if ($(this).is(":input[value$='usd']")) {
            var value = $(this).val().replace(/[^\d\.]/g, '') * 0.6196;
            var roundednumber = roundNumber(value, 2);
            $(this).val(roundednumber);
        }
        var invalue = $(this).val().replace(/[^\d\.]/g, '') / 0.6196;
        var roundedins = roundNumber(invalue, 2);
        $(this).parents('div.input-append').find('span.add-on').attr("data-original-title", "Or " + roundedins + " USD. For conversion to GBP, just subfix gbp");
    });

    $(":input[data-valid='currencytogbp']", target).each(function() {
        $(this).rules("add", {
            help: true,
            positivenumber: true,
            price: true,
            maxlength: 8,
            messages: {
                positivenumber: "Positive numbers only. For conversion to GBP, just subfix gbp",
                help: "For conversion to GBP, just subfix gbp"
            }
        });
    });

    $(":input[data-valid='text']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        $(this).rules("add", {
            maxlength: 100,
            messages: {
                maxlength: "Any chance you could be a spot more concise?"
            }
        });
        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
    });

    $(":input[data-valid='code']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var minlength = $(this).data("minlength");
        var minlengthmessage = $(this).data("minlengthmessage");
        var thisval = $(this).val();
        $(this).val(thisval.toUpperCase());
        $(this).rules("add", {
            code: true,
            maxlength: 8,
            minlength: 3,
            messages: {
                minlength: "More than that",
                maxlength: "Less than that"
            }
        });
        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (minlength !== undefined) {
            $(this).rules("add", {
                minlength: minlength
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (minlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    minlength: minlengthmessage
                }
            });
        }

    });

    $(":input[data-valid='alpha']", target).each(function() {
        var maxlength = $(this).data("maxlength");
        var maxlengthmessage = $(this).data("maxlengthmessage");
        var minlength = $(this).data("minlength");
        var minlengthmessage = $(this).data("minlengthmessage");
        $(this).rules("add", {
            alpha: true,
            maxlength: 50,
            minlength: 0,
            messages: {
                minlength: "More than that",
                maxlength: "Less than that"
            }
        });
        if (maxlength !== undefined) {
            $(this).rules("add", {
                maxlength: maxlength
            });
        }
        if (minlength !== undefined) {
            $(this).rules("add", {
                minlength: minlength
            });
        }
        if (maxlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxlengthmessage
                }
            });
        }
        if (minlengthmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    minlength: minlengthmessage
                }
            });
        }
    });

    $(":input[data-valid='numeric']", target).each(function() {
        var max = $(this).data("max");
        var maxmessage = $(this).data("maxmessage");
        var min = $(this).data("min");
        var minmessage = $(this).data("minmessage");
        $(this).rules("add", {
            number: true,
            max: 50,
            min: 0,
            messages: {
                maxmessage: "More than that",
                minmessage: "Less than that"
            }
        });
        if (max !== undefined) {
            $(this).rules("add", {
                maxlength: max
            });
        }
        if (min !== undefined) {
            $(this).rules("add", {
                minlength: min
            });
        }
        if (maxmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    maxlength: maxmessage
                }
            });
        }
        if (minmessage !== undefined) {
            $(this).rules("add", {
                messages: {
                    minlength: minmessage
                }
            });
        }
    });

    $(":input[data-valid='ukmobile']", target).each(function() {
        $(this).rules("add", {
            ukmobile: true,
            maxlength: 30
        });
    });

    $(":input[data-valid='ukphonenumber']", target).each(function() {
        $(this).rules("add", {
            ukphonenumber: true,
            maxlength: 30
        });
    });

    $(":input[data-valid='usphonenumber']", target).each(function() {
        $(this).rules("add", {
            usphonenumber: true,
            maxlength: 30
        });
    });

    $(":input[data-valid='postcode']", target).each(function() {
        $(this).rules("add", {
            postcode: true,
            maxlength: 8
        });
    });

    $(":input[data-valid='zipcode']", target).each(function() {
        $(this).rules("add", {
            zipcode: true,
            maxlength: 10
        });
    });

    $(":input[data-valid='email']", target).each(function() {
        $(this).rules("add", {
            email: true,
            maxlength: 60
        });
    });

    $(":input[data-valid='url']", target).each(function() {
        $(this).rules("add", {
            url: true,
            maxlength: 100
        });
    });

    $(":input.con-dobfield", target).each(function() {
        $(this).rules("add", {
            dob: true,
            beforetoday: true
        });
    });

    $(":input[data-valid='regex']", target).each(function() {
        var regex = $(this).data("regex");
        var regexmessage = $(this).data("regexmessage");
        $(this).rules("add", {
            regex: regex,
            messages: {
                regex: regexmessage
            }
        });
    });

    $(":input[data-required='required']", target).each(function() {
        $(this).rules('add', {
            required: true
        });
        if ($(this).is('select')) {
            $(this).prepend('<option value="">Required</option>');
        }
        else {
            $(this).attr('placeholder', 'Required');
        }
    });

    $(":input[data-required='required'][data-valid='time']", target).each(function() {
        $(this).val("12:00");
    });

    $("input.con-timepicker", target).each(function() {
        $(this).rules("add", {
            time: true
        });
    });

}