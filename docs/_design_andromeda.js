{
   "_id": "_design/andromeda",
   "language": "javascript",
   "views": {
       "byAppUsers": {
           "map": "function (doc) {\n        if (doc.app_id && doc.control_database) { // multi channel mode\n          emit(doc.full_name,doc.control_database)\n        }\n      }"
       }
   }
}