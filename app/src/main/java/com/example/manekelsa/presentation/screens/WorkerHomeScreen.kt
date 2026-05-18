package com.example.manekelsa.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manekelsa.R
import com.example.manekelsa.presentation.common.EmptyContent
import com.example.manekelsa.presentation.common.ErrorContent
import com.example.manekelsa.presentation.common.LoadingContent
import com.example.manekelsa.presentation.common.ManeTopBar
import com.example.manekelsa.presentation.common.WorkerCard
import com.example.manekelsa.presentation.common.labelRes
import com.example.manekelsa.presentation.viewmodel.WorkerHomeViewModel

@Composable
fun WorkerHomeScreen(
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: WorkerHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ManeTopBar(
                title = stringResource(R.string.worker_home_title),
                onSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            uiState.hasError -> ErrorContent(
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> WorkerHomeContent(
                contentPadding = innerPadding,
                viewModel = viewModel,
                uiState = uiState,
                onEditProfile = onEditProfile,
            )
        }
    }
}

@Composable
private fun WorkerHomeContent(
    contentPadding: PaddingValues,
    viewModel: WorkerHomeViewModel,
    uiState: com.example.manekelsa.presentation.viewmodel.WorkerHomeUiState,
    onEditProfile: () -> Unit,
) {
    val profile = uiState.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.profile_summary_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (profile == null) {
                    Text(
                        text = stringResource(R.string.profile_not_created_body),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(profile.serviceType.labelRes()),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = profile.area,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        AvailabilityToggleCard(
            checked = profile?.isAvailableToday ?: false,
            enabled = profile != null && !uiState.isUpdatingAvailability,
            onCheckedChange = viewModel::updateAvailability,
        )

        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.edit_profile))
        }

        Text(
            text = stringResource(R.string.preview_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        if (profile == null) {
            EmptyContent(
                title = stringResource(R.string.profile_not_created_title),
                body = stringResource(R.string.worker_preview_empty),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            WorkerCard(
                worker = profile,
                onClick = onEditProfile,
            )
        }
    }
}

@Composable
private fun AvailabilityToggleCard(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.availability_toggle_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (checked) {
                        stringResource(R.string.available_today)
                    } else {
                        stringResource(R.string.not_available)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (checked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
