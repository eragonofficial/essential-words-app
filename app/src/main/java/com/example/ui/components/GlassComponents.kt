package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CosmicDeepNavy
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicTealDepth
import com.example.ui.theme.GlassCyanAccent
import com.example.ui.theme.GlassCyanBorder
import com.example.ui.theme.GlassCyanFill
import com.example.ui.theme.GlassModalTint
import com.example.ui.theme.GlassRoyalBlueBorder
import com.example.ui.theme.GlassRoyalBlueFill
import com.example.ui.theme.GlassTier1Border
import com.example.ui.theme.GlassTier1Fill
import com.example.ui.theme.GlassTier1Highlight
import com.example.ui.theme.GlassTier2Border
import com.example.ui.theme.GlassTier2Fill
import com.example.ui.theme.GlassTier2Highlight
import com.example.ui.theme.GlassTier3Border
import com.example.ui.theme.GlassTier3Fill
import com.example.ui.theme.GlassTier3Highlight
import com.example.ui.theme.GlassVioletAccent
import com.example.ui.theme.GlassVioletBorder
import com.example.ui.theme.GlassVioletFill
import com.example.ui.theme.OrbCyan
import com.example.ui.theme.OrbEmerald
import com.example.ui.theme.OrbRoyalBlue
import com.example.ui.theme.OrbViolet
import com.example.ui.theme.TextBodyTranslucent
import com.example.ui.theme.TextMetaTranslucent
import com.example.ui.theme.TextPrimaryWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class GlassTier {
    TIER_1, // Main Containers, Top Bars, Sidebars, Panels
    TIER_2, // Cards, Modals/Dialogs, Flashcards, Dropdowns
    TIER_3  // Badges, Pills, Tags, Chips, Icon Containers
}

data class GlassTokens(
    val fill: Color,
    val border: Color,
    val topHighlight: Color,
    val shadowElevation: Dp
)

fun glassTokensFor(tier: GlassTier): GlassTokens = when (tier) {
    GlassTier.TIER_1 -> GlassTokens(
        fill = GlassTier1Fill,
        border = GlassTier1Border,
        topHighlight = GlassTier1Highlight,
        shadowElevation = 4.dp
    )
    GlassTier.TIER_2 -> GlassTokens(
        fill = GlassTier2Fill,
        border = GlassTier2Border,
        topHighlight = GlassTier2Highlight,
        shadowElevation = 10.dp
    )
    GlassTier.TIER_3 -> GlassTokens(
        fill = GlassTier3Fill,
        border = GlassTier3Border,
        topHighlight = GlassTier3Highlight,
        shadowElevation = 2.dp
    )
}

/**
 * Fixed deep cosmic gradient canvas (#060918 -> #0D1333 -> #091A2E) with slow-drifting,
 * softly diffused vibrant ambient light orbs behind all screens so frosted glass surfaces
 * reveal true luminous depth on every screen.
 */
@Composable
fun CosmicGlassBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "cosmicOrbsTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2.0).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(CosmicDeepNavy, CosmicMidnight, CosmicTealDepth)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val orb1Center = Offset(
                x = w * (0.18f + 0.08f * cos(phase)),
                y = h * (0.16f + 0.06f * sin(phase))
            )
            val orb1Radius = maxOf(w, h) * 0.44f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrbCyan.copy(alpha = 0.28f),
                        OrbCyan.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = orb1Radius
                ),
                radius = orb1Radius,
                center = orb1Center
            )

            val orb2Center = Offset(
                x = w * (0.84f - 0.09f * sin(phase)),
                y = h * (0.34f + 0.08f * cos(phase))
            )
            val orb2Radius = maxOf(w, h) * 0.48f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrbViolet.copy(alpha = 0.30f),
                        OrbViolet.copy(alpha = 0.11f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = orb2Radius
                ),
                radius = orb2Radius,
                center = orb2Center
            )

            val orb3Center = Offset(
                x = w * (0.38f + 0.10f * sin(phase * 0.8f)),
                y = h * (0.82f - 0.07f * cos(phase * 0.8f))
            )
            val orb3Radius = maxOf(w, h) * 0.50f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        OrbRoyalBlue.copy(alpha = 0.28f),
                        OrbEmerald.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = orb3Center,
                    radius = orb3Radius
                ),
                radius = orb3Radius,
                center = orb3Center
            )
        }

        content()
    }
}

/**
 * Multi-layered Frosted Glass surface modifier with translucent fill, top-left directional sheen,
 * inner top specular highlight hairline, and crisp translucent outer border.
 */
fun Modifier.glassContainer(
    tier: GlassTier = GlassTier.TIER_2,
    shape: Shape = RoundedCornerShape(20.dp),
    tintColor: Color? = null,
    borderColor: Color? = null,
    cornerRadiusDp: Dp = 20.dp
): Modifier {
    val tokens = glassTokensFor(tier)
    val effectiveFill = tintColor ?: tokens.fill
    val effectiveBorder = borderColor ?: tokens.border
    return this
        .shadow(
            elevation = tokens.shadowElevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.45f),
            spotColor = GlassCyanAccent.copy(alpha = 0.22f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    effectiveFill.copy(alpha = (effectiveFill.alpha + 0.04f).coerceAtMost(0.45f)),
                    effectiveFill
                )
            )
        )
        .drawBehind {
            val r = cornerRadiusDp.toPx()
            // Subtle directional frosted sheen inside card
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tokens.topHighlight.copy(alpha = 0.16f),
                        Color.Transparent,
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width * 0.65f, size.height * 0.55f)
                ),
                cornerRadius = CornerRadius(r, r)
            )
        }
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    tokens.topHighlight,
                    effectiveBorder,
                    effectiveBorder.copy(alpha = effectiveBorder.alpha * 0.6f)
                )
            ),
            shape = shape
        )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tier: GlassTier = GlassTier.TIER_2,
    cornerRadius: Dp = 20.dp,
    tintColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier.glassContainer(
        tier = tier,
        shape = shape,
        tintColor = tintColor,
        borderColor = borderColor,
        cornerRadiusDp = cornerRadius
    )
    val interactiveModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = GlassCyanAccent.copy(alpha = 0.35f)),
            onClick = onClick
        )
    } else {
        baseModifier
    }

    CompositionLocalProvider(LocalContentColor provides TextPrimaryWhite) {
        Column(modifier = interactiveModifier, content = content)
    }
}

@Composable
fun GlassSurfaceBox(
    modifier: Modifier = Modifier,
    tier: GlassTier = GlassTier.TIER_2,
    cornerRadius: Dp = 20.dp,
    tintColor: Color? = null,
    borderColor: Color? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    CompositionLocalProvider(LocalContentColor provides TextPrimaryWhite) {
        Box(
            modifier = modifier.glassContainer(
                tier = tier,
                shape = shape,
                tintColor = tintColor,
                borderColor = borderColor,
                cornerRadiusDp = cornerRadius
            ),
            contentAlignment = contentAlignment,
            content = content
        )
    }
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    tintColor: Color = GlassTier3Fill,
    borderColor: Color = GlassTier3Border,
    contentColor: Color = TextPrimaryWhite,
    shape: Shape = RoundedCornerShape(12.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = GlassCyanAccent.copy(alpha = 0.3f)),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Surface(
        shape = shape,
        color = tintColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.clip(shape).then(clickMod)
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun GlassActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tintColor: Color = GlassCyanFill,
    borderColor: Color = GlassCyanBorder,
    contentColor: Color = TextPrimaryWhite,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveFill = if (enabled) tintColor else GlassTier1Fill
    val effectiveBorder = if (enabled) borderColor else GlassTier1Border
    val effectiveContent = if (enabled) contentColor else TextMetaTranslucent

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = effectiveFill,
        contentColor = effectiveContent,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassTier2Highlight.copy(alpha = if (enabled) 0.5f else 0.15f),
                    effectiveBorder
                )
            )
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun GlassAudioIconPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tintColor: Color = GlassCyanFill,
    borderColor: Color = GlassCyanBorder,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = tintColor,
        contentColor = GlassCyanAccent,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun GlassAudioIconPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tintColor: Color = GlassCyanFill,
    borderColor: Color = GlassCyanBorder,
    contentDescription: String = "Pronounce"
) {
    GlassAudioIconPill(
        onClick = onClick,
        modifier = modifier,
        size = size,
        tintColor = tintColor,
        borderColor = borderColor
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = contentDescription,
            tint = GlassCyanAccent,
            modifier = Modifier.size((size.value * 0.5f).dp)
        )
    }
}

@Composable
fun GlassHairlineDivider(
    modifier: Modifier = Modifier,
    color: Color = GlassTier1Border
) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = color)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = GlassTier1Fill,
                scrolledContainerColor = GlassTier2Fill,
                navigationIconContentColor = TextPrimaryWhite,
                titleContentColor = TextPrimaryWhite,
                actionIconContentColor = TextPrimaryWhite
            )
        )
        GlassHairlineDivider(color = GlassTier1Highlight.copy(alpha = 0.25f))
    }
}

@Composable
fun GlassLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColors: List<Color> = listOf(GlassCyanAccent, GlassVioletAccent),
    height: Dp = 8.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(GlassTier1Fill)
            .border(0.75.dp, GlassTier1Border, CircleShape)
    ) {
        if (clamped > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .height(height)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(accentColors.map { it.copy(alpha = 0.80f) }))
            )
        }
    }
}

@Composable
fun glassTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimaryWhite,
    unfocusedTextColor = TextPrimaryWhite,
    disabledTextColor = TextMetaTranslucent,
    focusedContainerColor = GlassTier2Fill,
    unfocusedContainerColor = GlassTier1Fill,
    disabledContainerColor = GlassTier1Fill,
    focusedBorderColor = GlassCyanAccent,
    unfocusedBorderColor = GlassTier2Border,
    focusedLeadingIconColor = GlassCyanAccent,
    unfocusedLeadingIconColor = TextBodyTranslucent,
    focusedTrailingIconColor = GlassCyanAccent,
    unfocusedTrailingIconColor = TextBodyTranslucent,
    focusedLabelColor = GlassCyanAccent,
    unfocusedLabelColor = TextMetaTranslucent,
    focusedPlaceholderColor = TextMetaTranslucent,
    unfocusedPlaceholderColor = TextMetaTranslucent,
    cursorColor = GlassCyanAccent
)

@Composable
fun glassFilterChipColors(
    selectedFill: Color = GlassCyanFill,
    selectedBorder: Color = GlassCyanBorder
): SelectableChipColors = FilterChipDefaults.filterChipColors(
    containerColor = GlassTier3Fill,
    labelColor = TextBodyTranslucent,
    iconColor = TextBodyTranslucent,
    selectedContainerColor = selectedFill,
    selectedLabelColor = TextPrimaryWhite,
    selectedLeadingIconColor = TextPrimaryWhite
)

@Composable
fun glassFilterChipBorder(
    selected: Boolean,
    enabled: Boolean = true,
    selectedBorder: Color = GlassCyanBorder
) = FilterChipDefaults.filterChipBorder(
    enabled = enabled,
    selected = selected,
    borderColor = GlassTier3Border,
    selectedBorderColor = selectedBorder,
    borderWidth = 1.dp,
    selectedBorderWidth = 1.25.dp
)

@Composable
fun GlassModalDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                tier = GlassTier.TIER_2,
                cornerRadius = 24.dp,
                tintColor = GlassModalTint,
                borderColor = GlassTier2Highlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CompositionLocalProvider(LocalContentColor provides TextPrimaryWhite) {
                        title()
                    }
                    CompositionLocalProvider(LocalContentColor provides TextBodyTranslucent) {
                        text()
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
fun glassPrimaryButtonColors() = ButtonDefaults.buttonColors(
    containerColor = GlassRoyalBlueFill,
    contentColor = TextPrimaryWhite,
    disabledContainerColor = GlassTier1Fill,
    disabledContentColor = TextMetaTranslucent
)

@Composable
fun DualAudioButtons(
    word: String,
    onSpeakUs: () -> Unit,
    onSpeakUk: () -> Unit,
    modifier: Modifier = Modifier,
    usTestTag: String = "pronounce_us_$word",
    ukTestTag: String = "pronounce_uk_$word"
) {
    var activeAccent by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isUsActive = activeAccent == "US"
        GlassActionButton(
            onClick = {
                activeAccent = "US"
                onSpeakUs()
                coroutineScope.launch {
                    delay(1800L)
                    if (activeAccent == "US") activeAccent = null
                }
            },
            tintColor = if (isUsActive) GlassVioletAccent.copy(alpha = 0.45f) else GlassVioletFill,
            borderColor = if (isUsActive) GlassCyanAccent else GlassVioletBorder,
            cornerRadius = 10.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag(usTestTag)
        ) {
            Icon(
                imageVector = if (isUsActive) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Pronounce $word in US English",
                modifier = Modifier.size(15.dp),
                tint = if (isUsActive) GlassCyanAccent else TextPrimaryWhite
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "US 🔊",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isUsActive) GlassCyanAccent else TextPrimaryWhite
            )
        }

        val isUkActive = activeAccent == "UK"
        GlassActionButton(
            onClick = {
                activeAccent = "UK"
                onSpeakUk()
                coroutineScope.launch {
                    delay(1800L)
                    if (activeAccent == "UK") activeAccent = null
                }
            },
            tintColor = if (isUkActive) GlassCyanAccent.copy(alpha = 0.45f) else GlassRoyalBlueFill,
            borderColor = if (isUkActive) GlassCyanAccent else GlassRoyalBlueBorder,
            cornerRadius = 10.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag(ukTestTag)
        ) {
            Icon(
                imageVector = if (isUkActive) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Pronounce $word in UK English",
                modifier = Modifier.size(15.dp),
                tint = GlassCyanAccent
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "UK 🔊",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isUkActive) GlassCyanAccent else TextPrimaryWhite
            )
        }
    }
}


