package com.harsh.fittrack.di

import com.harsh.fittrack.core.time.Clock
import com.harsh.fittrack.core.time.SystemClock
import com.harsh.fittrack.core.util.GreetingProvider
import com.harsh.fittrack.data.local.DatabaseFactory
import com.harsh.fittrack.data.local.catalog.StaticExerciseCatalog
import com.harsh.fittrack.data.repository.AuthRepositoryImpl
import com.harsh.fittrack.data.repository.UserRepositoryImpl
import com.harsh.fittrack.data.repository.WorkoutRepositoryImpl
import com.harsh.fittrack.db.FitTrackDatabase
import com.harsh.fittrack.domain.repository.AuthRepository
import com.harsh.fittrack.domain.repository.ExerciseCatalog
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import com.harsh.fittrack.domain.usecase.record.ValidateWorkoutUseCase
import com.harsh.fittrack.feature.auth.AuthViewModel
import com.harsh.fittrack.feature.exercises.ExercisesViewModel
import com.harsh.fittrack.feature.home.HomeViewModel
import com.harsh.fittrack.feature.home.WorkoutDetailViewModel
import com.harsh.fittrack.feature.profile.ProfileViewModel
import com.harsh.fittrack.feature.record.RecordViewModel
import org.koin.core.parameter.parametersOf
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin module — bindings that work on every platform.
 *
 * Platform-specific bindings ([DatabaseFactory], [OAuthCredentialProvider]) are
 * supplied by `androidModule(...)` / `iosModule()` from the respective sourceSets.
 */
val sharedModule: Module = module {

    // --- core ---
    single<Clock> { SystemClock() }
    single { GreetingProvider(get()) }

    // --- database ---
    single<FitTrackDatabase> { get<DatabaseFactory>().create() }

    // --- firebase ---
    single<FirebaseAuth> { Firebase.auth }

    // --- data sources & repositories ---
    single<ExerciseCatalog> { StaticExerciseCatalog() }
    single<AuthRepository> { AuthRepositoryImpl(firebaseAuth = get(), credentials = get()) }
    single<UserRepository> { UserRepositoryImpl() }
    single<WorkoutRepository> { WorkoutRepositoryImpl(db = get()) }

    // --- use cases ---
    factory { ValidateWorkoutUseCase() }

    // --- view models ---
    factory { AuthViewModel(authRepository = get()) }
    factory { HomeViewModel(userRepository = get(), workoutRepository = get(), greetingProvider = get(), clock = get()) }
    factory { RecordViewModel(workoutRepository = get(), catalog = get(), validateWorkout = get(), clock = get()) }
    factory { ExercisesViewModel(catalog = get()) }
    factory { (workoutId: String) -> WorkoutDetailViewModel(workoutId = workoutId, workoutRepository = get()) }
    factory { ProfileViewModel(userRepository = get(), workoutRepository = get(), authRepository = get(), clock = get()) }
}
