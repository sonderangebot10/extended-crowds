<?php
define('QUALITY_MIN', 10);

class MajorityVoting implements QualityControlInterface{
    
    private $participants;
    private $task_id;
    private $task_type;
    private $choices;
    private $data;
    private $users;
    private $majority_correct;
    private $quality;
    private $dbc;
    
    public function __construct($task_data){
				// parse task data
        $this->participants = explode(';', $task_data['participants']);
        $this->task_id = $task_data['id'];
        $this->task_type = $task_data['type'];
        $this->choices = explode(';', $task_data['choices']);
        $this->data = explode(';', $task_data['data']);
        $this->users = array();
        
        $this->dbc = new DatabaseController();
        $this->dbc->connect();
    }
    
		// will return 1 if anyone participated 
    public function anyoneParticipated(){
        return $this->participants;
    }
    
    public function calculateQuality(){
        
        // save user emails for later use
        foreach($this->participants as $p){
            list($index, $email) = explode(':', $p);
            $this->users[$index] = $email;
        }
        
        // number of times an option been chosen
        $options_count = array();
        // all options in a set
        $options = new \Ds\Set();
        
        foreach($this->choices as $c){
            $options_count[$c] = 0;
            $options->add($c);
        }

        $chosen_options = array();
        $not_chosen_options = array();

        // parse task data
        // User1Data;User2Data;...;UserNData
        foreach($this->data as $v){
            //UserNData = index:Choices
            list($index, $choices) = explode(':', $v);
            
            //Choices = c1_c3_c6
            $choice = explode('_', $choices);

            //store all chosen options in a set
            //count number of times an option been chosen
            $set = new \Ds\Set();
            foreach($choice as $c){
                $options_count[$c] = $options_count[$c]+1; 
                $set->add($c);
            }
            $chosen_options[$index] = $set;
            $not_chosen_options[$index] = $options->diff($set);
        }

        // inferre truth
        // find out which option(s) was chosen
        // by the majority
        $this->majority_correct = new \Ds\Set(array_keys($options_count, max($options_count)));

        // as well as which options that was not part of that majority
        $majority_incorrect = $options->diff($this->majority_correct);

        // if two or more options get chosen the same
        // number of times, chose one randomly
        if($this->task_type == "single"){
            if(count($this->majority_correct) > 1){
                $i = rand(0, count($this->majority_correct)-1);
                $this->majority_correct = new \Ds\Set([$this->majority_correct[$i]]);
            }
            else $this->majority_correct = new \Ds\Set([$this->majority_correct[0]]);
        }

        // calculate quality
        for ($i = 0;$i <= count($this->users); $i++){
            
            if(array_key_exists($i, $this->users)){
                $id = $this->users[$i];
                
                // the "right" answers for this user
                $right = count($chosen_options[$i]->intersect($this->majority_correct));

                // for multiple choice add the options that the user correctly didnt chose
                if($this->task_type == "multiple"){
                    $right = $right + count($majority_incorrect->diff($chosen_options[$i]));
                }

                // query database for quality
                $user = $this->dbc->getQualityAll($id);
                list($old_quality, $data) = explode(':',$user[$this->task_type]);

                // append the new data to string
                if($this->task_type == "multiple"){
                    $data = rtrim($right . ',' . count($options) . ';' . $data, ';');   
                }
                else if($this->task_type == "single"){
                    $data = rtrim($right . ',' . '1' . ';' . $data, ';');  
                }

                $ds = explode(';', $data);

                if(count($ds) > QUALITY_MIN){
                    array_pop($ds);
                    $data = substr($data, 0, strrpos($data, ';'));
                }
                if(count($ds) == QUALITY_MIN){
                    $total_correct = 0.0;
                    $total_all = 0.0;
                    foreach($ds as $d){
                        list($correct, $all) = explode(',', $d);
                        $total_correct = $total_correct + $correct;
                        $total_all = $total_all + $all;
                    }
                    $new_quality = $total_correct / $total_all;
                }
                else{
                    $new_quality = $old_quality;
                }

                $quality_string = $new_quality . ':' . $data;
                $this->quality[$id] = $quality_string;
            }
        }

    }
    
		// updates quality for all participants
    public function updateQuality(){
        foreach($this->quality as $key => $val){
            $this->dbc->updateQuality($key, array("`$this->task_type`"), array("'$val'"));
        }
    }
    
		// store task result
    public function setTaskResult(){
        $answer = implode(";", $this->majority_correct->toArray());

        $this->dbc->updateHumanTask($this->task_id, array("answer"), array("'$answer'"));
        
        return $answer;
    }
}
?>