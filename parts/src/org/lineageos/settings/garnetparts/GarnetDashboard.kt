/*
 * Copyright (C) 2026 zylhdrXP
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
 
package org.lineageos.settings.garnetparts

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.lineageos.settings.R
import org.lineageos.settings.corecontrol.CoreControlActivity
import org.lineageos.settings.charge.ChargeActivity
import org.lineageos.settings.kernelmanager.KernelManagerActivity
import org.lineageos.settings.gpumanager.GpuManagerActivity
import org.lineageos.settings.saturation.SaturationActivity
import org.lineageos.settings.refreshrate.RefreshSettingsActivity
import org.lineageos.settings.speaker.ClearSpeakerActivity
import org.lineageos.settings.thermal.ThermalComposeActivity

data class GarnetFeature(
    val title: String,
    val summary: String,
    val iconRes: Int,
    val activityClass: Class<*>
)

val PremiumCardShape = RoundedCornerShape(32.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarnetDashboard(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val coreControl = GarnetFeature("Core Control", "Optimized CPU management", R.drawable.ic_cpu, CoreControlActivity::class.java)
    val kernelManager = GarnetFeature("Kernel Manager", "Advanced system tuning", R.drawable.ic_kernel_manager, KernelManagerActivity::class.java)
    val gpuManager = GarnetFeature("GPU Manager", "Peak graphics performance", R.drawable.ic_gpu_manager, GpuManagerActivity::class.java)
    val thermalEngine = GarnetFeature("Thermal Engine", "Pro-active heat management", R.drawable.ic_thermal_settings, ThermalComposeActivity::class.java)
    val carouselFeatures = listOf(
        GarnetFeature("Display Labs", "Color & Saturation", R.drawable.ic_saturation_tile, SaturationActivity::class.java),
        GarnetFeature("Clear Speaker", "Sonic dust removal", R.drawable.ic_clear_speaker, ClearSpeakerActivity::class.java),
        GarnetFeature("Smooth Display", "Per-app refresh rates", R.drawable.ic_refresh_default, RefreshSettingsActivity::class.java),
        GarnetFeature("Bypass Charge", "Direct power delivery", R.drawable.ic_charge, ChargeActivity::class.java)
    )

    val carouselState = rememberCarouselState { carouselFeatures.size }
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    val scrollState = rememberScrollState()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                "GARNET PARTS",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "SYSTEM IS YOURS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackPressed,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                StaggeredAnimatedItem(index = 0, isVisible = isVisible) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        HeroBanner(scrollValue = scrollState.value)
                    }
                }

                StaggeredAnimatedItem(index = 1, isVisible = isVisible, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GroupedFeatureCard(listOf(gpuManager), context)
                            GroupedFeatureCard(listOf(thermalEngine), context)
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            GroupedFeatureCard(
                                features = listOf(coreControl, kernelManager), 
                                context = context,
                                modifier = Modifier.fillMaxHeight(),
                                stretchHeight = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                StaggeredAnimatedItem(index = 2, isVisible = isVisible) {
                    Text(
                        "SYSTEM UTILITIES",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                StaggeredAnimatedItem(index = 3, isVisible = isVisible) {
                    HorizontalMultiBrowseCarousel(
                        state = carouselState,
                        preferredItemWidth = 190.dp,
                        itemSpacing = 16.dp,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) { index ->
                        val feature = carouselFeatures[index]
                        CarouselFeatureItem(
                            feature = feature, 
                            context = context,
                            modifier = Modifier.maskClip(PremiumCardShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HeroBanner(scrollValue: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_banner")
    
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    val iconOffset = (animationProgress - 0.5f) * 24f
    val parallaxOffset = scrollValue * 0.2f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.65f)
                    .graphicsLayer {
                        translationY = parallaxOffset * 0.5f
                    }
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary, 
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "❯",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "root@garnet:~#",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "RN 13 PRO 5G /\nPOCO X6 5G",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 28.sp,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    "System performance optimized",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                painter = painterResource(id = R.drawable.ic_garnet),
                contentDescription = "Garnet Engine",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp, y = iconOffset.dp)
                    .graphicsLayer {
                        translationY = parallaxOffset
                    }
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun GroupedFeatureCard(
    features: List<GarnetFeature>, 
    context: Context, 
    modifier: Modifier = Modifier, 
    stretchHeight: Boolean = false
) {
    val isGrouped = features.size > 1 
    val cardBgColor = if (isGrouped) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = PremiumCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val columnModifier = if (stretchHeight) Modifier.fillMaxSize() else Modifier
        Column(modifier = columnModifier) {
            features.forEachIndexed { index, feature ->
                val itemShape = when {
                    !isGrouped -> PremiumCardShape
                    index == 0 -> RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    index == features.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 32.dp, bottomEnd = 32.dp)
                    else -> RoundedCornerShape(4.dp)
                }

                if (stretchHeight) {
                    FeatureItemContent(
                        feature = feature, 
                        context = context, 
                        isGrouped = isGrouped, 
                        shape = itemShape,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                } else {
                    FeatureItemContent(
                        feature = feature, 
                        context = context, 
                        isGrouped = isGrouped, 
                        shape = itemShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (index < features.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 22.dp),
                        thickness = 1.dp,
                        color = if (isGrouped) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselFeatureItem(
    feature: GarnetFeature, 
    context: Context,
    modifier: Modifier = Modifier
) {
    val isGrouped = false

    Card(
        onClick = { context.startActivity(Intent(context, feature.activityClass)) },
        modifier = modifier.fillMaxSize(),
        shape = PremiumCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureIcon(feature.iconRes, isGrouped)
            
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = feature.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ArrowBubble(isGrouped)
            }
        }
    }
}

@Composable
fun FeatureItemContent(
    feature: GarnetFeature, 
    context: Context, 
    isGrouped: Boolean = false,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(shape)
            .clickable { context.startActivity(Intent(context, feature.activityClass)) }
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        FeatureIcon(feature.iconRes, isGrouped)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = feature.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = feature.summary,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ArrowBubble(isGrouped)
        }
    }
}

@Composable
private fun FeatureIcon(iconRes: Int, isGrouped: Boolean = false) {
    val targetBgColor = if (isGrouped) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary
    val iconTint = if (isGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    
    val cornerRadius by animateDpAsState(
        targetValue = if (isGrouped) 28.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, 
            stiffness = Spring.StiffnessLow
        ),
        label = "shape_morph"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(300),
        label = "bg_color_morph"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = animatedBgColor,
                shape = RoundedCornerShape(cornerRadius) 
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = iconTint
        )
    }
}

@Composable
private fun ArrowBubble(isGrouped: Boolean = false) {
    val bgColor = if (isGrouped) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary
    val iconTint = if (isGrouped) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surfaceContainer

    Box(
        modifier = Modifier
            .size(38.dp)
            .background(
                color = bgColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )
    }
}

@Composable
fun StaggeredAnimatedItem(
    index: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, delayMillis = index * 100, easing = FastOutSlowInEasing),
        label = "alpha_$index"
    )
    val translateY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 600, delayMillis = index * 100, easing = FastOutSlowInEasing),
        label = "translateY_$index"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translateY.toPx()
        }
    ) {
        content()
    }
}
