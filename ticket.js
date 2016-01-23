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
		var gcm_key = request.query.gcm_key;
		var title = request.query.title;
		var message = request.query.message;
		//http://1.214.121.11:44444/KMU/test1.php?api=device_gcm&gcm_key=cMWRzzbEGiCe&title=%ED%97%A4%ED%97%A4%ED%97%A4&message=%ED%9D%90%EC%95%84%EC%95%94		
		var headers = {
		    'User-Agent':       'Super Agent/0.0.1',
		    'Content-Type':     'application/x-www-form-urlencoded'
		}
		var options = {
		    url: 'http://localhost:44444/KMU/test1.php',
		    method: 'GET',
		    headers: headers,
		    qs: {'api': 'device_gcm', 'gcm_key': gcm_key , 'title': title , 'message' : message}
		}

		http(options, function (error, response, body) {
		    if (!error && response.statusCode == 200) {
		        // Print out the response body
		        console.log(body)
		    }
		});

		response.send({"success" : true, "message": "sent gcm" , "delay_seconds" : 15});
	});

    app.get('/', function (request, response) {
        console.log('Request to worker %d', cluster.worker.id);
        response.send('TicketBot Server with NodeJS server' + cluster.worker.id);
    });
    
    app.get("/request_ticket", function (request, response) {
    	// response.send('Request Ticket' + cluster.worker.id);
    	var gcm_key = request.query.gcm_key;
    	var train_number = request.query.train_num;
    	var date = request.query.date; 
    	var dep_time = request.query.dep_time;
    	var city_departure = request.query.city_departure;
    	var city_arrival = request.query.city_arrival;
    	response.send("request Successed " + gcm_key + "," + train_number + "," + dep_time + "," + city_departure + "," + city_arrival + ", expireSecond : 3600");
    	ticket_request(gcm_key, train_number, dep_time, city_departure, city_arrival, date, 4);
    	
    });

    app.listen(50005);
    console.log('Worker %d running!', cluster.worker.id);

}

function sleep(time, callback) {
    var stop = new Date().getTime();
    while(new Date().getTime() < stop + time) {
        ;
    }
    callback();
}

function request_http() {
	
}

function never_call () {
	console.log("You should never call this function");
}


function ticket_request(gcm_key, train_num, dep_time, city_dep, city_arrival, dep_date, expire_time) {
	var request = require('request');
 	var counter = 0;
 	
	while (counter <= expire_time) {
		console.log(a);
		sleep(1000, function() {
			console.log("Sleeped");
		});

		counter++;
		console.log("Counter Up ");
	}
	
}