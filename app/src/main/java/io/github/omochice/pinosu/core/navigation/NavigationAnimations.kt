package io.github.omochice.pinosu.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val ANIMATION_DURATION_MS = 300

val defaultEnterTransition: EnterTransition =
    fadeIn(tween(ANIMATION_DURATION_MS)) +
        slideInHorizontally(tween(ANIMATION_DURATION_MS)) { it / 4 }

val defaultExitTransition: ExitTransition =
    fadeOut(tween(ANIMATION_DURATION_MS)) +
        slideOutHorizontally(tween(ANIMATION_DURATION_MS)) { -it / 4 }

val defaultPopEnterTransition: EnterTransition =
    fadeIn(tween(ANIMATION_DURATION_MS)) +
        slideInHorizontally(tween(ANIMATION_DURATION_MS)) { -it / 4 }

val defaultPopExitTransition: ExitTransition =
    fadeOut(tween(ANIMATION_DURATION_MS)) +
        slideOutHorizontally(tween(ANIMATION_DURATION_MS)) { it / 4 }

val modalEnterTransition: EnterTransition =
    fadeIn(tween(ANIMATION_DURATION_MS)) +
        scaleIn(tween(ANIMATION_DURATION_MS), initialScale = 0.92f)

val modalExitTransition: ExitTransition =
    fadeOut(tween(ANIMATION_DURATION_MS)) +
        scaleOut(tween(ANIMATION_DURATION_MS), targetScale = 0.92f)

val modalPopEnterTransition: EnterTransition =
    fadeIn(tween(ANIMATION_DURATION_MS)) +
        scaleIn(tween(ANIMATION_DURATION_MS), initialScale = 1.08f)

val modalPopExitTransition: ExitTransition =
    fadeOut(tween(ANIMATION_DURATION_MS)) +
        scaleOut(tween(ANIMATION_DURATION_MS), targetScale = 1.08f)
