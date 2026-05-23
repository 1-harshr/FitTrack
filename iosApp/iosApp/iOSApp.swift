import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        MainViewControllerKt.startKoinIos(apiBaseUrl: "http://localhost:8080")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
