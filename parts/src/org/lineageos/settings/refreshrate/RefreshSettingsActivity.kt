/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package org.lineageos.settings.refreshrate

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import org.lineageos.settings.R
import org.lineageos.settings.refreshrate.ui.theme.RefreshRateTheme

class RefreshSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RefreshRateTheme {
                BackHandler { finish() }
                RefreshSettingsScreen(onBackPressed = ::finish)
            }
        }
    }
}

private data class RefreshApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

@Composable
private fun RefreshSettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val refreshUtils = remember(context) { RefreshUtils(context) }
    var apps by remember { mutableStateOf(emptyList<RefreshApp>()) }
    var query by remember { mutableStateOf("") }
    var refreshVersion by remember { mutableStateOf(0) }

    fun loadApps() {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        apps = packageManager.queryIntentActivities(launcherIntent, 0)
            .asSequence()
            .mapNotNull { resolveInfo ->
                val info = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                if (!info.enabled) return@mapNotNull null
                RefreshApp(
                    packageName = info.packageName,
                    label = info.loadLabel(packageManager).toString(),
                    icon = info.loadIcon(packageManager),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    LaunchedEffect(Unit) {
        loadApps()
    }

    val filteredApps = remember(apps, query, refreshVersion) {
        apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            RefreshTopAppBar(
                query = query,
                onQueryChanged = { query = it },
                onBackPressed = onBackPressed,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                RefreshAppCard(
                    app = app,
                    selectedMode = refreshUtils.getStateForPackage(app.packageName),
                    onModeSelected = { mode ->
                        refreshUtils.writePackage(app.packageName, mode)
                        refreshVersion++
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshTopAppBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    var searching by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            if (searching) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.refresh_search)) },
                )
            } else {
                Text(stringResource(R.string.refresh_title))
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.garnet_navigate_back),
                )
            }
        },
        actions = {
            IconButton(onClick = { searching = !searching }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.refresh_search),
                )
            }
        },
    )
}

@Composable
private fun RefreshAppCard(
    app: RefreshApp,
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                bitmap = remember(app.packageName) { app.icon.toBitmap(48, 48).asImageBitmap() },
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RefreshModeMenu(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected,
            )
        }
    }
}

@Composable
private fun RefreshModeMenu(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
) {
    val modes = listOf(
        R.string.refresh_default,
        R.string.refresh_60,
        R.string.refresh_90,
        R.string.refresh_120,
        R.string.refresh_60_land,
        R.string.refresh_90_land,
        R.string.refresh_120_land,
    )
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Text(
                text = stringResource(modes[selectedMode]),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            modes.forEachIndexed { index, label ->
                DropdownMenuItem(
                    text = { Text(stringResource(label)) },
                    onClick = {
                        expanded = false
                        onModeSelected(index)
                    },
                )
            }
        }
    }
}
