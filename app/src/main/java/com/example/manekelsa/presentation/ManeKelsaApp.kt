package com.example.manekelsa.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.manekelsa.domain.model.AppLanguage
import com.example.manekelsa.presentation.navigation.AppNavGraph
import com.example.manekelsa.presentation.viewmodel.AppLanguageViewModel
import java.util.Locale

@Composable
fun ManeKelsaApp(
    languageViewModel: AppLanguageViewModel = hiltViewModel(),
) {
    val appLanguage by languageViewModel.appLanguage.collectAsStateWithLifecycle()

    LocalizedApp(language = appLanguage) {
        val navController = rememberNavController()
        AppNavGraph(navController = navController)
    }
}

@Composable
private fun LocalizedApp(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val currentContext = LocalContext.current
    val activity = remember(currentContext) { currentContext.findActivity() }
    val baseContext = activity ?: currentContext

    val locale = remember(language) {
        Locale.forLanguageTag(language.localeTag)
    }

    val localizedContext = remember(baseContext, locale) {
        LocalizedActivityContextWrapper(
            base = baseContext,
            locale = locale,
        )
    }

    val localizedConfiguration = localizedContext.resources.configuration

    SideEffect {
        Locale.setDefault(locale)
    }

    /*
     * IMPORTANT:
     * We provide a ContextWrapper whose base is still the Activity.
     * This makes stringResource() read the selected locale immediately,
     * while hiltViewModel() can still unwrap LocalContext and find the Activity.
     */
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        key(language.storageCode) {
            content()
        }
    }
}

private class LocalizedActivityContextWrapper(
    base: Context,
    locale: Locale,
) : ContextWrapper(base) {
    private val localizedContext: Context = base.createConfigurationContext(
        Configuration(base.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
            setLocales(LocaleList(locale))
        },
    )

    override fun getAssets(): AssetManager {
        return localizedContext.assets
    }

    override fun getResources(): Resources {
        return localizedContext.resources
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
