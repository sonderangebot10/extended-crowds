//
//  LoginController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-06.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire
import CoreLocation
import MapKit

import Firebase

class LoginController: UIViewController, UITextFieldDelegate, CLLocationManagerDelegate {
    
    //MARK: Variable declarations
    @IBOutlet weak var emailField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var reasonLabel: UILabel!
    
    let locationManager = CLLocationManager()
    var lat: Double?
    var lng: Double?
    
    let preferences = UserDefaults.standard
    let sysUtils = SystemUtils()
    let config = Config()
    
    
    let dowloandURL: String = "https://www.dropbox.com/s/tqn7xyrdeodammu/cs.ipa?dl=0"
    
    static var firebaseToken : String? {
        return InstanceID.instanceID().token()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        // Setup so that you can tab to the next textfield by pressing 'next' on the keyboard
        emailField.tag = 0
        emailField.delegate = self
        passwordField.tag = 1
        passwordField.delegate = self
        
        
        // Change color of the placeholder text
        emailField.attributedPlaceholder = NSAttributedString(string: "Email address",
                                                              attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        passwordField.attributedPlaceholder = NSAttributedString(string: "Password",
                                                              attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(statusManager), name: .flagsChanged, object: Network.reachability)
        updateUserInterface()
        
        // Ask for Authorisation from the User.
        self.locationManager.requestAlwaysAuthorization()
        
        // For use in foreground
        self.locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            locationManager.startUpdatingLocation()
        }
        
        
        
        // Continue if you are already logged in from before
        let isLoggedin = preferences.object(forKey: "is_logged_in") as? Bool
        if true {
            
            // Swtich veiw
            let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
            let resultViewController = storyBoard.instantiateViewController(withIdentifier: "MainController") as! UITabBarController
            self.present(resultViewController, animated:true, completion:nil)
            
        } else {
            if isLoggedin == true {
                // Parameters
                let parameters: Parameters =
                            ["email": preferences.string(forKey: "email") ?? "null",
                            "password": preferences.string(forKey: "password") ?? "null",
                            "firebase": preferences.string(forKey: "token") ?? "null",
                            "lat": lat ?? preferences.double(forKey: "lat") ,
                            "lng": lng ?? preferences.double(forKey: "lng"),
                            "os": "ios",
                            "version": config.VERSION]
                
                // Send post request to server
                Alamofire.request("http://217.211.176.94:1212/login.php", method: .post, parameters: parameters)
                    .responseJSON { response in
                        
                        // Handle response
                        if let result = response.result.value {
                            let JSON = result as! NSDictionary
                            
                            // Everythings OK
                            if JSON["status"] as! String == "OK"{
                                // stop updating location to save battery
                                self.locationManager.stopUpdatingLocation()
                                
                                // Update preferences
                                self.preferences.set(self.lat, forKey: "lat")
                                self.preferences.set(self.lng, forKey: "lng")
                                self.preferences.set(true, forKey: "is_logged_in")
                                self.preferences.synchronize()
                                
                                // Swtich veiw
                                let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
                                let resultViewController = storyBoard.instantiateViewController(withIdentifier: "MainController") as! UITabBarController
                                self.present(resultViewController, animated:true, completion:nil)

                            } else { // Something went wrong
                                // new version
                                if JSON["reason"] as! String == "outdated" {
                                    print("New versioN!")
                                    let alert = UIAlertController(title: "New version", message: "There is a new version available", preferredStyle: UIAlertControllerStyle.alert)
                                    
                                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                    alert.addAction(UIAlertAction(title: "Download", style: .default, handler: { _ in
                                        DispatchQueue.main.async {
                                            UIApplication.shared.open(URL(string: self.dowloandURL)!)
                                        }
                                        
                                    }))
                                    self.present(alert, animated: true, completion: nil)
                                }
                                else if JSON["reason"] as! String == "maintenance"{
                                    print("maintenance!")
                                    
                                    let time = JSON["time"] as! String
                                    var minutes = Int(time)!
                                    
                                    var hours: Int = minutes / 60
                                    minutes = minutes - (hours*60)
                                    
                                    let days = hours / 24
                                    hours = hours - (days*24)
                                    
                                    let message: String = "Servers are currently down from maintenance, estamated time left: \(days) days \(hours) hours \(minutes) minute(s)."
                                    let alert = UIAlertController(title: "Maintenance",
                                                                  message: message, preferredStyle: UIAlertControllerStyle.alert)
                                    
                                    alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                                    self.present(alert, animated: true, completion: nil)
                                }
                                else{
                                    print(JSON["reason"]!)
                                    self.reasonLabel.text = JSON["reason"]! as? String
                                }
                            }
                        }
                }
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        if let refreshedToken = InstanceID.instanceID().token() {
            print("InstanceID token: \(refreshedToken)")
            preferences.set(refreshedToken, forKey: "token")
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        self.locationManager.stopUpdatingLocation()
    }
    
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
    
    func updateUserInterface() {
        guard let status = Network.reachability?.status else { return }
        switch status {
        case .unreachable:
            reasonLabel.textColor = .red
            reasonLabel.text = "This application need internet connectivity to function. Please activate."
        case .wifi:
            reasonLabel.textColor = .red
        case .wwan:
            reasonLabel.textColor = .red
        }
    }
    
    @objc func statusManager(_ notification: Notification) {
        updateUserInterface()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let locValue:CLLocationCoordinate2D = manager.location!.coordinate
      //  print("locations = \(locValue.latitude) \(locValue.longitude)")
        lat = locValue.latitude
        lng = locValue.longitude
    }
    
    //MARK: Actions
    
    @IBAction func loginPressed(_ sender: UIButton) {
        attemptLogin()
    }
    
    //MARK: Private functions
    
    private func attemptLogin(){
        
        var cancel = false
        var reason = ""
        
        // Check for a valid email address
        if !emailField.hasText {
            cancel = true
            reason = "Email address can't be empty"
        }
        else if !sysUtils.isEmailValid(email: emailField.text!){
            cancel = true
            reason = "Email is invalid"
        }
        
        // Check for a valid password
        else if !passwordField.hasText {
            cancel = true
            reason = "Password can't be empty"
        }
        else if !sysUtils.isPasswordValid(password: passwordField.text!) {
            cancel = true
            reason = "Password is or Email is invalid"
        }
       

        if(cancel){
            print(reason)
            reasonLabel.text = reason
        }
        else{
            let email: String = emailField.text!
            let password: String = passwordField.text!
            // Parameters
            let parameters: Parameters = ["email": email,
                                          "password": password,
                                          "firebase": preferences.string(forKey: "token") ?? "null",
                                          "lat": lat ?? 0,
                                          "lng": lng ?? 0,
                                          "os": "ios",
                                          "version": config.VERSION]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/login.php", method: .post, parameters: parameters)
                .responseJSON { response in
                
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{
                    
                        // stop updating location to save battery
                        self.locationManager.stopUpdatingLocation()
                        
                        // Update preferences
                        self.preferences.set(email,             forKey: "email")
                        self.preferences.set(password,          forKey: "password")
                        self.preferences.set(JSON["username"],  forKey: "username")
                        self.preferences.set(self.lat,          forKey: "lat")
                        self.preferences.set(self.lng,          forKey: "lng")
                        self.preferences.set(true,              forKey: "is_logged_in")
                        self.preferences.synchronize()
                        
                        // Swtich veiw
                        let storyBoard : UIStoryboard = UIStoryboard(name: "Main", bundle:nil)
                        let resultViewController = storyBoard.instantiateViewController(withIdentifier: "MainController") as! UITabBarController
                        self.present(resultViewController, animated:true, completion:nil)
                    }
                    else { // Something went wrong
                        // new version
                        if JSON["reason"] as! String == "outdated" {
                            print("New versioN!")
                            let alert = UIAlertController(title: "New version", message: "There is a new version available", preferredStyle: UIAlertControllerStyle.alert)
                            
                            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                            alert.addAction(UIAlertAction(title: "Download", style: .default, handler: { _ in
                                DispatchQueue.main.async {
                                    UIApplication.shared.open(URL(string: self.dowloandURL)!)
                                }
                                
                            }))
                            self.present(alert, animated: true, completion: nil)
                        }
                        else if JSON["reason"] as! String == "maintenance"{
                            print("maintenance!")
                            let time = JSON["time"] as! String
                            var minutes = Int(time)!
                            
                            var hours: Int = minutes / 60
                            minutes = minutes - (hours*60)
                            
                            let days = hours / 24
                            hours = hours - (days*24)
                            
                            
                            
                            let message: String = "Servers are currently down from maintenance, estamated time left: \(days) days \(hours) hours \(minutes) minute(s)."
                            let alert = UIAlertController(title: "Maintenance",
                                                          message: message, preferredStyle: UIAlertControllerStyle.alert)
                            
                            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                            self.present(alert, animated: true, completion: nil)
                        }
                        else{
                            print(JSON["reason"]!)
                            self.reasonLabel.text = JSON["reason"]! as? String
                        }
                    }
                }
            }
        }
    }
}
