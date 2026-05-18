
package com.example.manekelsa.presentation.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manekelsa.R
import com.example.manekelsa.domain.model.ServiceType
import com.example.manekelsa.presentation.common.ErrorContent
import com.example.manekelsa.presentation.common.LoadingContent
import com.example.manekelsa.presentation.common.ManeTopBar
import com.example.manekelsa.presentation.common.labelRes
import com.example.manekelsa.presentation.viewmodel.WorkerProfileEditViewModel
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun WorkerProfileEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: WorkerProfileEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            delay(1_200)
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            ManeTopBar(
                title = stringResource(R.string.worker_edit_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isSaved -> ProfileSavedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            uiState.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            uiState.hasError && !uiState.isSaving -> ErrorContent(
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> WorkerProfileForm(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                viewModel = viewModel,
                uiState = uiState,
            )
        }
    }
}

@Composable
private fun ProfileSavedContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.profile_saved_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_saved_body),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun WorkerProfileForm(
    modifier: Modifier,
    viewModel: WorkerProfileEditViewModel,
    uiState: com.example.manekelsa.presentation.viewmodel.WorkerProfileEditUiState,
) {
    val selectedServiceName = uiState.selectedServiceType
        ?.let { stringResource(it.labelRes()) }
        .orEmpty()
    val withExperienceTemplate = stringResource(R.string.generated_description_with_experience)
    val withoutExperienceTemplate = stringResource(R.string.generated_description_without_experience)
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoUrlChange(savePickedPhoto(context, uri))
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            viewModel.onPhotoUrlChange(uri.toString())
        }
        pendingCameraUri = null
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_name)) },
                singleLine = true,
                isError = uiState.nameError,
                supportingText = if (uiState.nameError) {
                    { Text(stringResource(R.string.validation_name_required)) }
                } else {
                    null
                },
            )
        }

        item {
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_phone_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = uiState.phoneError,
                supportingText = if (uiState.phoneError) {
                    { Text(stringResource(R.string.validation_phone_invalid)) }
                } else {
                    null
                },
            )
        }

        item {
            ServiceDropdown(
                selectedServiceType = uiState.selectedServiceType,
                hasError = uiState.serviceError,
                onServiceSelected = viewModel::onServiceSelected,
            )
        }

        item {
            OutlinedTextField(
                value = uiState.area,
                onValueChange = viewModel::onAreaChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_area)) },
                singleLine = true,
                isError = uiState.areaError,
                supportingText = if (uiState.areaError) {
                    { Text(stringResource(R.string.validation_area_required)) }
                } else {
                    null
                },
            )
        }

        item {
            OutlinedTextField(
                value = uiState.street,
                onValueChange = viewModel::onStreetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_street)) },
                singleLine = true,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.dailyRate,
                    onValueChange = viewModel::onDailyRateChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.label_daily_rate)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.dailyRateError,
                    supportingText = if (uiState.dailyRateError) {
                        { Text(stringResource(R.string.validation_rate_required)) }
                    } else {
                        null
                    },
                )
                OutlinedTextField(
                    value = uiState.twoHourRate,
                    onValueChange = viewModel::onTwoHourRateChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.label_two_hour_rate)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = uiState.twoHourRateError,
                    supportingText = if (uiState.twoHourRateError) {
                        { Text(stringResource(R.string.validation_rate_required)) }
                    } else {
                        null
                    },
                )
            }
        }

        item {
            OutlinedTextField(
                value = uiState.experienceYears,
                onValueChange = viewModel::onExperienceYearsChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_experience_years)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
        }

        item {
            PhotoPickerSection(
                photoUri = uiState.photoUrl,
                onChooseFromGallery = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                onTakePhoto = {
                    val uri = createProfilePhotoUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                },
            )
        }

        item {
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.label_description)) },
                minLines = 4,
            )
        }

        item {
            OutlinedButton(
                onClick = {
                    viewModel.generateDescription(
                        serviceName = selectedServiceName,
                        withExperienceTemplate = withExperienceTemplate,
                        withoutExperienceTemplate = withoutExperienceTemplate,
                    )
                },
                enabled = uiState.selectedServiceType != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.generate_description))
            }
        }

        item {
            if (uiState.hasError) {
                Text(
                    text = stringResource(R.string.generic_error_body),
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
                onClick = viewModel::saveProfile,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(imageVector = Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isSaving) {
                        stringResource(R.string.saving)
                    } else {
                        stringResource(R.string.save)
                    },
                )
            }
        }
    }
}

@Composable
private fun PhotoPickerSection(
    photoUri: String,
    onChooseFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.label_photo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfilePhotoPreview(photoUri = photoUri)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onChooseFromGallery,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.choose_from_gallery))
                }
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(imageVector = Icons.Rounded.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.take_photo))
                }
            }
        }
    }
}

@Composable
private fun ProfilePhotoPreview(photoUri: String) {
    Surface(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (photoUri.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = stringResource(R.string.content_desc_profile_photo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
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

private fun createProfilePhotoUri(context: Context): Uri {
    val photoDirectory = profilePhotoDirectory(context)
    val photoFile = File.createTempFile(
        "worker_profile_",
        ".jpg",
        photoDirectory,
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        photoFile,
    )
}

private fun savePickedPhoto(context: Context, sourceUri: Uri): String {
    return runCatching {
        val photoDirectory = profilePhotoDirectory(context)
        val photoFile = File.createTempFile(
            "worker_profile_",
            ".jpg",
            photoDirectory,
        )
        context.contentResolver.openInputStream(sourceUri).use { input ->
            photoFile.outputStream().use { output ->
                requireNotNull(input).copyTo(output)
            }
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile,
        ).toString()
    }.getOrElse {
        sourceUri.toString()
    }
}

private fun profilePhotoDirectory(context: Context): File {
    return File(context.filesDir, "profile_photos").apply {
        mkdirs()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceDropdown(
    selectedServiceType: ServiceType?,
    hasError: Boolean,
    onServiceSelected: (ServiceType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = selectedServiceType
        ?.let { stringResource(it.labelRes()) }
        .orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(stringResource(R.string.label_service)) },
            placeholder = { Text(stringResource(R.string.select_service)) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = hasError,
            supportingText = if (hasError) {
                { Text(stringResource(R.string.validation_service_required)) }
            } else {
                null
            },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ServiceType.entries.forEach { serviceType ->
                DropdownMenuItem(
                    text = { Text(stringResource(serviceType.labelRes())) },
                    onClick = {
                        onServiceSelected(serviceType)
                        expanded = false
                    },
                    trailingIcon = if (serviceType == selectedServiceType) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
