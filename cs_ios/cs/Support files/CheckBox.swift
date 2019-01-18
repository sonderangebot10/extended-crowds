//
//  CheckBox.swift
//  cs
//
//  Created by Johan Waller on 2017-11-17.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

class CheckBox: UIButton {
    
    public var name: String!
    public var index: Int!
    
    // Images
    let checkedImage = #imageLiteral(resourceName: "checkbox_checked")
    let uncheckedImage = #imageLiteral(resourceName: "checkbox_unchecked")
    
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
