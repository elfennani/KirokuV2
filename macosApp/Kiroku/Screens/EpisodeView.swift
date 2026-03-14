//
//  EpisodeView.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//

import SwiftUI
import Shared
import Combine

class EpisodeViewModel: ObservableObject {
    let episode: Episode
    @Published var urls: [URL] = []
    
    
    init(episode: Episode) {
        self.episode = episode
        
        Task {
            
        }
    }
}
