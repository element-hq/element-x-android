/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * A flow layout for reactions that will show a collapse/expand button when the layout wraps over a defined number of rows.
 * It displays an add more button when there are greater than 0 reactions and always displays the reaction and add more button
 * on the same row (moving them both to a new row if necessary).
 * @param expandButton The expand button
 * @param addMoreButton The add more button
 * @param modifier The modifier to apply to this layout
 * @param itemSpacing The horizontal spacing between items
 * @param rowSpacing The vertical spacing between rows
 * @param expanded Whether the layout should display in expanded or collapsed state
 * @param rowsBeforeCollapsible The number of rows before the collapse/expand button is shown
 * @param reactions The reaction buttons
 */
@Composable
fun TimelineItemReactionsLayout(
    expandButton: @Composable () -> Unit,
    addMoreButton: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    rowSpacing: Dp = 0.dp,
    expanded: Boolean = false,
    rowsBeforeCollapsible: Int? = 2,
    reactions: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        // Given the placeables and returns a structure representing
        // how they should wrap on to multiple rows given the constraints max width.
        fun calculateRows(placeables: List<Placeable>): List<List<Placeable>> {
            val rows = mutableListOf<List<Placeable>>()
            var currentRow = mutableListOf<Placeable>()
            var rowX = 0

            placeables.forEach { placeable ->
                val horizontalSpacing = if (currentRow.isEmpty()) 0 else itemSpacing.toPx().toInt()
                // If the current view does not fit on this row bump to the next
                if (rowX + placeable.width > constraints.maxWidth) {
                    rows.add(currentRow)
                    currentRow = mutableListOf()
                    rowX = 0
                }
                rowX += horizontalSpacing + placeable.width
                currentRow.add(placeable)
            }
            // If there are items in the current row remember to append it to the returned value
            if (currentRow.size > 0) {
                rows.add(currentRow)
            }
            return rows
        }

        // Used to render the collapsed state, this takes the rows inputted and adds the extra button to the last row,
        // removing only as many trailing reactions as needed to make space for it.
        fun replaceTrailingItemsWithButtons(rowsIn: List<List<Placeable>>, expandButton: Placeable, addMoreButton: Placeable?): List<List<Placeable>> {
            val rows = rowsIn.toMutableList()
            val lastRow = rows.last()
            val buttonsWidth = expandButton.width + itemSpacing.toPx().toInt() + (addMoreButton?.width ?: 0)
            var rowX = 0
            lastRow.forEachIndexed { i, placeable ->
                val horizontalSpacing = if (i == 0) 0 else itemSpacing.toPx().toInt()
                rowX += placeable.width + horizontalSpacing
                if (rowX > constraints.maxWidth - (buttonsWidth + horizontalSpacing)) {
                    val lastRowWithButton = lastRow.take(i) + listOfNotNull(expandButton, addMoreButton)
                    rows[rows.size - 1] = lastRowWithButton
                    return rows
                }
            }
            val lastRowWithButton = lastRow + listOfNotNull(expandButton, addMoreButton)
            rows[rows.size - 1] = lastRowWithButton
            return rows
        }

        // To prevent the add more and expand buttons from wrapping on to separate lines.
        // If there is one item on the last line, it moves the expand button down.
        fun ensureCollapseAndAddMoreButtonsAreOnTheSameRow(rowsIn: List<List<Placeable>>): List<List<Placeable>> {
            val lastRow = rowsIn.last().toMutableList()
            if (lastRow.size != 1) {
                return rowsIn
            }
            val rows = rowsIn.toMutableList()
            val secondLastRow = rows[rows.size - 2].toMutableList()
            val expandButtonPlaceable = secondLastRow.removeAt(secondLastRow.lastIndex)
            lastRow.add(0, expandButtonPlaceable)
            rows[rows.size - 2] = secondLastRow
            rows[rows.size - 1] = lastRow
            return rows
        }

        // Given a list of rows place them in the layout.
        fun layoutRows(rows: List<List<Placeable>>): MeasureResult {
            var width = 0
            var height = 0
            val placeables = rows.mapIndexed { i, row ->
                var rowX = 0
                var rowHeight = 0
                val verticalSpacing = if (i == 0) 0 else rowSpacing.toPx().toInt()
                val rowWithPoints = row.mapIndexed { j, placeable ->
                    val horizontalSpacing = if (j == 0) 0 else itemSpacing.toPx().toInt()
                    val point = IntOffset(rowX + horizontalSpacing, height + verticalSpacing)
                    rowX += placeable.width + horizontalSpacing
                    rowHeight = maxOf(rowHeight, placeable.height)
                    Pair(placeable, point)
                }
                height += rowHeight + verticalSpacing
                width = maxOf(width, rowX)
                rowWithPoints
            }.flatten()

            return layout(width = width, height = height) {
                placeables.forEach {
                    val (placeable, origin) = it
                    placeable.placeRelative(origin.x, origin.y)
                }
            }
        }

        var reactionsPlaceables = subcompose(0, reactions).map { it.measure(constraints) }
        if (reactionsPlaceables.isEmpty()) {
            return@SubcomposeLayout layoutRows(listOf())
        }
        var expandPlaceable = subcompose(1, expandButton).first().measure(constraints)
        // Enforce all reaction buttons have the same height
        val maxHeight = (reactionsPlaceables + listOf(expandPlaceable)).maxOf { it.height }
        val newConstrains = constraints.copy(minHeight = maxHeight)
        reactionsPlaceables = subcompose(2, reactions).map { it.measure(newConstrains) }
        expandPlaceable = subcompose(3, expandButton).first().measure(newConstrains)
        val addMorePlaceable = addMoreButton?.let { subcompose(4, addMoreButton).first().measure(newConstrains) }

        // Calculate the layout of the rows with the reactions button and add more button
        val reactionsAndAddMore = calculateRows(reactionsPlaceables + listOfNotNull(addMorePlaceable))
        // If we have extended beyond the defined number of rows we are showing the expand/collapse ui
        if (rowsBeforeCollapsible?.let { reactionsAndAddMore.size > it } == true) {
            if (expanded) {
                // Show all subviews with the add more button at the end
                var reactionsAndButtons = calculateRows(reactionsPlaceables + listOfNotNull(expandPlaceable, addMorePlaceable))
                reactionsAndButtons = ensureCollapseAndAddMoreButtonsAreOnTheSameRow(reactionsAndButtons)
                layoutRows(reactionsAndButtons)
            } else {
                // Truncate to `rowsBeforeCollapsible` number of rows and replace the reactions at the end of the last row with the buttons
                val collapsedRows = reactionsAndAddMore.take(rowsBeforeCollapsible)
                val collapsedRowsWithButtons = replaceTrailingItemsWithButtons(collapsedRows, expandPlaceable, addMorePlaceable)
                layoutRows(collapsedRowsWithButtons)
            }
        } else {
            // Otherwise we are just showing all items without the expand button
            layoutRows(reactionsAndAddMore)
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReactionsLayoutPreview() = ElementPreview {
    TimelineItemReactionsLayout(
        expanded = false,
        expandButton = {
            MessagesReactionButton(
                content = MessagesReactionsButtonContent.Text(
                    text = stringResource(id = R.string.screen_room_timeline_less_reactions)
                ),
                onClick = {},
                onLongClick = {}
            )
        },
        addMoreButton = {
            MessagesReactionButton(
                content = MessagesReactionsButtonContent.Icon(CompoundDrawables.ic_compound_reaction_add),
                onClick = {},
                onLongClick = {}
            )
        },
        reactions = {
            aTimelineItemReactions(count = 18).reactions.forEach {
                MessagesReactionButton(
                    content = MessagesReactionsButtonContent.Reaction(
                        it
                    ),
                    onClick = {},
                    onLongClick = {}
                )
            }
        }
    )
}
