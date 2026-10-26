package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.example.game.ads.AdMobBannerAd
import com.example.game.ads.AdMobManager
import com.example.game.model.GamePhase
import com.example.game.ui.GameOverDialog
import com.example.game.ui.GameHudOverlay
import com.example.game.ui.OceanGameCanvas
import com.example.game.ui.PauseMenuOverlay
import com.example.game.ui.StartMenuOverlay
import com.example.game.ui.VictoryDialog
import com.example.game.ui.WelcomeLandingScreen
import com.example.game.viewmodel.OceanGameViewModel
import com.example.ui.theme.OceanDeepEscapeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Google Mobile Ads SDK
        AdMobManager.initialize(this)

        setContent {
            OceanDeepEscapeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000814)
                ) {
                    OceanGameApp()
                }
            }
        }
    }
}

@Composable
fun OceanGameApp(viewModel: OceanGameViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    // Android Hardware Back Navigation Handler
    BackHandler {
        when (state.phase) {
            GamePhase.PLAYING -> viewModel.pauseGame()
            GamePhase.PAUSED -> viewModel.resumeGame()
            GamePhase.START_MENU -> viewModel.returnToWelcome()
            GamePhase.GAME_OVER, GamePhase.VICTORY -> viewModel.returnToMenu()
            GamePhase.WELCOME -> {
                // Exit app naturally
            }
            else -> viewModel.returnToMenu()
        }
    }

    val activity = LocalContext.current as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020B18))
    ) {
        // TOP SECTION: Full Game Canvas / Active Screen (Takes all available top space)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Core Game Screens by Phase
            when (state.phase) {
                GamePhase.WELCOME -> {
                    WelcomeLandingScreen(
                        currentName = state.playerName,
                        hasActiveSession = state.hasActiveSession,
                        currentLevel = state.currentLevel,
                        onStartGame = { chosenName ->
                            viewModel.updatePlayerName(chosenName)
                            viewModel.startGame()
                        },
                        onEnterDashboard = { chosenName ->
                            viewModel.updatePlayerName(chosenName)
                            viewModel.enterGameDashboard()
                        }
                    )
                }

                GamePhase.START_MENU -> {
                    StartMenuOverlay(
                        playerName = state.playerName,
                        currentLevel = state.currentLevel,
                        maxUnlockedLevel = state.maxUnlockedLevel,
                        highScore = state.highScore,
                        bestTimeRemaining = state.bestTimeRemaining,
                        isCloudConnected = state.isCloudConnected,
                        cloudUserEmail = state.cloudUserEmail,
                        hasActiveSession = state.hasActiveSession,
                        onStartClick = { viewModel.startGame() },
                        onUpdatePlayerName = { viewModel.updatePlayerName(it) },
                        onSelectLevel = { viewModel.selectLevel(it) },
                        onNextLevel = { viewModel.nextLevelSelector() },
                        onPrevLevel = { viewModel.prevLevelSelector() },
                        onSignInClick = { activity?.let { viewModel.signInWithGoogle(it) } },
                        onSignOutClick = { viewModel.signOutCloud() }
                    )
                }

                GamePhase.PLAYING -> {
                    OceanGameCanvas(
                        state = state,
                        onJoystickChange = { dx, dy -> viewModel.setJoystickInput(dx, dy) },
                        onBoostToggle = { viewModel.setBoosting(it) },
                        onFireHarpoon = { viewModel.fireHarpoon() },
                        onMoveLeft = { viewModel.setMoveLeft(it) },
                        onMoveRight = { viewModel.setMoveRight(it) },
                        onMoveUp = { viewModel.setMoveUp(it) },
                        onMoveDown = { viewModel.setMoveDown(it) }
                    )
                    GameHudOverlay(
                        state = state,
                        onPauseClick = { viewModel.pauseGame() },
                        onDashboardClick = { viewModel.returnToDashboard() }
                    )
                }

                GamePhase.PAUSED -> {
                    OceanGameCanvas(
                        state = state,
                        onJoystickChange = { _, _ -> },
                        onBoostToggle = { },
                        onFireHarpoon = { }
                    )
                    PauseMenuOverlay(
                        currentLevel = state.currentLevel,
                        onResumeClick = { viewModel.resumeGame() },
                        onRestartClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.restartCurrentLevel()
                                }
                            } else {
                                viewModel.restartCurrentLevel()
                            }
                        },
                        onExitClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.returnToMenu()
                                }
                            } else {
                                viewModel.returnToMenu()
                            }
                        },
                        onNextLevelClick = { viewModel.advanceToNextLevel() },
                        onPrevLevelClick = { viewModel.advanceToPrevLevel() }
                    )
                }

                GamePhase.GAME_OVER -> {
                    OceanGameCanvas(
                        state = state,
                        onJoystickChange = { _, _ -> },
                        onBoostToggle = { },
                        onFireHarpoon = { }
                    )
                    GameOverDialog(
                        player = state.player,
                        currentLevel = state.currentLevel,
                        timeRemainingSeconds = state.timeRemainingSeconds,
                        deathLocationX = state.deathLocationX,
                        deathLocationY = state.deathLocationY,
                        deathDepthMeters = state.deathDepthMeters,
                        deathKillerName = state.deathKillerName,
                        deathInspectionStep = state.deathInspectionStep,
                        livesRemaining = state.livesRemaining,
                        isOutOfLives = state.isOutOfLives,
                        dailyAdsWatchedToday = state.dailyAdsWatchedToday,
                        onStepChange = { viewModel.setDeathInspectionStep(it) },
                        onContinueClick = { viewModel.continueToGame() },
                        onRestartClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.restartCurrentLevel()
                                }
                            } else {
                                viewModel.restartCurrentLevel()
                            }
                        },
                        onExitMenuClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.returnToMenu()
                                }
                            } else {
                                viewModel.returnToMenu()
                            }
                        },
                        onWatchAdForLivesClick = {
                            if (activity != null) {
                                viewModel.watchRewardedAdForLives(activity)
                            }
                        }
                    )
                }

                GamePhase.VICTORY -> {
                    OceanGameCanvas(
                        state = state,
                        onJoystickChange = { _, _ -> },
                        onBoostToggle = { },
                        onFireHarpoon = { }
                    )
                    VictoryDialog(
                        currentLevel = state.currentLevel,
                        score = state.player.score,
                        pearls = state.player.pearls,
                        autoAdvanceCountdown = state.autoAdvanceCountdown,
                        onNextLevelClick = {
                            viewModel.cancelAutoAdvance()
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.startGame()
                                }
                            } else {
                                viewModel.startGame()
                            }
                        },
                        onMenuClick = {
                            viewModel.cancelAutoAdvance()
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    viewModel.returnToMenu()
                                }
                            } else {
                                viewModel.returnToMenu()
                            }
                        }
                    )
                }

                else -> {}
            }
        }

        // BOTTOM SECTION: External HUD & Banner Ad Bar (Positioned strictly BELOW game canvas, Height 64dp)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            color = Color(0xFF030E1C),
            border = BorderStroke(1.dp, Color(0xFF003854))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEFT: Standard 320x50 Ad container + Compact HP/Hearts + Lives indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .height(50.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        AdMobBannerAd(
                            bannerId = AdMobManager.LIVE_BANNER_AD_UNIT_ID,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 10 Lives / Hearts indicator (Counts down 10, 9, 8, 7...)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (i in 0 until 10) {
                            val active = state.livesRemaining > i
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFF0055) else Color(0x33FF0055),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${state.livesRemaining}/10",
                            color = Color(0xFFFF7597),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // 10 LIVES BADGE
                    Surface(
                        color = if (state.livesRemaining <= 3) Color(0xFF4A0010) else Color(0xFF2A0010),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (state.livesRemaining <= 3) Color(0xFFFF2255) else Color(0xFFFF0055))
                    ) {
                        Text(
                            text = if (state.livesRemaining <= 0) "☠️ OUT OF LIVES" else "❤️ ${state.livesRemaining} LIVES",
                            color = if (state.livesRemaining <= 3) Color(0xFFFF5252) else Color(0xFFFF7597),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // RIGHT: Telemetry Stats (Zone, Depth, Score)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val depth = (state.player.y * 0.85f).toInt()
                    Text(
                        text = "ZONE ${state.currentLevel}/500",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${depth}m",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SCORE: ${state.player.score}",
                        color = Color(0xFF64DFDF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
