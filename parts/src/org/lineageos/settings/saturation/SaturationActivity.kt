/*
 * Copyright (C) 2025 kenway214
 *           (C) 2026 zylhdrXP
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

package org.lineageos.settings.saturation

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.os.ServiceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.lineageos.settings.Constants
import org.lineageos.settings.R
import org.lineageos.settings.utils.TileUtils

class SaturationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val controller = SaturationController(this)
        controller.restore()

        setContent {
            SaturationTheme {
                BackHandler { finish() }
                SaturationScreen(
                    initialValue = controller.value,
                    onValueChanged = controller::setValue,
                    onReset = controller::reset,
                    onAddTile = {
                        TileUtils.requestAddTileService(
                            this,
                            SaturationTileService::class.java,
                            R.string.saturation_title,
                            R.drawable.ic_saturation_tile,
                        )
                    },
                    onBackPressed = ::finish,
                )
            }
        }
    }
}

private class SaturationController(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val surfaceFlinger: IBinder? = ServiceManager.getService("SurfaceFlinger")
    var value: Int = preferences.getInt(Constants.KEY_SATURATION, DEFAULT_VALUE)
        private set

    fun restore() {
        setValue(value)
    }

    fun setValue(newValue: Int) {
        value = newValue.coerceIn(MIN_VALUE, MAX_VALUE)
        preferences.edit { putInt(Constants.KEY_SATURATION, value) }
        val saturation = if (value == DEFAULT_VALUE) 1.001f else value / 100.0f
        surfaceFlinger?.let { binder ->
            val data = Parcel.obtain()
            try {
                data.writeInterfaceToken("android.ui.ISurfaceComposer")
                data.writeFloat(saturation)
                binder.transact(TRANSACTION_SET_SATURATION, data, null, 0)
            } catch (exception: RemoteException) {
                android.util.Log.e(TAG, "Unable to update display saturation", exception)
            } finally {
                data.recycle()
            }
        }
    }

    fun reset() {
        setValue(DEFAULT_VALUE)
    }

    private companion object {
        const val DEFAULT_VALUE = 100
        const val MIN_VALUE = 0
        const val MAX_VALUE = 200
        const val TRANSACTION_SET_SATURATION = 1022
        const val TAG = "SaturationController"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaturationScreen(
    initialValue: Int,
    onValueChanged: (Int) -> Unit,
    onReset: () -> Unit,
    onAddTile: () -> Unit,
    onBackPressed: () -> Unit,
) {
    var value by remember { mutableIntStateOf(initialValue) }
    var page by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    val previewImages = listOf(
        R.drawable.image_preview1,
        R.drawable.image_preview2,
        R.drawable.image_preview3,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saturation_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.garnet_navigate_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.saturation_actions),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tile_add)) },
                            leadingIcon = {
                                Icon(Icons.Default.AddToHomeScreen, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onAddTile()
                            },
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PreviewCarousel(
                    images = previewImages,
                    page = page,
                    onPageChanged = { page = it },
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.saturation_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = stringResource(R.string.custom_seekbar_value, "$value%"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        val saturationDescription = stringResource(R.string.saturation_title)
                        Slider(
                            value = value.toFloat(),
                            onValueChange = {
                                val newValue = it.toInt()
                                if (newValue != value) {
                                    value = newValue
                                    onValueChanged(newValue)
                                }
                            },
                            valueRange = 0f..200f,
                            steps = 199,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = saturationDescription
                                },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("0%", style = MaterialTheme.typography.labelMedium)
                            Text("100%", style = MaterialTheme.typography.labelMedium)
                            Text("200%", style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(
                            onClick = {
                                value = 100
                                onReset()
                            },
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(
                                    R.string.saturation_reset,
                                ),
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.saturation_footer_summary),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PreviewCarousel(
    images: List<Int>,
    page: Int,
    onPageChanged: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Column {
            Image(
                painter = painterResource(images[page]),
                contentDescription = stringResource(R.string.image_preview_content_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = { onPageChanged(page - 1) },
                    enabled = page > 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(
                            R.string.image_preview_previous_page_content_description,
                        ),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    images.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == page) 10.dp else 8.dp)
                                .background(
                                    color = if (index == page) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                IconButton(
                    onClick = { onPageChanged(page + 1) },
                    enabled = page < images.lastIndex,
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(
                            R.string.image_preview_next_page_content_description,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SaturationTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    val colorScheme = when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ->
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        darkTheme -> androidx.compose.material3.darkColorScheme()
        else -> androidx.compose.material3.lightColorScheme()
    }
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            androidx.core.view.WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
