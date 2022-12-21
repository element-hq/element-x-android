package io.element.android.x.designsystem.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            PreferenceTopAppBar(
                title = title,
                onBackPressed = onBackPressed,
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(it)
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    )
}

@Composable
@Preview(showBackground = false)
fun PreferenceScreenPreview() {
    PreferenceScreen(
        title = "Preference screen"
    ) {
        PreferenceCategoryPreview()
    }
}
