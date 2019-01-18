//
//  SecondViewController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class AccountManagement: UIViewController, UITextFieldDelegate {
    
    // MARK: Variable declarations
    @IBOutlet weak var emailLabel: UILabel!
    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var repeatPasswordField: UITextField!
    @IBOutlet weak var reasonLabel: UILabel!
    
    
    let preferences = UserDefaults.standard
    let sysUtils = SystemUtils()
    
    var currentPassword: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Update the email view
        emailLabel.text = preferences.string(forKey: "email") ?? "null"
        let username = preferences.string(forKey: "username") ?? "null"
        currentPassword = preferences.string(forKey: "password") ?? "null"
        
        // Change color of the placeholder text
        usernameField.attributedPlaceholder = NSAttributedString(string: username,
                                                              attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        usernameField.text = preferences.object(forKey: "username") as? String
        passwordField.attributedPlaceholder = NSAttributedString(string: "New password",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        repeatPasswordField.attributedPlaceholder = NSAttributedString(string: "Repeat password",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        
        // Setup so that you can tab to the next textfield by pressing 'next' on the keyboard
        usernameField.tag = 0
        usernameField.delegate = self
        passwordField.tag = 1
        passwordField.delegate = self
        repeatPasswordField.tag = 2
        repeatPasswordField.delegate = self
    }
    
    // MARK: TextField delegate
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool{
        // Try to find next responder
        if let nextField = textField.superview?.viewWithTag(textField.tag + 1) as? UITextField {
            nextField.becomeFirstResponder()
        } else {
            // Not found, so remove keyboard.
            textField.resignFirstResponder()
        }
        // Do not add a line break
        return false
    }
    
    
    // MARK: Actions
    
    @IBAction func updatePressed(_ sender: UIButton) {
        
        var cancel = false
        var reason: String?
        
        let username = usernameField.text!
        var password = passwordField.text ?? ""
        let repeatPassword = repeatPasswordField.text ?? ""
        
        // Check for valid username
        if !sysUtils.isUsernameValid(username: username ) {
            cancel = true
            reason = "The new username is not valid."
        }
        
        // Check if the new password are correct
        else if password != "" {
            if !sysUtils.isPasswordValid(password: password){
                cancel = true
                reason = "The password you have entered is not valid."
            }
            else if password != repeatPassword  {
                cancel = true
                reason = "The passwords you have entered do not match"
            }
        }
        
        // No new password entered, continue with the old one
        else {
            password = currentPassword!
        }
        
        if(cancel){
            reasonLabel.textColor = .red
            reasonLabel.text = reason ?? ""
        }
        else {
            // Parameters
            let parameters: Parameters =
                ["email": emailLabel.text!,
                 "password": password,
                 "username": username]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/update_user.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            
                            // Update preferences
                            self.preferences.set(JSON["password"], forKey: "password")
                            self.preferences.set(JSON["username"], forKey: "username")
                            self.preferences.synchronize()
                            self.currentPassword = password
                            
                            self.reasonLabel.textColor = .green
                            self.reasonLabel.text = "Update successful!"
                            
                        } else { // Something went wrong
                            print(JSON["reason"]!)
                            self.reasonLabel.text = JSON["reason"]! as? String
                        }
                    }
            }
        }
    }
    
    @IBAction func deletePressed(_ sender: UIButton) {
        
        let alert = UIAlertController(title: "Delete account", message: "Are you sure you want to delete your account?", preferredStyle: UIAlertControllerStyle.alert)
        alert.addAction(UIAlertAction(title: "Delete", style: UIAlertActionStyle.default, handler:{ action in
            // Parameters
            let parameters: Parameters =
                ["email": self.emailLabel.text!]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/delete_user.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            
                            // clean up preferences
                            if let bundle = Bundle.main.bundleIdentifier {
                                UserDefaults.standard.removePersistentDomain(forName: bundle)
                            }
           
                            // go to the log in screen
                            let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
                            let resultViewController = storyBoard.instantiateViewController(withIdentifier: "NavigationController") as! UINavigationController
                            self.present(resultViewController, animated:true, completion:nil)
                            
                        } else { // Something went wrong
                            print(JSON["reason"]!)
                            self.reasonLabel.text = JSON["reason"]! as? String
                        }
                    }
            }
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .default, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }
    
    @IBAction func logoutPressed(_ sender: UIButton) {
        
        // Update preferences
        self.preferences.set(false, forKey: "is_logged_in")
        self.preferences.synchronize()
        // Parameters
        let parameters: Parameters =
            ["email": emailLabel.text!]
        
        // Send post request to server
        Alamofire.request("http://217.211.176.94:1212/logout.php", method: .post, parameters: parameters)
            .responseJSON { response in
                
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{
      
                        self.preferences.set(false, forKey: "is_logged_in")
                        
                        // Swtich veiw
                        let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
                        let resultViewController = storyBoard.instantiateViewController(withIdentifier: "NavigationController") as! UINavigationController
                        self.present(resultViewController, animated:true, completion:nil)
                        
                        
                    } else { // Something went wrong
                    }
                }
        }
        
    }
    
}


