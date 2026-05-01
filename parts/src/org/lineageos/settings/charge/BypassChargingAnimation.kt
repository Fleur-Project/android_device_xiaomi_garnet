/*
 * Copyright (C) 2026 The zylhdrXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.charge

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery0Bar
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BypassChargingAnimation(
    modifier: Modifier = Modifier,
    isBypassing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EnergyFlow")
    
    val energyAlpha by animateFloatAsState(
        targetValue = if (isBypassing) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "EnergyAlpha"
    )

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DashPhase"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            
            val startY = height - 40.dp.toPx()
            val endY = 40.dp.toPx()
            val batteryCenterY = height / 2
            val curveOffset = 60.dp.toPx()

            val path = Path().apply {
                moveTo(centerX, startY)
                lineTo(centerX, batteryCenterY + 40.dp.toPx())
                quadraticBezierTo(
                    centerX - curveOffset, batteryCenterY,
                    centerX, batteryCenterY - 40.dp.toPx()
                )
                lineTo(centerX, endY)
            }

            val pathRight = Path().apply {
                moveTo(centerX, startY)
                lineTo(centerX, batteryCenterY + 40.dp.toPx())
                quadraticBezierTo(
                    centerX + curveOffset, batteryCenterY,
                    centerX, batteryCenterY - 40.dp.toPx()
                )
                lineTo(centerX, endY)
            }

            val staticStyle = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            
            drawPath(path = path, color = surfaceVariant, style = staticStyle)
            drawPath(path = pathRight, color = surfaceVariant, style = staticStyle)

            if (energyAlpha > 0f) {
                val animatedStyle = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(30f, 30f),
                        phase = -phase
                    )
                )

                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = energyAlpha),
                    style = animatedStyle
                )
                drawPath(
                    path = pathRight,
                    color = primaryColor.copy(alpha = energyAlpha),
                    style = animatedStyle
                )
            }
            
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(centerX - 15.dp.toPx(), startY),
                size = androidx.compose.ui.geometry.Size(30.dp.toPx(), 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }

        Icon(
            imageVector = Icons.Rounded.Battery0Bar,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isBypassing) 0.2f else 1f)
        )

        Icon(
            imageVector = Icons.Rounded.Memory,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .offset(y = (-100).dp),
            tint = if (isBypassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
