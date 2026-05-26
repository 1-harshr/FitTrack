package com.harsh.fittrack.navigation

enum class NavTab(val startRoute: Route) {
    Home(Route.Home),
    Record(Route.RecordWorkout),
    Exercises(Route.ExerciseLibrary),
    Profile(Route.Profile),
    Progress(Route.Progress),
}
