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

package io.element.android.features.licenses.impl.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.features.licenses.impl.model.License
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.persistentListOf

open class DependencyLicensesListStateProvider : PreviewParameterProvider<DependencyLicensesListState> {
    override val values: Sequence<DependencyLicensesListState>
        get() = sequenceOf(
            DependencyLicensesListState(
                licenses = AsyncData.Loading()
            ),
            DependencyLicensesListState(
                licenses = AsyncData.Failure(Exception("Failed to load licenses"))
            ),
            DependencyLicensesListState(
                licenses = AsyncData.Success(
                    persistentListOf(
                        aDependencyLicenseItem(),
                        aDependencyLicenseItem(name = null),
                    )
                )
            )
        )
}

internal fun aDependencyLicenseItem(
    name: String? = "A dependency",
) = DependencyLicenseItem(
    groupId = "org.some.group",
    artifactId = "a-dependency",
    version = "1.0.0",
    name = name,
    licenses = listOf(
        License(
            identifier = "Apache 2.0",
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    unknownLicenses = listOf(),
    scm = null,
)
