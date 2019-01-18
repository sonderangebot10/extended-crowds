//
//  RewardController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-20.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class RewardController: UIViewController {

    @IBOutlet weak var hitLabel: UILabel!
    @IBOutlet weak var sensorLabel: UILabel!
    
    @IBOutlet weak var errorLabel: UILabel!
    
    let preferences = UserDefaults.standard
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        // Parameters
        let parameters: Parameters = ["email": preferences.string(forKey: "email")!]
        
        // Send post request to server
        Alamofire.request("http://217.211.176.94:1212/reward.php", method: .post, parameters: parameters)
            .responseJSON { response in
                
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{
                        let points = JSON["points"] as! String
                        let tokens = points.components(separatedBy: ";")
                        self.sensorLabel.text = tokens[0]
                        self.hitLabel.text = tokens[1]
                        
                    } else { // Something went wrong
                        print(JSON["reason"]!)
                        self.errorLabel.text = JSON["reason"]! as? String
                    }
                }
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
