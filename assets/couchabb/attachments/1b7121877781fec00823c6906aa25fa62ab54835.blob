var FORMY = {};
function findSyncpointLocalDb() {
	var id = null;
	var local_db_name = null;
	
	$.getJSON('/sp_admin/_design/control/_view/by_type?key=%22installation%22', function(data) { 
		var record = null;
		$.each(data, function(key, val) {
			if (key == "rows") {
				record = val;
				id = record[0].id;
			}
		});
		if (record != null) {
			//console.log("record: " + JSON.stringify(record));
			console.log("id: " + id);
			$.getJSON('/sp_admin/' + id, function(data) {
				//console.log("data: " + JSON.stringify(data));
				local_db_name = data.local_db_name;
				FORMY.SyncpointLocalDb = local_db_name;
				console.log("local_db_name: " + FORMY.SyncpointLocalDb);
			});
		}
	});
}