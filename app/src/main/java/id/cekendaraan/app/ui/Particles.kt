package id.cekendaraan.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import id.cekendaraan.app.ui.theme.C
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private class Dot(
    val angle: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float,
    val size: Float,
    val wobble: Float,
)

/**
 * Drifting "neural" particle cloud: dots orbit slowly, breathe and twinkle, and nearby dots
 * are joined by faint links. As [progress] (0..1) rises the cloud tightens and lights up blue.
 * Only the draw phase reads the clock, so it never recomposes the screen.
 */
@Composable
fun ParticleRing(modifier: Modifier = Modifier, progress: Float = 0f) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "particles")
    val clock = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { clock.floatValue = (it - start) / 1_000_000_000f }
    }
    val dots = remember {
        val rnd = java.util.Random(11)
        List(64) {
            Dot(
                angle = (rnd.nextFloat() * Math.PI * 2).toFloat(),
                radius = 0.35f + rnd.nextFloat() * 0.65f,
                speed = (rnd.nextFloat() - 0.5f) * 0.36f,
                phase = rnd.nextFloat() * 6.28f,
                size = 1.3f + rnd.nextFloat() * 1.9f,
                wobble = 0.6f + rnd.nextFloat() * 1.2f,
            )
        }
    }
    val idle = Color(0xFF8E8E93)
    val lit = Color(0xFF6FB6FF)

    Canvas(modifier) {
        val t = clock.floatValue
        val cx = size.width / 2
        val cy = size.height / 2
        val rad = minOf(size.width, size.height) / 2

        drawCircle(
            brush = Brush.radialGradient(
                listOf(C.BlueGlow.copy(alpha = 0.35f + 0.35f * p), Color.Transparent),
                center = Offset(cx, cy),
                radius = rad * 1.3f,
            ),
            radius = rad * 1.3f,
            center = Offset(cx, cy),
        )

        val shrink = 1f - 0.38f * p
        val xs = FloatArray(dots.size)
        val ys = FloatArray(dots.size)
        dots.forEachIndexed { i, d ->
            val a = d.angle + d.speed * t
            val r = (d.radius * shrink + 0.045f * sin(t * d.wobble + d.phase)) * rad
            xs[i] = cx + cos(a) * r * 1.15f
            ys[i] = cy + sin(a) * r * 0.78f
        }

        val linkDist = rad * (0.30f + 0.08f * p)
        val linkColor = lerp(idle, lit, p)
        for (i in dots.indices) {
            for (j in i + 1 until dots.size) {
                val dist = hypot(xs[i] - xs[j], ys[i] - ys[j])
                if (dist < linkDist) {
                    val alpha = (1f - dist / linkDist) * (0.18f + 0.30f * p)
                    drawLine(linkColor.copy(alpha = alpha), Offset(xs[i], ys[i]), Offset(xs[j], ys[j]), strokeWidth = 1.2f)
                }
            }
        }

        dots.forEachIndexed { i, d ->
            val twinkle = 0.5f + 0.5f * sin(t * 1.7f + d.phase)
            val color = lerp(idle, lit, p * (0.4f + 0.6f * twinkle))
            drawCircle(
                color.copy(alpha = 0.45f + 0.45f * twinkle),
                radius = d.size * density * (0.85f + 0.3f * twinkle),
                center = Offset(xs[i], ys[i]),
            )
        }
    }
}
