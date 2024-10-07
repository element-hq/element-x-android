/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.licenses.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.licenses.impl.LicensesProvider
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject

class DependencyLicensesListPresenter @Inject constructor(
    private val licensesProvider: LicensesProvider,
) : Presenter<DependencyLicensesListState> {
    @Composable
    override fun present(): DependencyLicensesListState {
        var licenses by remember {
            mutableStateOf<AsyncData<ImmutableList<DependencyLicenseItem>>>(AsyncData.Loading())
        }
        LaunchedEffect(Unit) {
            runCatching {
                licenses = AsyncData.Success(licensesProvider.provides().toPersistentList())
            }.onFailure {
                licenses = AsyncData.Failure(it)
            }
        }
        return DependencyLicensesListState(licenses = licenses)
    }
}
