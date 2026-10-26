package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.game.ui.render3d.GodotOceanRenderer
import com.example.game.viewmodel.GameUiState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun OceanGameCanvas(
    state: GameUiState,
    onJoystickChange: (Float, Float) -> Unit,
    onBoostToggle: (Boolean) -> Unit,
    onFireHarpoon: () -> Unit,
    onMoveLeft: ((Boolean) -> Unit)? = null,
    onMoveRight: ((Boolean) -> Unit)? = null,
    onMoveUp: ((Boolean) -> Unit)? = null,
    onMoveDown: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var touchOrigin by remember { mutableStateOf<Offset?>(null) }
    var touchCurrent by remember { mutableStateOf<Offset?>(null) }
    var joystickDeltaX by remember { mutableFloatStateOf(0f) }
    var joystickDeltaY by remember { mutableFloatStateOf(0f) }

    var isLeftHeld by remember { mutableStateOf(false) }
    var isRightHeld by remember { mutableStateOf(false) }
    var isUpHeld by remember { mutableStateOf(false) }
    var isDownHeld by remember { mutableStateOf(false) }

    fun updateDirectionalInput() {
        var dx = 0f
        var dy = 0f
        if (isLeftHeld && !isRightHeld) dx = -1f
        else if (isRightHeld && !isLeftHeld) dx = 1f

        if (isUpHeld && !isDownHeld) dy = -1f
        else if (isDownHeld && !isUpHeld) dy = 1f

        if (dx != 0f && dy != 0f) {
            dx *= 0.7071f
            dy *= 0.7071f
        }

        onJoystickChange(dx, dy)
    }

    val density = LocalDensity.current
    val dpadDeadzonePx = with(density) { 120.dp.toPx() }
    val actionsDeadzoneWidthPx = with(density) { 140.dp.toPx() }
    val actionsDeadzoneHeightPx = with(density) { 110.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000814))
    ) {
        val player = state.player
        val timeSeconds = (1800f - state.timeRemainingSeconds)

        // 1. INTERACTIVE OCEAN CANVAS (Drag gesture navigation in open water, ignoring D-Pad & Action areas)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val inDPadArea = offset.x < dpadDeadzonePx && offset.y > size.height - dpadDeadzonePx
                            val inActionsArea = offset.x > size.width - actionsDeadzoneWidthPx && offset.y > size.height - actionsDeadzoneHeightPx
                            if (!inDPadArea && !inActionsArea && offset.x < size.width * 0.55f) {
                                touchOrigin = offset
                                touchCurrent = offset
                            }
                        },
                        onDragEnd = {
                            touchOrigin = null
                            touchCurrent = null
                            joystickDeltaX = 0f
                            joystickDeltaY = 0f
                            if (!isLeftHeld && !isRightHeld && !isUpHeld && !isDownHeld) {
                                onJoystickChange(0f, 0f)
                            }
                        },
                        onDragCancel = {
                            touchOrigin = null
                            touchCurrent = null
                            joystickDeltaX = 0f
                            joystickDeltaY = 0f
                            if (!isLeftHeld && !isRightHeld && !isUpHeld && !isDownHeld) {
                                onJoystickChange(0f, 0f)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            touchOrigin?.let { origin ->
                                val current = (touchCurrent ?: origin) + dragAmount
                                touchCurrent = current
                                val dx = current.x - origin.x
                                val dy = current.y - origin.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val maxRadius = 110f

                                val clampedDx = if (dist > maxRadius) (dx / dist) * maxRadius else dx
                                val clampedDy = if (dist > maxRadius) (dy / dist) * maxRadius else dy

                                joystickDeltaX = clampedDx
                                joystickDeltaY = clampedDy

                                if (!isLeftHeld && !isRightHeld && !isUpHeld && !isDownHeld) {
                                    onJoystickChange(clampedDx / maxRadius, clampedDy / maxRadius)
                                }
                            }
                        }
                    )
                }
        ) {

        Canvas(modifier = Modifier.fillMaxSize().testTag("ocean_game_canvas")) {
            val screenW = size.width
            val screenH = size.height

            val shake = state.screenShakeAmount
            val shakeX = if (shake > 0.1f) (sin(timeSeconds * 45f) * shake) else 0f
            val shakeY = if (shake > 0.1f) (cos(timeSeconds * 40f) * shake) else 0f

            // Camera centered on player submarine with dynamic screen shake on hit
            val cameraX = ((player.x - screenW * 0.5f) + shakeX).coerceIn(0f, (state.worldWidth - screenW).coerceAtLeast(0f))
            val cameraY = ((player.y - screenH * 0.5f) + shakeY).coerceIn(0f, (state.worldHeight - screenH).coerceAtLeast(0f))

            // 1. Draw Deep Ocean Water Caustics & Lighting
            GodotOceanRenderer.drawOceanEnvironment(
                scope = this,
                cameraX = cameraX,
                cameraY = cameraY,
                worldWidth = state.worldWidth,
                worldHeight = state.worldHeight,
                timeSeconds = timeSeconds
            )

            // 2. Draw Escape Portal
            val portalScreenX = state.escapePortal.x - cameraX
            val portalScreenY = state.escapePortal.y - cameraY
            GodotOceanRenderer.drawEscapePortal(
                scope = this,
                portal = state.escapePortal.copy(x = portalScreenX, y = portalScreenY),
                timeSeconds = timeSeconds
            )

            // 3. Draw Collectibles
            for (c in state.collectibles) {
                val cx = c.x - cameraX
                val cy = c.y - cameraY
                if (cx in -50f..screenW + 50f && cy in -50f..screenH + 50f) {
                    GodotOceanRenderer.drawCollectible(this, c.copy(x = cx, y = cy), timeSeconds)
                }
            }

            // 4. Draw Bubbles
            for (b in state.bubbles) {
                val bx = b.x - cameraX
                val by = b.y - cameraY
                if (bx in -20f..screenW + 20f && by in -20f..screenH + 20f) {
                    GodotOceanRenderer.drawBubble(this, b.copy(x = bx, y = by))
                }
            }

            // 5. Draw Harpoons
            for (h in state.harpoons) {
                val hx = h.x - cameraX
                val hy = h.y - cameraY
                GodotOceanRenderer.drawHarpoon(this, h.copy(x = hx, y = hy))
            }

            // 6. Draw Boss Shockwaves
            for (wave in state.shockwaves) {
                val wx = wave.x - cameraX
                val wy = wave.y - cameraY
                GodotOceanRenderer.drawBossShockwave(this, wave.copy(x = wx, y = wy))
            }

            // 7. Draw 3D Enemies & BIG Boss with Realistic AI Behaviors
            for (e in state.enemies) {
                val ex = e.x - cameraX
                val ey = e.y - cameraY
                if (ex in -200f..screenW + 200f && ey in -200f..screenH + 200f) {
                    GodotOceanRenderer.drawEnemy3D(this, e, ex, ey, timeSeconds)
                }
            }

            // 8. Draw 3D GODOT Player Submarine
            val playerScreenX = player.x - cameraX
            val playerScreenY = player.y - cameraY
            GodotOceanRenderer.drawSubmarine3D(
                scope = this,
                player = player,
                screenX = playerScreenX,
                screenY = playerScreenY,
                timeSeconds = timeSeconds
            )

            // 9. Floating Off-screen Indicator pointing to Escape Portal
            val pdx = state.escapePortal.x - player.x
            val pdy = state.escapePortal.y - player.y
            val pAngle = atan2(pdy, pdx)
            val indX = (screenW * 0.5f + cos(pAngle) * (screenW * 0.42f)).toFloat()
            val indY = (screenH * 0.5f + sin(pAngle) * (screenH * 0.38f)).toFloat()

            drawCircle(
                color = Color(0xFF00FF88),
                radius = 14f,
                center = Offset(indX, indY)
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(indX, indY)
            )

            // 10. Apex Boss Directional Tracking Beacon (Pulsing Red Skull Radar)
            val boss = state.enemies.find { it.isBoss }
            if (boss != null) {
                val bdx = boss.x - player.x
                val bdy = boss.y - player.y
                val bAngle = atan2(bdy, bdx)
                val bIndX = (screenW * 0.5f + cos(bAngle) * (screenW * 0.45f)).toFloat()
                val bIndY = (screenH * 0.5f + sin(bAngle) * (screenH * 0.40f)).toFloat()

                val pulse = (sin(timeSeconds * 6f) * 0.5f + 0.5f)
                drawCircle(
                    color = Color(0xFFFF0055).copy(alpha = 0.35f * pulse),
                    radius = 24f + pulse * 10f,
                    center = Offset(bIndX, bIndY)
                )
                drawCircle(
                    color = Color(0xFFFF0055),
                    radius = 15f,
                    center = Offset(bIndX, bIndY)
                )
                drawCircle(
                    color = Color(0xFFFFD166),
                    radius = 6f,
                    center = Offset(bIndX, bIndY)
                )
            }
        }

        // Virtual Joystick Indicator on Touch Origin (if using drag gesture)
        touchOrigin?.let { origin ->
            Box(
                modifier = Modifier
                    .offset { IntOffset(origin.x.toInt() - 60, origin.y.toInt() - 60) }
                    .size(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = 0.25f),
                        radius = 60.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = 0.7f),
                        radius = 24.dp.toPx(),
                        center = center + Offset(joystickDeltaX, joystickDeltaY)
                    )
                }
            }
        }
    }

        // 10. COMPACT FLOATING ANALOG CIRCULAR JOYSTICK (Bottom-Left, ~50% smaller)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            CompactFloatingAnalogJoystick(
                onJoystickChange = onJoystickChange,
                onMoveLeft = onMoveLeft,
                onMoveRight = onMoveRight,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown
            )
        }

        // Action Buttons on Right Side (Firing & Boost) - Scaled down & anchored tightly for 16:9 landscape
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Boost Thruster (Scaled down to 42dp)
                FloatingActionButton(
                    onClick = { onBoostToggle(true) },
                    containerColor = Color(0xFF003554),
                    contentColor = Color(0xFF00F0FF),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.size(42.dp).testTag("boost_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Thruster Boost",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Fire Harpoon (Scaled down to 48dp)
                FloatingActionButton(
                    onClick = onFireHarpoon,
                    containerColor = Color(0xFFFF0055),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(6.dp),
                    modifier = Modifier.size(48.dp).testTag("fire_harpoon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Launch Harpoon",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact Floating Analog Circular Joystick (~50% smaller than classic D-pad)
 * Delivers precise 360-degree analog submarine steering with glowing neon visual feedback.
 */
@Composable
fun CompactFloatingAnalogJoystick(
    onJoystickChange: (Float, Float) -> Unit,
    onMoveLeft: ((Boolean) -> Unit)? = null,
    onMoveRight: ((Boolean) -> Unit)? = null,
    onMoveUp: ((Boolean) -> Unit)? = null,
    onMoveDown: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val joystickRadiusPx = with(density) { 28.dp.toPx() }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(Color(0xB304111D))
            .border(2.dp, Color(0xFF00F0FF), CircleShape)
            .testTag("compact_analog_joystick")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dragVector = startOffset - center
                        val dist = sqrt(dragVector.x * dragVector.x + dragVector.y * dragVector.y)
                        val clamped = if (dist > joystickRadiusPx) {
                            Offset(dragVector.x / dist * joystickRadiusPx, dragVector.y / dist * joystickRadiusPx)
                        } else {
                            dragVector
                        }
                        knobOffset = clamped
                        val normX = (clamped.x / joystickRadiusPx).coerceIn(-1f, 1f)
                        val normY = (clamped.y / joystickRadiusPx).coerceIn(-1f, 1f)
                        onJoystickChange(normX, normY)
                        onMoveLeft?.invoke(normX < -0.3f)
                        onMoveRight?.invoke(normX > 0.3f)
                        onMoveUp?.invoke(normY < -0.3f)
                        onMoveDown?.invoke(normY > 0.3f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = knobOffset + dragAmount
                        val dist = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                        val clamped = if (dist > joystickRadiusPx) {
                            Offset(newOffset.x / dist * joystickRadiusPx, newOffset.y / dist * joystickRadiusPx)
                        } else {
                            newOffset
                        }
                        knobOffset = clamped
                        val normX = (clamped.x / joystickRadiusPx).coerceIn(-1f, 1f)
                        val normY = (clamped.y / joystickRadiusPx).coerceIn(-1f, 1f)
                        onJoystickChange(normX, normY)
                        onMoveLeft?.invoke(normX < -0.3f)
                        onMoveRight?.invoke(normX > 0.3f)
                        onMoveUp?.invoke(normY < -0.3f)
                        onMoveDown?.invoke(normY > 0.3f)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onJoystickChange(0f, 0f)
                        onMoveLeft?.invoke(false)
                        onMoveRight?.invoke(false)
                        onMoveUp?.invoke(false)
                        onMoveDown?.invoke(false)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onJoystickChange(0f, 0f)
                        onMoveLeft?.invoke(false)
                        onMoveRight?.invoke(false)
                        onMoveUp?.invoke(false)
                        onMoveDown?.invoke(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner Crosshair guides
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            drawLine(
                color = Color(0x3300F0FF),
                start = Offset(c.x, c.y - 28.dp.toPx()),
                end = Offset(c.x, c.y + 28.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0x3300F0FF),
                start = Offset(c.x - 28.dp.toPx(), c.y),
                end = Offset(c.x + 28.dp.toPx(), c.y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Floating Analog Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.toInt(), knobOffset.y.toInt()) }
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color(0xFF00F0FF), Color(0xFF00385C))
                    )
                )
                .border(1.5.dp, Color.White, CircleShape)
                .testTag("joystick_thumb_knob")
        )
    }
}
