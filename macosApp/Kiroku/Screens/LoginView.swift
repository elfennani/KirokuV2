//
//  LoginScreen.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//

import SwiftUI
import Shared

struct LoginView: View {
    @Environment(\.openURL) var openURL
    
    func initiateLogin() {
        let clientID = Constants.shared.clientId
        let loginUrl =
            "https://anilist.co/api/v2/oauth/authorize?client_id=\(clientID)&response_type=token"
        
        openURL(URL(string:loginUrl)!)
    }
    
    var body: some View {
        VStack(alignment: .center){
            Text("Welcome to Kiroku!")
                .font(.headline)
            
            Button("Login via AniList"){
                initiateLogin()
            }
        }
    }
}
