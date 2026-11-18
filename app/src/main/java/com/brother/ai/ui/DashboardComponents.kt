package com.brother.ai.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brother.ai.ui.theme.DeepPanel
import com.brother.ai.ui.theme.NeonBlue
import com.brother.ai.ui.theme.NeonCyan
import com.brother.ai.ui.theme.NeonGreen
import com.brother.ai.ui.theme.NeonPink
import com.brother.ai.ui.theme.NeonPurple
import com.brother.ai.ui.theme.PanelEdge
import com.brother.ai.ui.theme.TextDim
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class CoreState { IDLE, LISTENING, THINKING, SPEAKING }

/** Bordered neon panel with a small header label. */
@Composable
fun NeonPanel(
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = NeonCyan,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(DeepPanel.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Box(Modifier.padding(top = 8.dp)) { content() }
    }
}

/** Small telemetry chip: label on top, value below, glowing dot. */
@Composable
fun StatusChip(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier
                .size(7.dp)
                .background(color, RoundedCornerShape(50))
        )
        Column {
            Text(label, color = TextDim, fontSize = 9.sp, letterSpacing = 1.sp)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Vertical animated bar strip like an audio waveform. */
@Composable
fun Waveform(
    modifier: Modifier = Modifier,
    active: Boolean,
    barCount: Int = 36,
    color: Color = NeonCyan
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "phase"
    )
    val heights = remember(barCount) { List(barCount) { Random.nextFloat() } }

    Canvas(modifier.height(64.dp).fillMaxWidth()) {
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (barCount - 1)) / barCount
        for (i in 0 until barCount) {
            val base = heights[i]
            val wave = if (active) {
                (sin(phase * 2f + i * 0.55f).absoluteValue * 0.65f + 0.35f) *
                        (cos(phase + i * 0.2f).absoluteValue * 0.5f + 0.5f)
            } else {
                0.12f + 0.08f * sin(phase + i * 0.4f).absoluteValue
            }
            val h = size.height * (base.coerceIn(0.25f, 1f) * wave).coerceIn(0.05f, 1f)
            val x = i * (barWidth + gap)
            val cy = size.height / 2f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.15f), color, color.copy(alpha = 0.15f)),
                    startY = cy - h / 2,
                    endY = cy + h / 2
                ),
                topLeft = Offset(x, cy - h / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
            )
        }
    }
}

/** The central animated AI core: rotating neon arcs + pulsing center. */
@Composable
fun AiCore(size: Dp = 230.dp, state: CoreState, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "core")
    val spin1 by t.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "s1")
    val spin2 by t.animateFloat(360f, 0f, infiniteRepeatable(tween(6500, easing = LinearEasing)), label = "s2")
    val spin3 by t.animateFloat(0f, 360f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "s3")
    val pulse by t.animateFloat(
        0.86f, 1.1f,
        infiniteRepeatable(tween(if (state == CoreState.IDLE) 2400 else 750), RepeatMode.Reverse),
        label = "pulse"
    )

    val ringColor = when (state) {
        CoreState.LISTENING -> NeonCyan
        CoreState.THINKING -> NeonPurple
        CoreState.SPEAKING -> NeonGreen
        CoreState.IDLE -> NeonBlue
    }

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val R = this.size.width / 2f

            // ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ringColor.copy(alpha = 0.22f), Color.Transparent),
                    center = c, radius = R
                ),
                radius = R
            )
            // outer dashed ring
            drawCircle(
                color = PanelEdge, radius = R * 0.98f, center = c,
                style = Stroke(width = 2.dp.toPx())
            )
            // rotating arcs
            fun arc(radius: Float, sweep: Float, start: Float, width: Float, color: Color) {
                drawArc(
                    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
                    topLeft = Offset(c.x - radius, c.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = width, cap = StrokeCap.Round)
                )
            }
            arc(R * 0.92f, 130f, spin1, 3.5.dp.toPx(), ringColor.copy(alpha = 0.9f))
            arc(R * 0.92f, 40f, spin1 + 200f, 3.5.dp.toPx(), ringColor.copy(alpha = 0.45f))
            arc(R * 0.80f, 90f, spin2, 2.5.dp.toPx(), NeonPurple.copy(alpha = 0.85f))
            arc(R * 0.80f, 30f, spin2 + 150f, 2.5.dp.toPx(), NeonPurple.copy(alpha = 0.4f))
            arc(R * 0.68f, 200f, spin3, 1.8.dp.toPx(), NeonBlue.copy(alpha = 0.7f))
            // tick marks
            for (i in 0 until 36) {
                val a = Math.toRadians((i * 10).toDouble())
                val r1 = R * 0.985f
                val r2 = R * (if (i % 3 == 0) 0.94f else 0.965f)
                drawLine(
                    color = if (i % 3 == 0) ringColor.copy(alpha = 0.55f) else PanelEdge,
                    start = Offset(c.x + r1 * cos(a).toFloat(), c.y + r1 * sin(a).toFloat()),
                    end = Offset(c.x + r2 * cos(a).toFloat(), c.y + r2 * sin(a).toFloat()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            // pulsing core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, ringColor, ringColor.copy(alpha = 0.25f), Color.Transparent),
                    center = c, radius = R * 0.42f * pulse
                ),
                radius = R * 0.42f * pulse,
                center = c
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = R * 0.10f * pulse,
                center = c
            )
        }
    }
}
/** Big glowing mic button. */
@Composable
fun MicButton(listening: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "mic")
    val glow by t.animateFloat(
        0.55f, 1f,
        infiniteRepeatable(tween(if (listening) 600 else 1800), RepeatMode.Reverse),
        label = "glow"
    )
    Box(
        modifier
            .size(68.dp)
            .background(
                Brush.radialGradient(
                    listOf(NeonCyan.copy(alpha = 0.9f * glow), NeonBlue.copy(alpha = 0.9f))
                ),
                RoundedCornerShape(50)
            )
            .border(2.dp, Color.White.copy(alpha = 0.55f * glow), RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("🎤", fontSize = 26.sp)
    }
}

/** Small mute/unmute toggle beside the mic. Muted = mic input OFF, TTS unaffected. */
@Composable
fun MuteButton(muted: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(start = 14.dp)
            .size(48.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        (if (muted) NeonPink else NeonGreen).copy(alpha = 0.85f),
                        (if (muted) NeonPurple else NeonCyan).copy(alpha = 0.75f)
                    )
                ),
                RoundedCornerShape(50)
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(if (muted) "🔇" else "🔊", fontSize = 20.sp)
    }
}