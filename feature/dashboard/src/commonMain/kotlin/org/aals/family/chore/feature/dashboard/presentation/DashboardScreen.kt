package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.feature.dashboard.presentation.navigation.BehaviorRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ChildTodayRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.DashboardTabRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.FamilyManagementRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.HistoryRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentOverviewRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentRewardsRoute
import org.aals.family.chore.feature.dashboard.presentation.navigation.ParentTasksRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onAction = { action ->
            if (action is DashboardAction.Logout) {
                onLogout()
            } else {
                viewModel.onAction(action)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                DashboardTopBar(state, onAction)
                if (state is DashboardState.Success) {
                    ConnectivityBanner(state.isServerReachable)
                }
            }
        },
        bottomBar = {
            if (state is DashboardState.Success) {
                DashboardBottomBar(state, onAction)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                DashboardState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center))
                }
                is DashboardState.Success -> {
                    DashboardContent(state, onAction)
                }
            }
        }
    }
}

@Composable
fun DashboardBottomBar(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    val tabs = if (state.user.role == UserRole.PARENT) {
        listOf(
            ParentOverviewRoute to "Overview",
            ParentTasksRoute to "Tasks",
            BehaviorRoute to "Behavior",
            ParentRewardsRoute to "Rewards",
            FamilyManagementRoute to "Family"
        )
    } else {
        listOf(
            ChildTodayRoute to "Today",
            HistoryRoute to "History",
            ChildRewardsRoute to "Store"
        )
    }

    NavigationBar {
        tabs.forEach { (route, label) ->
            NavigationBarItem(
                selected = state.currentTab == route,
                onClick = { onAction(DashboardAction.ChangeTab(route)) },
                icon = { Icon(getIconForRoute(route), contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

fun getIconForRoute(route: DashboardTabRoute): ImageVector {
    return when (route) {
        ParentOverviewRoute -> Icons.Default.Dashboard
        ParentTasksRoute -> Icons.Default.List
        BehaviorRoute -> Icons.Default.Star
        ParentRewardsRoute -> Icons.Default.CardGiftcard
        FamilyManagementRoute -> Icons.Default.People
        ChildTodayRoute -> Icons.Default.Today
        HistoryRoute -> Icons.Default.History
        ChildRewardsRoute -> Icons.Default.ShoppingCart
        else -> Icons.Default.QuestionMark
    }
}

@Composable
fun DashboardContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    AnimatedContent(targetState = state.currentTab) { tab ->
        when (tab) {
            ParentOverviewRoute -> ParentOverviewContent(state, onAction)
            ParentTasksRoute -> ParentTasksContent(state, onAction)
            BehaviorRoute -> BehaviorTabContent(state, onAction)
            ParentRewardsRoute -> Text("Rewards Store", modifier = Modifier.fillMaxSize())
            FamilyManagementRoute -> FamilyManagementContent(state, onAction)
            
            ChildTodayRoute -> ChildTodayContent(state, onAction)
            HistoryRoute -> HistoryTabContent(state, onAction)
            ChildRewardsRoute -> Text("Reward Store", modifier = Modifier.fillMaxSize())
            else -> Text("Unknown Tab")
        }
    }
}

@Composable
fun FamilyManagementContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Family Members", style = MaterialTheme.typography.headlineSmall)
        }
        items(state.familyMembers) { member ->
            FamilyMemberCard(member)
        }
    }
}

@Composable
fun FamilyMemberCard(member: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(member.nickname, style = MaterialTheme.typography.titleMedium)
                Text(member.role.name, style = MaterialTheme.typography.bodySmall)
            }
            if (member.role == UserRole.CHILD) {
                Text(
                    text = "${member.points} pts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ParentTasksContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var chorePoints by remember { mutableStateOf("10") }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Task Management", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showCreateForm = !showCreateForm }) {
                Text(if (showCreateForm) "Cancel" else "Add Chore")
            }
        }

        if (showCreateForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = choreName,
                        onValueChange = { choreName = it },
                        label = { Text("Chore Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = chorePoints,
                        onValueChange = { chorePoints = it },
                        label = { Text("Points") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Assign to:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.familyMembers.filter { it.role == UserRole.CHILD }.forEach { child ->
                            FilterChip(
                                selected = state.selectedChildId == child.id,
                                onClick = { onAction(DashboardAction.SelectChild(child.id)) },
                                label = { Text(child.nickname) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            state.selectedChildId?.let { childId ->
                                onAction(DashboardAction.CreateChore(
                                    name = choreName,
                                    points = chorePoints.toIntOrNull() ?: 10,
                                    description = null,
                                    assignedTo = childId
                                ))
                                showCreateForm = false
                                choreName = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = choreName.isNotBlank() && state.selectedChildId != null
                    ) {
                        Text("Save Chore")
                    }
                }
            }
        }
        
        if (state.chores.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No chores created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(state.chores) { chore ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(chore.name, style = MaterialTheme.typography.titleMedium)
                                Text("Status: ${chore.status}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${chore.points} pts", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTabContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.transactions.sortedByDescending { it.timestamp }) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        if (transaction.amount >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.amount >= 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                    contentDescription = null,
                    tint = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note ?: "Points Update",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${transaction.id.takeLast(6)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (transaction.amount > 0) "+${transaction.amount}" else "${transaction.amount}",
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amount >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    TopAppBar(
        title = {
            if (state is DashboardState.Success) {
                Text(state.user.nickname)
            } else {
                Text("Dashboard")
            }
        },
        actions = {
            IconButton(onClick = { onAction(DashboardAction.Logout) }) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            }
        }
    )
}

@Composable
fun ConnectivityBanner(isReachable: Boolean) {
    if (!isReachable) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Server Unreachable - Offline Mode",
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ParentOverviewContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Family Overview", style = MaterialTheme.typography.headlineMedium)
        
        state.familyMembers.filter { it.role == UserRole.CHILD }.forEach { child ->
            ChildSummaryCard(child)
        }
    }
}

@Composable
fun ChildSummaryCard(child: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.People, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(child.nickname, style = MaterialTheme.typography.titleLarge)
                Text("Tasks to review: 0", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "${child.points}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChildTodayContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PointsHeader(state.user.points)
        
        Text(
            text = "Today's Tasks",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start
        )
        
        if (state.chores.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No tasks assigned for today yet!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.chores) { chore ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(chore.name, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PointsHeader(points: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Points", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$points",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviorTabContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Behavior Management", style = MaterialTheme.typography.headlineSmall)

        // Child Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Award to:", style = MaterialTheme.typography.labelLarge)
            state.familyMembers.filter { it.role == UserRole.CHILD }.forEach { child ->
                FilterChip(
                    selected = state.selectedChildId == child.id,
                    onClick = { onAction(DashboardAction.SelectChild(child.id)) },
                    label = { Text(child.nickname) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.behaviorItems) { item ->
                BehaviorCard(
                    item = item,
                    onClick = {
                        state.selectedChildId?.let { childId ->
                            onAction(DashboardAction.AwardPoints(childId, item))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BehaviorCard(
    item: BehaviorItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (item.isPositive) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (item.points > 0) "+${item.points}" else "${item.points}",
                style = MaterialTheme.typography.headlineSmall,
                color = if (item.isPositive) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}
