//
//  SecondViewController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

class DeviceInformation: UIViewController {
    
    @IBOutlet weak var informationLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let device = UIDevice()
        informationLabel.text = device.systemName + " " + device.systemVersion
        
    }
    
}


