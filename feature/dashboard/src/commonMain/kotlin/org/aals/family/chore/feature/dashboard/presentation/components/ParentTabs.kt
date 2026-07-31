package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_overview_title
import familychore.core.generated.resources.family_management_add_member
import familychore.core.generated.resources.family_management_invite_member
import familychore.core.generated.resources.family_management_nickname_label
import familychore.core.generated.resources.family_management_title
import familychore.core.generated.resources.pin_label
import familychore.core.generated.resources.pin_mandatory
import familychore.core.generated.resources.pin_optional
import familychore.core.generated.resources.pts_count
import familychore.core.generated.resources.role_child
import familychore.core.generated.resources.role_parent
import familychore.core.generated.resources.save
import familychore.core.generated.resources.task_assigned_to
import familychore.core.generated.resources.task_management_add_chore
import familychore.core.generated.resources.task_management_assign_to
import familychore.core.generated.resources.task_management_empty
import familychore.core.generated.resources.task_management_name_label
import familychore.core.generated.resources.task_management_points_label
import familychore.core.generated.resources.task_management_save_action
import familychore.core.generated.resources.task_management_title
import familychore.core.generated.resources.task_unknown_user
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.aals.family.chore.feature.dashboard.presentation.DashboardState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParentOverviewContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(Res.string.dashboard_overview_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.size(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.familyMembers) { member ->
                ChildSummaryCard(member)
            }
        }
    }
}

@Composable
fun ChildSummaryCard(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nickname, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                stringResource(Res.string.pts_count, user.points),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentTasksContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var choreName by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("10") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(Res.string.task_management_title),
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.AddCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.task_management_add_chore))
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        if (state.chores.isEmpty()) {
            Text(
                stringResource(Res.string.task_management_empty),
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.chores) { chore ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chore.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    stringResource(
                                        Res.string.task_assigned_to,
                                        state.familyMembers.find { it.id == chore.assignedTo }?.nickname
                                            ?: stringResource(Res.string.task_unknown_user)
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(stringResource(Res.string.pts_count, chore.points), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showAddDialog = false }) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(Res.string.task_management_add_chore),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.size(16.dp))
                    OutlinedTextField(
                        value = choreName,
                        onValueChange = { 
                            choreName = it
                            onAction(DashboardAction.OnChoreNameChange(it)) 
                        },
                        label = { Text(stringResource(Res.string.task_management_name_label)) },
                        isError = state.choreNameError != null,
                        supportingText = { state.choreNameError?.let { Text(it.asString()) } }
                    )
                    OutlinedTextField(
                        value = points,
                        onValueChange = { 
                            points = it
                            onAction(DashboardAction.OnChorePointsChange(it))
                        },
                        label = { Text(stringResource(Res.string.task_management_points_label)) },
                        isError = state.chorePointsError != null,
                        supportingText = { state.chorePointsError?.let { Text(it.asString()) } }
                    )
                    
                    Text(stringResource(Res.string.task_management_assign_to))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        state.familyMembers.filter { it.role == org.aals.family.chore.core.domain.model.UserRole.CHILD }.forEach { child ->
                            FilterChip(
                                selected = state.selectedAssigneeId == child.id,
                                onClick = { onAction(DashboardAction.SelectAssignee(child.id)) },
                                label = { Text(child.nickname) }
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = {
                            onAction(DashboardAction.CreateChore(
                                name = choreName,
                                points = points.toIntOrNull() ?: 0,
                                description = null,
                                assignedTo = state.selectedAssigneeId ?: ""
                            ))
                            // Simple logic to close if no error immediately (not ideal MVI but for now)
                            if (choreName.isNotBlank()) {
                                showAddDialog = false
                            }
                        }) {
                            Text(stringResource(Res.string.task_management_save_action))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(Res.string.family_management_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.size(16.dp))

        // Add Member Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(Res.string.family_management_add_member),
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Role Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserRole.entries.forEach { role ->
                        val label = when (role) {
                            UserRole.PARENT -> stringResource(Res.string.role_parent)
                            UserRole.CHILD -> stringResource(Res.string.role_child)
                        }
                        FilterChip(
                            selected = state.newMemberRole == role,
                            onClick = { onAction(DashboardAction.ChangeNewMemberRole(role)) },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = state.newMemberNickname,
                    onValueChange = { onAction(DashboardAction.OnMemberNicknameChange(it)) },
                    label = { Text(stringResource(Res.string.family_management_nickname_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.memberNicknameError != null,
                    supportingText = { state.memberNicknameError?.let { Text(it.asString()) } }
                )
                
                var pinVisible by remember { mutableStateOf(false) }
                val isParent = state.newMemberRole == UserRole.PARENT
                OutlinedTextField(
                    value = state.newMemberPin,
                    onValueChange = { onAction(DashboardAction.OnNewMemberPinChange(it)) },
                    label = { 
                        val hint = if (isParent) stringResource(Res.string.pin_mandatory) 
                                   else stringResource(Res.string.pin_optional)
                        Text("${stringResource(Res.string.pin_label)} ($hint)") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )

                Button(
                    onClick = { 
                        onAction(DashboardAction.AddMember(
                            state.newMemberNickname, 
                            state.newMemberRole,
                            state.newMemberPin.ifBlank { null }
                        )) 
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !state.isAddingMember && 
                        state.newMemberNickname.isNotBlank() && 
                        (!isParent || state.newMemberPin.isNotBlank()) &&
                        (state.isServerReachable || state.isOfflineMode)
                ) {
                    if (state.isAddingMember) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.familyMembers) { member ->
                FamilyMemberCard(member, onAction, state.isServerReachable)
            }
        }
    }
}

@Composable
fun FamilyMemberCard(
    user: User,
    onAction: (DashboardAction) -> Unit,
    isOnline: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.nickname, style = MaterialTheme.typography.titleMedium)
                val roleText = when (user.role) {
                    UserRole.PARENT -> stringResource(Res.string.role_parent)
                    UserRole.CHILD -> stringResource(Res.string.role_child)
                }
                Text(roleText, style = MaterialTheme.typography.bodySmall)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.role == UserRole.CHILD) {
                    Text(stringResource(Res.string.pin_label), style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = user.requiresPin,
                        onCheckedChange = { onAction(DashboardAction.UpdateUserPinRequirement(user.id, it)) },
                        modifier = Modifier.layoutScale(0.7f),
                        enabled = isOnline
                    )
                } else {
                    // Show that PIN is required for parents (just a label or icon)
                    Text(stringResource(Res.string.pin_label), style = MaterialTheme.typography.labelSmall)
                    Switch(
                        checked = true,
                        onCheckedChange = {},
                        modifier = Modifier.layoutScale(0.7f),
                        enabled = false
                    )
                }
                IconButton(onClick = { onAction(DashboardAction.ShowInviteQr(user.id)) }) {
                    Icon(Icons.Default.QrCode, contentDescription = stringResource(Res.string.family_management_invite_member))
                }
            }
        }
    }
}

internal fun Modifier.layoutScale(scale: Float) = this.then(Modifier.size((scale * 48).dp)) // Rough scale hack
