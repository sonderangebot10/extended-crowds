//
//  AssignedSingleChoice.swift
//  cs
//
//  Created by Johan Waller on 2017-11-22.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class AssignedSingleChoice: UIViewController {

    //MARK: Varable declaraions
    
    @IBOutlet weak var questionLabel: UILabel!
    @IBOutlet weak var optionsStack: UIStackView!
    @IBOutlet weak var reasonLabel: UILabel!
    
    var task: Task!
    var buttons =  [RadioButton]()
    var selected: String = ""
    
    let preferences = UserDefaults.standard
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        questionLabel.text = task.question
        let s = task.options!
        let options: [String] = s.components(separatedBy: ";")
        
        print(options.count)
        
        for option in options {
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = RadioButton(frame: CGRect(x: 20, y: 170, width: 50, height: 50))
            button.addTarget(self, action: #selector(manualAction(sender:)), for: .touchUpInside)
            button.score = option
            button.setImage(#imageLiteral(resourceName: "radio_button_unchecked"), for: .normal)
            buttons.append(button)
            
            let label = UILabel()
            label.text = option
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            
            optionsStack.addArrangedSubview(horizontalStack)
        }
    }
    
    //MARK: Actions
    
    @objc func manualAction (sender: RadioButton) {
        
        for b in buttons {
            b.isSelected = false
        }
        sender.isSelected = true
        selected = sender.score
        
    }
    
    @IBAction func createPressed(_ sender: UIButton) {
        
        print(selected)
        
        if selected == "" {
            reasonLabel.text = "Choose one radio button!"
        }
        else{
            // Parameters
            let parameters: Parameters = ["email": preferences.string(forKey: "email")!,
                                          "data": selected,
                                          "type": "single",
                                          "id": task.id,
                                          "file": "single_choice.php"]
            
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
