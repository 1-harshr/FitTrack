package com.harsh.fittrack.data.local.mapper

import com.harsh.fittrack.db.ExerciseEntity
import com.harsh.fittrack.db.SetEntryEntity
import com.harsh.fittrack.db.UserEntity
import com.harsh.fittrack.db.WorkoutEntity
import com.harsh.fittrack.domain.model.Equipment
import com.harsh.fittrack.domain.model.MovementType
import com.harsh.fittrack.domain.model.MuscleGroup
import com.harsh.fittrack.domain.model.Units
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MappersTest {

    // ── ExerciseEntity → Exercise ─────────────────────────────────────────────

    @Test
    fun `ExerciseEntity maps to domain Exercise correctly`() {
        val entity = ExerciseEntity(
            id = "bench_press",
            name = "Bench Press",
            primaryMuscle = "CHEST",
            secondaryMuscles = "ARMS|SHOULDERS",
            equipment = "BARBELL",
            movementType = "COMPOUND",
            instructions = "Step 1|Step 2",
            isCustom = 0L,
            catalogVersion = 1L,
        )
        val domain = entity.toDomain()

        assertEquals("bench_press", domain.id)
        assertEquals("Bench Press", domain.name)
        assertEquals(MuscleGroup.CHEST, domain.primaryMuscle)
        assertEquals(listOf(MuscleGroup.ARMS, MuscleGroup.SHOULDERS), domain.secondaryMuscles)
        assertEquals(Equipment.BARBELL, domain.equipment)
        assertEquals(MovementType.COMPOUND, domain.movementType)
        assertEquals(listOf("Step 1", "Step 2"), domain.instructions)
    }

    @Test
    fun `ExerciseEntity with empty secondaryMuscles maps to empty list`() {
        val entity = ExerciseEntity(
            id = "plank",
            name = "Plank",
            primaryMuscle = "CORE",
            secondaryMuscles = "",
            equipment = "BODYWEIGHT",
            movementType = "ISOLATION",
            instructions = "Hold|Breathe",
            isCustom = 0L,
            catalogVersion = 1L,
        )
        val domain = entity.toDomain()
        assertTrue(domain.secondaryMuscles.isEmpty())
    }

    @Test
    fun `ExerciseEntity with unknown enum defaults gracefully`() {
        val entity = ExerciseEntity(
            id = "mystery",
            name = "Mystery",
            primaryMuscle = "UNKNOWN_MUSCLE",  // not a valid enum
            secondaryMuscles = "",
            equipment = "UNKNOWN_EQUIP",
            movementType = "UNKNOWN_MOVE",
            instructions = "",
            isCustom = 0L,
            catalogVersion = 1L,
        )
        val domain = entity.toDomain()
        // Defaults to CHEST / BODYWEIGHT / COMPOUND — just must not throw
        assertEquals(MuscleGroup.CHEST, domain.primaryMuscle)
        assertEquals(Equipment.BODYWEIGHT, domain.equipment)
        assertEquals(MovementType.COMPOUND, domain.movementType)
    }

    // ── WorkoutEntity → Workout ───────────────────────────────────────────────

    @Test
    fun `WorkoutEntity maps completed flag correctly`() {
        val base = WorkoutEntity(
            id = "w1", userId = "u1", title = "Morning Workout",
            date = "2026-05-22", startedAt = 1000L,
            durationSeconds = 3600L, totalVolumeKg = 150.0,
            isCompleted = 1L,
        )
        assertTrue(base.toDomain().isCompleted)

        val inactive = base.copy(isCompleted = 0L)
        assertFalse(inactive.toDomain().isCompleted)
    }

    @Test
    fun `WorkoutEntity date string parses to LocalDate`() {
        val entity = WorkoutEntity(
            id = "w2", userId = "u1", title = "Test",
            date = "2026-05-22", startedAt = 0L,
            durationSeconds = 0L, totalVolumeKg = 0.0,
            isCompleted = 1L,
        )
        assertEquals(LocalDate.parse("2026-05-22"), entity.toDomain().date)
    }

    // ── SetEntryEntity → SetEntry ─────────────────────────────────────────────

    @Test
    fun `SetEntryEntity maps isCompleted flag correctly`() {
        val base = SetEntryEntity(
            id = "s1", exerciseEntryId = "e1",
            setNumber = 1L, reps = 8L, weight = 60.0,
            isCompleted = 1L,
        )
        assertTrue(base.toDomain().isCompleted)
        assertFalse(base.copy(isCompleted = 0L).toDomain().isCompleted)
    }

    @Test
    fun `SetEntryEntity maps numeric fields`() {
        val entity = SetEntryEntity(
            id = "s2", exerciseEntryId = "e1",
            setNumber = 3L, reps = 12L, weight = 80.5,
            isCompleted = 1L,
        )
        val domain = entity.toDomain()
        assertEquals(3, domain.setNumber)
        assertEquals(12, domain.reps)
        assertEquals(80.5, domain.weight)
    }

    // ── UserEntity → User ─────────────────────────────────────────────────────

    @Test
    fun `UserEntity maps units correctly`() {
        val kg = UserEntity(id = "u1", name = "Alice", email = "a@b.com", photoUrl = null, units = "KG")
        assertEquals(Units.KG, kg.toDomain().units)

        val lbs = UserEntity(id = "u2", name = "Bob", email = "b@c.com", photoUrl = null, units = "LBS")
        assertEquals(Units.LBS, lbs.toDomain().units)
    }

    @Test
    fun `UserEntity with unknown units defaults to KG`() {
        val entity = UserEntity(id = "u3", name = "Carol", email = "c@d.com", photoUrl = null, units = "STONE")
        assertEquals(Units.KG, entity.toDomain().units)
    }

    // ── Exercise roundtrip ────────────────────────────────────────────────────

    @Test
    fun `Exercise roundtrip domain → entity → domain is stable`() {
        val entity = ExerciseEntity(
            id = "squat", name = "Squat",
            primaryMuscle = "LEGS",
            secondaryMuscles = "GLUTES|CORE",
            equipment = "BARBELL",
            movementType = "COMPOUND",
            instructions = "Stand tall|Descend|Drive up",
            isCustom = 0L,
            catalogVersion = 1L,
        )
        val domain = entity.toDomain()
        val backToEntity = domain.toEntity()
        val backToDomain = backToEntity.toDomain()

        assertEquals(domain.id, backToDomain.id)
        assertEquals(domain.name, backToDomain.name)
        assertEquals(domain.primaryMuscle, backToDomain.primaryMuscle)
        assertEquals(domain.secondaryMuscles, backToDomain.secondaryMuscles)
        assertEquals(domain.equipment, backToDomain.equipment)
        assertEquals(domain.movementType, backToDomain.movementType)
        assertEquals(domain.instructions, backToDomain.instructions)
    }
}
