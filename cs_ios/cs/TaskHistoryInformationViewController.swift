//
//  TaskHistoryInformationViewController.swift
//  cs
//
//  Created by Johan Waller on 2017-11-21.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit

class TaskHistoryInformationViewController: UIViewController {
    
    

    @IBOutlet weak var questionLabel: UILabel!
    @IBOutlet weak var answerLabel: UILabel!
    @IBOutlet weak var createdLabel: UILabel!
    @IBOutlet weak var completedLabel: UILabel!
    
    let interpreter = SensorValueInterpreter()
    
    var task: TaskHistory.Task!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        let type = task.sensor!
        let answer = task.answer!
        var text: String = answer
        

        
        if answer == "no answer"{
            text = answer
        }
        else{
            switch type {
            case "light":
                text = interpreter.makeSenseOfLightReadings(data: task.answer)
            case "pressure":
                text = interpreter.makeSenseOfPressureReadings(data: task.answer, duration: task.duration)
            case "ambient_temperature":
                text = interpreter.makeSenseOfTemperatureReadings(data: task.answer)
            default:
                print("lol")
            }
        }
        questionLabel.text = task.question
        answerLabel.text = text
        createdLabel.text = task.created
        completedLabel.text = task.completed

    }

}
