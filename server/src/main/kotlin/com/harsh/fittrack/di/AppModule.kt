package com.harsh.fittrack.di

import com.harsh.fittrack.data.repository.ExerciseRepositoryImpl
import com.harsh.fittrack.data.repository.UserRepositoryImpl
import com.harsh.fittrack.data.repository.WorkoutRepositoryImpl
import com.harsh.fittrack.domain.repository.ExerciseRepository
import com.harsh.fittrack.domain.repository.UserRepository
import com.harsh.fittrack.domain.repository.WorkoutRepository
import org.koin.dsl.module

val serverModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<ExerciseRepository> { ExerciseRepositoryImpl() }
    single<WorkoutRepository> { WorkoutRepositoryImpl() }
}
