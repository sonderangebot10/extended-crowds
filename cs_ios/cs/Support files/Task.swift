//
//  Task.swift
//  cs
//
//  Created by Johan Waller on 2017-11-23.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import Foundation

class Task: Codable {
    
    var question: String!
    var type: String!
    var options: String!
    var id: String!
    
    init(id: String, question: String, type: String, options: String){
        self.id = id
        self.question = question
        self.type = type
        self.options = options
    }

    
    
}
