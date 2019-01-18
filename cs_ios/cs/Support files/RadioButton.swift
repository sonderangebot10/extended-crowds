//
//  RadioButton.swift
//  cs
//
//  Created by Johan Waller on 2017-11-17.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

@IBDesignable
public class RadioButton: UIButton {
        
    public var score: String!
    
    // Images
    let checkedImage = #imageLiteral(resourceName: "radio_button_checked")
    let uncheckedImage = #imageLiteral(resourceName: "radio_button_unchecked")
    
    override public var isSelected: Bool {
        didSet {
            setCheckedState()
        }
    }
    
    private func setCheckedState() {
        if self.isSelected {
            self.setImage(checkedImage, for: .normal)
        } else {
            self.setImage(uncheckedImage, for: .normal)
        }
    }
    
}
