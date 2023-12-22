/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.login.impl.accountprovider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.login.impl.R
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoundedIconAtomSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * https://www.figma.com/file/o9p34zmiuEpZRyvZXJZAYL/FTUE?type=design&node-id=604-60817
 */
@Composable
fun AccountProviderView(
    item: AccountProvider,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isMatrixOrg) {
                    RoundedIconAtom(
                        size = RoundedIconAtomSize.Medium,
                        resourceId = R.drawable.ic_matrix,
                        tint = Color.Unspecified,
                    )
                } else {
                    RoundedIconAtom(
                        size = RoundedIconAtomSize.Medium,
                        imageVector = CompoundIcons.Search,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    text = item.title,
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (item.isPublic) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(16.dp),
                        resourceId = R.drawable.ic_public,
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                }
            }
            if (item.subtitle != null) {
                Text(
                    modifier = Modifier
                        .padding(start = 46.dp, bottom = 12.dp, end = 26.dp),
                    text = item.subtitle,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AccountProviderViewPreview(@PreviewParameter(AccountProviderProvider::class) item: AccountProvider) = ElementPreview {
    AccountProviderView(
        item = item,
        onClick = { }
    )
}
