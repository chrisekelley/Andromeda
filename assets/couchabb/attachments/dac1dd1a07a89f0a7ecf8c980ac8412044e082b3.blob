
	// Set the require.js configuration for your application.
require.config({
  // Initialize the application with the main application file
  deps: ["main"],

  paths: {
    // JavaScript folders
    libs: "../assets/js/libs",
    plugins: "../assets/js/plugins",

    // Libraries
    jquery: "../assets/js/libs/jquery",
    underscore: "../assets/js/libs/underscore",
    backbone: "../assets/js/libs/backbone-full",
    backbone_couchdb: "../assets/js/libs/backbone-couchdb", // https://github.com/janmonschke/backbone-couchdb
    jquery_couch: "../assets/js/libs/jquery.couch",
    // Shim Plugin
    use: "../assets/js/plugins/use",
    // todomvc
    text: "../assets/js/plugins/text",
    //localstorage:  "../assets/js/plugins/backbone-localstorage"
    syncpoint: "../assets/js/libs/syncpoint-utils",
    Cordova: "../assets/js/libs/cordova-2.0.0"
  },

  use: {
    backbone: {
      deps: ["use!underscore", "jquery"],
      attach: "Backbone"
    },

    underscore: {
      attach: "_"
    },
    backbone_couchdb: {
    	deps: ["use!underscore", "jquery","use!backbone", "use!jquery_couch"]
    },
    jquery_couch: {
    	deps: ["jquery"]
    	//$.couch
    }
    /*localstorage: {
    	deps: ["use!underscore","use!backbone"]
    }*/
  }
});