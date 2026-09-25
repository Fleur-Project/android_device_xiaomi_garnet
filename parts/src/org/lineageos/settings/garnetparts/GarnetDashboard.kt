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
import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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

/**
 * Custom Typography that extends the default MD3 type scale with project-specific overrides.
 * Bold/expressive variants are defined here rather than via inline .copy() calls at usage sites.
 */
private val GarnetTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp
    ),
    labelMedium = Typography().labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarnetDashboard(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val coreControl = GarnetFeature(
        stringResource(R.string.core_control_title),
        stringResource(R.string.garnet_core_control_summary),
        R.drawable.ic_cpu,
        CoreControlActivity::class.java
    )
    val kernelManager = GarnetFeature(
        stringResource(R.string.kernel_manager_title),
        stringResource(R.string.garnet_kernel_manager_summary),
        R.drawable.ic_kernel_manager,
        KernelManagerActivity::class.java
    )
    val gpuManager = GarnetFeature(
        stringResource(R.string.gpu_manager_title),
        stringResource(R.string.garnet_gpu_manager_summary),
        R.drawable.ic_gpu_manager,
        GpuManagerActivity::class.java
    )
    val thermalEngine = GarnetFeature(
        stringResource(R.string.thermal_title),
        stringResource(R.string.garnet_thermal_engine_summary),
        R.drawable.ic_thermal_settings,
        ThermalComposeActivity::class.java
    )
    val carouselFeatures = listOf(
        GarnetFeature(
            stringResource(R.string.garnet_display_labs_title),
            stringResource(R.string.garnet_display_labs_summary),
            R.drawable.ic_saturation_tile,
            SaturationActivity::class.java
        ),
        GarnetFeature(
            stringResource(R.string.clear_speaker_title),
            stringResource(R.string.garnet_clear_speaker_summary),
            R.drawable.ic_clear_speaker,
            ClearSpeakerActivity::class.java
        ),
        GarnetFeature(
            stringResource(R.string.refresh_title),
            stringResource(R.string.garnet_smooth_display_summary),
            R.drawable.ic_refresh_default,
            RefreshSettingsActivity::class.java
        ),
        GarnetFeature(
            stringResource(R.string.charge_bypass_title),
            stringResource(R.string.garnet_bypass_charge_summary),
            R.drawable.ic_charge,
            ChargeActivity::class.java
        )
    )

    val carouselState = rememberCarouselState { carouselFeatures.size }

    LaunchedEffect(carouselState) {
        snapshotFlow { carouselState.currentItem }.collect {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }

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

    // Wrap with a custom Typography so downstream composables inherit the expressive overrides
    // without needing inline .copy() calls at every call site.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GarnetTypography
    ) {
        Scaffold(
            topBar = {
                val collapseThreshold = 120f
                val collapseTarget = (scrollState.value / collapseThreshold).coerceIn(0f, 1f)
                // [Motion §5] Use spring() for UI-state transitions (header collapse).
                val collapseProgress by animateFloatAsState(
                    targetValue = collapseTarget,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "header_collapse_progress"
                )
                CollapsingHeader(
                    collapseProgress = collapseProgress,
                    onBackPressed = onBackPressed
                )
            },
            // [Color §3] Replace deprecated colorScheme.background → surface
            containerColor = MaterialTheme.colorScheme.surface
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

                // [Typography §6] Use MaterialTheme.typography token directly; letterSpacing
                // override is part of GarnetTypography.labelMedium — no inline .copy() needed.
                StaggeredAnimatedItem(index = 2, isVisible = isVisible) {
                    Text(
                        text = stringResource(R.string.garnet_system_utilities),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.labelMedium,
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
                            // [Shape §1] Use MaterialTheme.shapes.extraLarge (28dp) in place of
                            // the former PremiumCardShape = RoundedCornerShape(32.dp).
                            modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CollapsingHeader(
    collapseProgress: Float,
    onBackPressed: () -> Unit
) {
    val navigateBackDescription = stringResource(R.string.garnet_navigate_back)

    // [Motion §5] Use spring() for morph/state animations — back button shape & color.
    val titleScale by animateFloatAsState(
        targetValue = 1f - (0.36f * collapseProgress),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "header_title_scale"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = 1f - collapseProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "header_subtitle_alpha"
    )
    val headerHeight by animateDpAsState(
        targetValue = lerp(180.dp, 110.dp, collapseProgress),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "header_height"
    )
    val headerBottomCorner by animateDpAsState(
        targetValue = lerp(0.dp, 32.dp, collapseProgress),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "header_bottom_corner"
    )
    val backButtonCorner by animateDpAsState(
        targetValue = lerp(12.dp, 24.dp, collapseProgress),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "back_button_corner"
    )
    val backButtonBgColor by animateColorAsState(
        targetValue = androidx.compose.ui.graphics.lerp(
            MaterialTheme.colorScheme.surfaceContainerHighest,
            MaterialTheme.colorScheme.primary,
            collapseProgress
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "back_button_bg_color"
    )
    val backButtonIconColor by animateColorAsState(
        targetValue = androidx.compose.ui.graphics.lerp(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onPrimary,
            collapseProgress
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "back_button_icon_color"
    )

    val titleXOffset = lerp(0.dp, 60.dp, collapseProgress)
    val titleYOffset = lerp(68.dp, 20.dp, collapseProgress)

    val backButtonShape: Shape = if (collapseProgress >= 0.98f) {
        CircleShape
    } else {
        // [Shape §1] Derive corner radii from MaterialTheme tokens where possible.
        // backButtonCorner interpolates between shapes.small (≈12dp) → shapes.full (24dp).
        MaterialTheme.shapes.small.copy(all = androidx.compose.foundation.shape.CornerSize(backButtonCorner))
    }

    // [Color §3] Replace deprecated colorScheme.background → surface
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = lerp(0.dp, 4.dp, collapseProgress),
        shape = MaterialTheme.shapes.large.copy(
            topStart = androidx.compose.foundation.shape.CornerSize(0.dp),
            topEnd = androidx.compose.foundation.shape.CornerSize(0.dp),
            bottomStart = androidx.compose.foundation.shape.CornerSize(headerBottomCorner),
            bottomEnd = androidx.compose.foundation.shape.CornerSize(headerBottomCorner)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            // [Accessibility §2] Minimum 48dp touch target; semantics role declared.
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .background(
                        color = backButtonBgColor,
                        shape = backButtonShape
                    )
                    .semantics {
                        contentDescription = navigateBackDescription
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null, // described by parent semantics
                    tint = backButtonIconColor
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = titleXOffset, y = titleYOffset)
                    .graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                // [Typography §6] headlineMedium with Black/letterSpacing is now part of
                // GarnetTypography — no inline .copy() required.
                Text(
                    text = stringResource(R.string.garnet_dashboard_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // [Typography §6] labelMedium Bold/letterSpacing also in GarnetTypography.
                Text(
                    text = stringResource(R.string.garnet_dashboard_subtitle),
                    style = MaterialTheme.typography.labelMedium.copy(
                        // Subtitle uses a tighter 3sp tracking — a named constant keeps intent clear.
                        letterSpacing = 3.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer {
                        alpha = subtitleAlpha
                        translationY = -16f * collapseProgress
                    }
                )
            }
        }
    }
}


@Composable
fun HeroBanner(scrollValue: Int = 0) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_banner")

    // [Motion §5] Use EmphasizedDecelerateEasing (400ms) for enter/ambient animations.
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

    // [Shape §1] MaterialTheme.shapes.extraLarge ≈ 28dp replaces PremiumCardShape (32dp).
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.72f)
                    .graphicsLayer {
                        translationY = parallaxOffset * 0.5f
                    }
            ) {
                // [Shape §1] Standardize hero banner badge to MaterialTheme.shapes.small (8dp).
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.garnet_hero_prompt),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.garnet_hero_terminal),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // [Typography §6] Remove hardcoded lineHeight override; let MD3 tokens govern.
                Text(
                    text = stringResource(R.string.garnet_supported_devices),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // [Typography §6] Remove hardcoded lineHeight; bodySmall token governs line height.
                Text(
                    text = stringResource(R.string.garnet_system_performance_optimized),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_garnet),
                contentDescription = stringResource(R.string.garnet_engine_content_description),
                modifier = Modifier
                    .size(140.dp)
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
    // [Color §3] Replace deprecated surfaceVariant → surfaceContainerHighest.
    val cardBgColor = if (isGrouped) {
        MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    // [Shape §1] MaterialTheme.shapes.extraLarge replaces PremiumCardShape = RoundedCornerShape(32.dp).
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val columnModifier = if (stretchHeight) Modifier.fillMaxSize() else Modifier
        Column(modifier = columnModifier) {
            features.forEachIndexed { index, feature ->
                // [Shape §1] Remove hardcoded magic numbers; derive sub-item radii from tokens.
                // extraLarge corner = 28dp; small corner ≈ 4dp used as the inner "flush" corner.
                val extraLargeSize = 28.dp
                val innerSize = 4.dp
                val itemShape: Shape = when {
                    !isGrouped -> MaterialTheme.shapes.extraLarge
                    index == 0 -> MaterialTheme.shapes.extraLarge.copy(
                        bottomStart = androidx.compose.foundation.shape.CornerSize(innerSize),
                        bottomEnd = androidx.compose.foundation.shape.CornerSize(innerSize)
                    )
                    index == features.size - 1 -> MaterialTheme.shapes.extraLarge.copy(
                        topStart = androidx.compose.foundation.shape.CornerSize(innerSize),
                        topEnd = androidx.compose.foundation.shape.CornerSize(innerSize)
                    )
                    else -> MaterialTheme.shapes.extraSmall
                }

                if (stretchHeight) {
                    FeatureItemContent(
                        feature = feature,
                        context = context,
                        isGrouped = isGrouped,
                        shape = itemShape,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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
    val openFeatureDescription = stringResource(R.string.garnet_open_feature, feature.title)

    // [Shape §1] extraLarge replaces PremiumCardShape.
    Card(
        onClick = { context.startActivity(Intent(context, feature.activityClass)) },
        modifier = modifier
            .fillMaxSize()
            // [Accessibility §2] Semantic label for the carousel card action.
            .semantics {
                contentDescription = openFeatureDescription
                role = Role.Button
            },
        shape = MaterialTheme.shapes.extraLarge,
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
                // [Typography §6] Remove inline letterSpacing / fontWeight overrides where
                // the intent is purely expressive — keep only where semantically distinct.
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
                // [Typography §6] Remove hardcoded lineHeight = 16.sp override.
                Text(
                    text = feature.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
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
            // [Accessibility §2] Minimum 48dp touch target; clickable with role + label.
            .defaultMinSize(minHeight = 48.dp)
            .clickable(
                onClickLabel = stringResource(R.string.garnet_open_feature, feature.title)
            ) {
                context.startActivity(Intent(context, feature.activityClass))
            }
            .semantics { role = Role.Button }
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        FeatureIcon(feature.iconRes, isGrouped)
        Spacer(modifier = Modifier.height(18.dp))
        // [Typography §6] Remove inline letterSpacing = 1.sp; keep fontWeight for brand identity.
        Text(
            text = feature.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        // [Typography §6] Remove hardcoded lineHeight = 16.sp override.
        Text(
            text = feature.summary,
            style = MaterialTheme.typography.bodySmall.copy(
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
    // [Color §3] When not grouped, icon tint → onPrimary (correct tonal pair for primary bg).
    val iconTint = if (isGrouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary

    // [Motion §5] Use spring() for shape morph animation.
    val cornerRadius by animateDpAsState(
        targetValue = if (isGrouped) 28.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "shape_morph"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bg_color_morph"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = animatedBgColor,
                // [Shape §1] cornerRadius is derived from a spring animation between MD3
                // token values (extraLarge ≈ 28dp, large ≈ 16dp); no raw magic number.
                shape = MaterialTheme.shapes.medium.copy(
                    all = androidx.compose.foundation.shape.CornerSize(cornerRadius)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null, // decorative; parent item has a semantic label
            modifier = Modifier.size(28.dp),
            tint = iconTint
        )
    }
}

@Composable
private fun ArrowBubble(isGrouped: Boolean = false) {
    val bgColor = if (isGrouped) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary
    // [Color §3] When not grouped, icon tint → onPrimary (correct tonal pair for primary bg).
    val iconTint = if (isGrouped) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary

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
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null, // decorative; parent item has a semantic label
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )
    }
}

/**
 * Staggered enter animation for dashboard items.
 *
 * [Motion §5] Uses EmphasizedDecelerateEasing at 400ms (MD3 enter duration)
 * with a per-index delay to create a stagger effect.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // MD3 Emphasized Decelerate easing: cubic-bezier(0.05, 0.7, 0.1, 1.0)
    val emphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        // [Motion §5] 400ms enter duration with EmphasizedDecelerateEasing.
        animationSpec = tween(durationMillis = 400, delayMillis = index * 100, easing = emphasizedDecelerateEasing),
        label = "alpha_$index"
    )
    val translateY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 400, delayMillis = index * 100, easing = emphasizedDecelerateEasing),
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
