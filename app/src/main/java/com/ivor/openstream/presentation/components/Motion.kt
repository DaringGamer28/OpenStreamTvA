package com.ivor.openstream.presentation.components

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Shared expressive-motion vocabulary. `OpenStreamTheme` opts into
 * `MotionScheme.expressive()`; hand-rolled animations should read their specs from here
 * instead of hard-coding `tween(...)` durations, and go instant when the user enabled
 * "Remove animations" (animator scale 0).
 */
object ExpressiveMotion {
    /** True when the system animator scale is 0: motion must be instant. */
    @Composable
    fun reducedMotion(): Boolean {
        val context = LocalContext.current
        return remember {
            runCatching {
                Settings.Global.getFloat(
                    context.contentResolver,
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f
                ) == 0f
            }.getOrDefault(false)
        }
    }

    /** Effect spec (fade, scale) from the theme, or instant when reduced motion. */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> effectsSpec(): AnimationSpec<T> =
        if (reducedMotion()) snap() else MaterialTheme.motionScheme.defaultEffectsSpec()

    /** Spatial spec (slide, offset) from the theme, or instant when reduced motion. */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> spatialSpec(): AnimationSpec<T> =
        if (reducedMotion()) snap() else MaterialTheme.motionScheme.defaultSpatialSpec()

    /** Fast effect spec for small feedback (ticks, icon morphs). */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> fastEffectsSpec(): AnimationSpec<T> =
        if (reducedMotion()) snap() else MaterialTheme.motionScheme.fastEffectsSpec()
}
