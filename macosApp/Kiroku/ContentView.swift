//
//  ContentView.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//

import SwiftUI
import Shared
import Combine

class ViewModel : ObservableObject {
    @Published var isAuthed: Bool? = nil
    private let getSession = KoinDependencies.shared.getSessionUseCase()
    
    init(isAuthed: Bool? = nil) {
        Task{
            let session = try? await getSession.invoke()
            self.isAuthed = session != nil
        }
    }
}

struct ContentView: View {
    @StateObject var viewModel = ViewModel()
    
    var body: some View {
        let isAuthed = viewModel.isAuthed
        NavigationStack{
            VStack {
                if(isAuthed == nil){
                    Text("Loading...").padding()
                }else if(isAuthed == true){
                    HomeView()
                }else {
                    LoginView()
                }
            }
        }
    }
}

#Preview {
    ContentView()
}
