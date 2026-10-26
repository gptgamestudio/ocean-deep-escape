package com.example.game.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.model.AIState
import com.example.game.model.BossShockwave
import com.example.game.model.Bubble
import com.example.game.model.Collectible
import com.example.game.model.CollectibleType
import com.example.game.model.Enemy
import com.example.game.model.EnemyType
import com.example.game.model.EscapePortal
import com.example.game.model.GamePhase
import com.example.game.model.Harpoon
import com.example.game.model.OverlayDialog
import com.example.game.model.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.game.auth.GoogleAuthManager
import com.example.game.data.FirebaseGameRepository
import com.example.game.audio.GameAudioManager
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class GameUiState(
    val phase: GamePhase = GamePhase.WELCOME,
    val playerName: String = "Commander Diver",
    val currentLevel: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val highScore: Int = 0,
    val bestTimeRemaining: Float = 0f,
    val timeRemainingSeconds: Float = 1800f, // 30 minutes
    val player: PlayerState = PlayerState(),
    val enemies: List<Enemy> = emptyList(),
    val harpoons: List<Harpoon> = emptyList(),
    val shockwaves: List<BossShockwave> = emptyList(),
    val collectibles: List<Collectible> = emptyList(),
    val bubbles: List<Bubble> = emptyList(),
    val escapePortal: EscapePortal = EscapePortal(),
    val activeOverlayDialog: OverlayDialog? = null,
    val isDrawerOpen: Boolean = false,
    val deathLocationX: Float = 0f,
    val deathLocationY: Float = 0f,
    val deathDepthMeters: Int = 0,
    val deathKillerName: String = "Apex Deep Predator",
    val deathInspectionStep: Int = 0,
    val screenShakeAmount: Float = 0f,
    val worldWidth: Float = 4200f,
    val worldHeight: Float = 3200f,
    val isCloudConnected: Boolean = false,
    val cloudUserEmail: String = "",
    val cloudSyncMessage: String = "",
    val livesRemaining: Int = 10,
    val dailyAdsWatchedToday: Int = 0,
    val autoAdvanceCountdown: Int = 3,
    val isOutOfLives: Boolean = false,
    val hitNoticeText: String = "",
    val hitNoticeTimer: Float = 0f,
    val hasActiveSession: Boolean = false
)

class OceanGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ocean_game_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        GameUiState(
            playerName = prefs.getString("player_name", "Commander Diver") ?: "Commander Diver",
            currentLevel = prefs.getInt("current_level", 1),
            maxUnlockedLevel = prefs.getInt("max_unlocked_level", 1),
            highScore = prefs.getInt("high_score", 0),
            bestTimeRemaining = prefs.getFloat("best_time", 0f),
            livesRemaining = prefs.getInt("player_lives", 10),
            dailyAdsWatchedToday = prefs.getInt("daily_ads_watched", 0)
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var autoAdvanceJob: Job? = null
    private var joystickInputX = 0f
    private var joystickInputY = 0f
    private var isBoosting = false

    private val firebaseRepo: FirebaseGameRepository? by lazy {
        try {
            FirebaseGameRepository(application)
        } catch (e: Exception) {
            null
        }
    }

    init {
        checkDailyAdReset()
        checkCloudAuthStatus()
    }

    fun checkCloudAuthStatus() {
        val user = GoogleAuthManager.currentUser
        val isConnected = user != null
        _uiState.value = _uiState.value.copy(
            isCloudConnected = isConnected,
            cloudUserEmail = user?.email ?: user?.displayName ?: ""
        )
        if (isConnected) {
            viewModelScope.launch {
                val profile = firebaseRepo?.loadUserProfile()
                if (profile != null) {
                    val maxUnlocked = maxOf(_uiState.value.maxUnlockedLevel, profile.maxUnlockedLevel)
                    val bestScore = maxOf(_uiState.value.highScore, profile.highScore)
                    _uiState.value = _uiState.value.copy(
                        currentLevel = maxOf(_uiState.value.currentLevel, maxUnlocked),
                        maxUnlockedLevel = maxUnlocked,
                        highScore = bestScore,
                        playerName = if (profile.displayName.isNotBlank()) profile.displayName else _uiState.value.playerName
                    )
                }
            }
        }
    }

    fun signInWithGoogle(context: Context, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = GoogleAuthManager.signInWithGoogle(context)
            if (result.isSuccess) {
                checkCloudAuthStatus()
                syncExpeditionToCloud()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun signOutCloud() {
        GoogleAuthManager.signOut()
        _uiState.value = _uiState.value.copy(
            isCloudConnected = false,
            cloudUserEmail = ""
        )
    }

    fun syncExpeditionToCloud() {
        viewModelScope.launch {
            val state = _uiState.value
            val depth = (state.player.y * 0.85f).toInt()
            firebaseRepo?.saveExpeditionStats(
                playerName = state.playerName,
                score = state.highScore,
                level = state.maxUnlockedLevel,
                pearls = state.player.pearls,
                depth = depth
            )
        }
    }

    fun enterGameDashboard() {
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.START_MENU
        )
    }

    fun updatePlayerName(newName: String) {
        val sanitized = newName.take(20).trim().ifEmpty { "Commander Diver" }
        prefs.edit()
            .putString("player_name", sanitized)
            .putString("player_username", sanitized)
            .apply()
        _uiState.value = _uiState.value.copy(playerName = sanitized)
    }

    fun selectLevel(level: Int) {
        val clamped = level.coerceIn(1, 500)
        _uiState.value = _uiState.value.copy(currentLevel = clamped)
        prefs.edit().putInt("current_level", clamped).apply()
    }

    fun nextLevelSelector() {
        val next = (_uiState.value.currentLevel + 1).coerceAtMost(500)
        selectLevel(next)
    }

    fun prevLevelSelector() {
        val prev = (_uiState.value.currentLevel - 1).coerceAtLeast(1)
        selectLevel(prev)
    }

    fun checkDailyAdReset() {
        val now = System.currentTimeMillis()
        val lastReset = prefs.getLong("last_ad_reset_time", 0L)
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (now - lastReset >= oneDayMs) {
            prefs.edit()
                .putInt("daily_ads_watched", 0)
                .putLong("last_ad_reset_time", now)
                .apply()
            _uiState.value = _uiState.value.copy(
                dailyAdsWatchedToday = 0
            )
        } else {
            _uiState.value = _uiState.value.copy(
                dailyAdsWatchedToday = prefs.getInt("daily_ads_watched", 0)
            )
        }
    }

    fun advanceToNextLevel() {
        val next = (_uiState.value.currentLevel + 1).coerceAtMost(500)
        selectLevel(next)
        startGame()
    }

    fun advanceToPrevLevel() {
        val prev = (_uiState.value.currentLevel - 1).coerceAtLeast(1)
        selectLevel(prev)
        startGame()
    }

    fun watchRewardedAdForLives(activity: android.app.Activity, onRewardCompleted: (Boolean) -> Unit = {}) {
        checkDailyAdReset()
        if (_uiState.value.dailyAdsWatchedToday >= 10) {
            onRewardCompleted(false)
            return
        }
        com.example.game.ads.AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                val updatedAds = (_uiState.value.dailyAdsWatchedToday + 1).coerceAtMost(10)
                val currentLives = _uiState.value.livesRemaining
                val updatedLives = if (currentLives <= 0) 10 else (currentLives + 10).coerceAtMost(50)
                prefs.edit()
                    .putInt("daily_ads_watched", updatedAds)
                    .putInt("player_lives", updatedLives)
                    .apply()
                _uiState.value = _uiState.value.copy(
                    livesRemaining = updatedLives,
                    dailyAdsWatchedToday = updatedAds,
                    isOutOfLives = false,
                    hitNoticeText = "❤️ +10 LIVES CREDITED!",
                    hitNoticeTimer = 2.5f,
                    player = _uiState.value.player.copy(livesRemaining = updatedLives)
                )
                // If game was paused or game over, auto continue session
                if (_uiState.value.phase == GamePhase.GAME_OVER) {
                    continueToGame()
                }
                onRewardCompleted(true)
            },
            onDismissed = {
                // dismissed
            }
        )
    }

    private fun startAutoAdvanceCountdown() {
        autoAdvanceJob?.cancel()
        _uiState.value = _uiState.value.copy(autoAdvanceCountdown = 3)
        autoAdvanceJob = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.value = _uiState.value.copy(autoAdvanceCountdown = sec)
                delay(1000L)
            }
            if (_uiState.value.phase == GamePhase.VICTORY) {
                _uiState.value = _uiState.value.copy(autoAdvanceCountdown = 0)
                startGame()
            }
        }
    }

    fun cancelAutoAdvance() {
        autoAdvanceJob?.cancel()
    }

    fun startGame() {
        cancelAutoAdvance()
        gameLoopJob?.cancel()
        val level = _uiState.value.currentLevel
        val curLives = if (_uiState.value.livesRemaining <= 0) 10 else _uiState.value.livesRemaining
        if (_uiState.value.livesRemaining <= 0) {
            prefs.edit().putInt("player_lives", 10).apply()
        }
        initializeWorld(level)

        isBtnLeft = false
        isBtnRight = false
        isBtnUp = false
        isBtnDown = false
        joystickInputX = 0f
        joystickInputY = 0f
        isBoosting = false

        _uiState.value = _uiState.value.copy(
            phase = GamePhase.PLAYING,
            timeRemainingSeconds = 1800f,
            activeOverlayDialog = null,
            isDrawerOpen = false,
            livesRemaining = curLives,
            isOutOfLives = false,
            hasActiveSession = true,
            player = _uiState.value.player.copy(livesRemaining = curLives)
        )

        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                if (_uiState.value.phase == GamePhase.PLAYING) {
                    updatePhysics(dt)
                }
                delay(16) // ~60fps
            }
        }
    }

    fun restartCurrentLevel() {
        cancelAutoAdvance()
        if (_uiState.value.livesRemaining <= 0) {
            prefs.edit().putInt("player_lives", 10).apply()
            _uiState.value = _uiState.value.copy(
                livesRemaining = 10,
                isOutOfLives = false
            )
        }
        startGame()
    }

    fun continueToGame() {
        cancelAutoAdvance()
        if (_uiState.value.livesRemaining <= 0) {
            prefs.edit().putInt("player_lives", 10).apply()
            _uiState.value = _uiState.value.copy(
                livesRemaining = 10,
                isOutOfLives = false
            )
        }
        startGame()
    }

    fun pauseGame() {
        if (_uiState.value.phase == GamePhase.PLAYING) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.PAUSED)
        }
    }

    fun resumeGame() {
        if (_uiState.value.phase == GamePhase.PAUSED) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.PLAYING)
        }
    }

    fun returnToMenu() {
        cancelAutoAdvance()
        gameLoopJob?.cancel()
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.START_MENU,
            hasActiveSession = true,
            isDrawerOpen = false,
            activeOverlayDialog = null
        )
    }

    fun returnToDashboard() {
        returnToMenu()
    }

    fun returnToWelcome() {
        cancelAutoAdvance()
        gameLoopJob?.cancel()
        _uiState.value = _uiState.value.copy(
            phase = GamePhase.WELCOME,
            isDrawerOpen = false,
            activeOverlayDialog = null
        )
    }

    fun openOverlay(dialog: OverlayDialog) {
        _uiState.value = _uiState.value.copy(activeOverlayDialog = dialog)
    }

    fun closeOverlay() {
        _uiState.value = _uiState.value.copy(activeOverlayDialog = null)
    }

    fun setDrawerOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDrawerOpen = open)
    }

    private var isBtnLeft = false
    private var isBtnRight = false
    private var isBtnUp = false
    private var isBtnDown = false

    fun setMoveLeft(pressed: Boolean) {
        isBtnLeft = pressed
        syncMovementInput()
    }

    fun setMoveRight(pressed: Boolean) {
        isBtnRight = pressed
        syncMovementInput()
    }

    fun setMoveUp(pressed: Boolean) {
        isBtnUp = pressed
        syncMovementInput()
    }

    fun setMoveDown(pressed: Boolean) {
        isBtnDown = pressed
        syncMovementInput()
    }

    private fun syncMovementInput() {
        var dx = 0f
        var dy = 0f
        if (isBtnLeft && !isBtnRight) dx = -1f
        else if (isBtnRight && !isBtnLeft) dx = 1f

        if (isBtnUp && !isBtnDown) dy = -1f
        else if (isBtnDown && !isBtnUp) dy = 1f

        if (dx != 0f && dy != 0f) {
            dx *= 0.7071f
            dy *= 0.7071f
        }
        joystickInputX = dx
        joystickInputY = dy
    }

    fun setJoystickInput(dx: Float, dy: Float) {
        if (!isBtnLeft && !isBtnRight && !isBtnUp && !isBtnDown) {
            joystickInputX = dx.coerceIn(-1f, 1f)
            joystickInputY = dy.coerceIn(-1f, 1f)
        }
    }

    fun setBoosting(boosting: Boolean) {
        if (boosting && !isBoosting) {
            GameAudioManager.playBoost()
        }
        isBoosting = boosting
    }

    fun setDeathInspectionStep(step: Int) {
        _uiState.value = _uiState.value.copy(deathInspectionStep = step.coerceIn(0, 3))
    }

    fun fireHarpoon() {
        val state = _uiState.value
        val player = state.player
        if (player.harpoonCooldown > 0f) return // Unlimited ammo: only rapid-fire cooldown check

        val speed = 750f
        val rad = Math.toRadians(player.headingDeg.toDouble())
        val vx = cos(rad).toFloat() * speed
        val vy = sin(rad).toFloat() * speed

        val newHarpoon = Harpoon(
            id = "harpoon_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            x = player.x + cos(rad).toFloat() * 45f,
            y = player.y + sin(rad).toFloat() * 45f,
            vx = vx,
            vy = vy,
            angleDeg = player.headingDeg
        )

        GameAudioManager.playShoot()

        _uiState.value = state.copy(
            harpoons = state.harpoons + newHarpoon,
            player = player.copy(
                harpoonAmmo = 999, // UNLIMITED AMMO
                harpoonCooldown = 0.22f // Rapid-fire response
            )
        )
    }

    private fun initializeWorld(level: Int) {
        val worldW = 4200f
        val worldH = 3200f

        val curLives = if (_uiState.value.livesRemaining <= 0) 10 else _uiState.value.livesRemaining
        val player = PlayerState(
            x = 240f,
            y = 500f,
            currentHp = 500f, // 5 full hearts
            maxHp = 500f,
            oxygen = 100f,
            shields = 100f,
            harpoonAmmo = 999, // UNLIMITED AMMO
            livesRemaining = curLives
        )

        val enemies = mutableListOf<Enemy>()

        // 1. Apex Boss - LEVIATHAN PRIME (Spawned near player at 680, 500 in Zone $level)
        val bossHp = 220f + (level - 1) * 30f // Level 1 is only 220 HP (4 harpoon hits) to easily trigger next level
        val bossX = 680f + ((level - 1) % 4) * 60f
        val bossY = 500f + ((level - 1) % 3) * 50f
        enemies.add(
            Enemy(
                id = "boss_leviathan",
                type = EnemyType.LEVIATHAN_PRIME,
                x = bossX,
                y = bossY,
                health = bossHp,
                maxHealth = bossHp,
                territoryMinX = 100f,
                territoryMaxX = 3900f,
                territoryMinY = 200f,
                territoryMaxY = 2900f,
                patrolTargetX = 500f,
                patrolTargetY = 550f,
                perceptionRadius = 900f,
                attackRange = 260f,
                isBoss = true
            )
        )

        // 2. Regular Predators distributed by depth territories
        val enemyCount = 8 + (level % 15)
        for (i in 0 until enemyCount) {
            val type = when (i % 4) {
                0 -> EnemyType.GREAT_WHITE_SHARK
                1 -> EnemyType.ABYSSAL_ANGLER
                2 -> EnemyType.GIANT_KRAKEN_SQUID
                else -> EnemyType.VIPER_EEL
            }
            val zoneMinY = 300f + (i * 280f).coerceAtMost(2500f)
            val zoneMaxY = zoneMinY + 600f
            val ex = 700f + Random.nextFloat() * 3000f
            val ey = zoneMinY + Random.nextFloat() * 400f

            enemies.add(
                Enemy(
                    id = "enemy_${i}_$level",
                    type = type,
                    x = ex,
                    y = ey,
                    health = type.baseHealth + level * 5f,
                    maxHealth = type.baseHealth + level * 5f,
                    territoryMinX = (ex - 500f).coerceAtLeast(100f),
                    territoryMaxX = (ex + 500f).coerceAtMost(worldW - 100f),
                    territoryMinY = zoneMinY,
                    territoryMaxY = zoneMaxY,
                    patrolTargetX = ex,
                    patrolTargetY = ey,
                    perceptionRadius = 480f,
                    attackRange = 160f
                )
            )
        }

        // Collectibles
        val items = mutableListOf<Collectible>()
        for (i in 0..18) {
            val type = when (i % 5) {
                0, 1 -> CollectibleType.PEARL
                2 -> CollectibleType.OXYGEN_CELL
                3 -> CollectibleType.REPAIR_KIT
                4 -> CollectibleType.SHIELD_CHARGE
                else -> CollectibleType.HARPOON_CRATE
            }
            items.add(
                Collectible(
                    id = "item_$i",
                    type = type,
                    x = 300f + Random.nextFloat() * 3500f,
                    y = 350f + Random.nextFloat() * 2600f
                )
            )
        }

        // Escape portal at far deep bottom right
        val portal = EscapePortal(
            x = 3800f,
            y = 2700f,
            triggerRadius = 260f,
            activationTimeRequired = 8f,
            currentHoldTime = 0f
        )

        _uiState.value = _uiState.value.copy(
            player = player,
            enemies = enemies,
            harpoons = emptyList(),
            shockwaves = emptyList(),
            collectibles = items,
            escapePortal = portal,
            worldWidth = worldW,
            worldHeight = worldH
        )
    }

    private fun updatePhysics(dt: Float) {
        val state = _uiState.value
        var nextPhase = state.phase
        var player = state.player
        var currentLives = state.livesRemaining
        var hitNoticeText = if (state.hitNoticeTimer > dt) state.hitNoticeText else ""
        var hitNoticeTimer = (state.hitNoticeTimer - dt).coerceAtLeast(0f)
        var screenShake = (state.screenShakeAmount - dt * 25f).coerceAtLeast(0f)
        // Continuous deep sea exploration (No time-out game over)
        val newTimeRemaining = state.timeRemainingSeconds

        // 1. SUBMARINE 3D PHYSICS & MOVEMENT
        val inputMag = sqrt(joystickInputX * joystickInputX + joystickInputY * joystickInputY)
        val maxSpeed = if (isBoosting) 340f else 220f

        var vx = player.vx
        var vy = player.vy
        var targetHeading = player.headingDeg
        var pitch = player.pitchDeg

        if (inputMag > 0.05f) {
            val targetRad = atan2(joystickInputY.toDouble(), joystickInputX.toDouble())
            targetHeading = Math.toDegrees(targetRad).toFloat()
            vx += (joystickInputX * maxSpeed - vx) * 7.5f * dt
            vy += (joystickInputY * maxSpeed - vy) * 7.5f * dt
            // 3D Pitch: dipping down when diving, tilting up when surfacing
            pitch += (joystickInputY * 20f - pitch) * 5f * dt
        } else {
            vx *= (1f - 2.5f * dt)
            vy *= (1f - 2.5f * dt)
            pitch *= (1f - 3f * dt)
        }

        // Propeller rotation speed
        val currentSpeed = sqrt(vx * vx + vy * vy)
        val newPropRot = (player.propellerRotation + currentSpeed * 4f * dt) % 360f

        // Position update with world bounds clamping
        val newX = (player.x + vx * dt).coerceIn(40f, state.worldWidth - 40f)
        val newY = (player.y + vy * dt).coerceIn(40f, state.worldHeight - 40f)

        // Oxygen consumption (very slow, recovers at surface)
        var newOxygen = player.oxygen
        if (newY < 150f) {
            newOxygen = (newOxygen + 25f * dt).coerceAtMost(player.maxOxygen)
        } else {
            newOxygen = (newOxygen - 0.7f * dt).coerceAtLeast(0f)
        }

        // Shields regeneration
        val newShields = (player.shields + 3f * dt).coerceAtMost(player.maxShields)

        // Invulnerability cooldown
        var newInvulnTimer = (player.invulnerabilityTimer - dt).coerceAtLeast(0f)
        var newInvuln = newInvulnTimer > 0f
        val newHarpoonCd = (player.harpoonCooldown - dt).coerceAtLeast(0f)

        // Cavitation & Propeller Wake Bubbles
        val updatedBubbles = state.bubbles.toMutableList()
        if (currentSpeed > 50f && Random.nextFloat() < 0.4f) {
            val rad = Math.toRadians(targetHeading.toDouble())
            val wakeX = newX - cos(rad).toFloat() * 55f + (Random.nextFloat() - 0.5f) * 10f
            val wakeY = newY - sin(rad).toFloat() * 55f + (Random.nextFloat() - 0.5f) * 10f
            updatedBubbles.add(
                Bubble(
                    x = wakeX,
                    y = wakeY,
                    radius = 2.5f + Random.nextFloat() * 4f,
                    speed = 25f + Random.nextFloat() * 40f,
                    alpha = 0.8f,
                    isCavitation = isBoosting
                )
            )
        }
        // Update bubbles
        val survivingBubbles = updatedBubbles.mapNotNull { b ->
            val bY = b.y - b.speed * dt
            val bAlpha = b.alpha - 0.4f * dt
            if (bAlpha > 0.05f && bY > 0f) b.copy(y = bY, alpha = bAlpha, wobble = b.wobble + dt * 4f) else null
        }

        // 2. ENEMY AI BEHAVIORS (Territory, Stalking, Telegraph Attack, Burst Strike, Boss Phases)
        val updatedEnemies = mutableListOf<Enemy>()
        val newShockwaves = state.shockwaves.toMutableList()

        for (enemy in state.enemies) {
            var eState = enemy.aiState
            var eTimer = enemy.aiTimer + dt
            var eTelegraph = enemy.telegraphProgress
            var eAnim = enemy.animationPhase + dt * 4f
            var eHitFlash = (enemy.hitFlashTimer - dt).coerceAtLeast(0f)
            var ex = enemy.x
            var ey = enemy.y
            var evx = enemy.vx
            var evy = enemy.vy
            var eHealth = enemy.health
            var bossPhase = enemy.bossPhase

            val dx = newX - ex
            val dy = newY - ey
            val distToPlayer = sqrt(dx * dx + dy * dy)

            // Boss Phase Transitions
            if (enemy.isBoss) {
                val hpRatio = enemy.health / enemy.maxHealth
                bossPhase = when {
                    hpRatio < 0.25f -> 3 // Berserk Overdrive
                    hpRatio < 0.60f -> 2 // Enraged
                    else -> 1
                }
            }

            // Check if player is inside enemy's territory zone
            val playerInTerritory = newX >= enemy.territoryMinX && newX <= enemy.territoryMaxX &&
                    newY >= enemy.territoryMinY && newY <= enemy.territoryMaxY

            // AI State Machine Logic
            when (eState) {
                AIState.PATROL_TERRITORY -> {
                    eTelegraph = 0f
                    // If player is in territory and within perception radius -> switch to STALKING
                    if (playerInTerritory && distToPlayer < enemy.perceptionRadius) {
                        eState = AIState.STALKING
                        eTimer = 0f
                    } else {
                        // Natural patrolling towards patrolTarget
                        val tdx = enemy.patrolTargetX - ex
                        val tdy = enemy.patrolTargetY - ey
                        val distToPatrol = sqrt(tdx * tdx + tdy * tdy)

                        if (distToPatrol < 50f || eTimer > 8f) {
                            // Pick new patrol point inside territory
                            val newTargetX = enemy.territoryMinX + Random.nextFloat() * (enemy.territoryMaxX - enemy.territoryMinX)
                            val newTargetY = enemy.territoryMinY + Random.nextFloat() * (enemy.territoryMaxY - enemy.territoryMinY)
                            enemy.copy(patrolTargetX = newTargetX, patrolTargetY = newTargetY)
                            eTimer = 0f
                        } else {
                            val patrolSpeed = enemy.type.baseSpeed * 0.45f
                            evx = (tdx / distToPatrol.coerceAtLeast(1f)) * patrolSpeed
                            evy = (tdy / distToPatrol.coerceAtLeast(1f)) * patrolSpeed
                        }
                    }
                }

                AIState.STALKING -> {
                    // If player fled territory -> return to patrol
                    if (!playerInTerritory || distToPlayer > enemy.perceptionRadius * 1.3f) {
                        eState = AIState.PATROL_TERRITORY
                        eTimer = 0f
                    } else if (enemy.isBoss && bossPhase == 3 && eTimer > 3.8f) {
                        // Boss Phase 3 Berserk: Unleash Abyssal Gravitational Vortex
                        eState = AIState.BOSS_VORTEX
                        eTimer = 0f
                    } else if (enemy.isBoss && bossPhase >= 2 && eTimer > 4.2f) {
                        // Boss Phase 2 Enraged: Unleash Double Sonar Shockwaves
                        eState = AIState.BOSS_SHOCKWAVE
                        eTimer = 0f
                    } else if (distToPlayer < enemy.attackRange) {
                        // In attack range -> begin telegraphing strike
                        eState = AIState.TELEGRAPH_ATTACK
                        eTimer = 0f
                        eTelegraph = 0f
                    } else {
                        // Smoothly approach player
                        val stalkSpeed = enemy.type.baseSpeed * (if (enemy.isBoss && bossPhase >= 2) 1.35f else 0.85f)
                        evx += ((dx / distToPlayer) * stalkSpeed - evx) * 3f * dt
                        evy += ((dy / distToPlayer) * stalkSpeed - evy) * 3f * dt

                        // Boss Sonar Shockwave attack during Phase 1
                        if (enemy.isBoss && eTimer > 5.5f) {
                            newShockwaves.add(
                                BossShockwave(
                                    id = "wave_${System.currentTimeMillis()}",
                                    x = ex,
                                    y = ey,
                                    radius = 25f,
                                    damage = 30f
                                )
                            )
                            eTimer = 0f
                        }
                    }
                }

                AIState.BOSS_SHOCKWAVE -> {
                    // Boss halts, charges spines and unleashes devastating expanding sonar rings
                    evx *= (1f - 3f * dt)
                    evy *= (1f - 3f * dt)
                    if (eTimer in 0.35f..0.45f && newShockwaves.none { it.id == "b_sw1_${enemy.id}" }) {
                        newShockwaves.add(
                            BossShockwave(
                                id = "b_sw1_${enemy.id}",
                                x = ex,
                                y = ey,
                                radius = 35f,
                                maxRadius = 450f,
                                speed = 320f,
                                damage = 40f
                            )
                        )
                    }
                    if (eTimer in 0.95f..1.05f && newShockwaves.none { it.id == "b_sw2_${enemy.id}" }) {
                        newShockwaves.add(
                            BossShockwave(
                                id = "b_sw2_${enemy.id}",
                                x = ex,
                                y = ey,
                                radius = 35f,
                                maxRadius = 500f,
                                speed = 340f,
                                damage = 45f
                            )
                        )
                    }
                    if (eTimer > 1.8f) {
                        eState = AIState.TELEGRAPH_ATTACK
                        eTimer = 0f
                    }
                }

                AIState.BOSS_VORTEX -> {
                    // Gravitational Abyssal Vortex: gaping jaws suck submarine and bubbles inward
                    evx *= (1f - 2f * dt)
                    evy *= (1f - 2f * dt)
                    val pullDist = distToPlayer.coerceAtLeast(60f)
                    if (pullDist < 950f) {
                        val pullForce = (1f - pullDist / 950f) * 260f
                        vx -= (dx / pullDist) * pullForce * dt
                        vy -= (dy / pullDist) * pullForce * dt

                        // Inward swirling cavitation bubbles
                        if (Random.nextFloat() < 0.7f) {
                            val swirl = eTimer * 7f + Random.nextFloat() * 6.28f
                            val r = 100f + Random.nextFloat() * 200f
                            updatedBubbles.add(
                                Bubble(
                                    x = ex + cos(swirl) * r,
                                    y = ey + sin(swirl) * r,
                                    radius = 3.5f + Random.nextFloat() * 4f,
                                    speed = 70f,
                                    alpha = 0.95f,
                                    isCavitation = true
                                )
                            )
                        }
                    }
                    if (eTimer in 1.4f..1.5f && newShockwaves.none { it.id == "vortex_wave_${enemy.id}" }) {
                        newShockwaves.add(
                            BossShockwave(
                                id = "vortex_wave_${enemy.id}",
                                x = ex,
                                y = ey,
                                radius = 30f,
                                maxRadius = 420f,
                                speed = 350f,
                                damage = 50f
                            )
                        )
                    }
                    if (eTimer > 3.4f) {
                        // After suction, launch explosive ramming strike
                        eState = AIState.BURST_STRIKE
                        eTimer = 0f
                        val burstSpeed = enemy.type.baseSpeed * 2.8f
                        evx = (dx / distToPlayer.coerceAtLeast(1f)) * burstSpeed
                        evy = (dy / distToPlayer.coerceAtLeast(1f)) * burstSpeed
                    }
                }

                AIState.TELEGRAPH_ATTACK -> {
                    // Charge telegraph indicator (flashing eye, threat vector)
                    val telegraphDuration = if (enemy.isBoss) 1.3f else 0.75f
                    eTelegraph = (eTimer / telegraphDuration).coerceIn(0f, 1f)

                    // Slow down / pivot toward player during telegraph
                    evx *= (1f - 3f * dt)
                    evy *= (1f - 3f * dt)

                    if (eTimer >= telegraphDuration) {
                        eState = AIState.BURST_STRIKE
                        eTimer = 0f
                        eTelegraph = 1f
                        // Launch burst velocity straight at submarine
                        val burstSpeed = enemy.type.baseSpeed * (if (enemy.isBoss) 2.5f else 1.9f)
                        val burstDist = distToPlayer.coerceAtLeast(1f)
                        evx = (dx / burstDist) * burstSpeed
                        evy = (dy / burstDist) * burstSpeed
                    }
                }

                AIState.BURST_STRIKE -> {
                    eTelegraph = 1f
                    // Dash forward for 0.75 seconds
                    if (eTimer > 0.75f) {
                        eState = AIState.CIRCLING_FLANK
                        eTimer = 0f
                        eTelegraph = 0f
                    }
                }

                AIState.CIRCLING_FLANK -> {
                    // Circle around player before next stalking run
                    val circleAngle = eTimer * 2f
                    val flankX = newX + cos(circleAngle) * 220f
                    val flankY = newY + sin(circleAngle) * 220f
                    val fdx = flankX - ex
                    val fdy = flankY - ey
                    val fdist = sqrt(fdx * fdx + fdy * fdy).coerceAtLeast(1f)

                    evx += ((fdx / fdist) * enemy.type.baseSpeed - evx) * 2.5f * dt
                    evy += ((fdy / fdist) * enemy.type.baseSpeed - evy) * 2.5f * dt

                    if (eTimer > 2.5f) {
                        eState = AIState.STALKING
                        eTimer = 0f
                    }
                }

                AIState.RETREATING -> {
                    // Low health retreat back to territory center
                    val rdx = enemy.patrolTargetX - ex
                    val rdy = enemy.patrolTargetY - ey
                    val rdist = sqrt(rdx * rdx + rdy * rdy).coerceAtLeast(1f)
                    evx = (rdx / rdist) * enemy.type.baseSpeed * 1.2f
                    evy = (rdy / rdist) * enemy.type.baseSpeed * 1.2f

                    if (eTimer > 4f) {
                        eState = AIState.PATROL_TERRITORY
                        eTimer = 0f
                    }
                }

                else -> {}
            }

            // Move enemy
            ex += evx * dt
            ey += evy * dt

            // Facing angle from velocity
            val eFacing = if (sqrt(evx * evx + evy * evy) > 5f) {
                Math.toDegrees(atan2(evy.toDouble(), evx.toDouble())).toFloat()
            } else enemy.facingAngleDeg

            // Collision check: Enemy bite / attack on submarine (1 HIT = -1 LIFE)
            val enemyHitRadius = if (enemy.isBoss) 85f else 40f
            if (distToPlayer < (enemyHitRadius + 30f) && !newInvuln) {
                newInvuln = true
                newInvulnTimer = 1.5f // 1.5 seconds invulnerability cooldown
                GameAudioManager.playHit()
                screenShake = 22f

                currentLives = (currentLives - 1).coerceAtLeast(0)
                prefs.edit().putInt("player_lives", currentLives).apply()

                if (currentLives <= 0) {
                    nextPhase = GamePhase.GAME_OVER
                    recordDeath(newX, newY, enemy.type.displayName, 0)
                } else {
                    hitNoticeText = "💔 -1 LIFE! $currentLives LIVES REMAINING"
                    hitNoticeTimer = 2.2f
                }
            }

            updatedEnemies.add(
                enemy.copy(
                    x = ex,
                    y = ey,
                    vx = evx,
                    vy = evy,
                    facingAngleDeg = eFacing,
                    health = eHealth,
                    aiState = eState,
                    aiTimer = eTimer,
                    telegraphProgress = eTelegraph,
                    animationPhase = eAnim,
                    hitFlashTimer = eHitFlash,
                    bossPhase = bossPhase
                )
            )
        }

        // 3. BOSS SHOCKWAVE EXPANSION & PLAYER DAMAGE (1 HIT = -1 LIFE)
        val survivingWaves = mutableListOf<BossShockwave>()
        for (wave in newShockwaves) {
            val newRad = wave.radius + wave.speed * dt
            val newAlpha = 1f - (newRad / wave.maxRadius).coerceIn(0f, 1f)
            val dx = newX - wave.x
            val dy = newY - wave.y
            val dist = sqrt(dx * dx + dy * dy)

            // Hit player with shockwave (1 HIT = -1 LIFE)
            if (kotlin.math.abs(dist - newRad) < 25f && !newInvuln) {
                newInvuln = true
                newInvulnTimer = 1.5f
                GameAudioManager.playHit()
                screenShake = 22f

                currentLives = (currentLives - 1).coerceAtLeast(0)
                prefs.edit().putInt("player_lives", currentLives).apply()

                if (currentLives <= 0) {
                    nextPhase = GamePhase.GAME_OVER
                    recordDeath(newX, newY, "Leviathan Acoustic Shockwave", 0)
                } else {
                    hitNoticeText = "💔 -1 LIFE! $currentLives LIVES REMAINING"
                    hitNoticeTimer = 2.2f
                }
            }

            if (newRad < wave.maxRadius && newAlpha > 0.05f) {
                survivingWaves.add(wave.copy(radius = newRad, alpha = newAlpha))
            }
        }

        // 4. HARPOONS PHYSICS & DAMAGE ON ENEMIES
        val survivingHarpoons = mutableListOf<Harpoon>()
        val enemiesAfterHarpoons = updatedEnemies.toMutableList()

        for (h in state.harpoons) {
            val hx = h.x + h.vx * dt
            val hy = h.y + h.vy * dt
            val life = h.lifeTime - dt
            var hit = false

            for (i in enemiesAfterHarpoons.indices) {
                val e = enemiesAfterHarpoons[i]
                val edx = hx - e.x
                val edy = hy - e.y
                val dist = sqrt(edx * edx + edy * edy)
                val hitDist = if (e.isBoss) 80f else 38f

                if (dist < hitDist) {
                    hit = true
                    // Weak point critical strike calculation (Hydrothermal Core hit)
                    val isWeakPointCrit = if (e.isBoss) {
                        val coreX = e.x + cos(Math.toRadians(e.facingAngleDeg.toDouble())).toFloat() * 10f
                        val coreY = e.y + sin(Math.toRadians(e.facingAngleDeg.toDouble())).toFloat() * 10f
                        val coreDist = sqrt((hx - coreX) * (hx - coreX) + (hy - coreY) * (hy - coreY))
                        coreDist < 42f
                    } else false

                    GameAudioManager.playHit()
                    val finalDamage = if (isWeakPointCrit) h.damage * 2.5f else h.damage
                    val newHp = e.health - finalDamage

                    if (newHp <= 0f) {
                        // Enemy Defeated!
                        if (e.isBoss) {
                            // Boss defeated -> Trigger Victory Fanfare & Advance Level!
                            GameAudioManager.playVictory()
                            nextPhase = GamePhase.VICTORY
                            recordVictory()
                        }
                        enemiesAfterHarpoons.removeAt(i)
                    } else {
                        // Apply hit flash and retreat if low health
                        val retreat = if (newHp < e.maxHealth * 0.3f && !e.isBoss) AIState.RETREATING else e.aiState
                        enemiesAfterHarpoons[i] = e.copy(
                            health = newHp,
                            hitFlashTimer = if (isWeakPointCrit) 0.35f else 0.18f,
                            aiState = retreat
                        )
                    }
                    break
                }
            }

            if (!hit && life > 0f && hx in 0f..state.worldWidth && hy in 0f..state.worldHeight) {
                survivingHarpoons.add(h.copy(x = hx, y = hy, lifeTime = life))
            }
        }

        // 5. COLLECTIBLES INTERACTION
        var newScore = player.score
        var newPearls = player.pearls
        var newHp = player.currentHp
        var newAmmo = player.harpoonAmmo
        val updatedCollectibles = mutableListOf<Collectible>()

        for (c in state.collectibles) {
            if (c.isCollected) continue
            val cdx = newX - c.x
            val cdy = newY - c.y
            val dist = sqrt(cdx * cdx + cdy * cdy)

            if (dist < 45f) {
                // Collect!
                newScore += c.type.scoreVal
                when (c.type) {
                    CollectibleType.PEARL -> newPearls += 1
                    CollectibleType.OXYGEN_CELL -> newOxygen = (newOxygen + 40f).coerceAtMost(player.maxOxygen)
                    CollectibleType.REPAIR_KIT -> newHp = (newHp + 100f).coerceAtMost(player.maxHp) // restores 1 heart
                    CollectibleType.SHIELD_CHARGE -> player = player.copy(shields = player.maxShields)
                    CollectibleType.HARPOON_CRATE -> newAmmo = (newAmmo + 8).coerceAtMost(player.maxHarpoonAmmo)
                }
            } else {
                updatedCollectibles.add(c)
            }
        }

        // 6. ESCAPE PORTAL TRIGGER AREA (Requires Boss Defeat)
        val portal = state.escapePortal
        val pdx = newX - portal.x
        val pdy = newY - portal.y
        val distToPortal = sqrt(pdx * pdx + pdy * pdy)

        val bossAlive = enemiesAfterHarpoons.any { it.isBoss }
        var portalHoldTime = portal.currentHoldTime
        if (distToPortal < portal.triggerRadius) {
            // Player is inside escape trigger area - extraction portal requires defeating Apex Boss first!
            if (!bossAlive) {
                portalHoldTime += dt
                if (portalHoldTime >= portal.activationTimeRequired && nextPhase == GamePhase.PLAYING) {
                    // Successfully escaped the depth abyss!
                    GameAudioManager.playVictory()
                    nextPhase = GamePhase.VICTORY
                    recordVictory()
                }
            }
        } else {
            portalHoldTime = (portalHoldTime - dt * 1.5f).coerceAtLeast(0f)
        }

        val updatedPortal = portal.copy(
            currentHoldTime = portalHoldTime,
            pulsePhase = (portal.pulsePhase + dt * 3f) % (2f * PI.toFloat())
        )

        // Commit state update
        _uiState.value = state.copy(
            phase = nextPhase,
            timeRemainingSeconds = newTimeRemaining,
            livesRemaining = currentLives,
            hitNoticeText = hitNoticeText,
            hitNoticeTimer = hitNoticeTimer,
            screenShakeAmount = screenShake,
            player = player.copy(
                x = newX,
                y = newY,
                vx = vx,
                vy = vy,
                headingDeg = targetHeading,
                pitchDeg = pitch,
                currentHp = (currentLives * 10f),
                oxygen = newOxygen,
                shields = newShields,
                pearls = newPearls,
                score = newScore,
                harpoonAmmo = newAmmo,
                harpoonCooldown = newHarpoonCd,
                isInvulnerable = newInvuln,
                invulnerabilityTimer = newInvulnTimer,
                propellerRotation = newPropRot,
                livesRemaining = currentLives
            ),
            enemies = enemiesAfterHarpoons,
            harpoons = survivingHarpoons,
            shockwaves = survivingWaves,
            collectibles = updatedCollectibles,
            bubbles = survivingBubbles,
            escapePortal = updatedPortal
        )
    }

    private fun recordDeath(x: Float, y: Float, killerName: String, remainingLives: Int) {
        val depthMeters = (y * 0.85f).toInt()
        val isOutOf = remainingLives <= 0
        _uiState.value = _uiState.value.copy(
            deathLocationX = x,
            deathLocationY = y,
            deathDepthMeters = depthMeters,
            deathKillerName = killerName,
            deathInspectionStep = 0,
            livesRemaining = remainingLives,
            isOutOfLives = isOutOf,
            player = _uiState.value.player.copy(livesRemaining = remainingLives)
        )
    }

    private fun recordVictory() {
        val nextLevel = (_uiState.value.currentLevel + 1).coerceAtMost(500)
        val maxUnlocked = maxOf(_uiState.value.maxUnlockedLevel, nextLevel)
        val score = _uiState.value.player.score + 1000

        prefs.edit()
            .putInt("current_level", nextLevel)
            .putInt("max_unlocked_level", maxUnlocked)
            .putInt("high_score", maxOf(_uiState.value.highScore, score))
            .apply()

        _uiState.value = _uiState.value.copy(
            currentLevel = nextLevel,
            maxUnlockedLevel = maxUnlocked,
            highScore = maxOf(_uiState.value.highScore, score)
        )
        syncExpeditionToCloud()
    }
}
