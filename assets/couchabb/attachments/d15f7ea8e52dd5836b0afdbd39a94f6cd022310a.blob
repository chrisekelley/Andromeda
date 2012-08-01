define([
  "namespace",

  // Libs
  "use!backbone",
  "use!backbone_couchdb"

  // Modules

  // Plugins
  //"use!localstorage"
],

function(namespace, Backbone) {

  // Create a new module
	var Todo = namespace.module();

	// Todo extendings
	//Todo.Model = Backbone.Model.extend({ /* ... */ });
	Todo.Model = Backbone.Model.extend({

		// Default attributes for the todo.
		defaults: {
			content: "empty todo...",
			done: false
		},

		// Ensure that each todo created has `content`.
		initialize: function() {
			if (!this.get("content")) {
				this.set({"content": this.defaults.content});
			}
		},

		// Toggle the `done` state of this todo item.
		toggle: function() {
			this.save({done: !this.get("done")});
		},

		// Remove this Todo from *localStorage*.
		clear: function() {
			this.destroy();
		}

	});

  Todo.TodoCollection = Backbone.Collection.extend({

	    // Reference to this collection's model.
	    model: Todo.Model,

	    // Save all of the todo items under the `"todos"` namespace.
	    //localStorage: new Store("Todos"),

	    // Filter down the list of all todo items that are finished.
	    done: function() {
	      return this.filter(function(todo){ return todo.get('done'); });
	    },

	    // Filter down the list to only todo items that are still not finished.
	    remaining: function() {
	      return this.without.apply(this, this.done());
	    },

	    // We keep the Todos in sequential order, despite being saved by unordered
	    // GUID in the database. This generates the next order number for new items.
	    nextOrder: function() {
	      if (!this.length) return 1;
	      return this.last().get('order') + 1;
	    },

	    // Todos are sorted by their original insertion order.
	    comparator: function(todo) {
	      return todo.get('order');
	    },
	    url : "/todo",
	  });
  Todo.TodoCollection = new Todo.TodoCollection({model: new Todo.Model()});
  //Todo.TodoCollection = new Todo.TodoCollection({ model:new Todo()});
  Todo.Router = Backbone.Router.extend({ /* ... */ });

  // This will fetch the Todomvc template and render it.
  Todo.Views.Todomvc = Backbone.View.extend({

	  // Instead of generating a new element, bind to the existing skeleton of
	  // the App already present in the HTML.
	  el: $("#main"),

	  template: "app/templates/todomvc.html",
	  // Instead of generating a new element, bind to the existing skeleton of
	  // the App already present in the HTML.
	  //el: $("#todoapp"),

	  // Our template for the line of statistics at the bottom of the app.
	  //statsTemplate: _.template(statsTemplate),

	  // Delegated events for creating new items, and clearing completed ones.
	  events: {
		  "keypress #new-todo":  "createOnEnter",
		  "keyup #new-todo":     "showTooltip",
		  "click .todo-clear a": "clearCompleted",
		  "click .mark-all-done": "toggleAllComplete"
	  },
    
    // At initialization we bind to the relevant events on the `Todos`
    // collection, when items are added or changed. Kick things off by
    // loading any preexisting todos that might be saved in *localStorage*.
    initialize: function() {
      _.bindAll(this, 'addOne', 'addAll', 'render', 'toggleAllComplete');

      this.input    = this.$("#new-todo");
      this.allCheckbox = this.$(".mark-all-done")[0];

      Todo.TodoCollection.bind('add',     this.addOne);
      Todo.TodoCollection.bind('reset',   this.addAll);
      Todo.TodoCollection.bind('all',     this.render);
      Todo.TodoCollection.fetch();
      //this.collection = Todo.TodoCollection;
    },
    render: function(done) {
    	var view = this;

    	var todosDone = Todo.TodoCollection.done().length;
    	var remaining = Todo.TodoCollection.remaining().length;
    	/*this.$('#todo-stats').html(this.statsTemplate({
        total:      Todo.TodoCollection.length,
        done:       todosDone,
        remaining:  remaining
      }));*/
    	namespace.fetchTemplate("app/templates/stats.html", function(tmpl2) {
    		var statsjson = {
    	        total:      Todo.TodoCollection.length,
    	        done:       todosDone,
    	        remaining:  remaining
    	      };
    		var htmltext = tmpl2(statsjson);
    		//(JSON.stringify(htmltext));
    		//view.statsEl = view.$('#todo-stats');
    		$('#todo-stats').html(htmltext);
    	});

    	// If a done function is passed, call it with the element
		if (_.isFunction(done)) {
			done(view.el);
			view.input    = view.$("#new-todo");
			view.allCheckbox = view.$(".mark-all-done")[0];
			//view.allCheckbox.checked = !remaining;
		}
		//view.addAll();
    }, 
    

    // Add a single todo item to the list by creating a view for it, and
    // appending its element to the `<ul>`.
    addOne: function(todo) {
      var view = new Todo.Views.TodoItem({model: todo});
      //var todoItemEl = view.render().el;
      //this.$("#todo-list").append(todoItemEl);
      //console.log("add one: " + JSON.stringify(todo));
      view.render(function(el) {
          $('#todo-list').append(el);
        });
    },

    // Add all items in the **Todo.TodoCollection** collection at once.
    addAll: function() {
      Todo.TodoCollection.each(this.addOne);
    },

    // Generate the attributes for a new Todo item.
    newAttributes: function() {
    	//console.log("input: " + this.input.val());
      return {
        content: this.input.val(),
        order:   Todo.TodoCollection.nextOrder(),
        done:    false
      };
    },

    // If you hit return in the main input field, create new **Todo** model,
    // persisting it to *localStorage*.
    createOnEnter: function(e) {
      if (e.keyCode != 13) return;
      Todo.TodoCollection.create(this.newAttributes());
      this.input.val('');
    },

    // Clear all done todo items, destroying their models.
    clearCompleted: function() {
      _.each(Todo.TodoCollection.done(), function(todo){ todo.clear(); });
      return false;
    },

    // Lazily show the tooltip that tells you to press `enter` to save
    // a new todo item, after one second.
    showTooltip: function(e) {
      var tooltip = this.$(".ui-tooltip-top");
      var val = this.input.val();
      tooltip.fadeOut();
      if (this.tooltipTimeout) clearTimeout(this.tooltipTimeout);
      if (val == '' || val == this.input.attr('placeholder')) return;
      var show = function(){ tooltip.show().fadeIn(); };
      this.tooltipTimeout = _.delay(show, 1000);
    },

    // Change each todo so that it's `done` state matches the check all
    toggleAllComplete: function () {
      var done = this.allCheckbox.checked;
      Todo.TodoCollection.each(function (todo) { todo.save({'done': done}); });
    }
    
  });
  
  Todo.Views.TodoItem = Backbone.View.extend({

	    //... is a list tag.
	    tagName:  "li",

	    // Cache the template function for a single item.
	    //template: _.template(todosTemplate),
	    template: "app/templates/todos.html",

	    // The DOM events specific to an item.
	    events: {
	      "click .check"              : "toggleDone",
	      "dblclick div.todo-content" : "edit",
	      "click span.todo-destroy"   : "clear",
	      "keypress .todo-input"      : "updateOnEnter",
	      "blur .todo-input"          : "close"
	    },

	    // The TodoView listens for changes to its model, re-rendering. Since there's
	    // a one-to-one correspondence between a **Todo** and a **TodoView** in this
	    // app, we set a direct reference on the model for convenience.
	    initialize: function() {
	      _.bindAll(this, 'render', 'close', 'remove');
	      this.model.bind('change', this.render);
	      this.model.bind('destroy', this.remove);
	    },


	    // Re-render the contents of the todo item.
	    render: function(done) {
	    	var view = this;
	    	var json = this.model.toJSON();
	    	//$(this.el).html(this.template(json));

	    	// Fetch the template, render it to the View element and call done.
	    	namespace.fetchTemplate(this.template, function(tmpl) {
	    		//var json = this.model.toJSON();
	    		var texttmpl = tmpl(json);
	    		view.el.innerHTML = texttmpl;
	    		view.input = view.$('.todo-input');
	    		// If a done function is passed, call it with the element
	    		if (_.isFunction(done)) {
	    			done(view.el);
	    		}
	    		//console.log("first return of " + JSON.stringify(json));
	    		//return view.el;
	    	});
	    	//console.log("second return of " + JSON.stringify(json));
	    	//return view.el;
	    },

	    // Toggle the `"done"` state of the model.
	    toggleDone: function() {
	      this.model.toggle();
	    },

	    // Switch this view into `"editing"` mode, displaying the input field.
	    edit: function() {
	      $(this.el).addClass("editing");
	      this.input.focus();
	    },

	    // Close the `"editing"` mode, saving changes to the todo.
	    close: function() {
	      this.model.save({content: this.input.val()});
	      $(this.el).removeClass("editing");
	    },

	    // If you hit `enter`, we're through editing the item.
	    updateOnEnter: function(e) {
	      if (e.keyCode == 13) this.close();
	    },

	    // Remove the item, destroy the model.
	    clear: function() {
	    	this.model.clear();
	    }

	  });

  // Required, return the module for AMD compliance
  return Todo;

});
