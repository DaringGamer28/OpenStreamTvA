package com.ivor.openstream.presentation.tv

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Every TV size, in one place.
 *
 * Derived from the actual window size against a 960x540 dp reference canvas (what a 1080p TV
 * reports at xhdpi). The scale factor is clamped so 720p, 1080p and 4K sets lay out the same.
 */
@Immutable
data class TvDimens(
    val scale: Float,
    val gutter: Dp,
    val gutterVertical: Dp,
    val gutterTight: Dp,
    val railGap: Dp,
    val rowGap: Dp,
    val topBarHeight: Dp,
    val posterCardWidth: Dp,
    val posterCardHeight: Dp,
    val landscapeCardWidth: Dp,
    val landscapeCardHeight: Dp,
    val cardTitleHeight: Dp,
    val heroHeight: Dp,
    val buttonHeight: Dp,
    val chipHeight: Dp,
    val focusRingWidth: Dp,
    val focusScale: Float,
    val minTarget: Dp,
    val navRailWidth: Dp,
    val navRailItemHeight: Dp,
    val sidePanelWidth: Dp,
    val listRowHeight: Dp,
    val listRowGap: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp
) {
    companion object {
        fun of(widthDp: Int, heightDp: Int): TvDimens {
            val scale = (widthDp / 960f).coerceIn(0.78f, 1.35f)
            val short = (heightDp / 540f).coerceIn(0.62f, 1.3f)
            val s = minOf(scale, short)

            fun v(base: Float): Dp = (base * s).dp

            val gutter = v(48f)
            val gutterVertical = v(27f)
            val railGap = v(12f)
            val safeWidth = (widthDp.dp - gutter * 2)
            val posterCardWidth = (safeWidth - railGap * 5) / 6
            val landscapeCardWidth = (safeWidth - railGap * 3) / 4

            return TvDimens(
                scale = s,
                gutter = gutter,
                gutterVertical = gutterVertical,
                gutterTight = v(24f),
                railGap = railGap,
                rowGap = v(20f),
                topBarHeight = v(52f),
                posterCardWidth = posterCardWidth,
                posterCardHeight = posterCardWidth * 3 / 2, // 2:3 portrait
                landscapeCardWidth = landscapeCardWidth,
                landscapeCardHeight = landscapeCardWidth * 9 / 16,
                cardTitleHeight = v(20f),
                heroHeight = (heightDp * 0.58f).dp,
                buttonHeight = v(44f),
                chipHeight = v(36f),
                focusRingWidth = v(3f),
                focusScale = 1.09f,
                minTarget = v(48f),
                navRailWidth = v(104f),
                navRailItemHeight = v(64f),
                sidePanelWidth = v(380f),
                listRowHeight = v(52f),
                listRowGap = v(8f),
                paddingSmall = v(8f),
                paddingMedium = v(16f),
                paddingLarge = v(24f)
            )
        }
    }
}

val LocalTvDimens = staticCompositionLocalOf { TvDimens.of(960, 540) }