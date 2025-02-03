/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    placeHolderTitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showBackButton: Boolean = true,
    resultState: SearchBarResultState<T> = SearchBarResultState.Initial(),
    shape: Shape = SearchBarDefaults.inputFieldShape,
    tonalElevation: Dp = SearchBarDefaults.TonalElevation,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    inactiveBarColors: SearchBarColors = ElementSearchBarDefaults.inactiveColors(),
    activeBarColors: SearchBarColors = ElementSearchBarDefaults.activeColors(),
    inactiveTextInputColors: TextFieldColors = ElementSearchBarDefaults.inactiveInputFieldColors(),
    activeTextInputColors: TextFieldColors = ElementSearchBarDefaults.activeInputFieldColors(),
    contentPrefix: @Composable ColumnScope.() -> Unit = {},
    contentSuffix: @Composable ColumnScope.() -> Unit = {},
    resultHandler: @Composable ColumnScope.(T) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    if (!active) {
        onQueryChange("")
        focusManager.clearFocus()
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { focusManager.clearFocus() },
                expanded = active,
                onExpandedChange = onActiveChange,
                enabled = enabled,
                placeholder = {
                    Text(text = placeHolderTitle)
                },
                leadingIcon = if (showBackButton && active) {
                    { BackButton(onClick = { onActiveChange(false) }) }
                } else {
                    null
                },
                trailingIcon = when {
                    active && query.isNotEmpty() -> {
                        {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = CompoundIcons.Close(),
                                    contentDescription = stringResource(CommonStrings.action_clear),
                                )
                            }
                        }
                    }

                    !active -> {
                        {
                            Icon(
                                imageVector = CompoundIcons.Search(),
                                contentDescription = stringResource(CommonStrings.action_search),
                                tint = ElementTheme.colors.iconTertiary,
                            )
                        }
                    }

                    else -> null
                },
                interactionSource = interactionSource,
                colors = if (active) activeTextInputColors else inactiveTextInputColors,
            )
        },
        expanded = active,
        onExpandedChange = onActiveChange,
        modifier = modifier.padding(horizontal = if (!active) 16.dp else 0.dp),
        shape = shape,
        colors = if (active) activeBarColors else inactiveBarColors,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
        content = {
            contentPrefix()
            when (resultState) {
                is SearchBarResultState.Results<T> -> {
                    resultHandler(resultState.results)
                }

                is SearchBarResultState.NoResultsFound<T> -> {
                    // No results found, show a message
                    Spacer(Modifier.size(80.dp))

                    Text(
                        text = stringResource(CommonStrings.common_no_results),
                        textAlign = TextAlign.Center,
                        color = ElementTheme.colors.textSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    // Not searching - nothing to show.
                }
            }
            contentSuffix()
        },
    )
}

object ElementSearchBarDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun inactiveColors() = SearchBarDefaults.colors(
        containerColor = ElementTheme.materialColors.surfaceVariant,
        dividerColor = ElementTheme.materialColors.outline,
    )

    @Composable
    fun inactiveInputFieldColors() = TextFieldDefaults.colors(
        unfocusedPlaceholderColor = ElementTheme.colors.textDisabled,
        focusedPlaceholderColor = ElementTheme.colors.textDisabled,
        unfocusedLeadingIconColor = ElementTheme.materialColors.primary,
        focusedLeadingIconColor = ElementTheme.materialColors.primary,
        unfocusedTrailingIconColor = ElementTheme.materialColors.primary,
        focusedTrailingIconColor = ElementTheme.materialColors.primary,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun activeColors() = SearchBarDefaults.colors(
        containerColor = Color.Transparent,
        dividerColor = ElementTheme.materialColors.outline,
    )

    @Composable
    fun activeInputFieldColors() = TextFieldDefaults.colors(
        unfocusedPlaceholderColor = ElementTheme.colors.textDisabled,
        focusedPlaceholderColor = ElementTheme.colors.textDisabled,
        unfocusedLeadingIconColor = ElementTheme.materialColors.primary,
        focusedLeadingIconColor = ElementTheme.materialColors.primary,
        unfocusedTrailingIconColor = ElementTheme.materialColors.primary,
        focusedTrailingIconColor = ElementTheme.materialColors.primary,
    )
}

@Immutable
sealed interface SearchBarResultState<in T> {
    /** No search results are available yet (e.g. because the user hasn't entered a search term). */
    class Initial<T> : SearchBarResultState<T>

    /** The search has completed, but no results were found. */
    class NoResultsFound<T> : SearchBarResultState<T>

    /** The search has completed, and some matching users were found. */
    data class Results<T>(val results: T) : SearchBarResultState<T>
}

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarInactivePreview() = ElementThemedPreview { ContentToPreview() }

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarActiveNoneQueryPreview() = ElementThemedPreview {
    ContentToPreview(
        query = "",
        active = true,
    )
}

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarActiveWithQueryPreview() = ElementThemedPreview {
    ContentToPreview(
        query = "search term",
        active = true,
    )
}

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarActiveWithQueryNoBackButtonPreview() = ElementThemedPreview {
    ContentToPreview(
        query = "search term",
        active = true,
        showBackButton = false,
    )
}

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarActiveWithNoResultsPreview() = ElementThemedPreview {
    ContentToPreview(
        query = "search term",
        active = true,
        resultState = SearchBarResultState.NoResultsFound<String>(),
    )
}

@Preview(group = PreviewGroup.Search)
@Composable
internal fun SearchBarActiveWithContentPreview() = ElementThemedPreview {
    ContentToPreview(
        query = "search term",
        active = true,
        resultState = SearchBarResultState.Results("result!"),
        contentPrefix = {
            Text(
                text = "Content that goes before the search results",
                modifier = Modifier
                    .background(color = Color.Red)
                    .fillMaxWidth()
            )
        },
        contentSuffix = {
            Text(
                text = "Content that goes after the search results",
                modifier = Modifier
                    .background(color = Color.Blue)
                    .fillMaxWidth()
            )
        }
    ) {
        Text(
            text = "Results go here",
            modifier = Modifier
                .background(color = Color.Green)
                .fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@ExcludeFromCoverage
private fun ContentToPreview(
    query: String = "",
    active: Boolean = false,
    showBackButton: Boolean = true,
    resultState: SearchBarResultState<String> = SearchBarResultState.Initial(),
    contentPrefix: @Composable ColumnScope.() -> Unit = {},
    contentSuffix: @Composable ColumnScope.() -> Unit = {},
    resultHandler: @Composable ColumnScope.(String) -> Unit = {},
) {
    SearchBar(
        query = query,
        active = active,
        resultState = resultState,
        showBackButton = showBackButton,
        onQueryChange = {},
        onActiveChange = {},
        placeHolderTitle = "Search for things",
        contentPrefix = contentPrefix,
        contentSuffix = contentSuffix,
        resultHandler = resultHandler,
    )
}
