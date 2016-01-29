<?php
	Class db_model {
		public $db_host = "DB_HOST";
		public $db_user = "DB_USER";
		public $db_pass = "DB_PASS";
		public $db_name = "DB_NAME";

		function init() {
			mysql_connect($db_host, $db_user, $db_pass);
			mysql_select_db($db_name);
			mysql_query("set names utf8");
		}

		function device_management($data) {
			foreach ($data as $key => $value) {
				if (preg_match("/[#\&\\+\-%@=\/\\\;,\.\'\"\^`~\_|\!\/\?\*$#<>()\[\]\{\}]/i", $value,  $match)) {
					exit();
				}
			}
			// create table client_data(idx varchar(300), train_number int(20), time_departure varchar(6),
			// time_arrive varchar(6), city_departure varchar(10), city_arrival varchar(10), 
			// gcm_clients Text, status varchar(10));
			$check = mysql_query("SELECT train_number, gcm_clients from client_data where" .
				"time_departure = '" . $data["time_departure"] . "' and " .
				"time_arrive = '" . $data["time_arrive"] . "' and " .
				"city_departure = '" . $data["city_departure"] . "' and ".
				"city_arrival = '" . $data["city_arrival"] . "' and ".
				"train_number = '" . $data["train_number"] . "' and ".
				"status = 'running'");

			if ($check) { // already have daemon
				while ($row = mysql_fetch_array($check)) {
					$gcm_json = json_decode($row["gcm_clients"], true);
				}
				$device_lists = array();
				foreach ($gcm_json as $value) {
					if ($value == $data["gcm_key"]) {
						$status = "false"; // already device added
					}
				}
				if ($status == "true") { // new device added
					array_push($gcm_json, $data["gcm_key"]); 
					$change = json_encode($gcm_json);
					$var_tn = $data["train_number"];
					$var_time = $data["time_departure"];
					mysql_query("UPDATE client_data set gcm_clients = '$change' where train_number = '$var_tn' and time_departure = '$var_time'");
				}
			} else {
				$status = "new daemon added"; 
				$arr_gcm = array($data["gcm_key"]);
				$change = json_encode($arr_gcm);
				$idx = uniqid('idx', true);
				$query = mysql_query("INSERT into client_data(idx, train_number, time_departure, time_arrive, city_departure, city_arrival, gcm_clients, status)
									  VALUES('$idx', '" . $data["train_number"] . "', '" . $data["time_departure"] . "', '" . 
									  $data["time_arrive"] . "', '" . $data["city_departure"] . "', '" . $data["city_arrival"] . "', '" . $change . "', 'running')");

			}

		}

	}
	
	Class RestFunctions {
		function curl_req($url) {
		    $ch = curl_init(); 

	        // set url 
	        curl_setopt($ch, CURLOPT_URL, $url); 

	        //return the transfer as a string 
	        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1); 

	        // $output contains the output string 
	        $output = curl_exec($ch); 

	        // close curl resource to free up system resources 
	        curl_close($ch); 
		}
		function sendGoogleCloudMessage( $data, $ids ) {
		    $apiKey = 'SERVER_KEY';
		    $url = 'https://gcm-http.googleapis.com/gcm/send';
		    $post = array(
		        'registration_ids'  => $ids,
		        'data'              => $data,
	        );
		    $headers = array( 
	            'Authorization: key=' . $apiKey,
	            'Content-Type: application/json'
	        );
		    $ch = curl_init();
		    curl_setopt($ch, CURLOPT_URL, $url );
		    curl_setopt($ch, CURLOPT_POST, true );
		    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers );
		    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true );
		    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode( $post ) );
		    $result = curl_exec( $ch );
		    if ( curl_errno( $ch ) ) {
		        echo 'GCM error: ' . curl_error( $ch );
		    }
		    curl_close( $ch );
		    //echo $result;
		}
		public function showmessage($intv) {
			echo "success";
			sleep($intv);
		}

		public function newTicketListener($departure, $arrive, $date, $hourInt, $expire, $train_number) {
			$do = $this->train_check($departure, $arrive, $date, $hourInt, "true");

			$count = 0;
			// print_r($do[0]);
			// echo json_encode($do);
			while ($count <= $expire - 1) {
				foreach ($do as $key => $value) {
					if ($value["train_number"] == $train_number) {
						$status = $value["status"];
						if ($status == "예약하기") { // 자리 있음 
							// $this->device_gcm($gcm_key, $title, $message, $train_num, $start_location, $start_time, $date);
							$content = "[changed] status = $status , train_num = $train_number , count : $count";
							file_get_contents("https://api.telegram.org/bot132762052:API_TOKEN_BOT/sendMessage?chat_id=46867995&text=$content");
							exit();
						} else { // 자리 없음
							$content = "status = $status , train_num = $train_number , count : $count";
						}
					}
					
				}
				
				$count++;
				sleep(1);
			}
			$m["success"] = "true";
			$m["data"] = $do;
			$m["content"] = $content;
			echo json_encode($m);
			//echo $departure . "," . $arrive . ',' . $date .','. $hourInt . ',' . $expire;
		}
		
		public function device_gcm($arr) { // 보여주기식
			$server_key = "GCM_SERVER_KEY";
			//"출발시간, 열차날짜, 기차번호 예약 가능합니다"

			// 1~399 KTX
			// 1000~1199 새마을
			// 1200 무궁화
			$train = $arr["train_num"];

			switch ($train) {
				case $train < 400:
					$type = "KTX";
					break;
				case $arr["train_num"] >= 1000:
					if ($arr["train_num"] < 1200) {
						$type="새마을";
					} else {
						$type="무궁화호"; 
					}
					break;
			}


			$data = array('title'=> $type ." " . $arr["start_location"] ."->" . $arr["dest_location"] . " 티켓 알림" ,  'message' => $arr["start_time"] . " 에 " .  $arr["start_location"] . "에서 출발 " . $arr["train_num"] . "번 예약 가능");
			$this->sendGoogleCloudMessage($data, array($arr["gcm_key"]));
		}

		public function add_device($gcmKey) {			
			// $results  = mysql_query("SELECT * from gcm_tables");

			// while ($row = mysql_fetch_array($results)) {
			// 	$idx = $row["idx"] + 1;
			// }

			// $result = mysql_query("INSERT into gcm_tables(idx, regid, status) VALUES('$idx', '$gcmKey', 'loaded')");
			// $m["success"] = "true";
			// $m["message"] = "success to regist device";
			// echo json_encode($m);
		}

		public function remove_device($gcmKey) {
			// $query = mysql_query("DELETE from gcm_tables where regid = '$gcmKey'");
			// if ($query) {
			// 	$m["success"] = "true";
			// 	$m["message"] = "success to delete device";
			// }
			// echo json_encode($m);
		}


		public function gcm_send($gcm_key) {
			$server_key = "SERVER_GCM";
			$data = array('title'=>"티켓봇 알림",  'message' => 'sdfsdf');
			$this->sendGoogleCloudMessage($data, array($gcm_key));
		}

		public function train_check($departure, $arrive, $date, $hourInt, $printall) {
			$ch = curl_init();
			$datas = "txtGoStartCode=&txtGoEndCode=&radJobId=1&selGoTrain=05&txtSeatAttCd_4=015&txtSeatAttCd_3=000&txtSeatAttCd_2=000&txtPsgFlg_2=0&txtPsgFlg_3=0&txtPsgFlg_4=0&txtPsgFlg_5=0&chkCpn=N&selGoSeat1=015&selGoSeat2=&txtPsgCnt1=1&txtPsgCnt2=0&selGoRoom=&useSeatFlg=&useServiceFlg=&checkStnNm=Y&txtMenuId=11&SeandYo=N&txtGoStartCode2=&txtGoEndCode2=&hidEasyTalk=&";
			
			$date_departure = date("Y.n.d", strtotime($date));
			if ($printall) {
				$OptprintAll = "true";
			}
			$year = date("Y", strtotime($date));
			$month = date("m", strtotime($date));
			$day = date("d" , strtotime($date));
			$yoil = array("일","월","화","수","목","금","토");
			$set_yoil = $yoil[date('w', strtotime($day)) -1 ];
			$set_date = date("Ymd" , strtotime($date));
			$datas .= "txtGoStart=$departure&txtGoEnd=$arrive&start=$date_departure&selGoHour=00&selGoYear=2016&selGoMonth=$month&selGoDay=$day&txtGoYoil=$set_yoil&txtGoAbrdDt=$set_date&txtPsgFlg_1=1";
			$pages .= "&txtGoPage=1";
			if ($date == date("Y-m-d")) { // If Ticket is today
				$hour = date("H");
				$minute = date("i");
				// 170900
				if ($minute < 10) {
					$minute = "0" . $minute;
				}
				$txtToHour = $hour . $minute . "00";
				$datas .= "&txtGoHour=". $txtToHour;
			}

			if ($hourInt && !$txtToHour) {
				$datas .= "&txtGoHour=". $hourInt . "0000";
			}

			curl_setopt($ch, CURLOPT_URL,"URL_TRAIN_SITE");
			curl_setopt($ch ,CURLOPT_POST, sizeof($datas));
			curl_setopt($ch, CURLOPT_POSTFIELDS, $datas);
			curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
			$server_output = curl_exec ($ch);
			curl_close ($ch);
			
			$html = str_get_html($server_output);

			$theads = $html->find("thead",0);
			$do = "";

			foreach ($theads as $key) {
				$do .= $key;
			}
			
			$doing = str_get_html($do);
			$ss = "";
			$json_array= array();

			foreach ($doing->find("tr") as $key) {
				if ($key->find("a", 0)->plaintext) {
					$atag = $key->find("a", 0)->plaintext;
				}

				$exp = explode("<td>", $key);
				if (isset($exp[2]) && isset($exp[3]) && isset($exp[5])) {
					$exps = explode(":", $exp[2]);
					$start_city = preg_replace("/([0-9])/", "", $exps[0]);
					preg_match("/([0-9]{2}:[0-9]{2})/", $exp[2], $save);
					preg_match("/([0-9]{2}:[0-9]{2})/", $exp[3], $save2);
					$start_time = $save[1];
					$exps = explode(":", $exp[3]);
					$end_city = preg_replace("/([0-9])/", "", $exps[0]);
					$end_time = $save2[1];
					$exp2 = explode("'", $exp[5]);
					$where = "false";
					if ($where == "false") { // 리스트 처리
						if (preg_match("/btnRsv/i", $exp2[3])) {
							//$m["status"] = $exp2[7];
							$status = $exp2[7];
						} else {
							$status = $exp2[3];
							

						}
						error_reporting(0);
						if ($OptprintAll) {
							$m["status"] = $status;
							$m["train_number"] = trim($atag);
							$m["city_departure"] = str_replace('<br/>', '', trim($start_city));
							$m["city_arrival"] = str_replace('<br/>', '', trim($end_city));
							$m["time_departure"] = str_replace('<br/>', '', trim($start_time));
							$m["time_arrive"] = str_replace('<br/>', '', trim($end_time));
							$m["dev_parameter"] = $datas;
							array_push($json_array, $m);
						} else {
							switch ($status) {
								case "좌석매진":
									$m["status"] = $status;
									$m["train_number"] = trim($atag);
									$m["city_departure"] = str_replace('<br/>', '', trim($start_city));
									$m["city_arrival"] = str_replace('<br/>', '', trim($end_city));
									$m["time_departure"] = str_replace('<br/>', '', trim($start_time));
									$m["time_arrive"] = str_replace('<br/>', '', trim($end_time));
									$m["dev_parameter"] = $datas;
									array_push($json_array, $m);
									break;
							}
						}
						
					
						
					} else { // 단일 처리
						if ($atag == $where_trainNum && str_replace('<br/>', '', trim($end_time)) == $where_time ) { // 값 처리

							if ($status == "좌석매진") {
								$m["success"] = "true";
								$m["status"] = "No_ticket";
							} else {
								$m["success"] = "true";
								$m["status"] = "Ticket_find";
							}
							exit();
						} else {
							$m["success"] = "false";
							$m["message"] = "No Train Information";
						}
					}

									
				}
				
			}
			if (!$json_array) {
				$m["success"] = "false";
				$m["message"] = "No Tickets.";
				//echo json_encode($m);
			} else {
				return $json_array;
			}
		}

		public function train_list($departure, $arrive, $date, $hourInt, $where="false" , $where_trainNum="NULL", $where_time="NULL") {
			$result = $this->train_check($departure, $arrive, $date, $hourInt);
			echo json_encode($result);
			return $result;
		}
	}

	$api = $_GET["api"];
	date_default_timezone_set('Asia/Seoul');

	include_once("simple_html_dom.php");
	
	switch ($api) {
		case "request": // 매진 티켓 루프 돌리기
			$rest = new RestFunctions();
			$data["train_number"] = $_GET["train_num"];
			$data["train_start"] = $_GET["start"];
			$data["train_fin"] = $_GET["fin"];
			$data["time"] = $_GET["time"];
			$data["gcm_key"] = $_GET["gcm_key"];




			//check_ticket($train_number, $str, $fin, $time, $gcm_key);
			$rest->gcm_send($_GET["gcm_key"]);
			break;
		case "list":
			//error_reporting(0);
			$rest = new RestFunctions();
			$departure = $_GET["departure"];
			$arrive = $_GET["arrive"];
			$date = $_GET["date"];
			$trainNum = $_GET["trainNum"];
			$time = $_GET["traintime"];

			$hourInt = $_GET["hour"];

			if ($trainNum) {
				$rest->train_list($departure, $arrive, $date, $hourInt, "true", $trainNum, $time);	
			} else {
				$rest->train_list($departure, $arrive, $date, $hourInt);
			}
			
			break;
		case "ticket_check": // status check
			$gcmKey = $_GET["gcm_key"];
			break;

		case "add_device":
			$rest = new RestFunctions();
			$gcmKey = $_GET["gcmKey"];
			$rest->add_device($gcmKey);
			break;
		case "delete_device":
			$rest = new RestFunctions();
			$gcmKey = $_GET["gcmKey"];
			$rest->remove_device($gcmKey);
			break;

		case "newTicketListener":
			$departure = $_GET["departure"];
			$arrive = $_GET["arrive"];
			$date = $_GET["date"]; 
			$hourInt = $_GET["hourInt"]; 
			$expire = $_GET["expire"];
			$train_number = $_GET["train_number"];
			$rest = new RestFunctions();
			//newTicketListener($departure, $arrive, $date, $hourInt, $expire)
			$rest->newTicketListener($departure, $arrive, $date, $hourInt, $expire, $train_number);
			break;

		case "device_gcm":
			$rest = new RestFunctions();
			$db = new db_model();
			$db->init();
			$m["gcm_key"] = $_GET["gcm_key"];
			$m["train_num"] = $_GET["train_num"];
			$m["start_location"] = $_GET["start_location"];
			$m["dest_location"] = $_GET["dest_location"];
			$m["start_time"] = $_GET["start_time"];
			$m["expire_seconds"] = $_GET["expire_seconds"];
			$rest->showmessage($m["expire_seconds"]);

			

			//$gcm_key, $title="티켓봇 알림", $message="남는 티켓 알림", $train_num, $start_location, $dest_location 
			// echo json_encode($m) 
			$rest->device_gcm($m); 
			
			break;
	}
	
?>
