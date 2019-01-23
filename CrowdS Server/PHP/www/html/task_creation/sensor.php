<?php

class Sensor implements CreateTaskInterface {
    private $email;
    private $description;
    private $cost;
    private $lng;
    private $lat;
    private $sensor;
    private $duration;
    private $readings;
    private $distributed;
    private $last_id;
    private $dbc;
    
    public function __construct($fields){
				// parse fields
        $this->email = $fields["email"];
        $this->description = $fields["description"];
        $this->cost = $fields["cost"];
        $this->lng = $fields["lng"];
        $this->lat = $fields["lat"];
        $this->sensor = $fields["sensor"];
        $this->duration = $fields["duration"];
        $this->readings = $fields["readings"];
        $this->distributed = $fields["distributed"];
                
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
        
    }
    
    public function insertTask($group){
        
        $members = implode(";",array_column($group, 'email'));
        
        // insert new sensor task into db
        $this->dbc->insertSensorTask(array("task_giver", "description", "latitude", "longitude", "sensor", "duration", "readings", "distributed", "members", "cost"), array($this->email, $this->description, $this->lat, $this->lng, $this->sensor, $this->duration, $this->readings, $this->distributed, $members, $this->cost));

        $this->last_id = $this->dbc->getLastId();
        $this->dbc->updateUser($this->email, array("sensing_created"), array("TRIM(TRAILING ';' FROM CONCAT('$this->last_id;',IFNULL(sensing_created,'')))"));
        
        return $this->last_id;
        
    }
		
		// send data back with firebase
    public function sendData($group){
				// create payload
        $payload = array();
        $payload['type'] = 'sensor';
        $payload['sensor'] = $this->sensor;
        $payload['duration'] = $this->duration;
        $payload['readings'] = $this->readings;
        
        $firebase = new Firebase();
        $push = new Push();

        $title = "";
        $message = "";

        $push->setTitle($title);
        $push->setMessage($message);
        $push->setIsBackground(FALSE);

        for ($i = 0; $i < count($group); $i++) {
            $payload['id'] = $this->last_id.':'.$i;
            $push->setPayload($payload);
            $json = $push->getPush();
            $firebase->send($group[$i]['firebase'], $json);
        }
    }
		
		// send notification message about new task
    public function sendNotification($group){
        $title = "New task!";
        $body = "Check the app now!";
        
        $firebase = new Firebase();
        $notification = new Notification();

        $notification->setTitle($title);
        $notification->setBody($body);

        $json = $notification->getNotification();

        for ($i = 0; $i < count($group); $i++) {
            $response = $firebase->sendNotification($group[$i]['firebase'], $json);
        }   
    }
}

?>