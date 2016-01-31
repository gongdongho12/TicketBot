var cluster = require('cluster');
var assert = require('assert');
var suspend = require("suspend");

callback = function(response) {
	var str = '';

	response.on('data', function (chunk) {
		str += chunk;
	});

	response.on('end', function () { // when data received completed
		console.log(str);

	});

}

w_callback = function() {
	var str = '';

	response.on('data', function (chunk) {
		str += chunk;
	});

	response.on('end', function () { // when data received completed
		var response = str;
		console.log(response);

	});

}



if (cluster.isMaster) {
    var cpuCount = require('os').cpus().length;
    for (var i = 0; i < cpuCount; i += 1) {
        cluster.fork();
    }
    cluster.on('exit', function (worker) {
        console.log('Worker %d died :(', worker.id);
        cluster.fork();
    });
} else {
    var express = require('express');
    var app = express();

	app.get("/request_gcm", function (request, response) {
		var http = require("request");
		// var train = request.query.train_num;
		// var start_loc = request.query.start_location;
		// var dest_loc = request.query.dest_location;
		// var start_time = request.query.start_time;

		// var gcm_key = request.query.gcm_key;
		// var title = request.query.title;
		// var message = request.query.message;
		// var date = request.query.date;

	
		var headers = {
		    'User-Agent':    'Super Agent/0.0.1',
		    'Content-Type':  'application/x-www-form-urlencoded'
		}
	
		var options = {
		    url: 'http://localhost:44444/KMU/test1.php',
		    method: 'GET',
		    headers: headers,
		    qs: {'api': 'device_gcm', 'gcm_key': request.query.gcm_key , 'train_num' : request.query.train_num , 'start_location' : request.query.start_location, 'dest_location' : request.query.dest_location, 'start_time' : request.query.start_time, 'expire_seconds' : request.query.expire_seconds}
		}

		http(options, function (error, response, body) {
		    if (!error && response.statusCode == 200) {
		        // Print out the response body
		        console.log(body)
		    }
		});

		response.send({"success" : true, "message": "sent gcm" , "delay_seconds" : 15 , "options" : options});
	});
	
	
	
    app.get('/', function (request, response) {
        console.log('Request to worker %d', cluster.worker.id);
        response.send('TicketBot Server with NodeJS server' + cluster.worker.id);
    });
    
    app.get("/request_ticket", function (request, response) {
    	// response.send('Request Ticket' + cluster.worker.id);
    	var departure = request.query.departure;
    	var arrive = request.query.arrive;
    	var date = request.query.date;
    	var hour = request.query.hour;
    	var expire = request.query.expire;
    	var train_num = request.query.train_number;

    	response.send("request Successed ");
    	ticket_request(departure, arrive, date, hour, 3600, train_num);
    	
    });

    app.listen(50005);
    console.log('Worker %d running!', cluster.worker.id);

}

function sleep(time, callback) {
    var stop = new Date().getTime();
    while(new Date().getTime() < stop + time) {

    }
    callback();
}

function request_http() {
	
}


function ticket_request(departure, arrive, date, hour, expire, train_num) {
	var http = require("request");
 	var counter = 0;
	var options = {
	    url: 'PHP FILE ticket-restapi.php',
	    method: 'GET',
	    qs: {'api': 'newTicketListener', 'departure' : departure , 'arrive' : arrive , 'date' : date, 'hour':hour, 'expire': expire , 'train_number' : train_num}
	}

	http(options, function (error, response, body) {
	    if (!error && response.statusCode == 200) {
	        //console.log(body)
	    }
	});

 	// test1newTicketListener

	
}
