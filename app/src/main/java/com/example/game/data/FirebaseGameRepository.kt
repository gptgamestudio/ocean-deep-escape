package com.example.game.data

import android.content.Context
import android.util.Log
import com.gpt.OceanDeepEscape.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class CloudUserProfile(
    val displayName: String = "",
    val highScore: Int = 0,
    val maxUnlockedLevel: Int = 1,
    val pearlsCollected: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CloudLeaderboardEntry(
    val userId: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val level: Int = 1,
    val depth: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class FirebaseGameRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        try {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }
    )

    private val auth = FirebaseAuth.getInstance()

    suspend fun saveExpeditionStats(
        playerName: String,
        score: Int,
        level: Int,
        pearls: Int,
        depth: Int
    ): Boolean {
        val currentUser = auth.currentUser ?: return false
        val uid = currentUser.uid

        return try {
            val userDoc = db.collection("users").document(uid)
            val currentSnapshot = userDoc.get().await()
            val existingHighScore = currentSnapshot.getLong("highScore")?.toInt() ?: 0
            val existingMaxLevel = currentSnapshot.getLong("maxUnlockedLevel")?.toInt() ?: 1
            val existingPearls = currentSnapshot.getLong("pearlsCollected")?.toInt() ?: 0

            val newHighScore = maxOf(existingHighScore, score)
            val newMaxLevel = maxOf(existingMaxLevel, level)
            val totalPearls = existingPearls + pearls

            val profileData = hashMapOf(
                "displayName" to playerName,
                "highScore" to newHighScore,
                "maxUnlockedLevel" to newMaxLevel,
                "pearlsCollected" to totalPearls,
                "updatedAt" to System.currentTimeMillis()
            )
            userDoc.set(profileData, SetOptions.merge()).await()

            // Update leaderboard if new score qualifies
            if (score > 0) {
                val leaderboardDoc = db.collection("leaderboard").document(uid)
                val currentLeaderboard = leaderboardDoc.get().await()
                val prevLeaderboardScore = currentLeaderboard.getLong("score")?.toInt() ?: 0

                if (score >= prevLeaderboardScore) {
                    val entry = hashMapOf(
                        "userId" to uid,
                        "playerName" to playerName,
                        "score" to score,
                        "level" to level,
                        "depth" to depth,
                        "timestamp" to System.currentTimeMillis()
                    )
                    leaderboardDoc.set(entry, SetOptions.merge()).await()
                }
            }
            true
        } catch (e: Exception) {
            Log.w("FirebaseGameRepo", "Failed to save expedition stats to Firestore: ${e.message}")
            false
        }
    }

    suspend fun loadUserProfile(): CloudUserProfile? {
        val currentUser = auth.currentUser ?: return null
        return try {
            val snapshot = db.collection("users").document(currentUser.uid).get().await()
            if (snapshot.exists()) {
                CloudUserProfile(
                    displayName = snapshot.getString("displayName") ?: "",
                    highScore = snapshot.getLong("highScore")?.toInt() ?: 0,
                    maxUnlockedLevel = snapshot.getLong("maxUnlockedLevel")?.toInt() ?: 1,
                    pearlsCollected = snapshot.getLong("pearlsCollected")?.toInt() ?: 0,
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("FirebaseGameRepo", "Failed to load user profile: ${e.message}")
            null
        }
    }

    suspend fun getTopLeaderboard(limit: Long = 10): List<CloudLeaderboardEntry> {
        return try {
            val querySnapshot = db.collection("leaderboard")
                .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { doc ->
                CloudLeaderboardEntry(
                    userId = doc.getString("userId") ?: "",
                    playerName = doc.getString("playerName") ?: "Anonymous Diver",
                    score = doc.getLong("score")?.toInt() ?: 0,
                    level = doc.getLong("level")?.toInt() ?: 1,
                    depth = doc.getLong("depth")?.toInt() ?: 0,
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            Log.w("FirebaseGameRepo", "Failed to load leaderboard: ${e.message}")
            emptyList()
        }
    }
}
