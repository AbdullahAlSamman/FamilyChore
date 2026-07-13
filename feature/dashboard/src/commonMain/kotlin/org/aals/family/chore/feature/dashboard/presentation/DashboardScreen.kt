package org.aals.family.chore.feature.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.behavior_award_to
import familychore.core.generated.resources.behavior_title
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.dashboard_logout
import familychore.core.generated.resources.dashboard_offline_banner
import familychore.core.generated.resources.dashboard_points_label
import familychore.core.generated.resources.dashboard_tab_behavior
import familychore.core.generated.resources.dashboard_tab_family
import familychore.core.generated.resources.dashboard_tab_history
import familychore.core.generated.resources.dashboard_tab_overview
import familychore.core.generated.resources.dashboard_tab_rewards
import familychore.core.generated.resources.dashboard_tab_store
import familychore.core.generated.resources.dashboard_tab_tasks
import familychore.core.generated.resources.dashboard_tab_today
import familychore.core.generated.resources.dashboard_title
import familychore.core.generated.resources.error_unknown
import familychore.core.generated.resources.family_management_add_child
import familychore.core.generated.resources.family_management_invite_member
import familychore.core.generated.resources.family_management_nickname_label
import familychore.core.generated.resources.family_management_require_pin
import familychore.core.generated.resources.family_management_title
import familychore.core.generated.resources.family_management_token_expiry
import familychore.core.generated.resources.ok
import familychore.core.generated.resources.pts_suffix
import familychore.core.generated.resources.save
import familychore.core.generated.resources.task_management_add_chore
import familychore.core.generated.resources.task_management_assign_to
import familychore.core.generated.resources.task_management_empty
import familychore.core.generated.resources.task_management_name_label
import familychore.core.generated.resources.task_management_points_label
import familychore.core.generated.resources.task_management_save_action
import familychore.core.generated.resources.task_management_title
import familychore.core.generated.resources.task_status_prefix
import familychore.core.generated.resources.task_summary_review_count
import familychore.core.generated.resources.task_today_empty
import familychore.core.generated.resources.task_today_title
import familychore.core.generated.resources.transaction_default_note
import familychore.core.generated.resources.transaction_id_prefix
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onLogout: (isServerOnline: Boolean, familyId: String?) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onAction = { action ->
            if (action is DashboardAction.Logout) {
                val successState = state as? DashboardState.Success
                val isOnline = successState?.isServerReachable ?: false
                val familyId = successState?.user?.familyId
                onLogout(isOnline, familyId)
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
            ParentOverviewRoute to stringResource(Res.string.dashboard_tab_overview),
            ParentTasksRoute to stringResource(Res.string.dashboard_tab_tasks),
            BehaviorRoute to stringResource(Res.string.dashboard_tab_behavior),
            ParentRewardsRoute to stringResource(Res.string.dashboard_tab_rewards),
            FamilyManagementRoute to stringResource(Res.string.dashboard_tab_family)
        )
    } else {
        listOf(
            ChildTodayRoute to stringResource(Res.string.dashboard_tab_today),
            HistoryRoute to stringResource(Res.string.dashboard_tab_history),
            ChildRewardsRoute to stringResource(Res.string.dashboard_tab_store)
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
    Box {
        AnimatedContent(targetState = state.currentTab) { tab ->
            when (tab) {
                ParentOverviewRoute -> ParentOverviewContent(state, onAction)
                ParentTasksRoute -> ParentTasksContent(state, onAction)
                BehaviorRoute -> BehaviorTabContent(state, onAction)
                ParentRewardsRoute -> Text(stringResource(Res.string.dashboard_tab_store), modifier = Modifier.fillMaxSize())
                FamilyManagementRoute -> FamilyManagementContent(state, onAction)
                
                ChildTodayRoute -> ChildTodayContent(state, onAction)
                HistoryRoute -> HistoryTabContent(state, onAction)
                ChildRewardsRoute -> Text(stringResource(Res.string.dashboard_tab_store), modifier = Modifier.fillMaxSize())
                else -> Text(stringResource(Res.string.error_unknown))
            }
        }

        state.inviteQrContent?.let { content ->
            InviteQrDialog(
                qrContent = content,
                onDismiss = { onAction(DashboardAction.DismissInviteQr) }
            )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.family_management_title), style = MaterialTheme.typography.headlineSmall)
                Button(
                    onClick = { onAction(DashboardAction.ShowInviteQr()) },
                    enabled = state.isServerReachable
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.family_management_invite_member))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(Res.string.family_management_add_child), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.newChildNickname,
                        onValueChange = { onAction(DashboardAction.OnChildNicknameChange(it)) },
                        label = { Text(stringResource(Res.string.family_management_nickname_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.childNicknameError != null,
                        supportingText = state.childNicknameError?.let { { Text(it.asString()) } },
                        enabled = !state.isAddingChild
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = state.requiresPinForNewChild,
                            onCheckedChange = { onAction(DashboardAction.TogglePinRequirement) },
                            enabled = !state.isAddingChild
                        )
                        Text(stringResource(Res.string.family_management_require_pin))
                    }
                    Button(
                        onClick = { onAction(DashboardAction.AddChild(state.newChildNickname, state.requiresPinForNewChild)) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !state.isAddingChild && state.newChildNickname.isNotBlank() && state.isServerReachable
                    ) {
                        if (state.isAddingChild) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text(stringResource(Res.string.save))
                        }
                    }
                }
            }
        }

        items(state.familyMembers) { member ->
            FamilyMemberCard(member, onAction, state.isServerReachable)
        }
    }
}

@Composable
fun FamilyMemberCard(member: User, onAction: (DashboardAction) -> Unit, isOnline: Boolean) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.family_management_require_pin),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = member.requiresPin,
                        onCheckedChange = { onAction(DashboardAction.UpdateUserPinRequirement(member.id, it)) },
                        enabled = isOnline,
                        modifier = Modifier.scale(0.8f)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onAction(DashboardAction.ShowInviteQr(member.id)) }) {
                        Icon(Icons.Default.QrCode, contentDescription = stringResource(Res.string.family_management_invite_member))
                    }
                }
                
                Text(
                    text = stringResource(Res.string.pts_suffix, member.points),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp)
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
            Text(stringResource(Res.string.task_management_title), style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showCreateForm = !showCreateForm }) {
                Text(if (showCreateForm) stringResource(Res.string.cancel) else stringResource(Res.string.task_management_add_chore))
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
                        onValueChange = { 
                            choreName = it 
                            onAction(DashboardAction.OnChoreNameChange(it))
                        },
                        label = { Text(stringResource(Res.string.task_management_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.choreNameError != null,
                        supportingText = state.choreNameError?.let { { Text(it.asString()) } }
                    )
                    OutlinedTextField(
                        value = chorePoints,
                        onValueChange = { 
                            chorePoints = it 
                            onAction(DashboardAction.OnChorePointsChange(it))
                        },
                        label = { Text(stringResource(Res.string.task_management_points_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.chorePointsError != null,
                        supportingText = state.chorePointsError?.let { { Text(it.asString()) } }
                    )
                    
                    Text(stringResource(Res.string.task_management_assign_to), style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.familyMembers.forEach { member ->
                            FilterChip(
                                selected = state.selectedAssigneeId == member.id,
                                onClick = { onAction(DashboardAction.SelectAssignee(member.id)) },
                                label = { Text(member.nickname) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            state.selectedAssigneeId?.let { assigneeId ->
                                onAction(DashboardAction.CreateChore(
                                    name = choreName,
                                    points = chorePoints.toIntOrNull() ?: 10,
                                    description = null,
                                    assignedTo = assigneeId
                                ))
                                showCreateForm = false
                                choreName = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = choreName.isNotBlank() && state.selectedAssigneeId != null
                    ) {
                        Text(stringResource(Res.string.task_management_save_action))
                    }
                }
            }
        }
        
        if (state.chores.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(Res.string.task_management_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(state.chores) { chore ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(chore.name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(Res.string.task_status_prefix, chore.status.name), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(stringResource(Res.string.pts_suffix, chore.points), color = MaterialTheme.colorScheme.primary)
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
                    text = transaction.note ?: stringResource(Res.string.transaction_default_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.transaction_id_prefix, transaction.id.takeLast(6)),
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
                Text(stringResource(Res.string.dashboard_title))
            }
        },
        actions = {
            IconButton(onClick = { onAction(DashboardAction.Logout) }) {
                Icon(Icons.Default.ExitToApp, contentDescription = stringResource(Res.string.dashboard_logout))
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
                text = stringResource(Res.string.dashboard_offline_banner),
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
        Text(
            text = stringResource(Res.string.dashboard_tab_overview),
            style = MaterialTheme.typography.headlineMedium
        )
        
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
                Text(stringResource(Res.string.task_summary_review_count, 0), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(Res.string.pts_suffix, child.points),
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
            text = stringResource(Res.string.task_today_title),
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
                    stringResource(Res.string.task_today_empty),
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
            Text(stringResource(Res.string.dashboard_points_label), style = MaterialTheme.typography.labelLarge)
            Text(
                text = stringResource(Res.string.pts_suffix, points),
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
        Text(stringResource(Res.string.behavior_title), style = MaterialTheme.typography.headlineSmall)

        // Child Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(Res.string.behavior_award_to), style = MaterialTheme.typography.labelLarge)
            state.familyMembers.filter { it.role == UserRole.CHILD }.forEach { child ->
                FilterChip(
                    selected = state.selectedAssigneeId == child.id,
                    onClick = { onAction(DashboardAction.SelectAssignee(child.id)) },
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
                        state.selectedAssigneeId?.let { assigneeId ->
                            onAction(DashboardAction.AwardPoints(assigneeId, item))
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
                text = stringResource(Res.string.pts_suffix, item.points),
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

@Composable
fun InviteQrDialog(
    qrContent: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.family_management_invite_member),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Image(
                    painter = rememberQrCodePainter(qrContent),
                    contentDescription = "Invite QR Code",
                    modifier = Modifier.size(250.dp)
                )

                Text(
                    text = stringResource(Res.string.family_management_token_expiry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.ok))
                }
            }
        }
    }
}
