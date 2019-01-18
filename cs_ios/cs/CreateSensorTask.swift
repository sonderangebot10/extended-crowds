//
//  CreateSensorTask.swift
//  cs
//
//  Created by Johan Waller on 2017-11-13.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import Alamofire
import MapKit

class CreateSensorTask: UIViewController, UITableViewDelegate, UITableViewDataSource,
            UIGestureRecognizerDelegate, CLLocationManagerDelegate, UITextFieldDelegate {

    
    // MARK: Variable declarations
    
    @IBOutlet weak var sensorTable: UITableView!
    @IBOutlet weak var readingsField: UITextField!
    @IBOutlet weak var mapView: MKMapView!
    @IBOutlet weak var errorLabel: UILabel!
    
    let preferences = UserDefaults.standard
    
    // MapView variables
    var initialLocation: CLLocation!
    let regionRadius: CLLocationDistance = 2000
    var annotation: MKPointAnnotation?
    var locationManager:CLLocationManager!
    var tapped: Bool = false
    
    var checked = [Bool]() // Have an array equal to the number of cells in your table
    var c = 0

    
    // Data model: These strings will be the data for the table view cells
    let sensors: [String] = ["ambient temperature", "light", "pressure"]
    
    // cell reuse id (cells that scroll out of view can be reused)
    let cellReuseIdentifier = "cell"
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Register the table view cell class and its reuse id
        self.sensorTable.register(UITableViewCell.self, forCellReuseIdentifier: cellReuseIdentifier)
        self.sensorTable.backgroundColor = UIColor(red: 0.45, green: 0.45, blue: 0.45, alpha: 0.00)
     
        // This view controller itself will provide the delegate methods and row data for the table view.
        sensorTable.delegate = self
        sensorTable.dataSource = self
        
        // Setup so that you can tab to the next textfield by pressing 'next' on the keyboard
        readingsField.tag = 0
        readingsField.delegate = self
        
        // Change color of the placeholder text
        readingsField.attributedPlaceholder = NSAttributedString(string: "Number of readings",
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
      //  getPositions()
        
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        determineCurrentLocation()
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        self.locationManager.stopUpdatingLocation()
    }
    
    
    // MARK: Delegate functions
    
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
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.sensors.count
    }
    
    // method to run when table view cell is tapped
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        print("You tapped cell number \(indexPath.row).")
        c = indexPath.row
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.backgroundColor = UIColor.clear
    }
    
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell:MyTableViewCell = self.sensorTable.dequeueReusableCell(withIdentifier: "MyTableViewCell") as! MyTableViewCell!
        
        // set the text from the data model
        cell.titleLabel.text = self.sensors[indexPath.row]
        
        return cell
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
    
    func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
        var annotationView = mapView.dequeueReusableAnnotationView(withIdentifier: "myAnnotation") as? MKPinAnnotationView
        
        if annotationView == nil {
            annotationView = MKPinAnnotationView(annotation: annotation, reuseIdentifier: "myAnnotation")
        } else {
            annotationView?.annotation = annotation
        }
        
        if let annotation = annotation as? Annotation {
            annotationView?.pinTintColor = annotation.pinTintColor
        }
        
        return annotationView
    }
    
    // MARK: Actions
    
    @objc func handleTap(gestureReconizer: UITapGestureRecognizer) {
        
        let location = gestureReconizer.location(in: mapView)
        let coordinate = mapView.convert(location,toCoordinateFrom: mapView)
        tapped = true
        
        // Add/move annotation:
        annotation!.coordinate = coordinate
        mapView.addAnnotation(annotation!)
        
    }
    
    @IBAction func createPressed(_ sender: UIButton) {
        print(sensors[c])
        
        let lat = annotation?.coordinate.latitude
        let lng = annotation?.coordinate.longitude
        let readings = readingsField.text ?? ""
    
        if readings == ""{
            errorLabel.text = "Number of readings can not be empty"
        }
        else if readings == "0" {
            errorLabel.text = "Number of must be greater than zero"
        }
        else if !tapped { // gotta tap that map
            errorLabel.text = "Please tap the map to provide a location"
        }
        else { // we are good to go if we come here
            // Parameters
            let parameters: Parameters = ["email": preferences.string(forKey: "email")!,
                                          "description": "empty",
                                          "firebase": "hahha",
                                          "duration": (Int(readings)!-1)*9,
                                          "readings": readings,
                                          "lat": lat!,
                                          "lng": lng!,
                                          "type": "sensor",
                                          "file": "sensor.php",
                                          "sensor": sensors[c].replacingOccurrences(of: " ", with: "_")]
            
            // Send post request to server
            Alamofire.request("http://217.211.176.94:1212/create_task.php", method: .post, parameters: parameters)
                .responseJSON { response in
                    print(response.result.value! )
                    // Handle response
                    if let result = response.result.value {
                        let JSON = result as! NSDictionary
                        
                        print("reply on create sensor task: \(JSON["status"])")
                        
                        // Everythings OK
                        if JSON["status"] as! String == "OK"{
                            
                            // stop updating location to save battery
                            self.locationManager.stopUpdatingLocation()
                            
                            // go back one view
                            _ = self.navigationController?.popViewController(animated: true)
 
                            
                        } else { // Something went wrong
                            print(JSON["reason"]!)
                            self.errorLabel.text = JSON["reason"]! as? String
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

