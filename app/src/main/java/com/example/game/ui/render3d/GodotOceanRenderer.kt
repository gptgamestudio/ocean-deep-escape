package com.example.game.ui.render3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.game.model.AIState
import com.example.game.model.BossShockwave
import com.example.game.model.Bubble
import com.example.game.model.Collectible
import com.example.game.model.CollectibleType
import com.example.game.model.Enemy
import com.example.game.model.EnemyType
import com.example.game.model.EscapePortal
import com.example.game.model.Harpoon
import com.example.game.model.PlayerState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GodotOceanRenderer {

    /**
     * Draw 3D-styled Deep Ocean Water Caustics, Volumetric Murk, and Depth Gradients
     */
    fun drawOceanEnvironment(
        scope: DrawScope,
        cameraX: Float,
        cameraY: Float,
        worldWidth: Float,
        worldHeight: Float,
        timeSeconds: Float
    ) {
        val width = scope.size.width
        val height = scope.size.height

        // Depth-based abyss gradient: lighter at surface, deep abyssal black-navy at 3000m depth
        val depthRatio = (cameraY / worldHeight).coerceIn(0f, 1f)
        val surfaceWater = Color(0xFF002244)
        val midWater = Color(0xFF001122)
        val deepAbyss = Color(0xFF00050D)

        val currentWaterColor = when {
            depthRatio < 0.5f -> lerpColor(surfaceWater, midWater, depthRatio * 2f)
            else -> lerpColor(midWater, deepAbyss, (depthRatio - 0.5f) * 2f)
        }

        scope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    currentWaterColor,
                    deepAbyss
                )
            ),
            size = Size(width, height)
        )

        // Volumetric Sunbeams / Deep Caustic Rays (Faint swaying 3D light shafts)
        val shaftCount = 5
        for (i in 0 until shaftCount) {
            val phase = timeSeconds * 0.4f + i * 1.5f
            val startX = (i * 240f + sin(phase) * 60f) % (width + 300f) - 150f
            val shaftAlpha = (0.04f + 0.03f * sin(phase * 1.3f)) * (1f - depthRatio * 0.8f)

            if (shaftAlpha > 0.005f) {
                val path = Path().apply {
                    moveTo(startX, 0f)
                    lineTo(startX + 140f, 0f)
                    lineTo(startX + 300f, height)
                    lineTo(startX + 100f, height)
                    close()
                }
                scope.drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF64DFDF).copy(alpha = shaftAlpha),
                            Color(0xFF003554).copy(alpha = 0f)
                        )
                    )
                )
            }
        }
    }

    /**
     * Draw 3D-styled GODOT Submarine with volumetric hull, riveted metallic finish,
     * glass dome with pilot silhouette, spinning 3D propellers, navigation lights, and volumetric searchlights.
     */
    fun drawSubmarine3D(
        scope: DrawScope,
        player: PlayerState,
        screenX: Float,
        screenY: Float,
        timeSeconds: Float
    ) {
        val heading = player.headingDeg
        val pitch = player.pitchDeg
        val roll = player.rollDeg

        // Pitch & heading rotation
        scope.rotate(degrees = heading + pitch, pivot = Offset(screenX, screenY)) {
            // Check if facing left to flip vertically
            val isFacingLeft = cos(Math.toRadians(heading.toDouble())) < 0
            val flipScaleY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipScaleY, pivot = Offset(screenX, screenY)) {

                // 1. DUAL VOLUMETRIC SEARCHLIGHT CONES (Projecting forward through the dark ocean)
                val beamLength = 480f
                val beamSpread = 120f
                val searchlightPath = Path().apply {
                    moveTo(screenX + 50f, screenY - 8f)
                    lineTo(screenX + 50f + beamLength, screenY - beamSpread)
                    lineTo(screenX + 50f + beamLength, screenY + beamSpread)
                    lineTo(screenX + 50f, screenY + 8f)
                    close()
                }
                scope.drawPath(
                    path = searchlightPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFE0FAFF).copy(alpha = 0.42f),
                            Color(0xFF00F0FF).copy(alpha = 0.18f),
                            Color(0xFF003554).copy(alpha = 0.0f)
                        ),
                        startX = screenX + 50f,
                        endX = screenX + 50f + beamLength
                    )
                )

                // 2. 3D ROTATING TWIN PROPELLERS (With metallic blades, cavitation blur, and hub)
                val propRotation = player.propellerRotation
                val propX = screenX - 58f
                val propY = screenY

                // Propeller Shaft
                scope.drawRect(
                    color = Color(0xFF2B3A4A),
                    topLeft = Offset(propX, propY - 5f),
                    size = Size(12f, 10f)
                )

                // 3D Propeller Blades (Simulating 3-blade rotation with depth compression)
                for (bladeIdx in 0..2) {
                    val bladeAngle = propRotation + bladeIdx * 120f
                    val bladeRad = Math.toRadians(bladeAngle.toDouble())
                    val bladeLen = 22f
                    val bladeSpread = sin(bladeRad).toFloat() * bladeLen
                    val bladeDepthAlpha = 0.5f + 0.5f * cos(bladeRad).toFloat().coerceIn(0f, 1f)

                    scope.drawLine(
                        color = Color(0xFFFFD166).copy(alpha = bladeDepthAlpha),
                        start = Offset(propX + 2f, propY),
                        end = Offset(propX - 4f, propY + bladeSpread),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }

                // Propeller Bronze Cone Hub
                scope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFE194), Color(0xFFB07D1E)),
                        center = Offset(propX, propY),
                        radius = 7f
                    ),
                    radius = 6f,
                    center = Offset(propX, propY)
                )

                // 3. MAIN SUBMARINE 3D HULL (Volumetric gradient: bright top light, cylindrical shadow bottom)
                val hullPath = Path().apply {
                    // Sleek hydrodynamic teardrop submarine body
                    moveTo(screenX - 48f, screenY - 14f)
                    // Conning tower transition
                    lineTo(screenX - 10f, screenY - 15f)
                    lineTo(screenX - 6f, screenY - 30f) // Conning tower start
                    lineTo(screenX + 16f, screenY - 30f)
                    lineTo(screenX + 22f, screenY - 15f)
                    // Nose dome
                    cubicTo(
                        screenX + 44f, screenY - 14f,
                        screenX + 58f, screenY - 6f,
                        screenX + 60f, screenY
                    )
                    cubicTo(
                        screenX + 58f, screenY + 6f,
                        screenX + 44f, screenY + 14f,
                        screenX + 22f, screenY + 15f
                    )
                    // Bottom keel line
                    lineTo(screenX - 48f, screenY + 14f)
                    // Tail taper
                    cubicTo(
                        screenX - 52f, screenY + 8f,
                        screenX - 54f, screenY - 8f,
                        screenX - 48f, screenY - 14f
                    )
                    close()
                }

                // Shaded Hull Fill (Godot-like metallic shader with rim lighting)
                scope.drawPath(
                    path = hullPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF8DA4C4), // Top specular sun highlight
                            Color(0xFF384E66), // Mid hull metallic blue-gray
                            Color(0xFF1B2836), // Deep lower hull shadow
                            Color(0xFF0F1720)  // Keel shadow
                        ),
                        startY = screenY - 30f,
                        endY = screenY + 20f
                    )
                )

                // Hull Seam Ribs / Armored Rivets (Giving industrial 3D physical detail)
                val ribPositions = listOf(-30f, -12f, 8f, 28f)
                for (rx in ribPositions) {
                    scope.drawLine(
                        color = Color(0xFF5A728F),
                        start = Offset(screenX + rx, screenY - 13f),
                        end = Offset(screenX + rx, screenY + 13f),
                        strokeWidth = 2f
                    )
                    // Tiny 3D brass rivets along the rib
                    scope.drawCircle(
                        color = Color(0xFFFFD166),
                        radius = 1.2f,
                        center = Offset(screenX + rx, screenY - 8f)
                    )
                    scope.drawCircle(
                        color = Color(0xFFFFD166),
                        radius = 1.2f,
                        center = Offset(screenX + rx, screenY + 8f)
                    )
                }

                // Hydrodynamic Stabilizer Fins (Dorsal & Ventral)
                // Top Fin
                val topFin = Path().apply {
                    moveTo(screenX - 44f, screenY - 12f)
                    lineTo(screenX - 52f, screenY - 26f)
                    lineTo(screenX - 40f, screenY - 24f)
                    lineTo(screenX - 34f, screenY - 12f)
                    close()
                }
                scope.drawPath(
                    path = topFin,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF4A607A), Color(0xFF223140)),
                        start = Offset(screenX - 48f, screenY - 26f),
                        end = Offset(screenX - 38f, screenY - 12f)
                    )
                )

                // Bottom Fin
                val bottomFin = Path().apply {
                    moveTo(screenX - 44f, screenY + 12f)
                    lineTo(screenX - 52f, screenY + 26f)
                    lineTo(screenX - 40f, screenY + 24f)
                    lineTo(screenX - 34f, screenY + 12f)
                    close()
                }
                scope.drawPath(
                    path = bottomFin,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF223140), Color(0xFF111A24)),
                        start = Offset(screenX - 44f, screenY + 12f),
                        end = Offset(screenX - 48f, screenY + 26f)
                    )
                )

                // 4. COCKPIT CANOPY DOME & PILOT SILHOUETTE
                // Glass canopy with glowing instruments and curved reflection
                val canopyPath = Path().apply {
                    moveTo(screenX + 32f, screenY - 9f)
                    cubicTo(
                        screenX + 46f, screenY - 7f,
                        screenX + 54f, screenY - 3f,
                        screenX + 56f, screenY
                    )
                    cubicTo(
                        screenX + 54f, screenY + 3f,
                        screenX + 46f, screenY + 7f,
                        screenX + 32f, screenY + 9f
                    )
                    close()
                }

                // Inner Cockpit Bio-Glow
                scope.drawPath(
                    path = canopyPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.85f),
                            Color(0xFF00527A).copy(alpha = 0.95f)
                        ),
                        center = Offset(screenX + 42f, screenY),
                        radius = 16f
                    )
                )

                // Pilot Diver Silhouette inside dome
                scope.drawCircle(
                    color = Color(0xFF0A1E2E),
                    radius = 3.5f,
                    center = Offset(screenX + 39f, screenY - 1f)
                )
                scope.drawRoundRect(
                    color = Color(0xFF0A1E2E),
                    topLeft = Offset(screenX + 36f, screenY + 2f),
                    size = Size(8f, 5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // Curved Glass Specular Highlight Arc
                scope.drawArc(
                    color = Color.White.copy(alpha = 0.7f),
                    startAngle = 200f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(screenX + 34f, screenY - 8f),
                    size = Size(18f, 16f),
                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                )

                // 5. NAVIGATION LIGHTS & STROBE BEACON
                // Conning Tower Strobe Beacon (Blinks white/amber every second)
                val strobeOn = (timeSeconds % 1.0f) < 0.2f
                if (strobeOn) {
                    scope.drawCircle(
                        color = Color(0xFFFFD166),
                        radius = 4.5f,
                        center = Offset(screenX + 5f, screenY - 33f)
                    )
                    scope.drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = Offset(screenX + 5f, screenY - 33f)
                    )
                } else {
                    scope.drawCircle(
                        color = Color(0xFF886611),
                        radius = 2.5f,
                        center = Offset(screenX + 5f, screenY - 33f)
                    )
                }

                // Port & Starboard running lights
                scope.drawCircle(
                    color = Color(0xFFFF0055), // Red
                    radius = 2.5f,
                    center = Offset(screenX + 18f, screenY - 14f)
                )
                scope.drawCircle(
                    color = Color(0xFF00FF66), // Green
                    radius = 2.5f,
                    center = Offset(screenX + 18f, screenY + 14f)
                )

                // 6. DUAL HARPOON / TORPEDO PODS (Pneumatic rails on port & starboard)
                val podYOffsets = listOf(-16f, 16f)
                val isHarpoonReady = player.harpoonCooldown <= 0.05f
                val statusLedColor = if (isHarpoonReady) Color(0xFF00FF88) else Color(0xFFFFB703)
                for (py in podYOffsets) {
                    // Metallic Pod Cylinder
                    scope.drawRoundRect(
                        color = Color(0xFF1E2D3D),
                        topLeft = Offset(screenX + 8f, screenY + py - 3.5f),
                        size = Size(26f, 7f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                    // Rail highlight
                    scope.drawLine(
                        color = Color(0xFF5A728F),
                        start = Offset(screenX + 10f, screenY + py - 2f),
                        end = Offset(screenX + 32f, screenY + py - 2f),
                        strokeWidth = 1.2f
                    )
                    // Arming status LED
                    scope.drawCircle(
                        color = statusLedColor,
                        radius = 1.8f,
                        center = Offset(screenX + 12f, screenY + py)
                    )
                }

                // 7. LOW HEALTH HULL DISTRESS (Underwater spark arcs and smoke when HP < 50%)
                if (player.currentHp < 250f) {
                    val distressPhase = (timeSeconds * 12f) % 6.28f
                    if (sin(distressPhase) > 0.3f) {
                        // Electric spark discharge arc
                        scope.drawLine(
                            color = Color(0xFFFFD166),
                            start = Offset(screenX - 10f, screenY - 14f),
                            end = Offset(screenX - 15f + sin(distressPhase) * 6f, screenY - 22f),
                            strokeWidth = 1.8f,
                            cap = StrokeCap.Round
                        )
                    }
                    // Smoldering hull warning glow
                    scope.drawCircle(
                        color = Color(0xFFFF0055).copy(alpha = 0.25f + 0.2f * sin(distressPhase)),
                        radius = 24f,
                        center = Offset(screenX - 8f, screenY)
                    )
                }

                // 8. SHIELD AURA & INVULNERABILITY SHIMMER
                if (player.shields > 10f || player.isInvulnerable) {
                    val shieldAlpha = if (player.isInvulnerable) {
                        0.4f + 0.4f * sin(timeSeconds * 18f)
                    } else {
                        (player.shields / player.maxShields) * 0.35f
                    }
                    scope.drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00F0FF).copy(alpha = shieldAlpha * 0.4f),
                                Color(0xFF64DFDF).copy(alpha = shieldAlpha)
                            ),
                            center = Offset(screenX + 4f, screenY),
                            radius = 65f
                        ),
                        topLeft = Offset(screenX - 62f, screenY - 36f),
                        size = Size(130f, 72f),
                        style = Stroke(width = 2.2f)
                    )
                }
            }
        }
    }

    /**
     * Draw Realistic 3D-styled Enemies with biological sine-wave locomotion,
     * glowing bioluminescent organs, predatory teeth, and attack warning indicators.
     */
    fun drawEnemy3D(
        scope: DrawScope,
        enemy: Enemy,
        screenX: Float,
        screenY: Float,
        timeSeconds: Float
    ) {
        when (enemy.type) {
            EnemyType.GREAT_WHITE_SHARK -> drawApexShark(scope, enemy, screenX, screenY, timeSeconds)
            EnemyType.ABYSSAL_ANGLER -> drawAbyssalAngler(scope, enemy, screenX, screenY, timeSeconds)
            EnemyType.GIANT_KRAKEN_SQUID -> drawKrakenSquid(scope, enemy, screenX, screenY, timeSeconds)
            EnemyType.VIPER_EEL -> drawViperEel(scope, enemy, screenX, screenY, timeSeconds)
            EnemyType.LEVIATHAN_PRIME -> drawLeviathanPrimeBoss(scope, enemy, screenX, screenY, timeSeconds)
        }

        // Draw Health Bar & Target Indicator directly above Boss
        if (enemy.isBoss) {
            val hpRatio = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)
            val barW = 150f
            val barH = 10f
            val barX = screenX - barW * 0.5f
            val barY = screenY - 95f

            // Frame
            scope.drawRoundRect(
                color = Color(0xDD000E1C),
                topLeft = Offset(barX - 4f, barY - 4f),
                size = Size(barW + 8f, barH + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            scope.drawRoundRect(
                color = Color(0xFFFF0055),
                topLeft = Offset(barX - 4f, barY - 4f),
                size = Size(barW + 8f, barH + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f)
            )
            // Health Fill
            scope.drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF0055), Color(0xFFFFD166)),
                    startX = barX,
                    endX = barX + barW
                ),
                topLeft = Offset(barX, barY),
                size = Size(barW * hpRatio, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Glowing diamond beacon
            val pulse = (sin(timeSeconds * 6f) * 0.5f + 0.5f)
            scope.drawCircle(
                color = Color(0xFFFFD166).copy(alpha = 0.5f + 0.5f * pulse),
                radius = 7f + pulse * 4f,
                center = Offset(screenX, barY - 14f)
            )
            scope.drawCircle(
                color = Color(0xFFFF0055),
                radius = 5f,
                center = Offset(screenX, barY - 14f)
            )
        }
    }

    /**
     * 1. APEX SHARK (Realistic 3D Megatooth predator with undulating tail & menacing jaws)
     */
    private fun drawApexShark(
        scope: DrawScope,
        enemy: Enemy,
        x: Float,
        y: Float,
        timeSeconds: Float
    ) {
        val angle = enemy.facingAngleDeg
        scope.rotate(degrees = angle, pivot = Offset(x, y)) {
            val isFacingLeft = cos(Math.toRadians(angle.toDouble())) < 0
            val flipY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipY, pivot = Offset(x, y)) {

                val undulation = sin(enemy.animationPhase) * 14f

                // Attack telegraph laser / threat cone
                if (enemy.telegraphProgress > 0.05f) {
                    scope.drawCircle(
                        color = Color(0xFFFF0055).copy(alpha = enemy.telegraphProgress * 0.5f),
                        radius = 28f + enemy.telegraphProgress * 15f,
                        center = Offset(x + 40f, y)
                    )
                    // Directional charge line
                    scope.drawLine(
                        color = Color(0xFFFF0055).copy(alpha = enemy.telegraphProgress * 0.8f),
                        start = Offset(x + 45f, y),
                        end = Offset(x + 180f, y),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                // Tail Fin (3D undulating with sine wave motion)
                val tailPath = Path().apply {
                    val tailX = x - 52f
                    val tailY = y + undulation
                    moveTo(x - 30f, y)
                    lineTo(tailX, tailY - 24f)
                    lineTo(tailX - 14f, tailY - 18f)
                    lineTo(tailX - 4f, tailY)
                    lineTo(tailX - 12f, tailY + 16f)
                    lineTo(tailX, tailY + 22f)
                    close()
                }
                scope.drawPath(
                    path = tailPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3A4B5E), Color(0xFF1D2833)),
                        startY = y - 24f,
                        endY = y + 24f
                    )
                )

                // Hydrodynamic Torso (3D counter-shading: Slate Gray top, White-Silver belly)
                val bodyPath = Path().apply {
                    moveTo(x - 35f, y)
                    // Dorsal curve
                    cubicTo(x - 10f, y - 18f, x + 20f, y - 20f, x + 48f, y - 4f)
                    // Snout tip
                    lineTo(x + 56f, y)
                    // Ventral / Jaw curve
                    cubicTo(x + 46f, y + 14f, x + 15f, y + 16f, x - 35f, y)
                    close()
                }
                scope.drawPath(
                    path = bodyPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A5C70), // Dorsal dark gray
                            Color(0xFF2E3E50), // Midbody
                            Color(0xFFCCD7E4)  // Ventral pale counter-shading
                        ),
                        startY = y - 20f,
                        endY = y + 16f
                    )
                )

                // Dorsal Fin with 3D depth
                val dorsalFin = Path().apply {
                    moveTo(x + 2f, y - 17f)
                    lineTo(x - 8f, y - 38f)
                    lineTo(x + 10f, y - 34f)
                    lineTo(x + 18f, y - 17f)
                    close()
                }
                scope.drawPath(
                    path = dorsalFin,
                    color = Color(0xFF283645)
                )

                // Pectoral Fin (Extended sideways in 3D perspective)
                val pectoralFin = Path().apply {
                    moveTo(x + 16f, y + 8f)
                    lineTo(x + 2f, y + 26f)
                    lineTo(x + 18f, y + 24f)
                    lineTo(x + 28f, y + 10f)
                    close()
                }
                scope.drawPath(
                    path = pectoralFin,
                    color = Color(0xFF334557)
                )

                // Gill Slits (3 vertical curved dark lines)
                for (g in 0..2) {
                    val gx = x + 18f - g * 4f
                    scope.drawLine(
                        color = Color(0xFF151D24),
                        start = Offset(gx, y - 4f),
                        end = Offset(gx - 1f, y + 7f),
                        strokeWidth = 1.5f
                    )
                }

                // Predatory Eye (Fierce glowing amber/red when hunting)
                val eyeColor = if (enemy.aiState == AIState.BURST_STRIKE || enemy.telegraphProgress > 0f) {
                    Color(0xFFFF0055)
                } else {
                    Color(0xFFFFB703)
                }
                scope.drawCircle(
                    color = eyeColor,
                    radius = 3.5f,
                    center = Offset(x + 42f, y - 4f)
                )
                scope.drawCircle(
                    color = Color.Black,
                    radius = 1.5f,
                    center = Offset(x + 42.5f, y - 4f)
                )

                // Jaws with sharp serrated teeth
                val jawOpening = if (enemy.telegraphProgress > 0f) 8f else 3f
                scope.drawLine(
                    color = Color(0xFF10171E),
                    start = Offset(x + 38f, y + 4f),
                    end = Offset(x + 54f, y + jawOpening),
                    strokeWidth = 2.5f
                )
                // Teeth
                for (t in 0..4) {
                    val tx = x + 40f + t * 3f
                    scope.drawLine(
                        color = Color.White,
                        start = Offset(tx, y + 4f),
                        end = Offset(tx, y + 7f),
                        strokeWidth = 1.2f
                    )
                }
            }
        }
    }

    /**
     * 2. ABYSSAL ANGLER (Deep-sea bio-horror with luminous esca lure and needle fangs)
     */
    private fun drawAbyssalAngler(
        scope: DrawScope,
        enemy: Enemy,
        x: Float,
        y: Float,
        timeSeconds: Float
    ) {
        val angle = enemy.facingAngleDeg
        scope.rotate(degrees = angle, pivot = Offset(x, y)) {
            val isFacingLeft = cos(Math.toRadians(angle.toDouble())) < 0
            val flipY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipY, pivot = Offset(x, y)) {

                // 1. BIOLUMINESCENT ESCA LURE (Hangs on curved antenna ahead of mouth)
                val lureTipX = x + 62f
                val lureTipY = y - 26f + sin(timeSeconds * 3f) * 6f

                // Curved Antenna Stalk
                val stalk = Path().apply {
                    moveTo(x + 10f, y - 22f)
                    cubicTo(x + 35f, y - 42f, x + 55f, y - 38f, lureTipX, lureTipY)
                }
                scope.drawPath(
                    path = stalk,
                    color = Color(0xFF4A3B32),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Glowing Esca Bulb with dynamic point-light radial aura
                val pulse = 0.7f + 0.3f * sin(timeSeconds * 5f)
                scope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.9f * pulse),
                            Color(0xFF05D9E8).copy(alpha = 0.4f * pulse),
                            Color.Transparent
                        ),
                        center = Offset(lureTipX, lureTipY),
                        radius = 32f
                    ),
                    radius = 32f,
                    center = Offset(lureTipX, lureTipY)
                )
                scope.drawCircle(
                    color = Color.White,
                    radius = 4.5f,
                    center = Offset(lureTipX, lureTipY)
                )

                // 2. MONSTROUS ROUND ABYSSAL BODY
                val body = Path().apply {
                    moveTo(x - 30f, y)
                    cubicTo(x - 20f, y - 28f, x + 15f, y - 28f, x + 35f, y - 10f)
                    // Massive gaping jaw
                    lineTo(x + 48f, y + 8f)
                    lineTo(x + 28f, y + 26f)
                    cubicTo(x, y + 28f, x - 25f, y + 16f, x - 30f, y)
                    close()
                }
                scope.drawPath(
                    path = body,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2A1B18), Color(0xFF140D0C)),
                        startY = y - 28f,
                        endY = y + 28f
                    )
                )

                // Translucent needle teeth extending outside jaws
                for (i in 0..7) {
                    val toothX = x + 30f + i * 2.4f
                    val toothLen = 10f + (i % 3) * 4f
                    scope.drawLine(
                        color = Color(0xFFEEF5F8).copy(alpha = 0.85f),
                        start = Offset(toothX, y + 8f),
                        end = Offset(toothX - 2f, y + 8f - toothLen),
                        strokeWidth = 1.4f,
                        cap = StrokeCap.Round
                    )
                }

                // Spooky Pale Glazed Eye
                scope.drawCircle(
                    color = Color(0xFF90E0EF),
                    radius = 4f,
                    center = Offset(x + 24f, y - 12f)
                )
                scope.drawCircle(
                    color = Color(0xFF03045E),
                    radius = 2f,
                    center = Offset(x + 25f, y - 12f)
                )
            }
        }
    }

    /**
     * 3. TITAN KRAKEN SQUID (8 multi-jointed tentacles in 3D layers, pulsing siphon, chromatic skin)
     */
    private fun drawKrakenSquid(
        scope: DrawScope,
        enemy: Enemy,
        x: Float,
        y: Float,
        timeSeconds: Float
    ) {
        val angle = enemy.facingAngleDeg
        scope.rotate(degrees = angle, pivot = Offset(x, y)) {
            val isFacingLeft = cos(Math.toRadians(angle.toDouble())) < 0
            val flipY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipY, pivot = Offset(x, y)) {

                // Background Tentacles (Darker for 3D depth)
                for (t in 0..3) {
                    val tentacleOffset = (t - 1.5f) * 12f
                    val wave = sin(enemy.animationPhase + t * 0.8f) * 16f
                    val tentaclePath = Path().apply {
                        moveTo(x - 10f, y + tentacleOffset)
                        cubicTo(
                            x - 35f, y + tentacleOffset + wave * 0.5f,
                            x - 60f, y + tentacleOffset - wave,
                            x - 85f, y + tentacleOffset + wave
                        )
                    }
                    scope.drawPath(
                        path = tentaclePath,
                        color = Color(0xFF4A154B),
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )
                }

                // Squid Conical Mantle
                val mantle = Path().apply {
                    moveTo(x + 55f, y) // Mantle tip
                    cubicTo(x + 35f, y - 20f, x + 5f, y - 18f, x - 15f, y - 12f)
                    lineTo(x - 15f, y + 12f)
                    cubicTo(x + 5f, y + 18f, x + 35f, y + 20f, x + 55f, y)
                    close()
                }
                scope.drawPath(
                    path = mantle,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF7209B7), Color(0xFFF72585), Color(0xFF480CA8)),
                        startX = x - 15f,
                        endX = x + 55f
                    )
                )

                // Large Abyssal Squid Eye
                scope.drawCircle(
                    color = Color(0xFFFFD166),
                    radius = 6f,
                    center = Offset(x - 6f, y - 6f)
                )
                scope.drawCircle(
                    color = Color.Black,
                    radius = 3.5f,
                    center = Offset(x - 6f, y - 6f)
                )

                // Foreground Tentacles (Brighter, dynamic lashing)
                for (t in 0..3) {
                    val tentacleOffset = (t - 1.5f) * 10f
                    val wave = cos(enemy.animationPhase + t * 1.1f) * 18f
                    val tentaclePath = Path().apply {
                        moveTo(x - 12f, y + tentacleOffset)
                        cubicTo(
                            x - 40f, y + tentacleOffset - wave * 0.6f,
                            x - 68f, y + tentacleOffset + wave,
                            x - 95f, y + tentacleOffset - wave
                        )
                    }
                    scope.drawPath(
                        path = tentaclePath,
                        color = Color(0xFFB5179E),
                        style = Stroke(width = 5.5f, cap = StrokeCap.Round)
                    )
                    // Glowing suction cups
                    for (c in 1..4) {
                        val cx = x - 12f - c * 18f
                        scope.drawCircle(
                            color = Color(0xFF4CC9F0),
                            radius = 1.8f,
                            center = Offset(cx, y + tentacleOffset)
                        )
                    }
                }
            }
        }
    }

    /**
     * 4. VIPER EEL (Serpentine armored predator with crackling electric nodes)
     */
    private fun drawViperEel(
        scope: DrawScope,
        enemy: Enemy,
        x: Float,
        y: Float,
        timeSeconds: Float
    ) {
        val angle = enemy.facingAngleDeg
        scope.rotate(degrees = angle, pivot = Offset(x, y)) {
            val isFacingLeft = cos(Math.toRadians(angle.toDouble())) < 0
            val flipY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipY, pivot = Offset(x, y)) {

                // Serpentine undulating spine (12 articulated vertebrae)
                val segments = 10
                for (s in segments downTo 0) {
                    val segX = x + 35f - s * 9f
                    val waveOffset = sin(enemy.animationPhase + s * 0.55f) * 12f
                    val segY = y + waveOffset
                    val radius = if (s == 0) 9f else (8f - s * 0.6f).coerceAtLeast(2.5f)

                    scope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00F5D4), Color(0xFF0077B6), Color(0xFF03045E)),
                            center = Offset(segX, segY),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(segX, segY)
                    )

                    // Electric arcs crackling between vertebrae
                    if (s % 2 == 0) {
                        scope.drawCircle(
                            color = Color(0xFF90E0EF),
                            radius = 2.2f,
                            center = Offset(segX, segY - radius * 0.8f)
                        )
                    }
                }

                // Viper Head & Fangs
                scope.drawCircle(
                    color = Color(0xFF00B4D8),
                    radius = 9f,
                    center = Offset(x + 36f, y)
                )
                // Glowing eyes
                scope.drawCircle(
                    color = Color(0xFFFF0055),
                    radius = 2.5f,
                    center = Offset(x + 40f, y - 4f)
                )
            }
        }
    }

    /**
     * 5. LEVIATHAN PRIME - THE APEX BOSS
     * Colossal size, 3-phase combat visuals, hydrothermal glowing core,
     * armored chitinous plates, shockwave arcs, and targeting vector beam.
     */
    private fun drawLeviathanPrimeBoss(
        scope: DrawScope,
        enemy: Enemy,
        x: Float,
        y: Float,
        timeSeconds: Float
    ) {
        val angle = enemy.facingAngleDeg
        scope.rotate(degrees = angle, pivot = Offset(x, y)) {
            val isFacingLeft = cos(Math.toRadians(angle.toDouble())) < 0
            val flipY = if (isFacingLeft) -1f else 1f
            scope.scale(scaleX = 1f, scaleY = flipY, pivot = Offset(x, y)) {

                val phase = enemy.bossPhase
                val hpRatio = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)

                // 1. BOSS CHARGE / TELEGRAPH VECTOR BEAM (Threat indicator when preparing ramming attack)
                if (enemy.telegraphProgress > 0.05f) {
                    val beamAlpha = enemy.telegraphProgress * 0.85f
                    // Threat warning line
                    scope.drawLine(
                        color = Color(0xFFFF0055).copy(alpha = beamAlpha),
                        start = Offset(x + 80f, y),
                        end = Offset(x + 500f, y),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    // Threat circles pulsing forward
                    for (c in 1..4) {
                        scope.drawCircle(
                            color = Color(0xFFFF0055).copy(alpha = beamAlpha * (1f - c * 0.2f)),
                            radius = c * 22f,
                            center = Offset(x + 80f + c * 80f, y),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }

                // 2. MASSIVE ARTICULATED ARMORED CARAPACE (Colossal 160px body)
                val tailWave = sin(enemy.animationPhase) * 22f
                val bodyLength = 140f

                // Massive Tail segments (Layered)
                for (seg in 4 downTo 1) {
                    val segX = x - bodyLength + seg * 26f
                    val segWave = sin(enemy.animationPhase + seg * 0.6f) * (18f - seg * 3f)
                    val segY = y + segWave

                    val platePath = Path().apply {
                        moveTo(segX - 18f, segY)
                        lineTo(segX, segY - 28f + seg * 4f)
                        lineTo(segX + 22f, segY - 24f + seg * 4f)
                        lineTo(segX + 18f, segY)
                        lineTo(segX + 22f, segY + 24f - seg * 4f)
                        lineTo(segX, segY + 28f - seg * 4f)
                        close()
                    }
                    scope.drawPath(
                        path = platePath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF2C1938), Color(0xFF14071A), Color(0xFF3F1952)),
                            startY = segY - 30f,
                            endY = segY + 30f
                        )
                    )
                }

                // Main Armored Carapace Body
                val carapace = Path().apply {
                    moveTo(x - 50f, y)
                    // Top armored ridge
                    cubicTo(x - 20f, y - 48f, x + 30f, y - 52f, x + 85f, y - 18f)
                    // Jaws & Mandibles
                    lineTo(x + 115f, y)
                    // Bottom armored plates
                    lineTo(x + 85f, y + 18f)
                    cubicTo(x + 30f, y + 52f, x - 20f, y + 48f, x - 50f, y)
                    close()
                }

                val carapaceColors = when (phase) {
                    3 -> listOf(Color(0xFFFF0055), Color(0xFF4A0E17), Color(0xFF1F0008)) // Phase 3 Berserk Red
                    2 -> listOf(Color(0xFFFFB703), Color(0xFF5A2A00), Color(0xFF220D00)) // Phase 2 Enraged Amber
                    else -> listOf(Color(0xFF7209B7), Color(0xFF2B0938), Color(0xFF0F0014)) // Phase 1 Deep Purple
                }
                scope.drawPath(
                    path = carapace,
                    brush = Brush.verticalGradient(
                        colors = carapaceColors,
                        startY = y - 50f,
                        endY = y + 50f
                    )
                )

                // 3. GLOWING HYDROTHERMAL CORE (Weak Point / Power Core at Center)
                val corePulse = 0.7f + 0.3f * sin(timeSeconds * 6f)
                val coreColor = when (phase) {
                    3 -> Color(0xFFFF0055)
                    2 -> Color(0xFFFF9E00)
                    else -> Color(0xFF00F0FF)
                }
                scope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            coreColor,
                            Color.Transparent
                        ),
                        center = Offset(x + 10f, y),
                        radius = 28f * corePulse
                    ),
                    radius = 28f * corePulse,
                    center = Offset(x + 10f, y)
                )

                // Weak Point Target Reticle (Indicates 2.5x Critical Hit Zone)
                val reticleRadius = 24f + 4f * sin(timeSeconds * 8f)
                scope.drawCircle(
                    color = Color(0xFFFFD166).copy(alpha = 0.85f),
                    radius = reticleRadius,
                    center = Offset(x + 10f, y),
                    style = Stroke(width = 2f)
                )
                // Reticle Crosshair Ticks
                for (a in listOf(0f, 90f, 180f, 270f)) {
                    val rad = Math.toRadians(a.toDouble())
                    val tickStart = Offset((x + 10f) + cos(rad).toFloat() * (reticleRadius - 5f), y + sin(rad).toFloat() * (reticleRadius - 5f))
                    val tickEnd = Offset((x + 10f) + cos(rad).toFloat() * (reticleRadius + 7f), y + sin(rad).toFloat() * (reticleRadius + 7f))
                    scope.drawLine(
                        color = Color(0xFFFFD166),
                        start = tickStart,
                        end = tickEnd,
                        strokeWidth = 2.2f
                    )
                }

                // 3B. ABYSSAL VORTEX DISTORTION SWIRL (When in BOSS_VORTEX or Phase 3 Berserk)
                if (enemy.aiState == AIState.BOSS_VORTEX || phase == 3) {
                    val vortexRot = timeSeconds * 12f
                    val mawX = x + 115f
                    val mawY = y
                    for (v in 1..4) {
                        val vRadius = v * 28f + (sin(vortexRot + v) * 8f)
                        scope.drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color.Transparent,
                                    Color(0xFFFF0055).copy(alpha = 0.5f),
                                    Color(0xFF9D4EDD).copy(alpha = 0.8f),
                                    Color.Transparent
                                ),
                                center = Offset(mawX, mawY)
                            ),
                            startAngle = (vortexRot * 40f + v * 90f) % 360f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(mawX - vRadius, mawY - vRadius),
                            size = Size(vRadius * 2f, vRadius * 2f),
                            style = Stroke(width = 3.2f)
                        )
                    }
                }

                // Armored Spines along back
                val spineCount = 6
                for (s in 0 until spineCount) {
                    val sx = x - 30f + s * 18f
                    val spineHeight = 18f + sin(s * 0.8f) * 12f
                    val spinePath = Path().apply {
                        moveTo(sx, y - 36f)
                        lineTo(sx + 6f, y - 36f - spineHeight)
                        lineTo(sx + 12f, y - 36f)
                        close()
                    }
                    scope.drawPath(
                        path = spinePath,
                        color = coreColor.copy(alpha = 0.85f)
                    )
                }

                // 4. MULTI-EYE CLUSTER (4 predatory eyes glowing menacingly)
                val eyePositions = listOf(
                    Offset(x + 68f, y - 16f),
                    Offset(x + 78f, y - 8f),
                    Offset(x + 84f, y + 2f),
                    Offset(x + 74f, y + 12f)
                )
                for (eyePos in eyePositions) {
                    scope.drawCircle(
                        color = Color(0xFFFF0055),
                        radius = 4f,
                        center = eyePos
                    )
                    scope.drawCircle(
                        color = Color.White,
                        radius = 1.8f,
                        center = eyePos
                    )
                }

                // Hit Flash Effect (Turns white for 0.15s when harpooned)
                if (enemy.hitFlashTimer > 0f) {
                    scope.drawPath(
                        path = carapace,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }

    /**
     * Draw Boss Shockwave Rings
     */
    fun drawBossShockwave(scope: DrawScope, wave: BossShockwave) {
        scope.drawCircle(
            color = Color(0xFFFF0055).copy(alpha = wave.alpha.coerceIn(0f, 1f)),
            radius = wave.radius,
            center = Offset(wave.x, wave.y),
            style = Stroke(width = 4f)
        )
        scope.drawCircle(
            color = Color(0xFF9D4EDD).copy(alpha = (wave.alpha * 0.5f).coerceIn(0f, 1f)),
            radius = (wave.radius * 0.85f).coerceAtLeast(1f),
            center = Offset(wave.x, wave.y),
            style = Stroke(width = 2f)
        )
    }

    /**
     * Draw Harpoons / Torpedoes fired by the submarine
     */
    fun drawHarpoon(scope: DrawScope, harpoon: Harpoon) {
        scope.rotate(degrees = harpoon.angleDeg, pivot = Offset(harpoon.x, harpoon.y)) {
            // Torpedo Body
            scope.drawRoundRect(
                color = Color(0xFFFFD166),
                topLeft = Offset(harpoon.x - 14f, harpoon.y - 3f),
                size = Size(28f, 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            // Glowing energy tip
            scope.drawCircle(
                color = Color(0xFF00F0FF),
                radius = 3.5f,
                center = Offset(harpoon.x + 14f, harpoon.y)
            )
            // Rocket Propulsion Wake Trail
            scope.drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF00F0FF).copy(alpha = 0.8f)),
                    startX = harpoon.x - 35f,
                    endX = harpoon.x - 14f
                ),
                start = Offset(harpoon.x - 35f, harpoon.y),
                end = Offset(harpoon.x - 14f, harpoon.y),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Draw Ocean Collectibles (Pearls, Repair kits, Oxygen cells)
     */
    fun drawCollectible(scope: DrawScope, item: Collectible, timeSeconds: Float) {
        val bobbingY = item.y + sin(item.bobbingPhase + timeSeconds * 2.5f) * 6f

        when (item.type) {
            CollectibleType.PEARL -> {
                // Iridescent glowing pearl inside clam shell
                scope.drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFE0FAFF), Color(0xFF64DFDF)),
                        center = Offset(item.x, bobbingY),
                        radius = 12f
                    ),
                    radius = 10f,
                    center = Offset(item.x, bobbingY)
                )
                // Shimmer halo
                scope.drawCircle(
                    color = Color(0xFF00F0FF).copy(alpha = 0.35f),
                    radius = 16f,
                    center = Offset(item.x, bobbingY),
                    style = Stroke(width = 1.5f)
                )
            }
            CollectibleType.OXYGEN_CELL -> {
                // High-tech Blue Oxygen Capsule
                scope.drawRoundRect(
                    color = Color(0xFF0077B6),
                    topLeft = Offset(item.x - 8f, bobbingY - 14f),
                    size = Size(16f, 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                scope.drawCircle(
                    color = Color(0xFF90E0EF),
                    radius = 4f,
                    center = Offset(item.x, bobbingY - 4f)
                )
            }
            CollectibleType.REPAIR_KIT -> {
                // Nanite Repair Box with Green Cross
                scope.drawRoundRect(
                    color = Color(0xFF1B4332),
                    topLeft = Offset(item.x - 12f, bobbingY - 12f),
                    size = Size(24f, 24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                // Cross
                scope.drawRect(
                    color = Color(0xFF52B788),
                    topLeft = Offset(item.x - 3f, bobbingY - 8f),
                    size = Size(6f, 16f)
                )
                scope.drawRect(
                    color = Color(0xFF52B788),
                    topLeft = Offset(item.x - 8f, bobbingY - 3f),
                    size = Size(16f, 6f)
                )
            }
            CollectibleType.SHIELD_CHARGE -> {
                // Shield Power Cell (Cyan Diamond)
                val diamond = Path().apply {
                    moveTo(item.x, bobbingY - 14f)
                    lineTo(item.x + 12f, bobbingY)
                    lineTo(item.x, bobbingY + 14f)
                    lineTo(item.x - 12f, bobbingY)
                    close()
                }
                scope.drawPath(path = diamond, color = Color(0xFF00F0FF))
            }
            CollectibleType.HARPOON_CRATE -> {
                // Munitions Crate
                scope.drawRect(
                    color = Color(0xFFB07D1E),
                    topLeft = Offset(item.x - 11f, bobbingY - 9f),
                    size = Size(22f, 18f)
                )
            }
        }
    }

    /**
     * Draw Dynamic Bubble Particles
     */
    fun drawBubble(scope: DrawScope, bubble: Bubble) {
        val wobbleX = bubble.x + sin(bubble.wobble) * 3f
        scope.drawCircle(
            color = if (bubble.isCavitation) Color.White.copy(alpha = bubble.alpha * 0.75f)
            else Color(0xFFBEE3F8).copy(alpha = bubble.alpha * 0.5f),
            radius = bubble.radius,
            center = Offset(wobbleX, bubble.y)
        )
        // Specular arc
        if (bubble.radius > 3f) {
            scope.drawCircle(
                color = Color.White.copy(alpha = bubble.alpha * 0.8f),
                radius = bubble.radius * 0.35f,
                center = Offset(wobbleX - bubble.radius * 0.35f, bubble.y - bubble.radius * 0.35f)
            )
        }
    }

    /**
     * Draw Escape Portal Trigger Area with beacon pulse
     */
    fun drawEscapePortal(scope: DrawScope, portal: EscapePortal, timeSeconds: Float) {
        val pulse = sin(portal.pulsePhase) * 12f
        // Outer trigger perimeter
        scope.drawCircle(
            color = Color(0xFF00FF88).copy(alpha = 0.25f + 0.1f * sin(timeSeconds * 4f)),
            radius = portal.triggerRadius + pulse,
            center = Offset(portal.x, portal.y),
            style = Stroke(width = 3f)
        )
        // Inner Beacon
        scope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFF00FF88).copy(alpha = 0.8f),
                    Color.Transparent
                ),
                center = Offset(portal.x, portal.y),
                radius = 70f
            ),
            radius = 70f,
            center = Offset(portal.x, portal.y)
        )
    }

    private fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = c1.red + (c2.red - c1.red) * f,
            green = c1.green + (c2.green - c1.green) * f,
            blue = c1.blue + (c2.blue - c1.blue) * f,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * f
        )
    }
}
