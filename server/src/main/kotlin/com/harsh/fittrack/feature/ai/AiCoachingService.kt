package com.harsh.fittrack.feature.ai

import com.harsh.fittrack.data.repository.query
import com.harsh.fittrack.data.table.ExerciseEntriesTable
import com.harsh.fittrack.data.table.ExercisesTable
import com.harsh.fittrack.data.table.SetEntriesTable
import com.harsh.fittrack.data.table.WorkoutsTable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val insightCache = ConcurrentHashMap<String, Pair<CoachingInsight, Long>>()
private const val CACHE_TTL_MS = 24L * 60 * 60 * 1000

class AiCoachingService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getInsight(userId: String): CoachingInsight {
        val cached = insightCache[userId]
        if (cached != null && System.currentTimeMillis() - cached.second < CACHE_TTL_MS) {
            return cached.first
        }
        val insight = generateInsight(userId)
        insightCache[userId] = insight to System.currentTimeMillis()
        return insight
    }

    private suspend fun generateInsight(userId: String): CoachingInsight {
        val summary = buildWorkoutSummary(userId)
        val apiKey = System.getenv("ANTHROPIC_API_KEY")
            ?: return fallbackInsight("ANTHROPIC_API_KEY not configured")

        return callClaude(apiKey, summary)
    }

    private suspend fun buildWorkoutSummary(userId: String): String = query {
        val cutoff = Instant.now().minusSeconds(30L * 86400).toEpochMilli()

        val rows = (WorkoutsTable
            .innerJoin(ExerciseEntriesTable)
            .innerJoin(ExercisesTable)
            .innerJoin(SetEntriesTable))
            .selectAll()
            .where {
                (WorkoutsTable.userId eq userId) and
                    WorkoutsTable.deletedAt.isNull() and
                    (WorkoutsTable.startedAt greaterEq cutoff) and
                    (SetEntriesTable.isCompleted eq true)
            }
            .toList()

        if (rows.isEmpty()) return@query "No workout data in the last 30 days."

        // Group: exercise -> list of (weightKg, reps)
        val exerciseSets = mutableMapOf<String, MutableList<Pair<Double, Int>>>()
        val muscleLastSeen = mutableMapOf<String, Long>()

        for (row in rows) {
            val name = row[ExercisesTable.name]
            val muscle = row[ExercisesTable.primaryMuscle]
            val weight = row[SetEntriesTable.weightKg].toDouble()
            val reps = row[SetEntriesTable.reps]
            val ts = row[WorkoutsTable.startedAt]

            exerciseSets.getOrPut(name) { mutableListOf() }.add(weight to reps)
            if ((muscleLastSeen[muscle] ?: 0L) < ts) muscleLastSeen[muscle] = ts
        }

        val sb = StringBuilder()
        sb.appendLine("Last 30-day workout summary:")
        exerciseSets.forEach { (exercise, sets) ->
            val best = sets.maxByOrNull { it.first * (1.0 + it.second / 30.0) }!!
            sb.appendLine("- $exercise: best set ${best.first}kg x ${best.second} reps (${sets.size} total sets)")
        }
        sb.appendLine("\nMuscle groups last trained:")
        val now = Instant.now().toEpochMilli()
        muscleLastSeen.forEach { (muscle, ts) ->
            val daysAgo = (now - ts) / 86400000
            sb.appendLine("- $muscle: ${daysAgo}d ago")
        }
        sb.toString()
    }

    private suspend fun callClaude(apiKey: String, workoutSummary: String): CoachingInsight {
        val systemPrompt = """
            You are a personal fitness coach. Analyze the workout data and respond ONLY with valid JSON matching this schema:
            {
              "targetMuscleGroups": ["string"],
              "progressionSuggestions": [{"exerciseName":"string","currentBestKg":0.0,"currentBestReps":0,"suggestion":"string"}],
              "weaknesses": ["string"],
              "dailyTip": "string",
              "generatedAt": 0
            }
            Set generatedAt to current Unix time in milliseconds. Be specific with weight targets (e.g. "Try 82.5kg for 5 reps").
        """.trimIndent()

        return try {
            val response = client.post("https://api.anthropic.com/v1/messages") {
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                contentType(ContentType.Application.Json)
                setBody(ClaudeRequest(
                    model = "claude-sonnet-4-6",
                    maxTokens = 512,
                    system = systemPrompt,
                    messages = listOf(ClaudeMessage(role = "user", content = workoutSummary)),
                ))
            }
            val body = response.body<ClaudeResponse>()
            val jsonText = body.content.firstOrNull()?.text ?: return fallbackInsight("Empty Claude response")
            Json { ignoreUnknownKeys = true }.decodeFromString<CoachingInsight>(jsonText)
        } catch (e: Exception) {
            fallbackInsight("Claude API error: ${e.message}")
        }
    }

    private fun fallbackInsight(reason: String) = CoachingInsight(
        targetMuscleGroups = emptyList(),
        progressionSuggestions = emptyList(),
        weaknesses = emptyList(),
        dailyTip = "Keep logging workouts consistently to get personalized coaching insights.",
        generatedAt = System.currentTimeMillis(),
    )
}

// ── Internal DTOs for Claude API ────────────────────────────────────────────

@Serializable
private data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<ClaudeMessage>,
)

@Serializable
private data class ClaudeMessage(val role: String, val content: String)

@Serializable
private data class ClaudeResponse(val content: List<ClaudeContentBlock>)

@Serializable
private data class ClaudeContentBlock(val type: String, val text: String = "")
