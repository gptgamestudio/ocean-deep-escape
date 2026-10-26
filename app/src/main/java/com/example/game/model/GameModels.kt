package com.example.game.model

enum class GamePhase {
    WELCOME,
    START_MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY,
    STORE
}

enum class OverlayDialog {
    STORE,
    HELP,
    PILOT_PROFILE,
    LEVEL_MAP
}

enum class EnemyType(
    val displayName: String,
    val baseHealth: Float,
    val baseSpeed: Float,
    val baseDamage: Float,
    val isApexBoss: Boolean = false
) {
    GREAT_WHITE_SHARK("Apex Megatooth Shark", 120f, 180f, 35f),
    ABYSSAL_ANGLER("Abyssal Hell-Angler", 160f, 140f, 45f),
    GIANT_KRAKEN_SQUID("Titan Abyssal Kraken", 220f, 160f, 50f),
    VIPER_EEL("Biolum Armored Viper", 140f, 210f, 30f),
    LEVIATHAN_PRIME("LEVIATHAN PRIME (ABYSSAL COLOSSUS)", 1500f, 130f, 85f, true)
}

enum class AIState {
    PATROL_TERRITORY,
    STALKING,
    TELEGRAPH_ATTACK,
    BURST_STRIKE,
    CIRCLING_FLANK,
    RETREATING,
    BOSS_ENRAGED,
    BOSS_SHOCKWAVE,
    BOSS_VORTEX
}

data class PlayerState(
    val x: Float = 200f,
    val y: Float = 600f,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val headingDeg: Float = 0f,
    val pitchDeg: Float = 0f,
    val rollDeg: Float = 0f,
    val speed: Float = 0f,
    val currentHp: Float = 500f, // 5 hearts x 100 HP each
    val maxHp: Float = 500f,
    val oxygen: Float = 100f,
    val maxOxygen: Float = 100f,
    val shields: Float = 100f,
    val maxShields: Float = 100f,
    val pearls: Int = 0,
    val score: Int = 0,
    val harpoonAmmo: Int = 20,
    val maxHarpoonAmmo: Int = 30,
    val harpoonCooldown: Float = 0f,
    val isInvulnerable: Boolean = false,
    val invulnerabilityTimer: Float = 0f,
    val propellerRotation: Float = 0f,
    val searchlightAngleDeg: Float = 0f,
    val livesRemaining: Int = 10
)

data class Enemy(
    val id: String,
    val type: EnemyType,
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f,
    val facingAngleDeg: Float = 0f,
    val health: Float,
    val maxHealth: Float,
    val territoryMinX: Float = 0f,
    val territoryMaxX: Float = 4000f,
    val territoryMinY: Float = 200f,
    val territoryMaxY: Float = 3000f,
    val patrolTargetX: Float = 0f,
    val patrolTargetY: Float = 0f,
    val perceptionRadius: Float = 450f,
    val attackRange: Float = 180f,
    val aiState: AIState = AIState.PATROL_TERRITORY,
    val aiTimer: Float = 0f,
    val telegraphProgress: Float = 0f, // 0f..1f warning flash before striking
    val animationPhase: Float = 0f,
    val hitFlashTimer: Float = 0f,
    val bossPhase: Int = 1, // 1: Stalker, 2: Enraged, 3: Overdrive Vortex
    val weakPointAngle: Float = 0f,
    val isBoss: Boolean = false
)

data class Harpoon(
    val id: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val angleDeg: Float,
    val lifeTime: Float = 3.5f,
    val damage: Float = 60f
)

data class BossShockwave(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float = 20f,
    val maxRadius: Float = 380f,
    val speed: Float = 280f,
    val alpha: Float = 1f,
    val damage: Float = 30f
)

data class Bubble(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val wobble: Float = 0f,
    val isCavitation: Boolean = false
)

enum class CollectibleType(val scoreVal: Int) {
    PEARL(150),
    OXYGEN_CELL(50),
    REPAIR_KIT(100),
    SHIELD_CHARGE(75),
    HARPOON_CRATE(80)
}

data class Collectible(
    val id: String,
    val type: CollectibleType,
    val x: Float,
    val y: Float,
    val isCollected: Boolean = false,
    val bobbingPhase: Float = 0f
)

data class EscapePortal(
    val x: Float = 3600f,
    val y: Float = 2400f,
    val triggerRadius: Float = 280f,
    val activationTimeRequired: Float = 8f,
    val currentHoldTime: Float = 0f,
    val pulsePhase: Float = 0f
)
