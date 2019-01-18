//
//  FirstViewController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

class AssignedTasks: UIViewController, UITableViewDataSource, UITableViewDelegate {

    //MARK: Variable definitions
    
    @IBOutlet weak var tasksTable: UITableView!
    
    let preferences = UserDefaults.standard
    
    // A list with completed tasks, fetched from the server
    var tasks: [Task] = [Task]()
    var index: Int!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tasksTable.dataSource = self
        tasksTable.delegate = self
        
        
        // Load the array with tasks when the view is initially loaded
        if let encoded = preferences.object(forKey: "tasks") as? Data {
            tasks = try! PropertyListDecoder().decode([Task].self, from: encoded)
            self.tasksTable.reloadData()
        }
        
        // Register to receive notification in your class
        NotificationCenter.default.addObserver(self, selector: #selector(self.loadList(_:)), name: NSNotification.Name(rawValue: "load"), object: nil)
        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        // Update tabel contents
        self.tasksTable.reloadData()
    }
    
    // Save the array with tasks when the view dissapears
    override func viewDidDisappear(_ animated: Bool) {
        try? preferences.setValue(PropertyListEncoder().encode(tasks), forKey: "tasks")
    }

    //MARK: Table delagate functions
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.tasks.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        
        // create a new cell if needed or reuse an old one
        let cell:MyTableViewCell = self.tasksTable.dequeueReusableCell(withIdentifier: "MyTableViewCell") as! MyTableViewCell!
        
        // set the text from the data model
        cell.titleLabel.text = self.tasks[indexPath.row].question
        cell.descriptionLabel.text = self.tasks[indexPath.row].type
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        index = indexPath.row
        let type = tasks[index].type as! String

        switch type {
        case "single":
            // Swtich veiw
            performSegue(withIdentifier: "assignedSingleSegue", sender: nil)
        case "multiple":
            // Swtich veiw
            performSegue(withIdentifier: "assignedMultipleSegue", sender: nil)
        case "numeric":
            // Swtich veiw
            performSegue(withIdentifier: "assignedNumericSegue", sender: nil)
        default:
            print("LOL")
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
        
        let type = tasks[index].type as! String
        
        switch type {
        case "single":
            let destination = segue.destination as! AssignedSingleChoice
            destination.task = tasks[index]
        case "multiple":
            // Swtich veiw
            let destination = segue.destination as! AssignedMultipleChoice
            destination.task = tasks[index]
        case "numeric":
            // Swtich veiw
            let destination = segue.destination as! AssignedNumeric
            destination.task = tasks[index]
        default:
            print("LOL")
        }
        
    }
    
    //MARK: Private functions

    @objc func loadList(_ notification: NSNotification){
        
        // Got a remote message with a new task
        if let newTask = notification.userInfo?["new"] as? Task{
            // Add the new task to the list
            self.tasks.append(newTask)
            
        }
        
        // A task has expired
        if let expiredTask = notification.userInfo?["expired"] as? Task{
            for i in 0...tasks.count - 1 {
                print("ids: \(tasks[i].id), \(expiredTask.id)")
                if tasks[i].id.range(of: expiredTask.id) != nil  {
                    print("hittaa!")
                    // remove the task from the list
                    self.tasks.remove(at: i)
                    break
                }
            }
        }
        
        // Remove a task from the list of tasks
        if let task = notification.userInfo?["task"] as? Task {
            for i in 0...tasks.count - 1 {
                if tasks[i] === task {
                    // remove the task from the list
                    self.tasks.remove(at: i)
                    break
                }
            }
        }
        
        if let _ = notification.userInfo?["forced_logout"] as? Bool {
            
            self.preferences.set(false, forKey: "is_logged_in")
            
            let storyBoard: UIStoryboard = UIStoryboard(name: "Main", bundle: nil)
            let newViewController = storyBoard.instantiateViewController(withIdentifier: "NavigationController") as! UINavigationController
            self.present(newViewController, animated: true, completion: nil)
        }
        
        // Update tabel contents
        self.tasksTable.reloadData()
        
    }
    

    


}





