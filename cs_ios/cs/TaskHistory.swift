//
//  TaskHistory.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class TaskHistory: UIViewController, UITableViewDataSource, UITableViewDelegate {

    public struct Task {
        let answer: String!
        let completed: String!
        let created: String!
        let id: String!
        let sensor: String!
        let duration: String!
        let type: String!
        let question: String!
    }

    @IBOutlet weak var taskTable: UITableView!
    
    let preferences = UserDefaults.standard
    
    // A list with completed tasks, fetched from the server
    var tasks = [Task]()
    var index: Int!
    var segue: String!

    override func viewDidLoad() {
        super.viewDidLoad()
 
        taskTable.dataSource = self
        taskTable.delegate = self
        
        let recognizer: UISwipeGestureRecognizer = UISwipeGestureRecognizer(target: self, action: #selector(swipeLeft(recognizer:)))
        
        recognizer.direction = .left
        self.view .addGestureRecognizer(recognizer)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        // Update tabel contents
        self.tasks.removeAll()
        loadTaskHistory()
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.tasks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        // create a new cell if needed or reuse an old one
        let cell:MyTableViewCell = self.taskTable.dequeueReusableCell(withIdentifier: "MyTableViewCell") as! MyTableViewCell!
        
        // set the text from the data model
        cell.titleLabel.text = self.tasks[indexPath.row].question
        cell.descriptionLabel.text = self.tasks[indexPath.row].type!
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        index = indexPath.row
        performSegue(withIdentifier: "historySegue", sender: nil)
    }
    
    override func prepare(for s: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
  
        if s.destination.title == "Ongoing tasks"{
        }
        else {
            let destination = s.destination as! TaskHistoryInformationViewController
            destination.task = tasks[index]
        }
    }
    
    @objc func swipeLeft(recognizer : UISwipeGestureRecognizer) {
        self.performSegue(withIdentifier: "SegueToOngoing", sender: nil)
    }
    
    private func loadTaskHistory() {
   
        // Remove all items before fetching new!
        self.tasks.removeAll()
        
        // Parameters
        let parameters: Parameters = ["email": preferences.string(forKey: "email") ?? "default"]
        
        // Send post request to server
        Alamofire.request("http://217.211.176.94:1212/task_history.php", method: .post, parameters: parameters)
            .responseJSON { response in
                
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{

                        let JSONArray = JSON["tasks"] as! NSArray
                        if JSONArray.count > 0 {
                            for i in 0 ... JSONArray.count-1 {
                                
                                let item = JSONArray[i] as! [String : AnyObject]
                                var answer: String!
                                var completed: String!
                                var created: String!
                                var id: String!
                                var sensor: String!
                                var duration: String!
                                var type: String!
                                var question: String!
                                
                                let taskType =  item["type"] as! String
                                if taskType == "sensing" {
                                    // Sensor task
                                    answer = item["answer"] as! String
                                    completed = item["completed"] as! String
                                    created = item["created"] as! String
                                    sensor = item["sensor"] as! String
                                    duration = item["duration"] as! String
                                    question = "Read sensor: \(sensor as! String)"
                                    type = "sensor task"
                                    id = "hahAA"
                                    
                                }
                                else {
                                    // Human intelligence task
                                    answer = item["answer"] as! String
                                    completed = item["completed"] as! String
                                    created = item["created"] as! String
                                    sensor = "NoN"
                                    duration = "NoN"
                                    question = item["question"] as! String
                                    type = "\(taskType) task"
                                    id = "hahAA"
                                }
                                
                                
                                let t = TaskHistory.Task(answer: answer,
                                                         completed: completed,
                                                         created: created,
                                                         id: id,
                                                         sensor: sensor,
                                                         duration: duration,
                                                         type: type,
                                                         question: question)
                                
                                self.tasks.append(t)
                            }
                        }
                        
                        self.taskTable.reloadData()
                    }
                    
                    else { // Something went wrong
                        print(JSON["reason"]!)
                    }
                }
        }
        
        
    }
    
    //MARK: Actions
    
    @IBAction func ongoingPressed(_ sender: UIBarButtonItem) {
    
    }
    
    
}


