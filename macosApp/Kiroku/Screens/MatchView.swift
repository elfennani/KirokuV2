//
//  MatchScreen.swift
//  Kiroku
//
//  Created by Nizar Elfennani on 13/3/2026.
//

import SwiftUI
import Shared
import Combine

class MatchViewModel: ObservableObject {
    let show: Show
    @Published var result: [BasicShow] = []
    @Published var isLoading: Bool = true
    private let search = KoinDependencies.shared.searchMatchingShowUseCase
    private let matchShow = KoinDependencies.shared.matchShowUseCase
    
    init(show:Show) {
        self.show = show
        Task{
            isLoading = true
            let result = try await search.invoke(showTitle: show.title)
            self.result = result
            isLoading = false
        }
    }
    
    func match(serviceId: String) async {
        try? await matchShow.invoke(showId: show.id, serviceId: serviceId)
    }
}

struct MatchView: View {
    let show: Show
    let viewModel : MatchViewModel
    @Environment(\.dismiss) var dismiss
    
    init(show: Show) {
        self.show = show
        self.viewModel = MatchViewModel(show: show)
    }
    
    var body: some View {
        if(viewModel.isLoading){
            VStack{
                Text("Loading...")
            }.padding()
        }else{
            List(viewModel.result, id: \.id){ item in
                Button("\(item.title)\(item.aniListId?.int32Value == show.id ? " (This is the same show!)" : "")"){
                    Task{
                        await viewModel.match(serviceId: item.id)
                        dismiss()
                    }
                }.buttonStyle(PlainButtonStyle())
            }
        }
    }
}
