package com.example.manekelsa.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.domain.model.UserRole
import com.example.manekelsa.presentation.viewmodel.RoleSelectionViewModel

@Composable
fun RoleSelectionScreen(
    onWorkerSelected: () -> Unit,
    onResidentSelected: () -> Unit,
    viewModel: RoleSelectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedRole) {
        when (uiState.savedRole) {
            UserRole.WORKER -> onWorkerSelected()
            UserRole.RESIDENT -> onResidentSelected()
            null -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.role_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.role_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleLanguageButtons(
                selectedLanguage = uiState.appLanguage,
                onLanguageSelected = viewModel::changeLanguage,
            )
            Spacer(modifier = Modifier.height(28.dp))

            RoleCard(
                title = stringResource(R.string.role_worker_title),
                body = stringResource(R.string.role_worker_desc),
                icon = Icons.Rounded.Work,
                enabled = !uiState.isSaving,
                onClick = { viewModel.selectRole(UserRole.WORKER) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoleCard(
                title = stringResource(R.string.role_resident_title),
                body = stringResource(R.string.role_resident_desc),
                icon = Icons.Rounded.Home,
                enabled = !uiState.isSaving,
                onClick = { viewModel.selectRole(UserRole.RESIDENT) },
            )

            if (uiState.hasError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.role_save_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun RoleLanguageButtons(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoleLanguageButton(
            text = stringResource(R.string.language_kannada),
            selected = selectedLanguage == AppLanguage.KANNADA,
            onClick = { onLanguageSelected(AppLanguage.KANNADA) },
            modifier = Modifier.weight(1f),
        )
        RoleLanguageButton(
            text = stringResource(R.string.language_english),
            selected = selectedLanguage == AppLanguage.ENGLISH,
            onClick = { onLanguageSelected(AppLanguage.ENGLISH) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RoleLanguageButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    body: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
