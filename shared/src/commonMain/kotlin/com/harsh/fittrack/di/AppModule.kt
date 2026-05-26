package com.harsh.fittrack.di

import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.core.time.SystemClock
import com.harsh.fittrack.core.util.GreetingProvider
import com.harsh.fittrack.data.local.DatabaseFactory
import com.harsh.fittrack.data.remote.FitTrackApi
import com.harsh.fittrack.data.remote.FitTrackApiImpl
import com.harsh.fittrack.data.remote.TokenStore
import com.harsh.fittrack.data.repository.AuthRepositoryImpl
import com.harsh.fittrack.data.repository.ExerciseRepositoryImpl
import com.harsh.fittrack.data.repository.PersonalRecordRepositoryImpl
import com.harsh.fittrack.data.repository.TemplateRepositoryImpl
import com.harsh.fittrack.data.repository.UserRepositoryImpl
import com.harsh.fittrack.data.repository.WorkoutRepositoryImpl
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.repository.AuthRepository
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.PersonalRecordRepository
import com.harsh.fittrack.domain.repository.TemplateRepository
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.domain.usecase.stats.CalculateStreakUseCase
import com.harsh.fittrack.domain.usecase.stats.CalculateWeeklyWorkoutsUseCase
import com.harsh.fittrack.feature.auth.AuthViewModel
import com.harsh.fittrack.feature.exercises.ExercisesViewModel
import com.harsh.fittrack.feature.home.HomeViewModel
import com.harsh.fittrack.feature.home.WorkoutDetailViewModel
import com.harsh.fittrack.feature.profile.ProfileViewModel
import com.harsh.fittrack.feature.progress.ProgressViewModel
import com.harsh.fittrack.feature.record.RecordViewModel
import com.harsh.fittrack.feature.record.TemplateViewModel
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule: Module = module {

    // --- core ---
    single<Clock> { SystemClock() }
    single { GreetingProvider(get()) }

    // --- database ---
    single<FitTrackDatabase> { get<DatabaseFactory>().create() }

    // --- token store ---
    single { TokenStore() }

    // --- remote ---
    single<FitTrackApi> {
        FitTrackApiImpl(
            tokenStore = get(),
            baseUrl = getOrNull(named("apiBaseUrl")) ?: "http://localhost:8080",
        )
    }

    // --- data sources & repositories ---
    single<AuthRepository> { AuthRepositoryImpl(db = get(), api = get(), tokenStore = get()) }
    single<ExerciseRepository> { ExerciseRepositoryImpl(db = get(), api = get()) }
    single<UserRepository> { UserRepositoryImpl(authRepository = get(), db = get(), api = get()) }
    single<PersonalRecordRepository> { PersonalRecordRepositoryImpl(db = get(), api = get()) }
    single<TemplateRepository> { TemplateRepositoryImpl(db = get(), api = get()) }
    single<WorkoutRepository> { WorkoutRepositoryImpl(db = get(), api = get(), personalRecordRepository = get()) }

    // --- use cases ---
    factory { ValidateWorkoutUseCase() }
    factory { CalculateStreakUseCase() }
    factory { CalculateWeeklyWorkoutsUseCase() }

    // --- view models ---
    factory { AuthViewModel(authRepository = get()) }
    factory { HomeViewModel(userRepository = get(), workoutRepository = get(), greetingProvider = get(), clock = get(), calculateStreak = get(), calculateWeeklyWorkouts = get(), api = get()) }
    factory { RecordViewModel(workoutRepository = get(), exerciseRepository = get(), validateWorkout = get(), clock = get(), personalRecordRepository = get()) }
    factory { TemplateViewModel(templateRepository = get()) }
    factory { ProgressViewModel(api = get()) }
    factory { ExercisesViewModel(exerciseRepository = get()) }
    factory { (workoutId: String) -> WorkoutDetailViewModel(workoutId = workoutId, workoutRepository = get()) }
    factory { ProfileViewModel(userRepository = get(), workoutRepository = get(), authRepository = get(), clock = get(), calculateStreak = get()) }
}
