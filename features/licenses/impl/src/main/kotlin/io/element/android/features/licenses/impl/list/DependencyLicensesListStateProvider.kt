/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.features.licenses.impl.model.License
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class DependencyLicensesListStateProvider : PreviewParameterProvider<DependencyLicensesListState> {
    override val values: Sequence<DependencyLicensesListState>
        get() = sequenceOf(
            aDependencyLicensesListState(
                licenses = AsyncData.Loading()
            ),
            aDependencyLicensesListState(
                licenses = AsyncData.Failure(Exception("Failed to load licenses"))
            ),
            aDependencyLicensesListState(
                licenses = AsyncData.Success(
                    persistentListOf(
                        aDependencyLicenseItem(),
                        aDependencyLicenseItem(name = null),
                    )
                )
            ),
            aDependencyLicensesListState(
                licenses = AsyncData.Success(
                    persistentListOf(
                        aDependencyLicenseItem(),
                        aDependencyLicenseItem(name = null),
                    )
                ),
                filter = "a filter",
            ),
        )
}

private fun aDependencyLicensesListState(
    licenses: AsyncData<ImmutableList<DependencyLicenseItem>>,
    filter: String = "",
): DependencyLicensesListState {
    return DependencyLicensesListState(
        licenses = licenses,
        filter = filter,
        eventSink = {},
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
