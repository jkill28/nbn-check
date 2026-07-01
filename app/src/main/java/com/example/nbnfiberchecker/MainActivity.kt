package com.example.nbnfiberchecker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nbnfiberchecker.data.AddressDetail
import com.example.nbnfiberchecker.data.NbnRepository
import com.example.nbnfiberchecker.ui.theme.NBNFiberCheckerTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = NbnRepository()

    var results by mutableStateOf<List<AddressDetail>>(emptyList())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    private val locationIds = listOf("LOC000007898843", "LOC000021958561")

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing = true
            val deferred = locationIds.map { id ->
                async { repository.fetchNbnDetails(id) }
            }
            results = deferred.awaitAll().mapNotNull { it.getOrNull() }
            isRefreshing = false
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NBNFiberCheckerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NBN Fiber Checker", style = MaterialTheme.typography.headlineMedium) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { innerPadding ->
        val state = rememberPullToRefreshState()
        if (state.isRefreshing) {
            LaunchedEffect(true) {
                viewModel.refresh()
            }
        }

        LaunchedEffect(viewModel.isRefreshing) {
            if (viewModel.isRefreshing) {
                state.startRefresh()
            } else {
                state.endRefresh()
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(state.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.results) { detail ->
                    NbnDetailCard(detail)
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = state
            )
        }
    }
}

@Composable
fun NbnDetailCard(detail: AddressDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tech change status: ${detail.techChangeStatus ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Target eligibility (targetEligibilityQuarter): ${detail.targetEligibilityQuarter ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "tech type: ${detail.techType ?: "N/A"} ${detail.techFlip ?: ""}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Address: ${detail.formattedAddress ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
