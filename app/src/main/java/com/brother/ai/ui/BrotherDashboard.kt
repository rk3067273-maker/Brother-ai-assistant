package com.brother.ai.ui

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.brother.ai.core.CommandEngine
import com.brother.ai.ui.theme.DeepSpace
import com.brother.ai.ui.theme.NeonCyan
import com.brother.ai.ui.theme.NeonGreen
import com.brother.ai.ui.theme.NeonPink
import com.brother.ai.ui.theme.NeonPurple
import com.brother.ai.ui.theme.TextDim
import com.brother.ai.ui.theme.TextPrimary
import com.brother.ai.voice.SpeechRecognizerManager
import com.brother.ai.voice.VoiceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun BrotherDashboard() {
    val context = LocalContext.current
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) startVoiceLoop(context, scope)
    }

    // ── Real engines (wired!) ──
    val voice = remember { VoiceEngine(context) }
    val recognizer = remember {
        SpeechRecognizerManager(
            context,
            onPartial = { },
            onFinal = { text ->
                coreState = CoreState.THINKING
                statusText = "THINKING…"
                lastResponse = "› $text"
                scope.launch {
                    val result = CommandEngine(context).execute(text)
                    lastResponse = result.spoken
                    coreState = CoreState.SPEAKING
                    statusText = "SPEAKING"
                    voice.speak(result.spoken)
                }
            },
            onError = { msg ->
                if (!muted) {
                    lastResponse = msg
                    coreState = CoreState.LISTENING
                    statusText = "LISTENING…"
                }
            }
        )
    }

    var listening by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var coreState by remember { mutableStateOf(CoreState.IDLE) }
    var statusText by remember { mutableStateOf("SYSTEMS ONLINE") }
    var lastResponse by remember { mutableStateOf("Ready. Tap the mic and speak, Bhai.") }

    // Real telemetry
    var batteryPct by remember { mutableStateOf("--%") }
    var ramUsed by remember { mutableStateOf("--") }
    var cpuLoad by remember { mutableStateOf("--") }
    var netOk by remember { mutableStateOf("--") }

    LaunchedEffect(Unit) {
        while (true) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            if (level >= 0) batteryPct = "$level%"

            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val mi = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(mi)
            if (mi.totalMem > 0) {
                val usedPct = ((mi.totalMem - mi.availMem) * 100 / mi.totalMem).toInt()
                ramUsed = "$usedPct%"
            }

            try {
                val load = java.io.File("/proc/loadavg").readText().split(" ").firstOrNull() ?: "--"
                cpuLoad = load
            } catch (_: Exception) {}

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
            netOk = if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) "OK" else "OFF"

            kotlinx.coroutines.delay(5000)
        }
    }

    fun startListening() {
        if (muted || !hasMicPermission) return
        listening = true
        coreState = CoreState.LISTENING
        statusText = "LISTENING…"
        recognizer.startListening()
    }

    fun stopListening() {
        listening = false
        recognizer.stop()
    }

    // Voice loop wiring
    LaunchedEffect(Unit) {
        voice.setOnDoneListener {
            if (!muted) startListening() else {
                listening = false
                coreState = CoreState.IDLE
                statusText = "MUTED"
            }
        }
        if (hasMicPermission) startVoiceLoop(context, scope)
    }

    fun executeCommand(cmd: String) {
        coreState = CoreState.THINKING
        statusText = "THINKING…"
        lastResponse = "› $cmd"
        scope.launch {
            val result = CommandEngine(context).execute(cmd)
            lastResponse = result.spoken
            coreState = CoreState.SPEAKING
            statusText = "SPEAKING"
            voice.speak(result.spoken)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(DeepSpace, Color(0xFF0D1226), DeepSpace)
                )
            )
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("BROTHER", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text("AI · ALWAYS ON · YOUR COMMAND", color = TextDim, fontSize = 10.sp, letterSpacing = 2.sp)
            }
            StatusChip(
                "STATUS",
                if (muted) "MUTED" else if (listening) "LIVE" else "STANDBY",
                if (muted) NeonPink else if (listening) NeonGreen else NeonCyan
            )
        }

        // ── Core visual ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            AiCore(state = coreState, size = 240.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (muted) "MUTED — MIC OFF" else statusText,
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp
                )
                Text("● ● ●", color = TextDim, fontSize = 9.sp)
            }
        }

        // ── Mic + Mute buttons ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MicButton(listening = listening, onClick = {
                when {
                    !hasMicPermission -> micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    listening -> stopListening()
                    else -> startListening()
                }
            })
            Spacer(Modifier.height(0.dp))
            MuteButton(muted = muted, onClick = {
                muted = !muted
                if (muted) {
                    recognizer.stop()       // only stops LISTENING, never TTS
                    listening = false
                    if (coreState != CoreState.SPEAKING) {
                        coreState = CoreState.IDLE
                        statusText = "MUTED"
                        lastResponse = "Mic muted — Brother শুনবে না, কিন্তু কাজ/কথা চলবে"
                    }
                } else {
                    startListening()
                    lastResponse = "Mic unmuted — auto-listening চালু"
                }
            })
        }

        Spacer(Modifier.height(14.dp))

        // ── Telemetry (real values) ──
        NeonPanel(title = "DEVICE TELEMETRY", accent = NeonCyan) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChip("BATTERY", batteryPct, NeonGreen)
                StatusChip("RAM", ramUsed, NeonCyan)
                StatusChip("CPU", cpuLoad, NeonPurple)
                StatusChip("NET", netOk, NeonGreen)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Waveform ──
        NeonPanel(title = "VOICE SPECTRUM", accent = NeonPurple) {
            Waveform(active = listening && !muted, color = if (listening) NeonCyan else NeonPurple)
        }

        Spacer(Modifier.height(12.dp))

        // ── Last response ──
        NeonPanel(title = "BROTHER SAYS", accent = NeonGreen) {
            Text(lastResponse, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
        }

        Spacer(Modifier.height(12.dp))

        // ── Quick commands (now really execute!) ──
        Text("QUICK COMMANDS", color = TextDim, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Open YouTube", "Battery কত?", "Open Settings", "Open Chrome", "Volume up").forEach { cmd ->
                Box(
                    Modifier
                        .background(NeonCyan.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable { executeCommand(cmd) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(cmd, color = NeonCyan, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Modules ──
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonPanel(title = "AUTOMATION", accent = NeonPurple, modifier = Modifier.weight(1f)) {
                Text("Phone control\nApps · Tap · Type\nGestures · System", color = TextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
            NeonPanel(title = "TERMUX", accent = NeonGreen, modifier = Modifier.weight(1f)) {
                Text("Shell command\nrun · script\nautomation", color = TextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NeonPanel(title = "VOICE LIBRARY", accent = NeonCyan, modifier = Modifier.weight(1f)) {
                Text("Voice: ${voice.category.label}\nCategory: 11 types\nPitch · Speed", color = TextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
            NeonPanel(title = "SETTINGS", accent = NeonPink, modifier = Modifier.weight(1f)) {
                Text("Name · Wake word\nPermissions · Theme\nLanguage: BN/EN", color = TextDim, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun startVoiceLoop(context: Context, scope: CoroutineScope) {
    // Keep the always-on voice service alive
    scope.launch {
        try {
            ContextCompat.startForegroundService(
                context,
                android.content.Intent(context, com.brother.ai.voice.VoiceForegroundService::class.java)
            )
        } catch (_: Exception) {}
    }
}