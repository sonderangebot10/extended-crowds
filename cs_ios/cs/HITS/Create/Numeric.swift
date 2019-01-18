//
//  Numeric.swift
//  cs
//
//  Created by Johan Waller on 2017-11-17.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire
import MapKit

class Numeric: UIViewController, UITextFieldDelegate, CLLocationManagerDelegate, UIGestureRecognizerDelegate {
    
    //MARK: Variable declarations
    
    @IBOutlet weak var questionField: UITextField!
    @IBOutlet weak var errorLabel: UILabel!
    @IBOutlet weak var createButton: UIButton!
    @IBOutlet weak var mapView: MKMapView!
    
    
    // MapView variables
    var initialLocation: CLLocation!
    let regionRadius: CLLocationDistance = 2000
    var annotation: MKPointAnnotation?
    var locationManager:CLLocationManager!
    var tapped: Bool = false
    
    // Shared preferences
    let preferences = UserDefaults.standard
    
    
    override func viewDidLoad(){
        super.viewDidLoad()
        
        
        // Change color of the placeholder text
        questionField.delegate = self
        questionField.attributedPlaceholder = NSAttributedString(string: "Question",
                                                                 attributes: [NSAttributedStringKey.foregroundColor: UIColor.gray])
        
        
        // Set map locaiton to the initial location
        initialLocation = CLLocation(latitude: preferences.double(forKey: "lat"), longitude: preferences.double(forKey: "lng"))
        centerMapOnLocation(location: initialLocation)
        annotation = MKPointAnnotation()
        
        let gestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(self.handleTap(gestureReconizer:)))
        gestureRecognizer.delegate = self
        mapView.addGestureRecognizer(gestureRecognizer)
        mapView.showsUserLocation = true
        
        
        // get positions and add annotations to the map
        getPositions()
    }
    
    // MARK: MapView and Position functions
    
    func centerMapOnLocation(location: CLLocation) {
        let coordinateRegion = MKCoordinateRegionMakeWithDistance(location.coordinate,
                                                                  regionRadius, regionRadius)
        mapView.setRegion(coordinateRegion, animated: true)
    }
    
    func determineCurrentLocation() {
        locationManager = CLLocationManager()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.requestAlwaysAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            //locationManager.startUpdatingHeading()
            locationManager.startUpdatingLocation()
        }
    }
    
    // MARK: TextView delegate
    
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
    
    //MARK: Actions
    
    @objc func handleTap(gestureReconizer: UITapGestureRecognizer) {
        
        let location = gestureReconizer.location(in: mapView)
        let coordinate = mapView.convert(location,toCoordinateFrom: mapView)
        tapped = true
        
        // Add/move annotation:
        annotation!.coordinate = coordinate
        mapView.addAnnotation(annotation!)
        
    }
    
    @IBAction func createPressed(_ sender: UIButton) {
        
        let lat = annotation?.coordinate.latitude
        let lng = annotation?.coordinate.longitude
        
        if questionField.text == "" {
            // Needs to write a question
            errorLabel.text = "You need to enter a question"
            errorLabel.textColor = UIColor.red
        }
        else if !tapped { // gotta tap that map
            errorLabel.text = "Please tap the map to provide a location"
        }
        else{
            
            // Parameters
            let parameters: Parameters = ["email": preferences.string(forKey: "email")!,
                                          "type": "hit",
                                          "hit_type": "numeric",
                                          "description": "empty",
                                          "question": questionField.text!,
                                          "answer_choices": "",
                                          "file": "numeric.php",
                                          "lat": lat!,
                                          "lng": lng!]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/create_task.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            // go back one view
                            _ = self.navigationController?.popViewController(animated: true)
                            
                            self.errorLabel.text = "Success!"
                            self.errorLabel.textColor = UIColor.green
                            self.createButton.isEnabled = false
                            
                        } else { // Something went wrong
                            print(JSON["reason"]!)
                            
                            self.errorLabel.text = JSON["reason"]! as? String
                            self.errorLabel.textColor = UIColor.red
                        }
                    }
            }
        }
    }
    
    
    private func getPositions(){
        // Parameters
        let parameters: Parameters = ["email": preferences.string(forKey: "email")!]
        
        // Send post request to server
        Alamofire.request("http://217.211.176.94:1212/positions.php", method: .post, parameters: parameters)
            .responseJSON { response in
                print(response.result.value! )
                // Handle response
                if let result = response.result.value {
                    let JSON = result as! NSDictionary
                    
                    // Everythings OK
                    if JSON["status"] as! String == "OK"{
                        
                        let JSONArray = JSON["coords"] as! NSArray
                        if JSONArray.count > 0 {
                            for i in 0 ... JSONArray.count-1 {
                                
                                let item = JSONArray[i] as! [String : AnyObject]
                                
                                let lat: Double =  Double(item["lat"] as! String)!
                                let lng: Double =  Double(item["lng"] as! String)!
                                
                                let newAnnotation = MKPointAnnotation()
                                newAnnotation.coordinate.latitude = lat
                                newAnnotation.coordinate.longitude = lng
                                
                                
                                self.mapView.addAnnotation(newAnnotation)
                                
                                //     self.tasks.append(t)
                            }
                        }
                        
                        
                    } else { // Something went wrong
                        print(JSON["reason"]!)
                        self.errorLabel.text = JSON["reason"]! as? String
                    }
                }
        }
    }
}







