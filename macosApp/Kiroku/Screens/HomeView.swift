//
//  HomeView.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//
import SwiftUI
import Shared
import Combine

class HomeViewModel : ObservableObject {
    @Published
    private(set) var shows: [Show] = []
    private let getShows = KoinDependencies.shared.getCurrentlyWatchingUseCase
    
    func getResourceData<T>(resource: Resource<T>) -> T? {
        switch onEnum(of: resource) {
        case .loading:
            return nil
        case .error(_):
            return nil
        case .success(let success):
            return success.data as T?
        }
    }

    init() {
        Task{
            for await resource in getShows.invoke() {
                guard let data = getResourceData(resource: resource) as? [Show]? else {continue}
                if(data != nil){
                    self.shows = data.unsafelyUnwrapped
                }
            }
        }
    }
}

struct HomeView : View{
    @StateObject var viewModel = HomeViewModel()
    
    var body : some View {
        VStack{
            List(viewModel.shows, id: \.id){ show in
                NavigationLink(value: show){
                    Text(show.title)
                }
            }.navigationDestination(for: Show.self, destination: { show in
                ShowView(show: show)
            })
        }.padding()
    }
}
