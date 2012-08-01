var AccountList = function() {};
			
AccountList.prototype.get = function(params, success, fail) {
	return cordova.exec( function(args) {
		success(args);
	}, function(args) {
		fail(args);
	}, 'AccountList', '', [params]);
};

//cordova.addConstructor(function() {
//	cordova.addPlugin('AccountList', new AccountList());
//});

window.AccountList = new AccountList();

var AccountSave = function() {};

AccountSave.prototype.get = function(params, success, fail) {
	return cordova.exec( function(args) {
		success(args);
	}, function(args) {
		fail(args);
	}, 'AccountSave', '', [params]);
};

//cordova.addConstructor(function() {
//	cordova.addPlugin('AccountList', new AccountList());
//});

window.AccountSave = new AccountSave();
