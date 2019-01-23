<?php

class VCG implements TaskAllocationInterface{

    private $users;
    private $dbc;
    private $cost = 0;
    
    public function __construct($users){
        $this->users = $users;
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
    public function getCost(){
        return $this->cost;
    }
    
		// Use a reversed VCG auction to get providers
    public function getUsers(){
        
        $n = count($this->users);

				// if the number of possible providers are less or equal to number needed,
				// return them all
        if($n <= MAX_DEVICES){
            $this->cost = $this->users[0]['bid'];
            return $this->users;
        }
        
        // sort by bid
        foreach($this->users as $key => $val) {
            $bid[$key] = $val['bid'];
            $time[$key] = $val['login_time'];
        }
        array_multisort($bid, SORT_ASC,
                        $time, SORT_ASC,
                        $this->users);
        
        $tmp = array_slice($this->users,0,MAX_DEVICES);
        
        $this->cost = $this->users[MAX_DEVICES]['bid'];
        
        return $tmp;
        
    }
}

?>