define([
  "namespace",

  // Libs
  "use!backbone",
  "use!backbone_couchdb"

  // Modules

  // Plugins
],

function(namespace, Backbone) {

  // Create a new module
  var Item = namespace.module();

  // Item extendings
  Item.Model = Backbone.Model.extend({ /* ... */ });
  Item.Collection = Backbone.Collection.extend({
		initialize: function() {
			this.page = 1;
		},
		db : {
			view: "byId",
		},
		url : "/item",
		model : Item
	});
  Item.Router = Backbone.Router.extend({ /* ... */ });

  // This will fetch the tutorial template and render it.
  Item.Views.Tutorial = Backbone.View.extend({
    template: "app/templates/item.html",

    render: function(done) {
      var view = this;

      // Fetch the template, render it to the View element and call done.
      namespace.fetchTemplate(this.template, function(tmpl) {
        view.el.innerHTML = tmpl();

        // If a done function is passed, call it with the element
        if (_.isFunction(done)) {
          done(view.el);
        }
      });
    }
  });

  // Required, return the module for AMD compliance
  return Item;

});
