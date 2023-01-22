package com.example.backpresscompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.backpresscompose.ui.theme.BackPressComposeTheme
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BackPressComposeTheme {
                // A surface container using the 'background' color from the theme
                MainScreen()
            }
        }

        onBackPressedDispatcher.addCallback(
            callbackGenerator("MainActivity")
        )
    }

    private fun callbackGenerator(title: String) = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            Log.e(TAG, "handleOnBackPressed:  $title")
        }
    }
}

@Composable
fun MainScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val selected = remember { mutableStateOf(0) }
        val selectionStack = remember { Stack<Int>() }
        val handleBackHandler = remember(selected.value) {
            selectionStack.isNotEmpty().or(selected.value != 0)
        }

        BackHandler(handleBackHandler) {
            Log.e(TAG, "MainScreen: BackHandler ")
            selected.value = if (selectionStack.isEmpty()) 0 else selectionStack.pop()
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(64.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(24) { index ->
                BoxView(
                    text = "$index",
                    isSelected = selected.value == index,
                    onClick = fun() {
                        if (selected.value != index && !selectionStack.contains(selected.value)) {
                            selectionStack.push(selected.value)
                        }
                        selected.value = index
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BackPressComposeTheme {
        MainScreen()
    }
}

@Composable
fun BoxView(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = { }
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
            }
        )
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = text
            )
        }
    }
}

@Preview
@Composable
fun previewBoxView() {
    BackPressComposeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BoxView("1")
            BoxView(text = "2", isSelected = true)
        }
    }
}