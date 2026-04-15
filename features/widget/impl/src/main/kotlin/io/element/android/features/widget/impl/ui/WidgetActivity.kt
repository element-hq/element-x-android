/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.widget.impl.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.webkit.PermissionRequest
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.IntentCompat
import dev.zacsweers.metro.Inject
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.widget.api.WidgetActivityData
import io.element.android.features.widget.impl.DefaultWidgetEntryPoint
import io.element.android.features.widget.impl.di.WidgetBindings
import io.element.android.libraries.androidutils.browser.ConsoleMessageLogger
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.ElementThemeApp
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import timber.log.Timber

private val loggerTag = LoggerTag("WidgetActivity")

class WidgetActivity :
    AppCompatActivity(),
    WidgetScreenNavigator {
    @Inject lateinit var presenterFactory: WidgetScreenPresenter.Factory
    @Inject lateinit var appPreferencesStore: AppPreferencesStore
    @Inject lateinit var enterpriseService: EnterpriseService
    @Inject lateinit var buildMeta: BuildMeta
    @Inject lateinit var consoleMessageLogger: ConsoleMessageLogger

    private lateinit var presenter: Presenter<WidgetScreenState>

    private var requestPermissionCallback: RequestPermissionCallback? = null

    private val requestPermissionsLauncher = registerPermissionResultLauncher()

    private val webViewTarget = mutableStateOf<WidgetActivityData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindings<WidgetBindings>().inject(this)

//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setWidgetType(intent)
        // If presenter is not created at this point, it means we have no widget to display, the Activity is finishing, so return early
        if (!::presenter.isInitialized) {
            return
        }

        Timber.d("Created WidgetActivity with widget type: ${webViewTarget.value}")

        setContent {
            val colors by remember(webViewTarget.value?.getSessionId()) {
                enterpriseService.semanticColorsFlow(sessionId = webViewTarget.value?.getSessionId())
            }.collectAsState(SemanticColorsLightDark.default)
            ElementThemeApp(
                appPreferencesStore = appPreferencesStore,
                compoundLight = colors.light,
                compoundDark = colors.dark,
                buildMeta = buildMeta,
            ) {
                val state = presenter.present()
                WidgetScreenView(
                    state = state,
                    onConsoleMessage = {
                        consoleMessageLogger.log("Widget", it)
                    },
                    requestPermissions = { permissions, callback ->
                        requestPermissionCallback = callback
                        requestPermissionsLauncher.launch(permissions)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setWidgetType(intent)
    }

    override fun finish() {
        // Also remove the task from recents
        finishAndRemoveTask()
    }

    override fun close() {
        finish()
    }

    private fun setWidgetType(intent: Intent?) {
        val widgetType = intent?.let {
            IntentCompat.getParcelableExtra(intent, DefaultWidgetEntryPoint.EXTRA_WIDGET_TYPE, WidgetActivityData::class.java)
        }
        val currentWidgetType = webViewTarget.value
        if (currentWidgetType == null) {
            if (widgetType == null) {
                Timber.tag(loggerTag.value).d("Re-opened the activity but we have no url to load or a cached one, finish the activity")
                finish()
            } else {
                Timber.tag(loggerTag.value).d("Set the widget type and create the presenter")
                webViewTarget.value = widgetType
                presenter = presenterFactory.create(widgetType, this)
            }
        } else {
            if (widgetType == null) {
                Timber.tag(loggerTag.value).d("Coming back from notification, do nothing")
            } else if (widgetType != currentWidgetType) {
                Timber.tag(loggerTag.value).d("User starts another widget, restart the Activity")
                setIntent(intent)
                recreate()
            } else {
                Timber.tag(loggerTag.value).d("Starting the same widget again, do nothing")
            }
        }
    }

    private fun registerPermissionResultLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val callback = requestPermissionCallback ?: return@registerForActivityResult
            val permissionsToGrant = mutableListOf<String>()
            permissions.forEach { (permission, granted) ->
                if (granted) {
                    val webKitPermission = when (permission) {
                        Manifest.permission.CAMERA -> PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        Manifest.permission.RECORD_AUDIO -> PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        else -> return@forEach
                    }
                    permissionsToGrant.add(webKitPermission)
                }
            }
            callback(permissionsToGrant.toTypedArray())
            requestPermissionCallback = null
        }
    }
}

