//
//  SecondViewController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

class CreateHITTask: UIViewController {

    //MARK: Variable declarations
    
    @IBOutlet weak var stackView: UIStackView!
    
    var buttons =  [RadioButton]()
    var types: [String] = ["Single choice", "Multiple choice", "Numeric"]
    var selected: String = "Single choice"
    
    
    override func viewDidLoad() {
        super.viewDidLoad()

        stackView.spacing = 10
        
        for s in types {
            let horizontalStack = UIStackView()
            horizontalStack.axis = .horizontal
            horizontalStack.alignment = .center
            horizontalStack.distribution = .fill
            horizontalStack.spacing = 5
            
            let button = RadioButton()
            button.addTarget(self, action: #selector(manualAction(sender:)), for: .touchUpInside)
            button.score = s
            button.setImage(#imageLiteral(resourceName: "radio_button_unchecked"), for: .normal)
            buttons.append(button)
            
            let label = UILabel()
            label.text = s
            label.textColor = UIColor.white
            label.isUserInteractionEnabled = true
            
            horizontalStack.addArrangedSubview(button)
            horizontalStack.addArrangedSubview(label)
            
            stackView.addArrangedSubview(horizontalStack)
            
        }
        buttons[0].isSelected = true
        
    }
    
    //MARK: Actions
    @objc func manualAction (sender: RadioButton) {
        
        for b in buttons {
            b.isSelected = false
        }
        sender.isSelected = true
        selected = sender.score

    }
    
    @IBAction func continuePressed(_ sender: UIButton) {
        
        switch selected {
        case "Single choice":
            // Swtich veiw
            performSegue(withIdentifier: "singleSegue", sender: nil)
        case "Multiple choice":
            // Swtich veiw
            performSegue(withIdentifier: "multipleSegue", sender: nil)
        case "Numeric":
            // Swtich veiw
            performSegue(withIdentifier: "numericSegue", sender: nil)
        default:
            print("LOL")
        }
        
        
    }
}

