//
//  OngoingTasksController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-27.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class OngoingTasksController: UIViewController, UITableViewDataSource, UITableViewDelegate  {

    public struct Task {
        let answer: String!
        let created: String!
        let sensor: String!
        let type: String!
        let question: String!
    }

    var segue: String!
    
    @IBOutlet weak var taskTable: UITableView!
    
    let preferences = UserDefaults.standard
    
    // A list with completed tasks, fetched from the server
    var tasks = [Task]()
    var index: Int!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        taskTable.dataSource = self
        taskTable.delegate = self
        
        
        let recognizer: UISwipeGestureRecognizer = UISwipeGestureRecognizer(target: self, action: #selector(swipeRight(recognizer:)))
        recognizer.direction = .right
        self.view .addGestureRecognizer(recognizer)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        // Update tabel contents
        
        
        loadOngoing()
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
        cell.descriptionLabel.text = "\(self.tasks[indexPath.row].type!), Created: \(self.tasks[indexPath.row].created!)"
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        index = indexPath.row
   //     performSegue(withIdentifier: "SegueSegue", sender: nil)
    }
    
    @objc func swipeRight(recognizer : UISwipeGestureRecognizer) {
  //      segue = "history"
 //       self.performSegue(withIdentifier: "SagueToHistory", sender: self)
    }
    
    private func loadOngoing() {
        
        // Parameters
        let parameters: Parameters = ["email": preferences.string(forKey: "email") ?? "default"]
        
        // Send post request to server
        Alamofire.request("http://217.211.176.94:1212/ongoing_task.php", method: .post, parameters: parameters)
            .responseJSON { response in
                
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{
                        
                        let JSONArray = JSON["tasks"] as! NSArray
                        print(JSONArray.count)
                        if JSONArray.count > 0 {
                            for i in 0 ... JSONArray.count-1 {
                                
                                let item = JSONArray[i] as! [String : AnyObject]
                                var answer: String!
                                var created: String!
                                var sensor: String!
                                var type: String!
                                var question: String!
                                
                                let taskType =  item["type"] as! String
                                if taskType == "sensing" {
                                    // Sensor task
                                    answer = "nil"
                                    created = item["created"] as! String
                                    sensor = item["sensor"] as! String
                                    question = "Read sensor: \(sensor as String!)"
                                    type = "sensor task"
                                    
                                }
                                else {
                                    // Human intelligence task
                                    answer = "nil"
                                    created = item["created"] as! String
                                    sensor = "NoN"
                                    question = item["question"] as! String
                                    type = "\(taskType) task"
                                }
                                
                                
                                let t = OngoingTasksController.Task(answer: answer,
                                                         created: created,
                                                         sensor: sensor,
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

}
