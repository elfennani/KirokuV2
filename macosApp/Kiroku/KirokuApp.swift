//
//  KirokuApp.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//

import SwiftUI
import Shared

@main
struct KirokuApp: App {
    @State private var appResetID = UUID()
    
    init() {
        doInitKoin()
    }
    
    var body: some Scene {
        Window("Kiroku", id: "Kiroku") {
            ContentView()
                .id(appResetID)
                .onOpenURL { url in
                    Task{
                        await handleDeepLink(url)
                    }
                }
        }
    }
    
    func handleDeepLink(_ url: URL) async {
        print("Parsing URL")
        let urlString = url.absoluteString
        guard let newUrl = URL(string: urlString.replacing("#", with: "?", maxReplacements: 1)) else {
            return
        }
        print("Parsed!")

        guard
            let components = URLComponents(url: newUrl, resolvingAgainstBaseURL: false),
            let queryItems = components.queryItems
        else {
            return
        }
        print("Got token!")

        guard let accessToken = queryItems.first(where: { $0.name == "access_token" })?.value else {return}
        print("Extracted Token!")
        let saveSession = KoinDependencies.shared.saveSessionUseCase
    
        let _ = try? await saveSession.invoke(token: accessToken)
        print("Saved Token!")
        
        appResetID = UUID()
        print("Resetting App")
    }
}
