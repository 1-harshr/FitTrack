import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        MainViewControllerKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}