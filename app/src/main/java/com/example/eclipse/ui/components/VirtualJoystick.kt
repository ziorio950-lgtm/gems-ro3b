package com.example.eclipse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    onMove: (forward: Float, strafe: Float) -> Unit
) {
    var thumbOffsetX by remember { mutableFloatStateOf(0f) }
    var thumbOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadius = 110f

    Box(
        modifier = modifier
            .testTag("virtual_joystick")
            .size(170.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val dx = offset.x - size.width / 2f
                        val dy = offset.y - size.height / 2f
                        val dist = hypot(dx, dy)
                        if (dist <= maxRadius) {
                            thumbOffsetX = dx
                            thumbOffsetY = dy
                        } else {
                            thumbOffsetX = (dx / dist) * maxRadius
                            thumbOffsetY = (dy / dist) * maxRadius
                        }
                        onMove(-thumbOffsetY / maxRadius, thumbOffsetX / maxRadius)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = thumbOffsetX + dragAmount.x
                        val newY = thumbOffsetY + dragAmount.y
                        val dist = hypot(newX, newY)

                        if (dist <= maxRadius) {
                            thumbOffsetX = newX
                            thumbOffsetY = newY
                        } else {
                            thumbOffsetX = (newX / dist) * maxRadius
                            thumbOffsetY = (newY / dist) * maxRadius
                        }
                        // Note: in first-person camera, negative Y on screen is forward
                        onMove(-thumbOffsetY / maxRadius, thumbOffsetX / maxRadius)
                    },
                    onDragEnd = {
                        thumbOffsetX = 0f
                        thumbOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffsetX = 0f
                        thumbOffsetY = 0f
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer ring
            drawCircle(
                color = Color(0x66181B22),
                radius = maxRadius,
                center = center
            )
            drawCircle(
                color = Color(0xAA00E5FF),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Inner thumb
            val thumbPos = Offset(center.x + thumbOffsetX, center.y + thumbOffsetY)
            drawCircle(
                color = Color(0x99E53935),
                radius = 38f,
                center = thumbPos
            )
            drawCircle(
                color = Color(0xFFFF5252),
                radius = 38f,
                center = thumbPos,
                style = Stroke(width = 3f)
            )
        }
    }
}

@Composable
fun CameraLookTouchPad(
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.0f,
    invertY: Boolean = false,
    onLook: (yaw: Float, pitch: Float) -> Unit
) {
    Box(
        modifier = modifier
            .testTag("camera_look_pad")
            .pointerInput(sensitivity, invertY) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val yawDelta = dragAmount.x * 0.0055f * sensitivity
                    val pitchFactor = if (invertY) -0.0035f else 0.0035f
                    val pitchDelta = -dragAmount.y * pitchFactor * sensitivity
                    onLook(yawDelta, pitchDelta)
                }
            }
    )
}
