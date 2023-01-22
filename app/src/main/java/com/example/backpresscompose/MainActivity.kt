package com.example.backpresscompose

import android.os.Build
import android.os.Bundle
import java.util.Stack
import android.util.Log
import android.window.OnBackInvokedCallback
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                1,
                onBackInvokedCallbackGenerator("MainActivity")
            )
        }
    }

    private fun callbackGenerator(title: String) = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            Log.e(TAG, "handleOnBackPressed:  $title")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onBackInvokedCallbackGenerator(title: String) =
        (OnBackInvokedCallback { Log.e(TAG, "onBackInvoked: $title") })
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BackInvokeHandler(handleBackHandler) {
                selected.value = if (selectionStack.isEmpty()) 0 else selectionStack.pop()
            }
        } else {
            BackHandler(handleBackHandler) {
                Log.e(TAG, "MainScreen: BackHandler ")
                selected.value = if (selectionStack.isEmpty()) 0 else selectionStack.pop()
            }
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

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun BackInvokeHandler(
    handleBackHandler: Boolean,
    priority : Int = 1,
    callback : () -> Unit = {}
) {
    val _callback = remember {
        OnBackInvokedCallback {
            Log.e(TAG, "BackInvokeHandler: ")
            callback()
        }
    }

    val activity = LocalContext.current as AppCompatActivity
    if (handleBackHandler) {
        activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(priority, _callback)
    } else {
        activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(_callback)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner.lifecycle, activity.onBackInvokedDispatcher,handleBackHandler) {
        onDispose {
            activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(_callback)
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