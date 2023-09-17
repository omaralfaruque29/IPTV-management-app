// Set the below variable to the changing root of the urls for this deployment

var deployspecificurlsection = "http://maf.dev.m4.net/searchSuggest-1.0/";

//These must generate the same format of date from each script. 
var m4dateformat = {
    moment:"DD-MMM-YYYY",
    datepickers:"dd-M-yyyy",
    regex:/^(0[1-9]|[12][0-9]|3[01])-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)-(19|20)\d\d$/,
    dobregex: /^(((0[1-9]|[12]\d|3[01])[\s\/-]?(0[13578]|1[02])[\s\/-]?((19|[2-9]\d)\d{2}))|((0[1-9]|[12]\d|30)[\s\/-]?(0[13456789]|1[012])[\s\/-]?((19|[2-9]\d)\d{2}))|((0[1-9]|1\d|2[0-8])[\s\/-]?02[\s\/-]?((19|[2-9]\d)\d{2}))|(29[\s\/-]?02[\s\/-]?((1[6-9]|[2-9]\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))))$/,
    dobregexmessage:"Please use a valid format (01012001, 01-01-2001, 01 01 2001 or 01/01/2001)"
};

var m4language = {
    datepickers:{
        days: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
        daysShort: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"],
        daysMin: ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"],
        months: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
        monthsShort: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
        today: "Today"
    },
    moment:{
        months : "January_February_March_April_May_June_July_August_September_October_November_December".split("_"),
        monthsShort : "Jan_Feb_Mar_Apr_May_Jun_Jul_Aug_Sep_Oct_Nov_Dec".split("_"),
        weekdays : "Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday".split("_"),
        weekdaysShort : "Sun_Mon_Tue_Wed_Thu_Fri_Sat".split("_"),
        weekdaysMin : "Su_Mo_Tu_We_Th_Fr_Sa".split("_"),
        longDateFormat : {
            LT : "HH:mm",
            L : "DD/MM/YYYY",
            LL : "D MMMM YYYY",
            LLL : "D MMMM YYYY LT",
            LLLL : "dddd, D MMMM YYYY LT"
        },
        calendar : {
            sameDay : '[Today at] LT',
            nextDay : '[Tomorrow at] LT',
            nextWeek : 'dddd [at] LT',
            lastDay : '[Yesterday at] LT',
            lastWeek : '[Last] dddd [at] LT',
            sameElse : 'L'
        },
        relativeTime : {
            future : "in %s",
            past : "%s ago",
            s : "a few seconds",
            m : "a minute",
            mm : "%d minutes",
            h : "an hour",
            hh : "%d hours",
            d : "a day",
            dd : "%d days",
            M : "a month",
            MM : "%d months",
            y : "a year",
            yy : "%d years"
        },
        ordinal : function (number) {
            var b = number % 10,
            output = (~~ (number % 100 / 10) === 1) ? 'th' :
            (b === 1) ? 'st' :
            (b === 2) ? 'nd' :
            (b === 3) ? 'rd' : 'th';
            return number + output;
        },
        week : {
            dow : 1, // Monday is the first day of the week.
            doy : 4  // The week that contains Jan 4th is the first week of the year.
        }
    }
};

var searchSuggestSetting = {
    "search1": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }]
    },   
    "navbarsearch": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }],
        success: function( responseData, resultsList) { 

            if(responseData.headings){
                record['headings'] = responseData.headings;
                resultsList.push(record);
            } 

            //Push the main values object to the root of respinseData. This will blow away the headings
            //but that is OK.
            if(responseData.values) responseData = responseData.values;

            for(i=0;i<responseData.length;i++){
                record = {};
                record["key"] = responseData[i].key;
                if($.isArray(responseData[i].value)){
                    record["value"] = [];
                    for(j=0;
                        j<responseData[i].value.length;
                        j++){
                        record["value"][j] = responseData[i].value[j];
                    }
                }
                resultsList.push(record);
            }
        },
        renderItem: function (ul, data) {
            console.log('here it is--');
            if(data.headings){
                return $("<li class='columnautocomplete'></li>")
                .data("item.autocomplete", data)
                .append("<div class='heading'><b>" + data.headings[0] + "</b></div><div class='heading'><b>" + data.headings[1] + "</b></div>")
                .appendTo(ul);
            }else{
                return $("<li class='columnautocomplete'></li>")
                .data("item.autocomplete", data)
                //Format the below line to contain the desired amount of columns and ensure that you are selecting the desired array value for each column
                .append("<a data-target='#wordmodal' data-toggle='modal' data-remote='/GREWordGroupHelper/cts/" + data.value[0] + ".htm'><div class='col'>" + data.value[0] + "</div><div class='col'>" + data.value[1] + "</div></a>")
                .appendTo(ul);
            }
        }             
    },  
    "search2": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }]
    },  
    "availdestination": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }]
    },  
    "availresort": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }]
    },   
    "availhotel": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        dataType: "jsonp",
        dataparams: [{
            key:"id", 
            value:"PROPERTY1"
        }]
    }   
};

var submitSetting = {
	"table1submit": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        type: "POST",
        data: $(this).parents('table').find('tbody :input').not('button').serializeArray(),
        error: function(){
            alert('Test alternate alert');
        }
    },
    "modal1submit": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        type: "POST",
        data: $(this).parents('div.modal').find('form:visible :input').not('button').serializeArray(),
        error: function(){
            alert('Test alternate alert');
        }
    },
    "search1": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        searchid: "PROPERTY1"
    },
    "example1": {
        // Append the service specific chunk of the url for this search. This will not change from deploy to deploy.
        url: deployspecificurlsection+"newSearchSuggestServlet?callback=?",
        searchid: "PROPERTY1"
    },
    "pricelineupdated": {},
    "pricelinedeleted": {},
    "addrouteextra": {},
    "archiverouteextra": {},
    "addrouteleg": {},
    "archiverouteleg": {},
    "savetag" : {}
};