//
//  AppDelegate.swift
//  cs
//
//  Created by Johan Waller on 2017-11-03.
//  Copyright © 2017 Johan Waller. All rights reserved.
//

import UIKit
import UserNotifications

import Firebase
import Alamofire
import CoreLocation



@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, CLLocationManagerDelegate {

    var window: UIWindow?
    let gcmMessageIDKey = "gcm.message_id"
    
    let preferences = UserDefaults.standard
    let locationManager = CLLocationManager()
    var lat: Double?
    var lng: Double?


    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        do {
            Network.reachability = try Reachability(hostname: "www.google.com")
            do {
                try Network.reachability?.start()
            } catch let error as Network.Error {
                print(error)
            } catch {
                print(error)
            }
        } catch {
            print(error)
        }
        
        // location mngr
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
    
        FirebaseApp.configure()
    
        // [START set_messaging_delegate]
        Messaging.messaging().delegate = self
        // [END set_messaging_delegate]
    
        // Register for remote notifications. This shows a permission dialog on first run, to
        // show the dialog at a more appropriate time move this registration accordingly.
        // [START register_for_notifications]
        if #available(iOS 10.0, *) {
            // For iOS 10 display notification (sent via APNS)
            UNUserNotificationCenter.current().delegate = self
            
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_, _ in })
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            application.registerUserNotificationSettings(settings)
        }
    
        application.registerForRemoteNotifications()
    
        // [END register_for_notifications]
    
        return true
    }

    // [START receive_message]
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        // If you are receiving a notification message while your app is in the background,
        // this callback will not be fired till the user taps on the notification launching the application.
        // TODO: Handle data of notification
        
        // With swizzling disabled you must let Messaging know about the message, for Analytics
        // Messaging.messaging().appDidReceiveMessage(userInfo)
        
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
        
        if let data = userInfo["data"] as? String{
            
            var map = [String : String]()
            var question: String!
            var id: String!
            var choices: String!
            var type: String!
            var hit_type: String!
            locationManager.startUpdatingLocation()
            
            let dataArr = data.components(separatedBy: "{")
            let payload = dataArr[2].replacingOccurrences(of: "\"", with: "").replacingOccurrences(of: "}", with: "")
            let payloadArr = payload.components(separatedBy: ",")
            for s in payloadArr {
                let tokens = s.components(separatedBy: ":")
                if tokens[0] == "id"{
                    if tokens.count < 3 {
                        map.updateValue(tokens[1], forKey: tokens[0])
                    }
                    else{
                        map.updateValue(tokens[1]+":"+tokens[2], forKey: tokens[0])
                    }
                    
                } else{
                    map.updateValue(tokens[1], forKey: tokens[0])
                }
            }
            print(map)
            
            type = map["type"]
            switch type{
            case "hit":
                print("new hit")
                question = map["question"]
                choices  = map["choices"]
                hit_type = map["hit_type"]
                id = map["id"]
                
                let task: Task = Task(id: id, question: question, type: hit_type, options: choices)
                let dict:[String: Task] = ["new": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "expired":
                print("expired")
                id = map["id"]
                
                let task: Task = Task(id: id, question: "", type: "", options: "")
                let dict:[String: Task] = ["expired": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "heartbeat":
                print("heartbeat")
                // Parameters
                let parameters: Parameters = ["email": preferences.string(forKey: "email") ?? "default",
                                              "lat": lat ?? 0,
                                              "lng": lng ?? 0]
                
                // Send post request to server
                Alamofire.request("http://217.211.176.94:1212/heartbeat_update.php", method: .post, parameters: parameters)
            case "forced_logout":
                let dict:[String: Bool] = ["forced_logout": true]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
                print("logout")
            default:
                print("lol")
            }
            
            
            self.locationManager.stopUpdatingLocation()
        }
        
        
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // If you are receiving a notification message while your app is in the background,
        // this callback will not be fired till the user taps on the notification launching the application.
        // TODO: Handle data of notification
        
        // With swizzling disabled you must let Messaging know about the message, for Analytics
        // Messaging.messaging().appDidReceiveMessage(userInfo)
        
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
 
        if let data = userInfo["data"] as? String{

            var map = [String : String]()
            var question: String!
            var id: String!
            var choices: String!
            var type: String!
            var hit_type: String!
            locationManager.startUpdatingLocation()
            
            let dataArr = data.components(separatedBy: "{")
            let payload = dataArr[2].replacingOccurrences(of: "\"", with: "").replacingOccurrences(of: "}", with: "")
            let payloadArr = payload.components(separatedBy: ",")
            for s in payloadArr {
                let tokens = s.components(separatedBy: ":")
                if tokens[0] == "id"{
                    if tokens.count < 3 {
                        map.updateValue(tokens[1], forKey: tokens[0])
                    }
                    else{
                        map.updateValue(tokens[1]+":"+tokens[2], forKey: tokens[0])
                    }

                } else{
                    map.updateValue(tokens[1], forKey: tokens[0])
                }
            }
            print(map)
            
            type = map["type"]
            switch type{
            case "hit":
                print("new hit")
                question = map["question"]
                choices  = map["choices"]
                hit_type = map["hit_type"]
                id = map["id"]
                
                let task: Task = Task(id: id, question: question, type: hit_type, options: choices)
                let dict:[String: Task] = ["new": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "expired":
                print("expired")
                id = map["id"]
                
                let task: Task = Task(id: id, question: "", type: "", options: "")
                let dict:[String: Task] = ["expired": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "heartbeat":
                print(!)
                // Parameters
                
                let parameters: Parameters = ["email": preferences.string(forKey: "email") ?? "default",
                                              "lat": lat ?? 0,
                                              "lng": lng ?? 0]
                
                // Send post request to server
                Alamofire.request("http://217.211.176.94:1212/heartbeat_update.php", method: .post, parameters: parameters)
            case "forced_logout":
                let dict:[String: Bool] = ["forced_logout": true]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
                print("logout")
            default:
                print("lol")
            }
            
            
         }

        self.locationManager.stopUpdatingLocation()
        completionHandler(UIBackgroundFetchResult.newData)
    }
    // [END receive_message]

    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Unable to register for remote notifications: \(error.localizedDescription)")
    }

    // This function is added here only for debugging purposes, and can be removed if swizzling is enabled.
    // If swizzling is disabled then this function must be implemented so that the APNs token can be paired to
    // the FCM registration token.
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("APNs token retrieved: \(deviceToken)")
        
        if let refreshedToken = InstanceID.instanceID().token() {
            print("InstanceID token: \(refreshedToken)")
            preferences.set(refreshedToken, forKey: "token")
        }
        
        // With swizzling disabled you must set the APNs token here.
        // Messaging.messaging().apnsToken = deviceToken
    }
}

// [START ios_10_message_handling]
@available(iOS 10, *)
extension AppDelegate : UNUserNotificationCenterDelegate {
    
    // Receive displayed notifications for iOS 10 devices.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        
        // With swizzling disabled you must let Messaging know about the message, for Analytics
        // Messaging.messaging().appDidReceiveMessage(userInfo)
        
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
        
        // Print full message.
        print(userInfo)
        
        print("notification??")
        completionHandler([])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        // Print message ID.
        if let messageID = userInfo[gcmMessageIDKey] {
            print("Message ID: \(messageID)")
        }
        
        if let data = userInfo["data"] as? String{
            
            var map = [String : String]()
            var question: String!
            var id: String!
            var choices: String!
            var type: String!
            var hit_type: String!
            locationManager.startUpdatingLocation()
            
            let dataArr = data.components(separatedBy: "{")
            let payload = dataArr[2].replacingOccurrences(of: "\"", with: "").replacingOccurrences(of: "}", with: "")
            let payloadArr = payload.components(separatedBy: ",")
            for s in payloadArr {
                let tokens = s.components(separatedBy: ":")
                if tokens[0] == "id"{
                    if tokens.count < 3 {
                        map.updateValue(tokens[1], forKey: tokens[0])
                    }
                    else{
                        map.updateValue(tokens[1]+":"+tokens[2], forKey: tokens[0])
                    }
                    
                } else{
                    map.updateValue(tokens[1], forKey: tokens[0])
                }
            }
            print(map)
            
            type = map["type"]
            switch type{
            case "hit":
                print("new hit")
                question = map["question"]
                choices  = map["choices"]
                hit_type = map["hit_type"]
                id = map["id"]
                
                let task: Task = Task(id: id, question: question, type: hit_type, options: choices)
                let dict:[String: Task] = ["new": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "expired":
                print("expired")
                id = map["id"]
                
                let task: Task = Task(id: id, question: "", type: "", options: "")
                let dict:[String: Task] = ["expired": task]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
            case "heartbeat":
                print(!)
                // Parameters
                

                let parameters: Parameters = ["email": preferences.string(forKey: "email") ?? "default",
                                              "lat": lat ?? 0,
                                              "lng": lng ?? 0]
                
                
                // Send post request to server
                Alamofire.request("http://217.211.176.94:1212/heartbeat_update.php", method: .post, parameters: parameters)
            case "forced_logout":
                let dict:[String: Bool] = ["forced_logout": true]
                NotificationCenter.default.post(name: NSNotification.Name(rawValue: "load"), object: nil, userInfo: dict)
                print("logout")
            default:
                print("lol")
            }
            
            
        }
        
        self.locationManager.stopUpdatingLocation()
        completionHandler()
    }
}
// [END ios_10_message_handling]


extension AppDelegate : MessagingDelegate {
    // [START refresh_token]
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String) {
        print("Firebase registration token: \(fcmToken)")
        preferences.set(fcmToken, forKey: "token")
        
        // TODO: If necessary send token to application server.
        // Note: This callback is fired at each app startup and whenever a new token is generated.
    }
    // [END refresh_token]
    
    // [START ios_10_data_message]
    // Receive data messages on iOS 10+ directly from FCM (bypassing APNs) when the app is in the foreground.
    // To enable direct data messages, you can set Messaging.messaging().shouldEstablishDirectChannel to true.
    func messaging(_ messaging: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {
        print("Received data message: \(remoteMessage.appData)")
    }
    // [END ios_10_data_message]
    
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let locValue:CLLocationCoordinate2D = manager.location!.coordinate
        //  print("locations = \(locValue.latitude) \(locValue.longitude)")
        lat = locValue.latitude
        lng = locValue.longitude
    }
}


