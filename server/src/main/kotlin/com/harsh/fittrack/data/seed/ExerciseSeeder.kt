package com.harsh.fittrack.data.seed

import com.harsh.fittrack.data.table.ExercisesTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import java.time.OffsetDateTime

/**
 * Seeds the exercise catalog into the database on server startup.
 *
 * Uses INSERT OR REPLACE (upsert) keyed on [id] so it is safe to re-run on
 * every boot — existing rows are refreshed if their catalogVersion changed.
 *
 * To add or update exercises: bump the [catalogVersion] of the changed entries.
 * Clients query GET /exercises?sinceVersion=N and pull only the delta.
 */
object ExerciseSeeder {

    private val catalog: List<SeedExercise> = listOf(
        // ── Chest ────────────────────────────────────────────────────────────
        SeedExercise(
            id = "bench_press", name = "Bench Press",
            primaryMuscle = "CHEST", secondaryMuscles = listOf("ARMS", "SHOULDERS"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Lie flat on a bench, feet firmly planted on the floor.",
                "Grip the bar slightly wider than shoulder-width, unrack and hold it above your chest.",
                "Inhale and lower the bar to your mid-chest in a controlled arc.",
                "Press the bar back up explosively, exhaling at the top. Keep your back slightly arched.",
            ),
        ),
        SeedExercise(
            id = "incline_db_press", name = "Incline Dumbbell Press",
            primaryMuscle = "CHEST", secondaryMuscles = listOf("SHOULDERS", "ARMS"),
            equipment = "DUMBBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Set the bench to 30–45°. Sit with a dumbbell on each knee.",
                "Kick the dumbbells up and press them above your upper chest.",
                "Lower slowly until your elbows are at 90°, feeling a stretch.",
                "Drive the dumbbells back up and squeeze at the top.",
            ),
        ),
        SeedExercise(
            id = "cable_flye", name = "Cable Flye",
            primaryMuscle = "CHEST", secondaryMuscles = emptyList(),
            equipment = "CABLE", movementType = "ISOLATION",
            instructions = listOf(
                "Set both pulleys at shoulder height. Stand in the centre, one foot forward.",
                "Grab the handles with a slight bend in your elbows.",
                "Bring your hands together in a wide hugging arc, keeping elbows fixed.",
                "Return slowly under control and repeat.",
            ),
        ),
        SeedExercise(
            id = "push_up", name = "Push-Up",
            primaryMuscle = "CHEST", secondaryMuscles = listOf("ARMS", "CORE"),
            equipment = "BODYWEIGHT", movementType = "COMPOUND",
            instructions = listOf(
                "Place hands slightly wider than shoulder-width, body in a straight line.",
                "Lower your chest to just above the floor, elbows at ~45°.",
                "Push through your palms to return to the start.",
                "Keep your core braced throughout the movement.",
            ),
        ),
        // ── Back ─────────────────────────────────────────────────────────────
        SeedExercise(
            id = "deadlift", name = "Deadlift",
            primaryMuscle = "BACK", secondaryMuscles = listOf("LEGS", "GLUTES"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Stand with feet hip-width, bar over mid-foot. Hinge at the hips and grip the bar.",
                "Brace your core, take a deep breath and drive your feet into the floor.",
                "Keep the bar close to your body as you extend your hips and knees simultaneously.",
                "Lock out at the top, then lower the bar under control by hinging at the hips first.",
            ),
        ),
        SeedExercise(
            id = "pull_up", name = "Pull-Up",
            primaryMuscle = "BACK", secondaryMuscles = listOf("ARMS"),
            equipment = "BODYWEIGHT", movementType = "COMPOUND",
            instructions = listOf(
                "Hang from the bar with an overhand grip, hands shoulder-width apart.",
                "Engage your lats and pull your chest toward the bar.",
                "Pause briefly at the top, then lower with control.",
                "Avoid swinging — use a slow eccentric for maximum muscle engagement.",
            ),
        ),
        SeedExercise(
            id = "barbell_row", name = "Barbell Row",
            primaryMuscle = "BACK", secondaryMuscles = listOf("ARMS"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Hinge forward ~45°, grip the bar slightly wider than shoulder-width.",
                "Row the bar toward your lower chest, leading with your elbows.",
                "Squeeze your shoulder blades at the top.",
                "Lower the bar slowly, maintaining your hip hinge.",
            ),
        ),
        SeedExercise(
            id = "lat_pulldown", name = "Lat Pulldown",
            primaryMuscle = "BACK", secondaryMuscles = listOf("ARMS"),
            equipment = "CABLE", movementType = "COMPOUND",
            instructions = listOf(
                "Sit at the cable machine, secure your thighs under the pad.",
                "Grip the bar wider than shoulder-width, lean back slightly.",
                "Pull the bar down to your upper chest, driving elbows toward your hips.",
                "Return slowly, fully extending your arms at the top.",
            ),
        ),
        SeedExercise(
            id = "cable_row", name = "Cable Row",
            primaryMuscle = "BACK", secondaryMuscles = listOf("ARMS"),
            equipment = "CABLE", movementType = "COMPOUND",
            instructions = listOf(
                "Sit at a low cable pulley, feet on the platform, slight bend in knees.",
                "Grip the handle and pull toward your lower abdomen.",
                "Keep your torso upright and squeeze your shoulder blades.",
                "Extend your arms fully on the return, feeling the stretch.",
            ),
        ),
        // ── Legs ─────────────────────────────────────────────────────────────
        SeedExercise(
            id = "squat", name = "Squat",
            primaryMuscle = "LEGS", secondaryMuscles = listOf("GLUTES", "CORE"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Position the bar on your upper traps, feet shoulder-width, toes slightly out.",
                "Brace your core, take a breath, and descend by sitting back and down.",
                "Keep your chest up and knees tracking over your toes.",
                "Drive through your heels to stand, exhaling at the top.",
            ),
        ),
        SeedExercise(
            id = "romanian_deadlift", name = "Romanian Deadlift",
            primaryMuscle = "LEGS", secondaryMuscles = listOf("GLUTES", "BACK"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Hold the bar at hip level with an overhand grip, slight bend in knees.",
                "Push your hips back as you lower the bar along your legs.",
                "Feel a deep hamstring stretch, keeping your back straight.",
                "Drive your hips forward to return to standing.",
            ),
        ),
        SeedExercise(
            id = "leg_press", name = "Leg Press",
            primaryMuscle = "LEGS", secondaryMuscles = listOf("GLUTES"),
            equipment = "MACHINE", movementType = "COMPOUND",
            instructions = listOf(
                "Sit in the leg press machine, feet shoulder-width on the platform.",
                "Release the safety handles and lower the weight until knees reach 90°.",
                "Press through your heels to extend your legs, without locking out.",
                "Keep your lower back flat against the seat throughout.",
            ),
        ),
        SeedExercise(
            id = "lunges", name = "Dumbbell Lunges",
            primaryMuscle = "LEGS", secondaryMuscles = listOf("GLUTES"),
            equipment = "DUMBBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Hold a dumbbell in each hand, stand tall.",
                "Step one foot forward and lower your back knee toward the floor.",
                "Keep your front knee over your ankle, torso upright.",
                "Push back to standing and alternate legs.",
            ),
        ),
        SeedExercise(
            id = "leg_curl", name = "Leg Curl",
            primaryMuscle = "LEGS", secondaryMuscles = emptyList(),
            equipment = "MACHINE", movementType = "ISOLATION",
            instructions = listOf(
                "Lie face-down on the leg curl machine, ankles under the pad.",
                "Curl your heels toward your glutes as far as possible.",
                "Hold briefly at the top, then lower with control.",
                "Avoid lifting your hips off the bench.",
            ),
        ),
        // ── Shoulders ────────────────────────────────────────────────────────
        SeedExercise(
            id = "overhead_press", name = "Overhead Press",
            primaryMuscle = "SHOULDERS", secondaryMuscles = listOf("ARMS", "CORE"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Grip the bar just outside shoulder-width, bar resting on your upper chest.",
                "Brace your core and press the bar directly overhead.",
                "Lock out at the top with your arms fully extended.",
                "Lower the bar back to your chest under control.",
            ),
        ),
        SeedExercise(
            id = "lateral_raise", name = "Lateral Raise",
            primaryMuscle = "SHOULDERS", secondaryMuscles = emptyList(),
            equipment = "DUMBBELL", movementType = "ISOLATION",
            instructions = listOf(
                "Stand with a dumbbell in each hand, slight bend in elbows.",
                "Raise your arms out to the sides until they reach shoulder height.",
                "Lead with your pinkies, as if pouring water from a jug.",
                "Lower slowly under control — resist the urge to swing.",
            ),
        ),
        // ── Arms ─────────────────────────────────────────────────────────────
        SeedExercise(
            id = "bicep_curl", name = "Bicep Curl",
            primaryMuscle = "ARMS", secondaryMuscles = emptyList(),
            equipment = "DUMBBELL", movementType = "ISOLATION",
            instructions = listOf(
                "Stand tall, a dumbbell in each hand, palms facing forward.",
                "Curl the weights toward your shoulders, keeping elbows fixed.",
                "Squeeze your biceps at the top.",
                "Lower slowly for a full stretch at the bottom.",
            ),
        ),
        SeedExercise(
            id = "tricep_pushdown", name = "Tricep Pushdown",
            primaryMuscle = "ARMS", secondaryMuscles = emptyList(),
            equipment = "CABLE", movementType = "ISOLATION",
            instructions = listOf(
                "Stand at a high cable pulley, grip the bar overhand.",
                "Keep elbows tucked at your sides and push the bar down until arms are straight.",
                "Squeeze your triceps hard at full extension.",
                "Return slowly, bending the elbows to about 90°.",
            ),
        ),
        SeedExercise(
            id = "hammer_curl", name = "Hammer Curl",
            primaryMuscle = "ARMS", secondaryMuscles = emptyList(),
            equipment = "DUMBBELL", movementType = "ISOLATION",
            instructions = listOf(
                "Hold dumbbells with a neutral grip (palms facing each other).",
                "Curl both dumbbells toward your shoulders simultaneously.",
                "Avoid rotating your wrists — keep the neutral grip throughout.",
                "Lower under control.",
            ),
        ),
        SeedExercise(
            id = "skull_crusher", name = "Skull Crusher",
            primaryMuscle = "ARMS", secondaryMuscles = emptyList(),
            equipment = "BARBELL", movementType = "ISOLATION",
            instructions = listOf(
                "Lie on a bench and hold the barbell above your chest, arms extended.",
                "Bend only at the elbows, lowering the bar toward your forehead.",
                "Extend back up without moving your upper arms.",
                "Keep control — don't rush this movement.",
            ),
        ),
        // ── Core ─────────────────────────────────────────────────────────────
        SeedExercise(
            id = "plank", name = "Plank",
            primaryMuscle = "CORE", secondaryMuscles = listOf("SHOULDERS"),
            equipment = "BODYWEIGHT", movementType = "ISOLATION",
            instructions = listOf(
                "Place your forearms on the floor, elbows under shoulders.",
                "Extend your legs behind you, weight on your toes.",
                "Keep your body in a straight line from head to heels.",
                "Hold, breathing steadily — avoid letting your hips sag or rise.",
            ),
        ),
        SeedExercise(
            id = "russian_twist", name = "Russian Twist",
            primaryMuscle = "CORE", secondaryMuscles = emptyList(),
            equipment = "BODYWEIGHT", movementType = "ISOLATION",
            instructions = listOf(
                "Sit on the floor, knees bent, feet slightly raised.",
                "Lean back to ~45° and clasp your hands together.",
                "Rotate your torso left and right, tapping the floor beside each hip.",
                "Keep your core tight and movement controlled.",
            ),
        ),
        // ── Glutes ───────────────────────────────────────────────────────────
        SeedExercise(
            id = "hip_thrust", name = "Hip Thrust",
            primaryMuscle = "GLUTES", secondaryMuscles = listOf("LEGS"),
            equipment = "BARBELL", movementType = "COMPOUND",
            instructions = listOf(
                "Sit against a bench, bar over your hips, feet flat on the floor.",
                "Drive through your heels to lift your hips until your body is parallel to the floor.",
                "Squeeze your glutes hard at the top.",
                "Lower your hips slowly back toward the floor.",
            ),
        ),
        SeedExercise(
            id = "glute_bridge", name = "Glute Bridge",
            primaryMuscle = "GLUTES", secondaryMuscles = listOf("CORE"),
            equipment = "BODYWEIGHT", movementType = "COMPOUND",
            instructions = listOf(
                "Lie on your back, knees bent, feet flat on the floor near your hips.",
                "Drive through your heels, squeezing your glutes to lift your hips.",
                "Pause at the top, then lower slowly.",
                "Add a resistance band above the knees for extra challenge.",
            ),
        ),
        // ── Calves ───────────────────────────────────────────────────────────
        SeedExercise(
            id = "calf_raise", name = "Calf Raise",
            primaryMuscle = "CALVES", secondaryMuscles = emptyList(),
            equipment = "MACHINE", movementType = "ISOLATION",
            instructions = listOf(
                "Position your shoulders under the pads, feet on the edge of the platform.",
                "Rise up onto the balls of your feet as high as possible.",
                "Hold briefly at the top, then lower your heels below the platform level.",
                "Control the negative for maximum stretch.",
            ),
        ),
        // ── Cardio ───────────────────────────────────────────────────────────
        SeedExercise(
            id = "treadmill_run", name = "Treadmill Run",
            primaryMuscle = "LEGS", secondaryMuscles = listOf("CALVES"),
            equipment = "MACHINE", movementType = "CARDIO",
            instructions = listOf(
                "Set your target speed and incline on the treadmill.",
                "Land mid-foot with each stride, keeping an upright posture.",
                "Swing your arms naturally to maintain rhythm.",
                "Cool down by gradually reducing speed over the final 2 minutes.",
            ),
        ),
        SeedExercise(
            id = "jump_rope", name = "Jump Rope",
            primaryMuscle = "CALVES", secondaryMuscles = listOf("LEGS", "SHOULDERS"),
            equipment = "BODYWEIGHT", movementType = "CARDIO",
            instructions = listOf(
                "Hold the handles at hip height, rope behind your feet.",
                "Jump just high enough to clear the rope — minimal ground contact time.",
                "Keep your elbows close to your sides, wrists doing the rotation.",
                "Land softly on the balls of your feet with slightly bent knees.",
            ),
        ),
    )

    fun seed() {
        val existingCount = transaction {
            ExercisesTable.selectAll().count()
        }
        if (existingCount >= catalog.size) return  // already seeded

        transaction {
            catalog.forEach { ex ->
                ExercisesTable.upsert(ExercisesTable.id) {
                    it[id]               = ex.id
                    it[name]             = ex.name
                    it[primaryMuscle]    = ex.primaryMuscle
                    it[secondaryMuscles] = ex.secondaryMuscles
                    it[equipment]        = ex.equipment
                    it[movementType]     = ex.movementType
                    it[instructions]     = ex.instructions
                    it[isCustom]         = false
                    it[catalogVersion]   = 1
                    it[createdAt]        = OffsetDateTime.now()
                    it[updatedAt]        = OffsetDateTime.now()
                }
            }
        }
    }

    private data class SeedExercise(
        val id: String,
        val name: String,
        val primaryMuscle: String,
        val secondaryMuscles: List<String>,
        val equipment: String,
        val movementType: String,
        val instructions: List<String>,
    )
}
