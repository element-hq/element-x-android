/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.x.R

class PermissionRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ElementTheme {
                PermissionRationaleView(onBackClick = ::finish)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PermissionRationaleView(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_permission_rationale_title)) },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = stringResource(R.string.screen_permission_rationale_subtitle),
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
            HorizontalDivider()
            for (reason in permissionReasons) {
                ListItem(
                    leadingContent = ListItemContent.Icon(IconSource.Vector(reason.icon)),
                    content = { Text(stringResource(reason.titleId)) },
                    supportingContent = { Text(stringResource(reason.descriptionId)) },
                )
            }
        }
    }
}

private data class PermissionReason(
    val icon: ImageVector,
    val titleId: Int,
    val descriptionId: Int,
)

private val permissionReasons
    @Composable
    get() = listOf(
        PermissionReason(
            icon = CompoundIcons.TakePhoto(),
            titleId = R.string.screen_permission_rationale_camera_title,
            descriptionId = R.string.screen_permission_rationale_camera_description,
        ),
        PermissionReason(
            icon = CompoundIcons.MicOn(),
            titleId = R.string.screen_permission_rationale_microphone_title,
            descriptionId = R.string.screen_permission_rationale_microphone_description,
        ),
        PermissionReason(
            icon = CompoundIcons.LocationPin(),
            titleId = R.string.screen_permission_rationale_location_title,
            descriptionId = R.string.screen_permission_rationale_location_description,
        ),
        PermissionReason(
            icon = CompoundIcons.Notifications(),
            titleId = R.string.screen_permission_rationale_notifications_title,
            descriptionId = R.string.screen_permission_rationale_notifications_description,
        ),
    )

@PreviewsDayNight
@Composable
internal fun PermissionRationaleViewPreview() = ElementPreview {
    PermissionRationaleView(onBackClick = {})
}
