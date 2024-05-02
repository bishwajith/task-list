package com.bish.tasklist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bish.tasklist.Utils.capitalize
import com.bish.tasklist.Utils.taskCreatedDate
import com.bish.tasklist.model.Task
import com.bish.tasklist.model.getBackgroundColor
import com.bish.tasklist.model.getLabel
import com.bish.tasklist.model.getNextStateLabel
import com.bish.tasklist.model.getTextColor
import com.bish.tasklist.ui.theme.TaskListTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModelImpl by viewModels<MainViewModelImpl>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskListTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val showBottomSheet = remember { mutableStateOf(false) }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(

                title = {
                    Column {
                        Text(text = "My Tasks")
                        Text(text = "Total tasks ${viewModel.taskList.size}", fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White,
                ),
                scrollBehavior = scrollBehavior,
                actions = {
                    Box(modifier = Modifier.padding(8.dp)) {
                        val dropDownMenuState = remember {
                            mutableStateOf(false)
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more),
                            contentDescription = "",
                            modifier = Modifier.clickable {
                                dropDownMenuState.value = true
                            }
                        )

                        DropdownMenu(
                            expanded = dropDownMenuState.value,
                            modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary),
                            onDismissRequest = {
                                dropDownMenuState.value = false
                            }) {
                            DropdownMenuItem(
                                contentPadding = PaddingValues(
                                    vertical = 0.dp,
                                    horizontal = 12.dp
                                ),

                                text = {
                                    Text(text = "Delete All")
                                }, onClick = {
                                    viewModel.onDeleteAllClick()
                                    dropDownMenuState.value = false
                                },
                                modifier = Modifier
                                    .height(36.dp)
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                shape = RoundedCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    showBottomSheet.value = true
                    coroutineScope.launch {
                        sheetState.expand()
                    }
                }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add), contentDescription = ""
                )
            }
        }
    ) {

        val listState = rememberLazyListState()
        LaunchedEffect(key1 = viewModel.taskList.size, block = {
            listState.scrollToItem(0)
        })
        Column(modifier = Modifier.padding(it)) {
            if (viewModel.filterUiList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(text = "Filter by: ")
                    }
                    itemsIndexed(viewModel.filterUiList) { index, uiModel ->
                        val selectedBackgroundColor =
                            if (uiModel.isSelected) Color.Gray else Color.Transparent
                        Text(
                            text = uiModel.status.getLabel(),
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    viewModel.onFilterClick(index, uiModel)
                                }
                                .background(selectedBackgroundColor)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(uiModel.status.getBackgroundColor())
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            color = uiModel.status.getTextColor(),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Normal,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            LazyColumn(state = listState) {
                itemsIndexed(viewModel.taskList) { index: Int, item: Task ->
                    Card(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .padding(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = item.title.capitalize(),
                                    style = TextStyle(
                                        fontSize = 20.sp, color = Color.Black,
                                    )
                                )
                                Text(
                                    modifier = Modifier.padding(2.dp),
                                    text = item.description,
                                    style = TextStyle(
                                        fontSize = 14.sp, color = Color.Black,
                                    )
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Text(
                                    text = item.status.getLabel(),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(item.status.getBackgroundColor())
                                        .padding(vertical = 8.dp, horizontal = 16.dp),
                                    color = item.status.getTextColor(),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Normal,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .fillMaxHeight()
                            ) {
                                val dropDownMenuState = remember {
                                    mutableStateOf(false)
                                }
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_more),
                                        contentDescription = "",
                                        modifier = Modifier.clickable {
                                            dropDownMenuState.value = true
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = dropDownMenuState.value,
                                        modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary),
                                        onDismissRequest = {
                                            dropDownMenuState.value = false
                                        }) {
                                        val nextState = item.status.getNextStateLabel()
                                        if (nextState != null)
                                            DropdownMenuItem(
                                                contentPadding = PaddingValues(
                                                    vertical = 0.dp,
                                                    horizontal = 12.dp
                                                ),

                                                text = {
                                                    Text(text = "Move it to $nextState")
                                                }, onClick = {
                                                    viewModel.onUpdateState(item, index)
                                                    dropDownMenuState.value = false
                                                },
                                                modifier = Modifier
                                                    .height(36.dp)
                                            )
                                        DropdownMenuItem(text = {
                                            Text(text = "Delete")
                                        }, onClick = {
                                            viewModel.onDeleteClick(item)
                                            dropDownMenuState.value = false
                                        },
                                            modifier = Modifier.height(36.dp)
                                        )
                                    }
                                }
                                Spacer(
                                    modifier = Modifier
                                        .height(IntrinsicSize.Max)
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 10.dp),
                                    text = item.timeStamp.taskCreatedDate(),
                                    style = TextStyle(
                                        fontSize = 12.sp, color = Color.Black,
                                    )
                                )
                            }

                        }
                    }
                }
            }
        }

        if (showBottomSheet.value) {
            ModalBottomSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 50.dp),
                onDismissRequest = {
                    showBottomSheet.value = false
                },
                sheetState = sheetState
            ) {
                val title = remember {
                    mutableStateOf("")
                }
                val description = remember {
                    mutableStateOf("")
                }
                Box(modifier = Modifier.padding(bottom = 24.dp)) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .imePadding()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Create new task",
                            style = TextStyle.Default.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Sheet content
                        TextField(
                            value = title.value, onValueChange = { titleStr ->
                                title.value = titleStr
                            },
                            placeholder = { Text(text = "Title") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                                focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Sheet content
                        TextField(
                            value = description.value, onValueChange = { descriptionStr ->
                                description.value = descriptionStr

                            },
                            placeholder = { Text(text = "Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                                focusedContainerColor = MaterialTheme.colorScheme.onTertiary,
                            )
                        )
                        Spacer(modifier = Modifier.weight(1.0f))
                        Button(
                            enabled = title.value.isNotEmpty(),
                            onClick = {
                                viewModel.createTask(title.value, description.value)
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        showBottomSheet.value = false
                                    }
                                }

                            }) {
                            Text("Create")
                        }

                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}