//
//  RegisterController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-06.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire

class RegisterController: UIViewController, UITextFieldDelegate {
    
    //MARK: Variable declarations
    
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var repeatPasswordField: UITextField!
    @IBOutlet weak var responseField: UILabel!
    @IBOutlet weak var questionStack: UIStackView!
    
    var success = false
    
    var buttons1 =  [RadioButton]()
    var buttons2 =  [RadioButton]()
    var buttons3 =  [RadioButton]()
    var selected1: String = ""
    var selected2: String = ""
    var selected3: String = ""
    
    let preferences = UserDefaults.standard
    let sysUtils = SystemUtils()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Setup so that you can tab to the next textfield by pressing 'next' on the keyboard
        emailField.tag = 0
        emailField.delegate = self
        usernameField.tag = 1
        usernameField.delegate = self
        passwordField.tag = 2
        passwordField.delegate = self
        repeatPasswordField.tag = 3
        repeatPasswordField.delegate = self
        
        // Change color of the placeholder text
        emailField.attributedPlaceholder = NSAttributedString(string: "Email address",
                                                              attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        passwordField.attributedPlaceholder = NSAttributedString(string: "Password",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        repeatPasswordField.attributedPlaceholder = NSAttributedString(string: "Repeat password",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        usernameField.attributedPlaceholder = NSAttributedString(string: "Username",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        
        
        
        // Get all sensors
        // Retreive and sort my aavailable sensors
        // Find out which ones are in common
        
        
        
        // Create all question related stuff
        questionStack.axis = .vertical
        questionStack.alignment = .center
        questionStack.distribution = .fill
        questionStack.spacing = 5
        
        
        // question 1
        let question1Stack = UIStackView()
        question1Stack.axis = .vertical
        question1Stack.alignment = .center
        question1Stack.distribution = .fill
        question1Stack.spacing = 5
        
        // text 1
        let question1Text = UILabel()
        question1Text.text = "How old is your phone?"
        question1Text.textColor = UIColor.white
        question1Text.numberOfLines = 3
        question1Text.font = question1Text.font?.withSize(20)
        
        // options 1
        let options1Stack = UIStackView()
        options1Stack.axis = .vertical
        options1Stack.alignment = .center
        options1Stack.distribution = .fill
        options1Stack.spacing = 5
        let q1: [String] = ["Less than 1 year", "Between 1 and 2 years", "More than 2 years"]
        
        options1Stack.addArrangedSubview(question1Text)
        for i in 1 ... 3 { // generate radiobuttons
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = RadioButton()
            button.addTarget(self, action: #selector(radioAction1(sender:)), for: .touchUpInside)
            button.score = String(i)
            button.setImage(#imageLiteral(resourceName: "radio_button_unchecked"), for: .normal)
            buttons1.append(button)
            
            let label = UILabel()
            label.text = q1[i-1]
            label.font = label.font.withSize(20)
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            options1Stack.addArrangedSubview(horizontalStack)
            
        }
        question1Stack.addArrangedSubview(options1Stack)
        
        // question 2
        let question2Stack = UIStackView()
        question2Stack.axis = .vertical
        question2Stack.alignment = .center
        question2Stack.distribution = .fill
        question2Stack.spacing = 5
        
        // text 2
        let question2Text = UILabel()
        question2Text.text = "How motivated are you to participate?"
        question2Text.textColor = UIColor.white
        question2Text.numberOfLines = 3
        question2Text.font = question1Text.font?.withSize(20)
        
        // options 2
        let options2Stack = UIStackView()
        options2Stack.axis = .vertical
        options2Stack.alignment = .center
        options2Stack.distribution = .fill
        options2Stack.spacing = 5
        let q2: [String] = ["Very", "Moderate", "little"]
        
        options2Stack.addArrangedSubview(question2Text)
        for i in 1 ... 3 { // generate radiobuttons
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = RadioButton()
            button.addTarget(self, action: #selector(radioAction2(sender:)), for: .touchUpInside)
            button.score = String(i)
            button.setImage(#imageLiteral(resourceName: "radio_button_unchecked"), for: .normal)
            buttons2.append(button)
            
            let label = UILabel()
            label.text = q2[i-1]
            label.font = label.font.withSize(20)
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            options2Stack.addArrangedSubview(horizontalStack)
            
        }
        question2Stack.addArrangedSubview(options2Stack)
        
        
        // question 3
        let question3Stack = UIStackView()
        question3Stack.axis = .vertical
        question3Stack.alignment = .center
        question3Stack.distribution = .fill
        question3Stack.spacing = 5
        
        // text 3
        let question3Text = UILabel()
        question3Text.text = "How long does your battery last (normal usage)?"
        question3Text.textColor = UIColor.white
        question3Text.numberOfLines = 3
        question3Text.font = question1Text.font?.withSize(20)
        
        // options 3
        let options3Stack = UIStackView()
        options3Stack.axis = .vertical
        options3Stack.alignment = .center
        options3Stack.distribution = .fill
        options3Stack.spacing = 5
        let q3: [String] = ["More than 24h", "Between 12h and 24h", "Less than 12h"]
        
        options3Stack.addArrangedSubview(question3Text)
        for i in 1 ... 3 { // generate radiobuttons
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = RadioButton()
            button.addTarget(self, action: #selector(radioAction3(sender:)), for: .touchUpInside)
            button.score = String(i)
            button.setImage(#imageLiteral(resourceName: "radio_button_unchecked"), for: .normal)
            buttons3.append(button)
            
            let label = UILabel()
            label.text = q3[i-1]
            label.font = label.font.withSize(20)
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            options3Stack.addArrangedSubview(horizontalStack)
            
        }
        question3Stack.addArrangedSubview(options3Stack)
        
        questionStack.addArrangedSubview(question1Stack)
        questionStack.addArrangedSubview(question2Stack)
        questionStack.addArrangedSubview(question3Stack)
 
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool
    {
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
    
    
    //MARK: Actions
    
    @objc func radioAction1 (sender: RadioButton) {
        
        for b in buttons1 {
            b.isSelected = false
        }
        sender.isSelected = true
        selected1 = sender.score
        
    }
    
    @objc func radioAction2 (sender: RadioButton) {
        
        for b in buttons2 {
            b.isSelected = false
        }
        sender.isSelected = true
        selected2 = sender.score
        
    }
    
    @objc func radioAction3 (sender: RadioButton) {
        
        for b in buttons3 {
            b.isSelected = false
        }
        sender.isSelected = true
        selected3 = sender.score
        
    }
    
        
    @IBAction func registerPressed(_ sender: UIButton) {

        var cancel = false
        var reason = ""
        
        // Check for a valid email address
        if !emailField.hasText {
            cancel = true
            reason = "Email address can't be empty"
        }
        else if !sysUtils.isEmailValid(email: emailField.text!){
            cancel = true
            reason = emailField.text! //"Email is invalid"
        }
            
        // Check for a valid username
        else if !usernameField.hasText {
            cancel = true
            reason = "Username can't be empty"
        }
        else if !sysUtils.isUsernameValid(username: usernameField.text!){
            cancel = true
            reason = usernameField.text! //"Username is invalid"
        }
            
            // Check for a valid password
        else if !passwordField.hasText {
            cancel = true
            reason = "Password can't be empty"
        }
        else if !sysUtils.isPasswordValid(password: passwordField.text!) {
            cancel = true
            reason = passwordField.text! //"Password is or Email is invalid"
        }
        else if !repeatPasswordField.hasText {
            cancel = true
            reason = "Please repeat the password"
        }
        else if repeatPasswordField.text! != passwordField.text! {
            cancel = true
            reason = repeatPasswordField.text! //"Passwords does not match"
        }
        else if !isPressed(buttons: buttons1){
            cancel = true
            reason = "Please answer all questions"
            
        }
        else if !isPressed(buttons: buttons2){
            cancel = true
            reason = "Please answer all questions"
        }
        else if !isPressed(buttons: buttons3){
            cancel = true
            reason = "Please answer all questions"
        }
        
        if(cancel){
            responseField.text = reason
            responseField.textColor = UIColor.red
        }
        else{
            
            print("is anybode nil here? \(selected1), \(selected2), \(selected3)")
            let device = UIDevice()
            let bid : Int = Int(selected1)! + Int(selected2)! + Int(selected3)!
            
            // Parameters
            let parameters: Parameters =
                    ["email": emailField.text ?? "trix@hotmail.com",
                    "username": usernameField.text ?? "fisvind",
                    "password": passwordField.text ?? "lolol",
                    "bid": bid,
                    "firebase": preferences.string(forKey: "token") ?? "hahha",
                    "device_sensors": "100001000100000011",
                    "device_os":    device.systemVersion,
                    "device_model": "ios"]

            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/register_user.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                        
                            self.responseField.text = "Success! \nPlease go back and log in!"
                            self.responseField.textColor = UIColor.green
                            
                        } else { // Something went wrong
                            self.responseField.text = JSON["reason"]! as? String
                            self.responseField.textColor = UIColor.red
                        }
                    }
                    
                    print(response)
            }
        }
    }
    
    /**
     This funcitons returns true if one button is checked
     */
    private func isPressed(buttons: [RadioButton]) -> Bool {
        
        for button in buttons {
            if button.isSelected {
                return true
            }
        }
        
        return false
    }
    
}
