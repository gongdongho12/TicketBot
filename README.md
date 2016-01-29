# Ticketbot
국민대 16학번 예비대학 해커톤

## File Description

simple_html_dom.php - Parse Library 
ticket-restapi.php - Ticket Daemon, REST
ticket.js - Ticket Requester (Node.js)

## Requirements

PHP Web Server , Node.js
node.js - cluster, assert, suspend, request, express
php >= 5.3.3 (PHP7 Support)


## Request Ticket Guide (Web Server required contains php files)
```
  node ticket.js // example Port 50005
  localhost:50005/request_ticket?departure={departure_location}&arrive={arrive_location}&date={date 2016-01-12}&hour={01~24}&expire={seconds}&train_num{train_number}
```


## Contact
Contact : support@rainclab.net
