//
//  Untitled.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//
import SwiftUI
import Shared
import Combine

class ShowViewModel : ObservableObject {
    private let show: Show
    @Published var episodes : [Episode] = []
    @Published var isUnmatched : Bool = false
    @Published var isLoading: Bool = true
    private let getEpisodes = KoinDependencies.shared.getEpisodesUseCase
    
    init(show: Show) {
        self.show = show
        
        Task{
            for await episodeList in self.getEpisodes.invoke(showId: show.id){
                if(episodeList == nil){
                    isUnmatched = true
                }else{
                    isUnmatched = false
                    episodes = episodeList!
                }
                isLoading = false
            }
        }
    }
}

struct ShowView: View {
    let show: Show
    @ObservedObject var viewModel: ShowViewModel
    
    init(show: Show) {
        self.show = show
        self.viewModel = ShowViewModel(show: show)
    }
    
    var body: some View {
        if(viewModel.isLoading){
            VStack{
                Text("Loading...")
            }.padding()
        }else{
            if(viewModel.isUnmatched){
                NavigationLink(destination: MatchView(show: show)){
                    Text("Match Show")
                }
            }else{
                List(viewModel.episodes, id: \.id){ episode in
                    Text(episode.name)
                }
            }
        }
    }
}
