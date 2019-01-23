<?php

class MultipleChoice implements CreateTaskInterface {
    private $email;
    private $description;
    private $cost;
    private $lng;
    private $lat;
    private $question;
    private $answer_choices;
    private $hit_type;
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
        $this->question = $fields["question"];
        $this->answer_choices = $fields["answer_choices"];
        $this->hit_type = $fields["hit_type"];
        $this->distributed = $fields["distributed"];
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function insertTask($group){
        
        $members = implode(";",array_column($group, 'email'));
        
        // insert new hit task into db
        $this->dbc->insertHumanTask(array("task_giver", "task_type", "description", "latitude", "longitude", "question", "answer_choices", "distributed", "members", "cost"), array($this->email, $this->hit_type, $this->description, $this->lat, $this->lng, $this->question, $this->answer_choices, $this->distributed, $members, $this->cost));

        $this->last_id = $this->dbc->getLastId();
        $this->dbc->updateUser($this->email, array("hits_created"), array("TRIM(TRAILING ';' FROM CONCAT('$this->last_id;',IFNULL(hits_created,'')))"));
        return $this->last_id;
    }
    
		// send data back with firebase
    public function sendData($group){
				// create payload 
        $payload = array();
        $payload['type'] = 'hit'; 
        $payload['question'] = $this->question;
        $payload['choices'] = $this->answer_choices;
        $payload['hit_type'] = $this->hit_type;       
        
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