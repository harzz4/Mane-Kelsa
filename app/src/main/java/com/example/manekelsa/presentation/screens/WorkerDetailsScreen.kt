package com.example.manekelsa.presentation.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.WorkerProfile
import com.example.manekelsa.presentation.common.AvailabilityBadge
import com.example.manekelsa.presentation.common.CallButton
import com.example.manekelsa.presentation.common.EmptyContent
import com.example.manekelsa.presentation.common.ErrorContent
import com.example.manekelsa.presentation.common.LoadingContent
import com.example.manekelsa.presentation.common.ManeTopBar
import com.example.manekelsa.presentation.common.WorkerPhotoThumbnail
import com.example.manekelsa.presentation.common.labelRes
import com.example.manekelsa.presentation.viewmodel.WorkerDetailsViewModel

@Composable
fun WorkerDetailsScreen(
    onBack: () -> Unit,
    viewModel: WorkerDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ManeTopBar(
                title = stringResource(R.string.details_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            uiState.hasError && uiState.profile == null -> ErrorContent(
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            uiState.profile == null -> EmptyContent(
                title = stringResource(R.string.worker_not_found_title),
                body = stringResource(R.string.worker_not_found_body),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> WorkerDetailsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                profile = requireNotNull(uiState.profile),
                canCall = uiState.canCall,
                dialPhoneNumber = uiState.dialPhoneNumber,
                hasRated = uiState.hasRated,
                isGivingThumbsUp = uiState.isGivingThumbsUp,
                onGiveThumbsUp = viewModel::giveThumbsUp,
            )
        }
    }
}

@Composable
private fun WorkerDetailsContent(
    modifier: Modifier,
    profile: WorkerProfile,
    canCall: Boolean,
    dialPhoneNumber: String?,
    hasRated: Boolean,
    isGivingThumbsUp: Boolean,
    onGiveThumbsUp: () -> Unit,
) {
    val context = LocalContext.current
    val callUnavailableMessage = stringResource(R.string.call_unavailable_message)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        WorkerPhotoThumbnail(photoUri = profile.photoUrl)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(profile.serviceType.labelRes()),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    AvailabilityBadge(status = profile.availabilityStatus)
                }

                DetailRow(
                    label = stringResource(R.string.area),
                    value = profile.locationText(),
                )
                DetailRow(
                    label = stringResource(R.string.phone),
                    value = profile.phoneNumber,
                )
                DetailRow(
                    label = stringResource(R.string.daily_rate),
                    value = stringResource(R.string.money_value, profile.dailyRate),
                )
                DetailRow(
                    label = stringResource(R.string.two_hour_rate),
                    value = stringResource(R.string.money_value, profile.twoHourRate),
                )
                DetailRow(
                    label = stringResource(R.string.experience),
                    value = if (profile.experienceYears > 0) {
                        stringResource(R.string.years_value, profile.experienceYears)
                    } else {
                        stringResource(R.string.experience_not_added)
                    },
                )
                DetailRow(
                    label = stringResource(R.string.rating),
                    value = stringResource(R.string.rating_count_value, profile.thumbsUpCount),
                )
            }
        }

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.label_description),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.description.ifBlank { stringResource(R.string.no_description) },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        CallButton(
            enabled = canCall,
            onClick = {
                val phone = dialPhoneNumber
                if (phone == null) {
                    Toast.makeText(context, callUnavailableMessage, Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:${Uri.encode(phone)}"),
                    )
                    runCatching { context.startActivity(intent) }
                        .onFailure {
                            Toast.makeText(context, callUnavailableMessage, Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = onGiveThumbsUp,
            enabled = !hasRated && !isGivingThumbsUp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(imageVector = Icons.Rounded.ThumbUp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasRated) {
                    stringResource(R.string.already_liked)
                } else {
                    stringResource(R.string.thumbs_up)
                },
            )
        }

        if (!canCall) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.call_disabled_reason),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun WorkerProfile.locationText(): String {
    val safeArea = area.ifBlank { stringResource(R.string.area_not_added) }
    val safeStreet = street.orEmpty()
    return if (safeStreet.isBlank()) {
        safeArea
    } else {
        stringResource(R.string.street_area_value, safeStreet, safeArea)
    }
}
