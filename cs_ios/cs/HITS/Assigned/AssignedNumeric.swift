//
//  AssignedNumeric.swift
//  cs
//
//  Created by Johan Waller on 2017-11-22.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class AssignedNumeric: UIViewController {

    
    @IBOutlet weak var questionLabel: UILabel!
    @IBOutlet weak var answerField: UITextField!
    @IBOutlet weak var reasonLabel: UILabel!
    
    
    let preferences = UserDefaults.standard
    
    var task: Task!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        questionLabel.text = task.question
        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    //MARK: Actions
    @IBAction func createPressed(_ sender: UIButton) {
        
        let answer: String = answerField.text!
        
        if answer == "" {
            reasonLabel.text = "Answer can not be empty!"
        }
        else{
            
            // Parameters
            let parameters: Parameters = ["email": preferences.string(forKey: "email")!,
                                          "data": answer,
                                          "type": "numeric",
                                          "id": task.id,
                                          "file": "numeric.php"]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/update_task.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            
                            // this call updates the listview
                            let dict:[String: Task] = ["task": self.task]
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
                            
                            // go back one view
                            _ = self.navigationController?.popViewController(animated: true)
                        }
                        else { // Something went wrong
                            print(JSON["reason"]!)
                            self.reasonLabel.text = JSON["reason"]! as? String
                        }
                    }
            }
            
        }
    }
    


}
