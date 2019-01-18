//
//  SensorValueInterpreter.swift
//  cs
//
//  Created by Johan Waller on 2017-12-04.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import Foundation

class SensorValueInterpreter {
    
    
    
    func makeSenseOfTemperatureReadings(data: String) -> String{
        let readings = transformToFloatArray(data: data)
    
        // calculate the mean
        var mean: Float = 0
        var sum: Float = 0
        
        for f in readings {
            sum += f
        }
        mean = sum / Float(readings.count);
        
        return String(mean) + "°C";
    }
    
    /**
     * This function interprets given light readings as following states:
     *
     * Lighting condition    From (lux)    To (lux)    Mean value (lux)
     * Pitch Black        0              10             5
     * Very Dark          11             50             30
     * Dark Indoors       51             200            125
     * Dim Indoors        201            400            300
     * Normal Indoors     401            1000           700
     * Bright Indoors     1001           5000           3000
     * Dim Outdoors       5001           10,000         7500
     * Cloudy Outdoors    10,001         30,000         20,000
     * Direct Sunlight    30,001         100,000        65,000
     *
     * @param data Sensor readings
     * @return returns a sensible interpretation of the readings
     */
    func makeSenseOfLightReadings(data: String) -> String{
    
    // Data = "5;10;19;33"
    // where each reading are separated with a ";"
        let readings = transformToFloatArray(data: data)
    
        // calculate the mean
        var mean: Float = 0
        var sum: Float = 0
        
        for f in readings {
            sum += f
        }
        mean = sum / Float(readings.count);
        
        if(0 < mean && mean <= 10) {
            return "Pitch black (" + String(mean) + " lx)"
        }
        else if(10 < mean && mean <= 50) {
            return "Very dark (" + String(mean) + " lx)"
        }
        else if(50 < mean && mean <= 200) {
            return "Dark indoors (" + String(mean) + " lx)"
        }
        else if(200 < mean && mean <= 400) {
            return "Dim indoors (" + String(mean) + " lx)"
        }
        else if(400 < mean && mean <= 1000) {
            return "Normal indoors (" + String(mean) + " lx)"
        }
        else if(1000 < mean && mean <= 5000) {
            return "Bright indoors (" + String(mean) + " lx)"
        }
        else if(5000 < mean && mean <= 10000) {
            return "Dim outdoors (" + String(mean) + " lx)"
        }
        else if(10000 < mean && mean <= 30000) {
            return "Cloudy outdoors (" + String(mean) + " lx)"
        }
        else{
            return "Direct sunlight (" + String(mean) + " lx)"
        }
        
    }
    
    /**
     * This function interprets given pressure readings as following states:
     *
     * 1022.6894+
     * Rising or steady pressure means continued fair weather.
     * Slowly falling pressure means fair weather.
     * Rapidly falling pressure means cloudy and warmer conditions.
     *
     * 1009.1439-1022.6894
     * Rising or steady pressure means present conditions will continue.
     * Slowly falling pressure means little change in the weather.
     * Rapidly falling pressure means that rain is likely, or snow if it is cold enough.
     *
     * 1009.1439-
     * Rising or steady pressure indicates clearing and cooler weather.
     * Slowly falling pressure indicates rain
     * Rapidly falling pressure indicates a storm is coming.
     *
     * @param data the gathered pressure readings
     * @return Returns a sensible interpretation of the supplied data
     */
    
    func makeSenseOfPressureReadings(data: String, duration: String) -> String {
        
        let readings = transformToFloatArray(data: data)
        let duration = Int(duration)
        
        if readings[0] > 1022.6894 {
            if isPressureIncreasingRapidly(readings: readings, duration: duration!) >= 0 { // rising or steady
                return "The weather will probably continue as is."
            }
            else if(isPressureIncreasingRapidly(readings: readings, duration: duration!) == -1){ // slowly falling
                return "The weather will probably continue as is."
            }
            else { // rapidly falling
                return "The weather is probably getting cloudy and warmer."
            }
        }
        else if (readings[0] > 1009.1439 && readings[0] < 1022.6894){
            if(isPressureIncreasingRapidly(readings: readings, duration: duration!) > 0){ // rising or steady
                return "The weather will probably continue as is."
            }
            else if(isPressureIncreasingRapidly(readings: readings, duration: duration!) == -1){ // slowly falling
                return "The weather will probably continue as is."
            }
            else { // rapidly falling
                return "It is probably going to rain (or snow)."
            }
        }
        else{
            if(isPressureIncreasingRapidly(readings: readings, duration: duration!) >= 0){ // rising or steady
                return "The weather will probably be clearer and cooler"
            }
            else if(isPressureIncreasingRapidly(readings: readings, duration: duration!) == -1){ // slowly falling
                return "It is probably going to rain (or snow)."
            }
            else { // rapidly falling
                return "There will probably be a storm."
            }
        }
    }
    
    /**
     * Simple function to check if pressure is increasing rappidly or not
     * @param readings pressure readings
     * @return 1: increasing, -1: decreasing, 0: no major change
     */
    private func isPressureIncreasingRapidly(readings: [Float],duration: Int) -> Int {
    
        let dif = readings[0] - readings[readings.count - 1]
        if dif < 0 { // increase
            if duration >= 60{
                return 2 // rapid
            }
            else{
                return 1 // slowly
            }
        }
        else if dif > 0 {
            if duration >= 60 {
                return -2 // rapid
            }
            else{
                return -1 // slowly
            }
        }
        else {
            return 0 // no major change
        }
    }
    
    private func transformToFloatArray(data: String) -> [Float]{
    
        let tokens: [String] = data.components(separatedBy: ";")
        var readings = [Float]()
        for i in 0 ... tokens.count - 1 {
            print(i)
            readings.append(Float(tokens[i])!)
        }
     
        return readings
    }
    
}
