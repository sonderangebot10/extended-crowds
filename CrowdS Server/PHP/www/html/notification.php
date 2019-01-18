<?php

class Notification {
 
    // notification message title
    private $title;
    private $body;
    private $icon;
    // push message payload
    private $notification;
 
    function __construct() {
         
    }
 
    public function setTitle($title) {
        $this->title = $title;
    }
 
    public function setBody($body) {
        $this->body = $body;
    }
 
    public function setIcon($imageUrl) {
        $this->icon = $imageUrl;
    }
    
    public function getNotification() {
        $res = array();
        $res['body'] = $this->body;
        $res['title'] = $this->title;
        $res['icon'] = $this->icon;
        return $res;
    }
 
}
?>