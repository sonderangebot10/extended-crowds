//
//  AssignedMultipleChoice.swift
//  cs
//
//  Created by Johan Waller on 2017-11-22.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class AssignedMultipleChoice: UIViewController {
    
    @IBOutlet weak var questionLabel: UILabel!
    @IBOutlet weak var optoinsStack: UIStackView!
    @IBOutlet weak var reasonLabel: UILabel!
    
    let preferences = UserDefaults.standard
    
    var task: Task!
    var buttons = [CheckBox]()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        questionLabel.text = task.question
        let s = task.options!
        let options: [String] = s.components(separatedBy: ";")
        
        for option in options {
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = CheckBox()
            button.addTarget(self, action: #selector(manualAction(sender:)), for: .touchUpInside)
            button.name = option
            button.setImage(#imageLiteral(resourceName: "checkbox_unchecked"), for: .normal)
            buttons.append(button)
            
            let label = UILabel()
            label.text = option
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            let tap = UITapGestureRecognizer(target: self, action: #selector(manualAction2(sender:)))
            tap.setValue(option, forKey: "name")
            label.addGestureRecognizer(tap)
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            
            optoinsStack.addArrangedSubview(horizontalStack)
        }
    }

    @objc func manualAction (sender: CheckBox) {
        
        sender.isSelected = !sender.isSelected
     //   selected = sender.name
        
    }
    
    @objc func manualAction2 (sender: UITapGestureRecognizer) {
        
        for b in buttons {
            if b.name == sender.value(forKey: "name") as! String {
                b.isSelected = !b.isSelected
            }
        }
    //    selected = sender.value(forKey: "name") as! String
    }

    @IBAction func createPressed(_ sender: UIButton) {
        var answer: String = ""
        for button in buttons {
            if button.isSelected {
               answer += "_" + button.name
            }
        }
        if answer == ""{
            reasonLabel.text = "Choose at least one box!"
        }
        else{ // remove first '_'
            answer.remove(at: answer.startIndex)
            
            // Parameters
            let parameters: Parameters = ["email": preferences.string(forKey: "email")!,
                                          "data": answer,
                                          "type": "multiple",
                                          "id": task.id,
                                          "file": "multiple_choice.php"]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/update_task.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            
                            //TODO: Remove this task from the list of tasks.
                            
                            
                            // this call updates the listview
                            let dict:[String: Task] = ["task": self.task]
                            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
                            
                            

                        } else { // Something went wrong
                            print(JSON["reason"]!)
                            
                            self.reasonLabel.text = JSON["reason"]! as? String
                            self.reasonLabel.textColor = UIColor.red
                        }
 
                    }
 
            
            
            }
  
            // go back one view
            _ = self.navigationController?.popViewController(animated: true)
        }
        
        
    }
    
    

}
