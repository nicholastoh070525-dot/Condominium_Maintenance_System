package com.example.condominium_maintenance_system.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.Coral
import com.example.condominium_maintenance_system.ui.theme.CoralPale
import com.example.condominium_maintenance_system.ui.theme.Gold
import com.example.condominium_maintenance_system.ui.theme.GoldPale
import com.example.condominium_maintenance_system.ui.theme.Purple
import com.example.condominium_maintenance_system.ui.theme.PurplePale
import com.example.condominium_maintenance_system.ui.theme.Success
import com.example.condominium_maintenance_system.ui.theme.SuccessPale

enum class GlyphType {
    HOME,
    LIST,
    CALENDAR,
    PEOPLE,
    ACCOUNT,
    SHIELD,
    WORK,
    BELL,
    CARD,
    BACK,
    ADD,
    CHECK,
    SEARCH,
    WARNING,
    CLOSE,
    UPLOAD
}

@Composable
fun Glyph(
    type: GlyphType,
    tint: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 1.9.dp
) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(
                color = tint,
                start = Offset(w * x1, h * y1),
                end = Offset(w * x2, h * y2),
                strokeWidth = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )
        }

        fun path(build: Path.() -> Unit) {
            val path = Path().apply(build)
            drawPath(path, tint, style = stroke)
        }

        when (type) {
            GlyphType.HOME -> {
                path {
                    moveTo(w * 0.15f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.85f, h * 0.48f)
                }
                path {
                    moveTo(w * 0.23f, h * 0.43f)
                    lineTo(w * 0.23f, h * 0.82f)
                    lineTo(w * 0.77f, h * 0.82f)
                    lineTo(w * 0.77f, h * 0.43f)
                }
                path {
                    moveTo(w * 0.43f, h * 0.82f)
                    lineTo(w * 0.43f, h * 0.61f)
                    lineTo(w * 0.57f, h * 0.61f)
                    lineTo(w * 0.57f, h * 0.82f)
                }
            }

            GlyphType.LIST -> {
                listOf(0.3f, 0.5f, 0.7f).forEach { y ->
                    drawCircle(tint, radius = strokeWidth.toPx() * 0.55f, center = Offset(w * 0.18f, h * y))
                    line(0.35f, y, 0.84f, y)
                }
            }

            GlyphType.CALENDAR -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f, h * 0.2f),
                    size = Size(w * 0.76f, h * 0.68f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.1f),
                    style = stroke
                )
                line(0.12f, 0.4f, 0.88f, 0.4f)
                line(0.34f, 0.1f, 0.34f, 0.3f)
                line(0.66f, 0.1f, 0.66f, 0.3f)
            }

            GlyphType.PEOPLE -> {
                drawCircle(tint, w * 0.14f, Offset(w * 0.37f, h * 0.36f), style = stroke)
                drawCircle(tint, w * 0.11f, Offset(w * 0.67f, h * 0.42f), style = stroke)
                drawArc(
                    tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.13f, h * 0.52f),
                    size = Size(w * 0.49f, h * 0.34f),
                    style = stroke
                )
                drawArc(
                    tint,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(w * 0.51f, h * 0.58f),
                    size = Size(w * 0.38f, h * 0.28f),
                    style = stroke
                )
            }

            GlyphType.ACCOUNT -> {
                drawCircle(tint, w * 0.15f, Offset(w * 0.5f, h * 0.35f), style = stroke)
                drawArc(
                    tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.2f, h * 0.53f),
                    size = Size(w * 0.6f, h * 0.35f),
                    style = stroke
                )
            }

            GlyphType.SHIELD -> {
                path {
                    moveTo(w * 0.5f, h * 0.1f)
                    lineTo(w * 0.82f, h * 0.25f)
                    lineTo(w * 0.78f, h * 0.56f)
                    cubicTo(w * 0.74f, h * 0.73f, w * 0.62f, h * 0.84f, w * 0.5f, h * 0.9f)
                    cubicTo(w * 0.38f, h * 0.84f, w * 0.26f, h * 0.73f, w * 0.22f, h * 0.56f)
                    lineTo(w * 0.18f, h * 0.25f)
                    close()
                }
            }

            GlyphType.WORK -> {
                drawRoundRect(
                    tint,
                    Offset(w * 0.15f, h * 0.18f),
                    Size(w * 0.7f, h * 0.72f),
                    androidx.compose.ui.geometry.CornerRadius(w * 0.09f),
                    style = stroke
                )
                line(0.38f, 0.18f, 0.38f, 0.1f)
                line(0.62f, 0.18f, 0.62f, 0.1f)
                path {
                    moveTo(w * 0.32f, h * 0.52f)
                    lineTo(w * 0.44f, h * 0.64f)
                    lineTo(w * 0.7f, h * 0.37f)
                }
            }

            GlyphType.BELL -> {
                path {
                    moveTo(w * 0.2f, h * 0.7f)
                    cubicTo(w * 0.3f, h * 0.58f, w * 0.3f, h * 0.2f, w * 0.5f, h * 0.18f)
                    cubicTo(w * 0.7f, h * 0.2f, w * 0.7f, h * 0.58f, w * 0.8f, h * 0.7f)
                    close()
                }
                drawArc(
                    tint,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.38f, h * 0.68f),
                    size = Size(w * 0.24f, h * 0.2f),
                    style = stroke
                )
            }

            GlyphType.CARD -> {
                drawRoundRect(
                    tint,
                    Offset(w * 0.1f, h * 0.22f),
                    Size(w * 0.8f, h * 0.58f),
                    androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
                    style = stroke
                )
                line(0.1f, 0.4f, 0.9f, 0.4f)
                line(0.27f, 0.65f, 0.52f, 0.65f)
            }

            GlyphType.BACK -> {
                line(0.78f, 0.5f, 0.23f, 0.5f)
                line(0.23f, 0.5f, 0.45f, 0.28f)
                line(0.23f, 0.5f, 0.45f, 0.72f)
            }

            GlyphType.ADD -> {
                line(0.5f, 0.2f, 0.5f, 0.8f)
                line(0.2f, 0.5f, 0.8f, 0.5f)
            }

            GlyphType.CHECK -> {
                line(0.18f, 0.52f, 0.4f, 0.73f)
                line(0.4f, 0.73f, 0.84f, 0.26f)
            }

            GlyphType.SEARCH -> {
                drawCircle(tint, w * 0.24f, Offset(w * 0.43f, h * 0.43f), style = stroke)
                line(0.61f, 0.61f, 0.84f, 0.84f)
            }

            GlyphType.WARNING -> {
                path {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.88f, h * 0.82f)
                    lineTo(w * 0.12f, h * 0.82f)
                    close()
                }
                line(0.5f, 0.39f, 0.5f, 0.61f)
                drawCircle(tint, w * 0.035f, Offset(w * 0.5f, h * 0.71f))
            }

            GlyphType.CLOSE -> {
                line(0.22f, 0.22f, 0.78f, 0.78f)
                line(0.78f, 0.22f, 0.22f, 0.78f)
            }

            GlyphType.UPLOAD -> {
                line(0.5f, 0.15f, 0.5f, 0.65f)
                line(0.5f, 0.15f, 0.3f, 0.35f)
                line(0.5f, 0.15f, 0.7f, 0.35f)
                line(0.2f, 0.72f, 0.2f, 0.86f)
                line(0.2f, 0.86f, 0.8f, 0.86f)
                line(0.8f, 0.86f, 0.8f, 0.72f)
            }
        }
    }
}

@Composable
fun CondoCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = color,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class PillTone {
    BLUE,
    GOLD,
    CORAL,
    PURPLE,
    GREEN
}

@Composable
fun StatusPill(text: String, tone: PillTone = PillTone.GOLD) {
    val (foreground, background) = when (tone) {
        PillTone.BLUE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        PillTone.GOLD -> Color(0xFF8B591C) to GoldPale
        PillTone.CORAL -> Color(0xFF985442) to CoralPale
        PillTone.PURPLE -> Color(0xFF635992) to PurplePale
        PillTone.GREEN -> Success to SuccessPale
    }
    Surface(shape = CircleShape, color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            maxLines = 1
        )
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ModuleCard(
    code: String,
    title: String,
    detail: String,
    symbol: String,
    symbolColor: Color,
    symbolBackground: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(symbolBackground, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = symbol, color = symbolColor, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                Text(text = "›", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(text = code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    background: Color = GoldPale,
    foreground: Color = Color(0xFF6A4B39)
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(background.copy(alpha = 0.82f), background)
                ),
                shape = RoundedCornerShape(17.dp)
            )
            .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(17.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = foreground, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(19.dp),
        color = if (highlighted) BlueDeep else MaterialTheme.colorScheme.surface,
        border = if (highlighted) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlighted) Color(0xFFB8C8ED) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = if (highlighted) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InfoBanner(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    glyph: GlyphType = GlyphType.SHIELD,
    badge: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(glyph, MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (badge != null) {
                StatusPill(badge, PillTone.BLUE)
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    CondoCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(5.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
