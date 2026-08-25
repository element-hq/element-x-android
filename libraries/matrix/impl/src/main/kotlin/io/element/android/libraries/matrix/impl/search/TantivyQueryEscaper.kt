/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

/**
 * Characters that terminate a bare word wherever they appear in it. Escaping them keeps the token a
 * single literal word. `\` is included so a backslash the user typed cannot escape one of ours, and
 * `*` is the prefix/all-documents operator rather than a word breaker.
 */
private val ESCAPED_ANYWHERE = charArrayOf('\\', '^', '`', ':', '{', '}', '"', '\'', '[', ']', '(', ')', '*')

/**
 * Characters that are operators only in the first position of a term: `-`/`+` occurrence, `/` regex,
 * `>`/`<` elastic range, `!` field separator, `~` slop. Inside a word they are ordinary text, so
 * escaping them there would be over-broad — `C++` and `1/2/2024` must survive untouched.
 */
private val ESCAPED_WHEN_LEADING = charArrayOf('-', '+', '/', '>', '<', '!', '~')

/**
 * Bare-word operators, neutralised so the query stays literal text end to end: `a AND b` searches
 * for the three words rather than silently switching to a boolean AND. Mid-query these change
 * meaning rather than failing; only a dangling operator is a syntax error.
 */
private val OPERATOR_WORDS = setOf("AND", "OR", "NOT", "IN", "TO")

private val WHITESPACE_REGEX = Regex("\\s+")

private const val ESCAPE_HEADROOM = 8

/**
 * Escapes a user-typed search string so tantivy's query parser reads it as literal text, and makes
 * every word of it mandatory.
 *
 * **Escaping.** `matrix-sdk-search` hands the query to the **strict** `QueryParser::parse_query` with
 * `body` as the only default field, so an unescaped `:` sends tantivy looking for a field named
 * `https` and the whole search fails — pasting a link finds nothing at all. Unbalanced quotes and
 * brackets, a leading `-`, and elastic ranges like `>5` fail the same way, and the error arrives as
 * an opaque `ClientError` string we cannot tell apart from an I/O failure.
 *
 * Escaping is per token rather than wrapping the whole query in quotes: whitespace stays the term
 * separator, and only a token that actually contained an operator changes shape. Wrapping instead
 * would turn `design review notes` into one ordered phrase and quietly break every ordinary search.
 *
 * **Conjunction.** tantivy's parser is OR-by-default and the FFI takes a bare string, so
 * `set_conjunction_by_default()` is unreachable from here — `check the photo` matched every message
 * containing `the`, and buried the wanted one under hundreds of unrelated hits ranked by relevance.
 * Prefixing each token with `+` makes it a Must clause, which parses to exactly the query
 * `set_conjunction_by_default()` would have built.
 *
 * The `+` goes on **after** escaping, never before: `+` is escaped in leading position, so prefixing
 * first would hand the escaper its own operator and produce a literal `\+`. That parses cleanly and
 * silently restores OR, so it cannot be caught by anything but an assertion on the exact string.
 *
 * The costs, stated plainly: deliberate `"phrase"`, `-exclude` and boolean syntax stop working as
 * operators; and a query is now only as good as its worst word — one typo, or one word the user
 * misremembers, empties the result set where before it degraded to partial matches.
 *
 * Pinned to tantivy 0.26.1 grammar. PII: the query is never logged.
 */
internal fun String.escapeForTantivy(): String =
    split(WHITESPACE_REGEX)
        .filter { it.isNotEmpty() }
        .joinToString(separator = " ") { token ->
            // tantivy's default tokenizer splits on non-alphanumerics, so a token like `:)` or an
            // emoji indexes no terms at all. Required, such a token could only ever subtract, and a
            // single emoji beside real words would silently empty the results. Optional, it is
            // inert — which is what it was before conjunctions too.
            if (token.any(Char::isLetterOrDigit)) "+${token.escapeToken()}" else token.escapeToken()
        }

private fun String.escapeToken(): String = buildString(length + ESCAPE_HEADROOM) {
    if (this@escapeToken in OPERATOR_WORDS) append('\\')
    this@escapeToken.forEachIndexed { index, char ->
        // One forward pass: chained replace() calls would re-escape the backslashes they just added.
        if (char in ESCAPED_ANYWHERE || index == 0 && char in ESCAPED_WHEN_LEADING) {
            append('\\')
        }
        append(char)
    }
}
