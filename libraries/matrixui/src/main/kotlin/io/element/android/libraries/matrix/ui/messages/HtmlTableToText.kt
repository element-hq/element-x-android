/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * Finds all `<table>` elements in the document and replaces them in-place
 * with `<pre><code>` blocks containing a pipe-based text representation.
 */
fun Document.convertTablesToText() {
    // Snapshot the list to avoid concurrent modification
    val tables = getElementsByTag("table").toList()
    for (table in tables) {
        val rows = extractRows(table)
        if (rows.isEmpty()) {
            table.remove()
            continue
        }

        val maxCols = rows.maxOf { it.size }
        // Pad rows with fewer cells
        val normalizedRows = rows.map { row ->
            row + List(maxCols - row.size) { "" }
        }

        val headerRowCount = detectHeaderRowCount(table)

        val colWidths = IntArray(maxCols) { col ->
            normalizedRows.maxOf { it[col].length }.coerceAtLeast(1)
        }

        val lines = buildList {
            for ((i, row) in normalizedRows.withIndex()) {
                add(formatRow(row, colWidths))
                if (i == headerRowCount - 1 && headerRowCount > 0) {
                    add(formatSeparator(colWidths))
                }
            }
        }

        val text = lines.joinToString("\n")
        val pre = Element("pre")
        val code = Element("code")
        code.appendText(text)
        pre.appendChild(code)
        table.replaceWith(pre)
    }
}

private fun extractRows(table: Element): List<List<String>> {
    val rows = mutableListOf<List<String>>()

    val thead = table.getElementsByTag("thead").first()
    val tbody = table.getElementsByTag("tbody").first()

    if (thead != null) {
        for (tr in thead.getElementsByTag("tr")) {
            rows.add(extractCells(tr))
        }
    }
    if (tbody != null) {
        for (tr in tbody.getElementsByTag("tr")) {
            rows.add(extractCells(tr))
        }
    }

    // If no thead/tbody, get tr elements directly from the table
    if (thead == null && tbody == null) {
        for (tr in table.getElementsByTag("tr")) {
            rows.add(extractCells(tr))
        }
    }

    return rows
}

private fun extractCells(tr: Element): List<String> {
    return tr.children()
        .filter { it.tagName() == "th" || it.tagName() == "td" }
        .map { it.text().trim() }
}

/**
 * Detects the number of header rows.
 * If `<thead>` exists, its row count is used.
 * Otherwise, if the first row contains only `<th>` elements, it's treated as a header.
 */
private fun detectHeaderRowCount(table: Element): Int {
    val thead = table.getElementsByTag("thead").first()
    if (thead != null) {
        return thead.getElementsByTag("tr").size
    }

    // Check if the first <tr> contains only <th> elements
    val firstTr = table.getElementsByTag("tr").firstOrNull() ?: return 0
    val cells = firstTr.children().filter { it.tagName() == "th" || it.tagName() == "td" }
    return if (cells.isNotEmpty() && cells.all { it.tagName() == "th" }) 1 else 0
}

private fun formatRow(cells: List<String>, colWidths: IntArray): String {
    return cells.mapIndexed { i, cell ->
        cell.padEnd(colWidths[i])
    }.joinToString(" | ")
}

private fun formatSeparator(colWidths: IntArray): String {
    return colWidths.joinToString("-+-") { "-".repeat(it) }
}
