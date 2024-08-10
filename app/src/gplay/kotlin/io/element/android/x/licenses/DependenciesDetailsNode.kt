/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.x.licenses

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class DependenciesDetailsNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(
    buildContext = buildContext,
    plugins = plugins
) {
    data class Inputs(
        val licenseItem: DependencyLicenseItem,
    ) : NodeInputs

    private val licenseItem = inputs<Inputs>().licenseItem

    @Composable
    override fun View(modifier: Modifier) {
        LicenseItemDetails(
            modifier = modifier,
            licenseItem = licenseItem,
            onBack = ::navigateUp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseItemDetails(
    licenseItem: DependencyLicenseItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = licenseItem.name!!) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            val licenses = licenseItem.licenses.orEmpty() + licenseItem.unknownLicenses.orEmpty()
            for (license in licenses) {
                val text = buildString {
                    if (license.name != null) {
                        append(license.name)
                        append("\n")
                        append("\n")
                    }
                    if (license.url != null) {
                        append(license.url)
                    }
                }
                ClickableLinkText(text = text, interactionSource = remember { MutableInteractionSource() })
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun LicenseItemDetailsPreview() {
    ElementPreview {
        LicenseItemDetails(
            licenseItem = DependencyLicenseItem(
                groupId = "org.some.group",
                artifactId = "fake-dependency",
                version = "1.0.0",
                name = "Fake dependency",
                licenses = listOf(
                    License(
                        identifier = "Apache 2.0",
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    )
                ),
                unknownLicenses = listOf(),
                scm = null,
            ),
            onBack = {}
        )
    }
}
