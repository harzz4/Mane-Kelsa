package com.example.manekelsa.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.AvailabilityStatus
import com.example.manekelsa.domain.model.WorkerProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_back),
                    )
                }
            }
        },
        actions = {
            if (onSettings != null) {
                IconButton(onClick = onSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.loading))
        }
    }
}

@Composable
fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.generic_error_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun EmptyContent(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun AvailabilityBadge(
    status: AvailabilityStatus,
    modifier: Modifier = Modifier,
) {
    val available = status == AvailabilityStatus.AVAILABLE_TODAY
    val container = if (available) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = Color.White,
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun WorkerCard(
    worker: WorkerProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top,
                ) {
                    WorkerPhotoThumbnail(photoUri = worker.photoUrl)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = worker.name.ifBlank { stringResource(R.string.worker_name_missing) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Work,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(worker.serviceType.labelRes()),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                AvailabilityBadge(status = worker.availabilityStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = worker.locationText(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(text = stringResource(R.string.daily_rate_value, worker.dailyRate))
                    },
                )
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(text = stringResource(R.string.two_hour_rate_value, worker.twoHourRate))
                    },
                )
                AssistChip(
                    onClick = onClick,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = {
                        Text(text = stringResource(R.string.rating_count_value, worker.thumbsUpCount))
                    },
                )
            }
        }
    }
}

@Composable
fun WorkerPhotoThumbnail(
    photoUri: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (photoUri.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = stringResource(R.string.content_desc_profile_photo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        } else {
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.content_desc_profile_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
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

@Composable
fun CallButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Call,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = stringResource(R.string.call))
    }
}
