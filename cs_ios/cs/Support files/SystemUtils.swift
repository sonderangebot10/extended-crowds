//
//  SystemUtils.swift
//  cs
//
//  Created by Johan Waller on 2017-11-07.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import Foundation

class SystemUtils {
    
    public func isEmailValid(email: String) -> Bool{
    //TODO: Replace this with your own logic
    return email.contains("@")
    }
    
    public func isPasswordValid(password: String) -> Bool {
    //TODO: Replace this with your own logic
    return password.count > 4 && password.count < 33;
    }
    
    public func isUsernameValid(username: String) -> Bool {
    //TODO: Replace this with your own logic
    return username.count > 2 && username.count < 17;
    }
    
    public func getTimeString() -> String{
        
        let date = Date()
        let calender = Calendar.current
        let components = calender.dateComponents([.year,.month,.day,.hour,.minute,.second], from: date)
        
        let year = components.year
        let month = components.month
        let day = components.day
        let hour = components.hour
        let minute = components.minute
        let second = components.second
        
        let today_string = String(year!) + "-" + String(month!) + "-" + String(day!) + " " + String(hour!)  + ":" + String(minute!) + ":" +  String(second!)
        
        return today_string
    }
}
