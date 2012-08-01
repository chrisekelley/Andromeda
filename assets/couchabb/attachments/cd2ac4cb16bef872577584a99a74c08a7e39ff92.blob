require([
  "namespace",

  // Libs
  "jquery",
  "use!backbone",
  "use!backbone_couchdb",

  // Modules
  //"modules/example",
  "modules/todo"
],

function(namespace, $, Backbone, Example, Todo) {
	var Router = Backbone.Router.extend({
		routes: {
			"": "index",
			":hash": "index"
				//":tutorial": "tutorial"
				//"/": "index",
				//"couchabb/_design/couchabb/index.html": "index"
		},

		index: function(hash) {
			var route = this;
			//** Configure the database **//
			FORMY.SyncpointLocalDb = null;
			
			var docUrl = document.URL;
		    docUrl = docUrl.split("/");
		    docUrl = docUrl.slice(3, 4);
		    console.log("docUrl: " + docUrl + " document.URL: " + document.URL);
		    
			Backbone.couch_connector.config.db_name = docUrl;
			Backbone.couch_connector.config.ddoc_name = "couchabb";
			console.log("Init of Backbone.couch_connector.config.db_name: " + Backbone.couch_connector.config.db_name);
			console.log("Init of Backbone.couch_connector.config.ddoc_name: " + Backbone.couch_connector.config.ddoc_name);
			//$.when( findSyncpointLocalDb() ).then(

			var id = null;
			var local_db_name = null;
			if (docUrl != "couchabb") {
				var jqxhr = $.getJSON('/sp_control/_design/control/_view/by_type?key=%22installation%22', function(data) { 
					var record = null;
					$.each(data, function(key, val) {
						if (key == "rows") {
							record = val;
							id = record[0].id;
							console.log("id: " + id);
						}
					}
					);
				}).complete(
						function() {
							/*if (record != null) {
							  //console.log("record: " + JSON.stringify(record));
							  console.log("id: " + id);*/
							var jqxhr = $.getJSON('/sp_control/' + id, function(data) {
								//console.log("data: " + JSON.stringify(data));
								local_db_name = data.local_db_name;
								FORMY.SyncpointLocalDb = local_db_name;
								console.log("local_db_name: " + FORMY.SyncpointLocalDb);
								Backbone.couch_connector.config.db_name = FORMY.SyncpointLocalDb;
								console.log("Setting Backbone.couch_connector.config.db_name. ");
								console.log("Backbone.couch_connector.config.db_name: " + FORMY.SyncpointLocalDb);
								//return dfd.promise();
							}).complete(
									function() {
										loadWebpage(hash);
									}
							); 
						}
						/*} else {
							  console.log("Syncpoint setup incomplete: Backbone.couch_connector.config.db_name: " + Backbone.couch_connector.config.db_name);
							  Backbone.couch_connector.config.db_name = "couchabb";
						  }*/
				);
			} else {
				loadWebpage(hash);
			}
			
		},
	});
  

  // Shorthand the application namespace
  var app = namespace.app;
  // Treat the jQuery ready function as the entry point to the application.
  // Inside this function, kick-off all initialization, everything up to this
  // point should be definitions.
  $(function() {
    // Define your master router on the application namespace and trigger all
    // navigation from this instance.
    app.router = new Router();
    // Trigger the initial route and enable HTML5 History API support
    //Backbone.history.start({ pushState: true, root: "/" + Backbone.couch_connector.config.db_name + "/_design/couchabb/index.html" });
    var docUrl = document.URL;
    docUrl = docUrl.split("/");
    docUrl = docUrl.slice(3, 4);
    console.log("docUrl: " + docUrl + " document.URL: " + document.URL);
    Backbone.history.start({ pushState: true, root: "/" + docUrl + "/_design/couchabb/index.html" });
  });

  // All navigation that is relative should be passed through the navigate
  // method, to be processed by the router.  If the link has a data-bypass
  // attribute, bypass the delegation completely.
  $(document).on("click", "a:not([data-bypass])", function(evt) {
    // Get the anchor href and protcol
    var href = $(this).attr("href");
    var protocol = this.protocol + "//";

    // Ensure the protocol is not part of URL, meaning its relative.
    if (href && href.slice(0, protocol.length) !== protocol &&
        href.indexOf("javascript:") !== 0) {
      // Stop the default event to ensure the link will not cause a page
      // refresh.
      evt.preventDefault();

      // `Backbone.history.navigate` is sufficient for all Routers and will
      // trigger the correct events.  The Router's internal `navigate` method
      // calls this anyways.
      Backbone.history.navigate(href, true);
    }
  });
  
  var FORMY = {};
  
  function loadWebpage(hash) {
	  // Fetch the template, render it to the View element and call done.
	  namespace.fetchTemplate("app/templates/todomvc.html", function(tmpl) {
		  console.log("we can proceed now: " + Backbone.couch_connector.config.db_name);
		  var htmlText = tmpl();
		  //this.$("#main").innerHTML = htmlText;
		  this.$("#main").html(htmlText);
		  console.log("rendered index template. ");
		  var todoView = new Todo.Views.Todomvc();
		  todoView.render(
				  function(el){
					  //$("#main").html(el);
					  //Todo.TodoCollection.fetch();
					  // Fix for hashes in pushState and hash fragment
					  if (hash && !route._alreadyTriggered) {
						  // Reset to home, pushState support automatically converts hashes
						  Backbone.history.navigate("", false);
						  // Trigger the default browser behavior
						  location.hash = hash;
						  // Set an internal flag to stop recursive looping
						  route._alreadyTriggered = true;
					  }
				  });
	  });
  }
  
  function findSyncpointLocalDb() {
	  //var dfd = new jQuery.Deferred();
	  var id = null;
	  var local_db_name = null;
	  var jqxhr = $.getJSON('/sp_control/_design/control/_view/by_type?key=%22installation%22', function(data) { 
		  var record = null;
		  $.each(data, function(key, val) {
			  if (key == "rows") {
				  record = val;
				  id = record[0].id;
				  console.log("id: " + id);
			  }
		  }
		  );
	  }).complete(
			  function() {
			  /*if (record != null) {
					  //console.log("record: " + JSON.stringify(record));
					  console.log("id: " + id);*/
			  $.getJSON('/sp_control/' + id, function(data) {
				  //console.log("data: " + JSON.stringify(data));
				  local_db_name = data.local_db_name;
				  FORMY.SyncpointLocalDb = local_db_name;
				  console.log("local_db_name: " + FORMY.SyncpointLocalDb);
				  Backbone.couch_connector.config.db_name = FORMY.SyncpointLocalDb;
				  console.log("Backbone.couch_connector.config.db_name: " + FORMY.SyncpointLocalDb);
				  //return dfd.promise();
			  });
			  }
			  /*} else {
					  console.log("Syncpoint setup incomplete: Backbone.couch_connector.config.db_name: " + Backbone.couch_connector.config.db_name);
					  Backbone.couch_connector.config.db_name = "couchabb";
				  }*/
	  );
	  //console.log("after if/else: Backbone.couch_connector.config.db_name: " + Backbone.couch_connector.config.db_name);
	  //})

	  /*  .error(
			  console.log("Syncpoint not setup: Backbone.couch_connector.config.db_name: " + Backbone.couch_connector.config.db_name)
	  );*/
	  return jqxhr;
  }

});
