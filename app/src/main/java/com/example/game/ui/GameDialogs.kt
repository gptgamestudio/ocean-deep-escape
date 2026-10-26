package com.example.game.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.PlayerState

/**
 * Fixed Top-Center Banner Ad: Always docked cleanly at top-center, non-obstructive
 */
@Composable
fun FixedTopCenterBannerAd(
    bannerId: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF000182E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF005580)),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = modifier.testTag("banner_ad_container")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFFFFD166),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "AD",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Text(
                text = "DEEP TRENCH EXPEDITION GEAR: SPONSORED SPONSOR",
                color = Color(0xFFE0FAFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Container para sa Username at Start Button (HTML player-login-box port)
 *
 * <div id="player-login-box" style="margin-top: 20px; text-align: center;">
 *     <input type="text" id="usernameInput" placeholder="Enter your username..." 
 *            style="padding: 12px; font-size: 16px; border-radius: 8px; border: 1px solid #00d2ff; background: #081b29; color: #fff; outline: none; width: 80%; max-width: 300px;">
 *     <br><br>
 *     <button id="startGameBtn" onclick="enterGame()" 
 *             style="padding: 12px 30px; font-size: 18px; font-weight: bold; background: #00d2ff; color: #000; border: none; border-radius: 8px; cursor: pointer;">
 *         START GAME
 *     </button>
 * </div>
 */
@Composable
fun PlayerLoginBox(
    username: String,
    onUsernameChange: (String) -> Unit,
    onStartGame: (String) -> Unit,
    hasActiveSession: Boolean = false,
    currentLevel: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .testTag("player-login-box")
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PILOT CALLSIGN / USERNAME",
            color = Color(0xFF00F0FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // usernameInput: High contrast, clear borders, readable font size ('benjie1977')
        BasicTextField(
            value = username,
            onValueChange = {
                onUsernameChange(it)
                if (errorMessage != null) {
                    errorMessage = null
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(Color(0xFF00F0FF)),
            modifier = Modifier
                .testTag("usernameInput")
                .fillMaxWidth(0.85f)
                .widthIn(max = 280.dp)
                .background(Color(0xFF04111D), shape = RoundedCornerShape(8.dp))
                .border(
                    width = 2.dp,
                    color = if (errorMessage != null) Color(0xFFFF5252) else Color(0xFF00F0FF),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (username.isEmpty()) {
                        Text(
                            text = "benjie1977",
                            color = Color(0xFF5582A6),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Alert validation notice if empty ("Paki-type ang iyong username bago pumasok!")
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = Color(0x33FF5252),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
            ) {
                Text(
                    text = "⚠️ $errorMessage",
                    color = Color(0xFFFF8888),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // startGameBtn (<button id="startGameBtn" onclick="enterGame()">START GAME</button>)
        Button(
            onClick = {
                val trimmed = username.trim().ifEmpty { "benjie1977" }
                if (trimmed.isEmpty()) {
                    val alertMsg = "Paki-type ang iyong username bago pumasok!"
                    errorMessage = alertMsg
                    Toast.makeText(context, alertMsg, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                errorMessage = null

                // I-save ang username sa device memory (localStorage equivalent)
                val prefs = context.getSharedPreferences("ocean_game_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("player_username", trimmed)
                    .putString("player_name", trimmed)
                    .apply()

                // Tawagin ang function na nagpapasimula ng iyong laro (startGameEngine)
                onStartGame(trimmed)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasActiveSession) Color(0xFF00FF88) else Color(0xFF00F0FF),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            modifier = Modifier.testTag("startGameBtn")
        ) {
            Text(
                text = if (hasActiveSession) "▶ RETURN TO GAME (LEVEL $currentLevel)" else "ENTER THE GAME / DASHBOARD",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * 1. Welcome Landing Screen: First dashboard with player-login-box directly below subtitle
 */
@Composable
fun WelcomeLandingScreen(
    currentName: String = "",
    hasActiveSession: Boolean = false,
    currentLevel: Int = 1,
    onStartGame: (String) -> Unit,
    onEnterDashboard: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Kapag nag-load ang game, i-check kung may nakatagong username na (window.onload) - default to 'benjie1977'
    var username by remember(currentName) {
        val prefs = context.getSharedPreferences("ocean_game_prefs", android.content.Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("player_username", null) 
            ?: prefs.getString("player_name", null)
            ?: currentName.takeIf { it != "Commander Diver" && it.isNotBlank() }
            ?: "benjie1977"
        mutableStateOf(savedUsername)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000B18),
                        Color(0xFF001F3F),
                        Color(0xFF000814)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "OCEAN DEEP ESCAPE",
                color = Color(0xFF00F0FF),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "GODOT 3D ABYSSAL SUBMARINE SURVIVAL",
                color = Color(0xFF64DFDF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )

            // DIRECTLY UNDERNEATH GODOT 3D ABYSSAL: PILOT CALLSIGN / USERNAME & START BUTTON
            PlayerLoginBox(
                username = username,
                onUsernameChange = { username = it },
                hasActiveSession = hasActiveSession,
                currentLevel = currentLevel,
                onStartGame = { validName ->
                    onStartGame(validName)
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Briefing badge beneath the login box
            Surface(
                color = Color(0x99001428),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF005580)),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "BRIEFING: 500 Abyssal Zones • Defeat Apex Boss • Unlimited Harpoons",
                    color = Color(0xFFCCD7E4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = {
                    val finalName = username.trim().ifEmpty { "Commander Diver" }
                    onEnterDashboard(finalName)
                }
            ) {
                Text(
                    text = "⚙️ VIEW EXPEDITION DASHBOARD & CLOUD SYNC",
                    color = Color(0xFF64DFDF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 2. Start Menu Dashboard: Player name input, Level records, Level selector (1 to 500)
 */
@Composable
fun StartMenuOverlay(
    playerName: String,
    currentLevel: Int,
    maxUnlockedLevel: Int,
    highScore: Int,
    bestTimeRemaining: Float,
    isCloudConnected: Boolean = false,
    cloudUserEmail: String = "",
    onStartClick: () -> Unit,
    onUpdatePlayerName: (String) -> Unit,
    onSelectLevel: (Int) -> Unit,
    onNextLevel: () -> Unit,
    onPrevLevel: () -> Unit,
    onStoreClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    hasActiveSession: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(playerName) { mutableStateOf(playerName) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6000B18)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF001428)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF005580)),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(top = 70.dp, bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Text(
                    text = "COMMAND EXPEDITION DASHBOARD",
                    color = Color(0xFF00F0FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // FIREBASE FIRESTORE CLOUD SYNC CARD
                Surface(
                    color = if (isCloudConnected) Color(0xFF00281C) else Color(0xFF001F38),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isCloudConnected) Color(0xFF00FF88) else Color(0xFF0077B6)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("firebase_cloud_status_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isCloudConnected) Color(0xFF00FF88) else Color(0xFFFFD166))
                                )
                                Text(
                                    text = if (isCloudConnected) "☁️ FIRESTORE CONNECTED" else "☁️ FIREBASE CLOUD SAVE",
                                    color = if (isCloudConnected) Color(0xFF00FF88) else Color(0xFFFFD166),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCloudConnected) {
                                    "Naka-sync ang iyong High Score at Level progression sa Google Cloud ($cloudUserEmail)"
                                } else {
                                    "I-konek ang iyong Google account para ma-save ang High Score at Unlocked Levels sa Firestore!"
                                },
                                color = Color(0xFFCCD7E4),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        if (isCloudConnected) {
                            OutlinedButton(
                                onClick = onSignOutClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8888)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "SIGN OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = onSignInClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00F0FF),
                                    contentColor = Color(0xFF001428)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("google_signin_button")
                            ) {
                                Text(text = "SIGN IN", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Pilot Callsign Card
                Surface(
                    color = Color(0xFF00223A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006699)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PILOT CALLSIGN",
                                color = Color(0xFF64DFDF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00F0FF),
                                        unfocusedBorderColor = Color(0xFF005580)
                                    ),
                                    modifier = Modifier.width(220.dp).height(50.dp).testTag("pilot_name_input")
                                )
                            } else {
                                Text(
                                    text = playerName,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isEditingName) {
                                    onUpdatePlayerName(tempName)
                                }
                                isEditingName = !isEditingName
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditingName) Color(0xFF00FF88) else Color(0xFF005580),
                                contentColor = if (isEditingName) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("edit_name_button")
                        ) {
                            Text(
                                text = if (isEditingName) "SAVE" else "EDIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Expedition Level Selector (Levels 1 to 500)
                Surface(
                    color = Color(0xFF00223A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF006699)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXPEDITION LEVEL (1 TO 500)",
                            color = Color(0xFFFFD166),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = onPrevLevel,
                                modifier = Modifier.size(36.dp).background(Color(0xFF003355), CircleShape).testTag("prev_level_button")
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Level", tint = Color.White)
                            }

                            Text(
                                text = "LEVEL $currentLevel / 500",
                                color = Color(0xFF00F0FF),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )

                            IconButton(
                                onClick = onNextLevel,
                                modifier = Modifier.size(36.dp).background(Color(0xFF003355), CircleShape).testTag("next_level_button")
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Level", tint = Color.White)
                            }
                        }
                        Text(
                            text = "Max Unlocked Depth: Level $maxUnlockedLevel",
                            color = Color(0xFFCCD7E4),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Records Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // High Score Card
                    Surface(
                        color = Color(0xFF001A33),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "HIGH SCORE", color = Color(0xFF64DFDF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$highScore", color = Color(0xFFFFD166), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Expedition Status Card (Unlimited Dives, No Lives Limit)
                    Surface(
                        color = Color(0xFF001A33),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "EXPEDITION STATUS", color = Color(0xFF64DFDF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "UNLIMITED DIVES", color = Color(0xFF00FF88), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // PRIMARY "DIVE NOW / START EXPEDITION" BUTTON
                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color(0xFF001428)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                        .testTag("start_expedition_btn")
                ) {
                    Text(
                        text = if (hasActiveSession) "▶ RETURN TO GAME (LEVEL $currentLevel)" else "DIVE NOW (LEVEL $currentLevel)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // SETTINGS & APK DOWNLOAD DRAWER (Nasa pinakababa para masubukan ang APK)
                val context = androidx.compose.ui.platform.LocalContext.current
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                val apkLiveUrl = "https://ais-pre-d3ck4buolsylmdpoqeyas7-593782651097.asia-east1.run.app/OceanDeepEscape.apk"
                val apkFileName = "OceanDeepEscape.apk"

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xF0001B33)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00FF88)),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("apk_download_drawer_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Download APK",
                                tint = Color(0xFF00FF88),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "📱 AUTO-UPDATED APK DOWNLOAD DRAWER",
                                color = Color(0xFF00FF88),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Status pill
                        Surface(
                            color = Color(0xFF00381B),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF88))
                        ) {
                            Text(
                                text = "● LATEST BUILD AUTO-SYNCED & READY (21 MB)",
                                color = Color(0xFF00FF88),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Live URL Box
                        Surface(
                            color = Color(0xFF000F1F),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF005580)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "DIRECT APK / APP PREVIEW URL:",
                                    color = Color(0xFF64DFDF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = apkLiveUrl,
                                    color = Color(0xFF00F0FF),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Workspace File: $apkFileName (Root folder)",
                                    color = Color(0xFFCCD7E4),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Direct Open/Download in browser
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkLiveUrl)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(apkLiveUrl))
                                        android.widget.Toast.makeText(
                                            context,
                                            "Na-copy ang direct APK download URL sa clipboard!",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FF88),
                                    contentColor = Color(0xFF001B33)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "OPEN / DOWNLOAD", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }

                            // Copy Direct URL
                            Button(
                                onClick = {
                                    clipboardManager.setText(
                                        androidx.compose.ui.text.AnnotatedString(apkLiveUrl)
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        "Na-copy ang direct APK Link: $apkLiveUrl",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF005580),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "COPY LINK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // File Explorer Guide
                            Button(
                                onClick = {
                                    clipboardManager.setText(
                                        androidx.compose.ui.text.AnnotatedString(apkFileName)
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        "Sa AI Studio File Explorer sa kaliwa: I-right click ang $apkFileName at piliin ang Download!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF003355),
                                    contentColor = Color(0xFF64DFDF)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "FILE EXPLORER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ADMOB MONETIZATION & APP-ADS.TXT VERIFICATION CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xF000182E)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD166)),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("app_ads_txt_monetization_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "AdMob Monetization",
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "💰 ADMOB MONETIZATION (app-ads.txt)",
                                color = Color(0xFFFFD166),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "Opisyal na naka-save sa root `/app-ads.txt` at `assets/app-ads.txt` para sa proteksyon at verification ng revenue:",
                            color = Color(0xFFCCD7E4),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                        // Code snippet box
                        Surface(
                            color = Color(0xFF000F1F),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF004466)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "google.com, pub-9786472984542330, DIRECT, f08c47fec0942fa0",
                                    color = Color(0xFF00FF88),
                                    fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Banner: ca-app-pub-9786472984542330/7618248449\n• Interstitial: ca-app-pub-9786472984542330/5354302327",
                                    color = Color(0xFF64DFDF),
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(
                                        androidx.compose.ui.text.AnnotatedString(
                                            "google.com, pub-9786472984542330, DIRECT, f08c47fec0942fa0"
                                        )
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        "Na-copy ang app-ads.txt entry!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB703),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "COPY APP-ADS.TXT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(
                                        context,
                                        "I-upload ang app-ads.txt sa root ng developer website mo (e.g. https://domain.com/app-ads.txt) at i-link ito sa Google Play Store listing!",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF005580),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "SETUP INSTRUCTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. GameOverDialog with 4-Step Death Scene Review Carousel & Continue to Game
 */
@Composable
fun GameOverDialog(
    player: PlayerState,
    currentLevel: Int,
    timeRemainingSeconds: Float,
    deathLocationX: Float = 0f,
    deathLocationY: Float = 0f,
    deathDepthMeters: Int = 0,
    deathKillerName: String = "Apex Deep Predator",
    deathInspectionStep: Int = 0,
    livesRemaining: Int = 10,
    isOutOfLives: Boolean = false,
    dailyAdsWatchedToday: Int = 0,
    onStepChange: (Int) -> Unit = {},
    onContinueClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitMenuClick: () -> Unit,
    onWatchAdForLivesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF0000814)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF001428)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF0055)),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Text(
                    text = if (isOutOfLives) "☠️ OUT OF LIVES!" else "HULL BREACH: -1 LIFE LOST",
                    color = Color(0xFFFF0055),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )

                // Lives Indicator Badge / Watch Ads to Continue notice
                Surface(
                    color = if (isOutOfLives) Color(0xFF330010) else Color(0xFF00223A),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isOutOfLives) Color(0xFFFFB703) else Color(0xFF00FF88))
                ) {
                    Text(
                        text = if (isOutOfLives) {
                            "WATCH ADS TO CONTINUE (10 LIVES CREDIT) • PROGRESS SAVED AT LEVEL $currentLevel"
                        } else {
                            "❤️ $livesRemaining/10 LIVES REMAINING • SAVED AT LEVEL $currentLevel"
                        },
                        color = if (isOutOfLives) Color(0xFFFFD166) else Color(0xFF00FF88),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // 4-STEP DEATH SCENE REVIEW CAROUSEL
                Surface(
                    color = Color(0xFF001E36),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF005580)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onStepChange((deathInspectionStep - 1 + 4) % 4) },
                                modifier = Modifier.size(32.dp).testTag("death_review_prev")
                            ) {
                                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Review Step", tint = Color(0xFF00F0FF))
                            }

                            Text(
                                text = "DEATH SCENE REVIEW (${deathInspectionStep + 1}/4)",
                                color = Color(0xFFFFD166),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            IconButton(
                                onClick = { onStepChange((deathInspectionStep + 1) % 4) },
                                modifier = Modifier.size(32.dp).testTag("death_review_next")
                            ) {
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Review Step", tint = Color(0xFF00F0FF))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (deathInspectionStep) {
                            0 -> {
                                Text(
                                    text = "KILLER PREDATOR: $deathKillerName",
                                    color = Color(0xFFFF0055),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lethal impact penetrated the kinetic shield and breached the titanium conning tower.",
                                    color = Color(0xFFCCD7E4),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            1 -> {
                                Text(
                                    text = "LOCATION & DEPTH TELEMETRY",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Depth: ${deathDepthMeters}m | World Coordinates: [X: ${deathLocationX.toInt()}, Y: ${deathLocationY.toInt()}]",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            2 -> {
                                Text(
                                    text = "TACTICAL ESCAPE ADVICE",
                                    color = Color(0xFF64DFDF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Watch for the red attack vector before enemies dash. Boost thrusters laterally to dodge burst strikes.",
                                    color = Color(0xFFCCD7E4),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            else -> {
                                Text(
                                    text = "EXPEDITION CHECKPOINT SECURED",
                                    color = Color(0xFF00FF88),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Level $currentLevel progress is locked in. You will NEVER be reset back to Level 1.",
                                    color = Color(0xFFCCD7E4),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // REWARDED AD BUTTON: Watch Ad (+10 Lives, resets every 24h)
                Button(
                    onClick = onWatchAdForLivesClick,
                    enabled = dailyAdsWatchedToday < 10,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOutOfLives) Color(0xFFFFB703) else Color(0xFF003854),
                        contentColor = if (isOutOfLives) Color.Black else Color(0xFFFFD166)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(50.dp)
                        .testTag("watch_ad_lives_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.Black,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "AD",
                                color = Color(0xFFFFB703),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (dailyAdsWatchedToday < 10) {
                                if (isOutOfLives) "WATCH ADS TO CONTINUE (10 LIVES CREDIT)" else "WATCH AD (+10 LIVES) • ${10 - dailyAdsWatchedToday}/10 TODAY"
                            } else {
                                "DAILY ADS COMPLETED (10/10) • RESETS 24H"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // PRIMARY ACTION BUTTON (CONTINUE / RESTART LEVEL)
                Button(
                    onClick = if (isOutOfLives) onRestartClick else onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOutOfLives) Color(0xFF00FF88) else Color(0xFF00F0FF),
                        contentColor = Color(0xFF001428)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(48.dp)
                        .testTag("continue_to_game_btn")
                ) {
                    Text(
                        text = if (isOutOfLives) {
                            "RESTART LEVEL $currentLevel (10 LIVES REFILL)"
                        } else {
                            "CONTINUE LEVEL $currentLevel ($livesRemaining LIVES LEFT)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Secondary Navigation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRestartClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCCD7E4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("restart_level_button")
                    ) {
                        Text(text = "RESTART LEVEL $currentLevel", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onExitMenuClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCCD7E4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("exit_to_menu_button")
                    ) {
                        Text(text = "DASHBOARD", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * 4. Victory Dialog: Surviving / Escaping the zone
 * Automatically advances to next level (countdown timer) or allows immediate tap!
 */
@Composable
fun VictoryDialog(
    currentLevel: Int,
    score: Int,
    pearls: Int,
    autoAdvanceCountdown: Int = 3,
    onNextLevelClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6000B18)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF001428)),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF88)),
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "CONGRATS! NEXT LEVEL UNLOCKED!",
                        color = Color(0xFF00FF88),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "Apex Boss defeated! Submarine extracting to deeper waters.",
                    color = Color(0xFFCCD7E4),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                // LEVEL CLEARED BANNER (Manual Advance - No auto-advancing countdown)
                Surface(
                    color = Color(0xFF003820),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00FF88))
                ) {
                    Text(
                        text = "⚡ ZONE CONQUERED! READY FOR NEXT CHALLENGE",
                        color = Color(0xFF00FF88),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(text = "NEXT: ZONE $currentLevel/500", color = Color(0xFFFFD166), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "SCORE: $score", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "PEARLS: $pearls", color = Color(0xFF64DFDF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onMenuClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00F0FF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00A8CC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("victory_menu_button")
                    ) {
                        Text(text = "🏠 DASHBOARD (REST)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNextLevelClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FF88),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("next_level_button")
                    ) {
                        Text(
                            text = "▶ DIVE TO ZONE $currentLevel NOW >>",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5. Pause Menu Overlay
 */
@Composable
fun PauseMenuOverlay(
    currentLevel: Int = 1,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    onNextLevelClick: () -> Unit = {},
    onPrevLevelClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000814)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF001428)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF005580)),
            modifier = Modifier.fillMaxWidth(0.65f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "EXPEDITION PAUSED • ZONE $currentLevel/500",
                    color = Color(0xFF00F0FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                // Level Switcher row in pause menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onPrevLevelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64DFDF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "◀ PREV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "LVL $currentLevel",
                        color = Color(0xFFFFD166),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )

                    OutlinedButton(
                        onClick = onNextLevelClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64DFDF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "NEXT ▶", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onResumeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF), contentColor = Color(0xFF001428)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("resume_button")
                ) {
                    Text(text = "RESUME EXPEDITION", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestartClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pause_restart_button")
                ) {
                    Text(text = "RESTART LEVEL $currentLevel")
                }

                OutlinedButton(
                    onClick = onExitClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCCD7E4)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pause_exit_button")
                ) {
                    Text(text = "EXIT TO DASHBOARD")
                }
            }
        }
    }
}
