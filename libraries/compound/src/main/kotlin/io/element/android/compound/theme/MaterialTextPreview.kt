/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.utils.toHrf

@Preview(heightDp = 1200, widthDp = 420)
@Composable
internal fun MaterialTextPreview() = Row(
    modifier = Modifier.background(Color.Yellow)
) {
    MaterialPreview(
        modifier = Modifier.weight(1f),
        darkTheme = false,
    )
    MaterialPreview(
        modifier = Modifier.weight(1f),
        darkTheme = true,
    )
}

private data class Model(
    val name: String,
    val bgColor: Color,
    val textColor: Color,
)

@Composable
private fun MaterialPreview(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) = Column(modifier = modifier) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        textAlign = TextAlign.Center,
        text = if (darkTheme) "Dark" else "Light",
        color = Color.Black,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
    ElementTheme(
        darkTheme = darkTheme,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            listOf(
                Model("Background", MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onBackground),
                Model("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary),
                Model("PrimaryContainer", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer),
                Model("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary),
                Model("SecondaryContainer", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer),
                Model("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary),
                Model("TertiaryContainer", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer),
                Model("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface),
                Model("SurfaceVariant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant),
                Model("InverseSurface", MaterialTheme.colorScheme.inverseSurface, MaterialTheme.colorScheme.inverseOnSurface),
                Model("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError),
                Model("ErrorContainer", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer),
            ).forEach {
                TextPreview(
                    name = it.name,
                    bgColor = it.bgColor,
                    textColor = it.textColor,
                )
            }
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = "Below\n".repeat(3),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        // the alpha applied to the scrim color does not seem to be mandatory.
                        // The library ignores the alpha level provided and apply it's own.
                        // For testing the color, manually set an alpha.
                        .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .padding(16.dp),
                    text = "${"Scrim"}\n${MaterialTheme.colorScheme.scrim.toHrf()}",
                    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun TextPreview(
    name: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) = Text(
    modifier = modifier
        .padding(1.dp)
        .fillMaxWidth()
        .background(bgColor)
        .padding(horizontal = 16.dp, vertical = 8.dp),
    text = "$name\n${textColor.toHrf()}\n${bgColor.toHrf()}",
    style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
    textAlign = TextAlign.Center,
    color = textColor,
)
