package com.elfennani.kiroku.presentation.screen.episode.component

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elfennani.kiroku.presentation.theme.AppTheme
import com.elfennani.kiroku.presentation.theme.primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private const val TAG = "AppSlider"

@Composable
fun AppSlider(
    modifier: Modifier = Modifier,
    value: Float,
    heightDp: Dp = 3.dp,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (Float) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .height(heightDp+8.dp)
            .graphicsLayer {
                sliderWidth = size.width
            }
            .pointerInput(Unit) {
                var lastValue = value
                var interaction: DragInteraction.Start? = null
                detectHorizontalDragGestures(
                    onDragStart = {
                        scope.launch {
                            interactionSource.emit(
                                DragInteraction.Start().also { interaction = it }
                            )
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (interaction != null)
                                interactionSource.emit(DragInteraction.Stop(interaction))
                        }
                        Log.d(TAG, "AppSlider: Drag End")
                        onValueChangeFinished(lastValue)
                    },
                    onDragCancel = {
                        scope.launch {
                            if (interaction != null)
                                interactionSource.emit(DragInteraction.Cancel(interaction))
                        }
                    },
                    onHorizontalDrag = { change, _ ->
                        Log.d(TAG, "AppSlider: Dragging")
                        lastValue = min(max(change.position.x / sliderWidth, 0f), 1f)
                        onValueChange(lastValue)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        onValueChangeFinished(offset.x / sliderWidth)
                        Log.d(TAG, "AppSlider: Press")
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
                .background(Color.White.copy(0.5f)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(value / 1f)
                .height(heightDp)
                .blur(4.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(primary)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(value / 1f)
                .height(heightDp)
                .background(primary),
        )

        Box(
            modifier = Modifier
                .requiredSize(32.dp)
                .offset(x = (-16).dp)
                .offset(x = with(density) { (value * sliderWidth).toDp() })
                .clip(CircleShape)
                .indication(interactionSource = interactionSource, indication = ripple())
                .padding(horizontal = 10.dp, vertical = 14.dp)
                .background(primary, RoundedCornerShape(2.dp))
                .blur(8.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(primary)
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
private fun AppSliderPrev() {
    var value by remember { mutableFloatStateOf(0.1f) }
    var slidingValue by remember { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = scope.launch {
            while (true) {
                value += 0.01f
                value = value.coerceIn(0f, 1f)
                delay(100)
            }
        }
        onDispose {
            job.cancel()
        }
    }

    AppTheme {
        Scaffold(
            containerColor = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .padding(vertical = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(floor(((slidingValue ?: value) * 100)).toInt().toString())
                AppSlider(
                    modifier = Modifier.fillMaxWidth(),
                    value = slidingValue ?: value,
                    onValueChange = { slidingValue = it },
                    onValueChangeFinished = {
                        slidingValue = null
                        value = it
                    }
                )
            }
        }
    }
}