package com.example.manekelsa.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.presentation.common.EmptyContent
import com.example.manekelsa.presentation.common.ErrorContent
import com.example.manekelsa.presentation.common.LoadingContent
import com.example.manekelsa.presentation.common.ManeTopBar
import com.example.manekelsa.presentation.common.WorkerCard
import com.example.manekelsa.presentation.common.labelRes
import com.example.manekelsa.presentation.viewmodel.ResidentHomeViewModel

@Composable
fun ResidentHomeScreen(
    onWorkerClick: (String) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ResidentHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ManeTopBar(
                title = stringResource(R.string.resident_home_title),
                onSettings = onOpenSettings,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ResidentFilters(
                areaQuery = uiState.areaQuery,
                availableOnly = uiState.availableOnly,
                selectedServiceType = uiState.selectedServiceType,
                onAreaQueryChange = viewModel::onAreaQueryChange,
                onAvailableOnlyChange = viewModel::onAvailableOnlyChange,
                onServiceSelected = viewModel::onServiceSelected,
            )

            when {
                uiState.isLoading -> LoadingContent(modifier = Modifier.fillMaxSize())
                uiState.hasError -> ErrorContent(
                    onRetry = viewModel::retry,
                    modifier = Modifier.fillMaxSize(),
                )
                uiState.workers.isEmpty() -> EmptyContent(
                    title = stringResource(R.string.empty_workers_title),
                    body = stringResource(R.string.empty_workers_body),
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.workers,
                        key = { worker -> worker.id },
                    ) { worker ->
                        WorkerCard(
                            worker = worker,
                            onClick = { onWorkerClick(worker.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResidentFilters(
    areaQuery: String,
    availableOnly: Boolean,
    selectedServiceType: ServiceType?,
    onAreaQueryChange: (String) -> Unit,
    onAvailableOnlyChange: (Boolean) -> Unit,
    onServiceSelected: (ServiceType?) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = areaQuery,
            onValueChange = onAreaQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.area_search_label)) },
            placeholder = { Text(stringResource(R.string.area_search_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                )
            },
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = availableOnly,
                    onClick = { onAvailableOnlyChange(!availableOnly) },
                    label = { Text(stringResource(R.string.available_today)) },
                )
            }
            item {
                FilterChip(
                    selected = selectedServiceType == null,
                    onClick = { onServiceSelected(null) },
                    label = { Text(stringResource(R.string.filter_all_services)) },
                )
            }
            items(ServiceType.entries) { serviceType ->
                FilterChip(
                    selected = selectedServiceType == serviceType,
                    onClick = { onServiceSelected(serviceType) },
                    label = { Text(stringResource(serviceType.labelRes())) },
                )
            }
        }
    }
}
