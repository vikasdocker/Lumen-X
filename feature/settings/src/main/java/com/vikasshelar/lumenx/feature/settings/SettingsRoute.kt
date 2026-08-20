package com.vikasshelar.lumenx.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vikasshelar.lumenx.core.designsystem.GlassSurface
import com.vikasshelar.lumenx.core.designsystem.LumenBackground
import com.vikasshelar.lumenx.core.designsystem.LumenColors

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onBackClick = onBackClick,
        onHapticsChange = viewModel::setHapticsEnabled,
        onSoundChange = viewModel::setSoundEnabled,
        onThemeChange = viewModel::setTheme,
    )
}

@Composable
fun SettingsScreen(
    state: UserSettings,
    onBackClick: () -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
) {
    LumenBackground(isIlluminated = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = LumenColors.Mist,
                    )
                }
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LumenColors.Mist,
                )
            }

            SettingsToggleRow(
                title = "Haptic feedback",
                subtitle = "Physical response on power, mode, and timer actions.",
                checked = state.hapticsEnabled,
                onCheckedChange = onHapticsChange,
            )

            SettingsToggleRow(
                title = "Sound feedback",
                subtitle = "Short system tones for power and state changes.",
                checked = state.soundEnabled,
                onCheckedChange = onSoundChange,
            )

            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = LumenColors.Mist,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppTheme.entries.forEach { theme ->
                            TextButton(
                                onClick = { onThemeChange(theme) },
                            ) {
                                Text(
                                    text = theme.label,
                                    color = if (state.theme == theme) LumenColors.Beam else LumenColors.Steel,
                                    fontWeight = if (state.theme == theme) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }

            GlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Hardware policy",
                        style = MaterialTheme.typography.titleMedium,
                        color = LumenColors.Mist,
                    )
                    Text(
                        text = "Intensity is enabled only when Android exposes real LED strength levels. Screen brightness is never used as a flashlight substitute.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LumenColors.Steel,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    GlassSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LumenColors.Mist,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LumenColors.Steel,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
