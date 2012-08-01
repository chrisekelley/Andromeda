var accountListing = function() {
	window.AccountList.get(
            {
                type: 'com.google' // if not specified get all accounts
                                     // google account example: 'com.google'
            }, 
            function (result) {
            	console.log("result length: " + result.length);
            	var accountList = "";
            	for (i in result) {
            		//console.log(result[i]);
            		var account = result[i];
            		//alert("Account: " + account);
            		accountList = accountList + '<a href="#" class="btn large" onclick="selectAccount(\'' + account + '\');">' + account + '</a><br/>';;
            	}
            	document.getElementById("accountInfo").innerHTML = accountList;
            	//document.getElementById("accountInfo").innerHTML = "Howdy!";
            	//alert("accountList:\n" + accountList);
            },
            function (error) {
                console.log(error);
            }
        );
};

function selectAccount(account) {
	//alert(account + " selected");
	window.AccountSave.get(
			{
				name: account	
			}, 
			function (result) {
				console.log("result length: " + result.length);
				alert("Success: " + result + " registered.");
			},
			function (error) {
				console.log(error);
			}
	);
}

/*function onDeviceReady() {
	window.AccountList.get(
			{
				type: 'com.google' // if not specified get all accounts
					// google account example: 'com.google'
			}, 
			function (result) {
				console.log(result.length);
				for (i in result)
					console.log(result[i]);
				var account = result[i];
				alert("Account: " + result[i]);
			},
			function (error) {
				console.log(error);
			}
	);
}*/

function init() {
    // the next line makes it impossible to see Contacts on the HTC Evo since it
    // doesn't have a scroll button
    // document.addEventListener("touchmove", preventBehavior, false);
    document.addEventListener("deviceready", accountListing, true);
}