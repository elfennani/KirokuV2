package com.elfennani.kiroku.presentation.screen.media.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.elfennani.kiroku.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DraggableItem(
    modifier: Modifier = Modifier,
    state: DraggableState = rememberDraggableState { },
    backdrop: @Composable BoxScope.() -> Unit = { DraggableBackdrop(state = state) },
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        backdrop()
        Box(
            Modifier
                .pointerInput(Unit) {
                    state.run { gestureHandler() }
                }
                .offset(x = state.offsetDp)
        ) {
            content()
        }
    }
}

@Composable
fun DraggableBackdrop(
    modifier: Modifier = Modifier,
    state: DraggableState,
    icon: @Composable () -> Unit = {
        Icon(
            painterResource(R.drawable.outline_download_24),
            "Download",
            modifier = Modifier.size(32.dp)
        )
    }
) {
    Row(
        modifier = Modifier
            .width(state.offsetDp)
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.primary.copy(
                    (state.offset / state.threshold).coerceIn(0f, 1f)
                )
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        AnimatedVisibility(
            visible = state.isOverThreshold,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            val hapticFeedback = LocalHapticFeedback.current
            LaunchedEffect(Unit) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }

            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
            ) {
                icon()
            }
        }
    }

}

class DraggableState(
    private val width: Int,
    private val scope: CoroutineScope,
    private val density: Density,
    private val onSuccess: () -> Unit
) {
    val offsetAnimatable = Animatable(0f)
    val offset by derivedStateOf { offsetAnimatable.value }
    val offsetDp by derivedStateOf { with(density) { offset.toDp() } }
    val threshold by derivedStateOf { width / 3 }
    val thresholdDp by derivedStateOf { with(density) { (width / 3).toDp() } }

    val isOverThreshold by derivedStateOf { offset > threshold }

    fun snapTo(value: Float) {
        scope.launch {
            offsetAnimatable.snapTo(value)
        }
    }

    fun animateBack(bouncing: Boolean = false) {

        if (bouncing)
            scope.launch {
                offsetAnimatable.animateTo(
                    0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                )
            }
        else
            scope.launch {
                offsetAnimatable.animateTo(
                    0f,
                )
            }
    }

    suspend fun PointerInputScope.gestureHandler() {
        detectHorizontalDragGestures(
            onDragStart = {},
            onDragEnd = {
                animateBack(true)
                onSuccess()
            },
            onDragCancel = {
                animateBack()
            },
            onHorizontalDrag = { _, dragAmount ->
//                Log.d(TAG, "gestureHandler: $dragAmount")
                snapTo(
                    (offsetAnimatable.value + dragAmount).coerceIn(
                        minimumValue = 0f,
                        maximumValue = width.toFloat() / 2
                    )
                )
            }
        )
    }
}

@Composable
fun rememberDraggableState(onSuccess: () -> Unit): DraggableState {
    val width = LocalWindowInfo.current.containerSize.width
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val draggableState = remember {
        DraggableState(width, density = density, scope = scope, onSuccess = onSuccess)
    }

    return draggableState
}