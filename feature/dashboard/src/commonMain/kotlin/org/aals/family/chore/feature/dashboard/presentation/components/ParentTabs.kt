package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import familychore.core.generated.resources.change_pin
import familychore.core.generated.resources.change_picture
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
import familychore.core.generated.resources.cancel
import familychore.core.generated.resources.ok
import familychore.core.generated.resources.task_assigned_to
import familychore.core.generated.resources.task_management_add_chore
import familychore.core.generated.resources.task_management_assign_to
import familychore.core.generated.resources.task_management_empty
import familychore.core.generated.resources.task_management_name_label
import familychore.core.generated.resources.task_management_points_label
import familychore.core.generated.resources.task_management_save_action
import familychore.core.generated.resources.task_management_title
import familychore.core.generated.resources.task_unknown_user
import familychore.core.generated.resources.recurrence_label
import familychore.core.generated.resources.recurrence_custom
import familychore.core.generated.resources.recurrence_daily
import familychore.core.generated.resources.recurrence_weekly
import familychore.core.generated.resources.recurrence_monthly
import familychore.core.generated.resources.recurrence_none
import familychore.core.generated.resources.active_days_label
import familychore.core.generated.resources.requires_photo_proof_label
import familychore.core.generated.resources.due_date_label
import familychore.core.generated.resources.time_window_label
import familychore.core.generated.resources.window_start_label
import familychore.core.generated.resources.window_end_label
import familychore.core.generated.resources.date_hint
import familychore.core.generated.resources.time_hint
import familychore.core.generated.resources.day_sunday
import familychore.core.generated.resources.day_monday
import familychore.core.generated.resources.day_tuesday
import familychore.core.generated.resources.day_wednesday
import familychore.core.generated.resources.day_thursday
import familychore.core.generated.resources.day_friday
import familychore.core.generated.resources.day_saturday
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.aals.family.chore.core.domain.model.User
import org.aals.family.chore.core.domain.model.UserRole
import org.aals.family.chore.core.domain.util.formatEpochMillisToDate
import org.aals.family.chore.core.domain.util.parseTimeString
import org.aals.family.chore.core.domain.util.startOfTodayEpochMillis
import org.aals.family.chore.core.presentation.components.CameraCaptureView
import org.aals.family.chore.core.presentation.components.MemberAvatar
import org.aals.family.chore.core.presentation.permissions.rememberCameraPermissionManager
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
                val count = state.pendingCounts[member.id] ?: 0
                androidx.compose.material3.BadgedBox(
                    badge = {
                        if (count > 0) {
                            androidx.compose.material3.Badge { Text(count.toString()) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                ) {
                    ChildSummaryCard(member)
                }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParentTasksContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                org.aals.family.chore.core.presentation.components.ChoreImage(
                                    imagePath = state.choreImages[chore.id] ?: chore.iconName,
                                    size = 48.dp,
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable { onAction(DashboardAction.ShowChoreImageSourceDialog(chore.id)) }
                                )
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
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(stringResource(Res.string.pts_count, chore.points), color = MaterialTheme.colorScheme.primary)
                                    Text(chore.status.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (chore.status == org.aals.family.chore.core.domain.model.ChoreStatus.PENDING_APPROVAL) {
                                Spacer(modifier = Modifier.size(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                                    Button(onClick = { onAction(DashboardAction.ApproveChore(chore.id, chore.assignedTo)) }) {
                                        Text("Approve")
                                    }
                                    Button(onClick = { onAction(DashboardAction.DeclineChore(chore.id, chore.assignedTo)) }) {
                                        Text("Decline")
                                    }
                                }
                            }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        org.aals.family.chore.core.presentation.components.ChoreImage(
                            imagePath = state.addChoreForm.tempImagePath ?: state.addChoreForm.iconName,
                            size = 56.dp,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { onAction(DashboardAction.ShowChoreImageSourceDialog("NEW_CHORE")) }
                        )
                        OutlinedTextField(
                            value = choreName,
                            onValueChange = { 
                                choreName = it
                                onAction(DashboardAction.OnChoreNameChange(it)) 
                            },
                            label = { Text(stringResource(Res.string.task_management_name_label)) },
                            singleLine = true,
                            isError = state.addChoreForm.nameError != null,
                            supportingText = { state.addChoreForm.nameError?.let { Text(it.asString()) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = points,
                        onValueChange = { 
                            points = it
                            onAction(DashboardAction.OnChorePointsChange(it))
                        },
                        label = { Text(stringResource(Res.string.task_management_points_label)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = state.addChoreForm.pointsError != null,
                        supportingText = { state.addChoreForm.pointsError?.let { Text(it.asString()) } }
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

                    Spacer(Modifier.size(16.dp))
                    Text(stringResource(Res.string.recurrence_label), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        org.aals.family.chore.core.domain.model.Recurrence.entries.forEach { rec ->
                            val labelRes = when (rec) {
                                org.aals.family.chore.core.domain.model.Recurrence.NONE -> Res.string.recurrence_none
                                org.aals.family.chore.core.domain.model.Recurrence.CUSTOM -> Res.string.recurrence_custom
                                org.aals.family.chore.core.domain.model.Recurrence.DAILY -> Res.string.recurrence_daily
                                org.aals.family.chore.core.domain.model.Recurrence.WEEKLY -> Res.string.recurrence_weekly
                                org.aals.family.chore.core.domain.model.Recurrence.MONTHLY -> Res.string.recurrence_monthly
                            }
                            FilterChip(
                                selected = state.addChoreForm.recurrence == rec,
                                onClick = { onAction(DashboardAction.OnChoreRecurrenceChange(rec)) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }

                    if (state.addChoreForm.recurrence == org.aals.family.chore.core.domain.model.Recurrence.WEEKLY) {
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(Res.string.active_days_label), style = MaterialTheme.typography.labelMedium)
                        val dayNames = listOf(
                            Res.string.day_sunday,
                            Res.string.day_monday,
                            Res.string.day_tuesday,
                            Res.string.day_wednesday,
                            Res.string.day_thursday,
                            Res.string.day_friday,
                            Res.string.day_saturday
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dayNames.forEach { dayRes ->
                                val dayNumber = dayNames.indexOf(dayRes) + 1
                                FilterChip(
                                    selected = state.addChoreForm.activeDays.contains(dayNumber),
                                    onClick = { onAction(DashboardAction.OnChoreActiveDayToggle(dayNumber)) },
                                    label = { Text(stringResource(dayRes)) }
                                )
                            }
                        }
                    }

                    if (state.addChoreForm.recurrence == org.aals.family.chore.core.domain.model.Recurrence.CUSTOM) {
                        Spacer(Modifier.size(12.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.addChoreForm.dueDate,
                                onValueChange = {},
                                label = { Text(stringResource(Res.string.due_date_label)) },
                                placeholder = { Text(stringResource(Res.string.date_hint)) },
                                singleLine = true,
                                readOnly = true,
                                isError = state.addChoreForm.dueDateError != null,
                                supportingText = { state.addChoreForm.dueDateError?.let { Text(it.asString()) } },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showDatePicker = true }
                            )
                        }

                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(Res.string.time_window_label), style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = state.addChoreForm.activeWindowStart,
                                    onValueChange = {},
                                    label = { Text(stringResource(Res.string.window_start_label)) },
                                    placeholder = { Text(stringResource(Res.string.time_hint)) },
                                    singleLine = true,
                                    readOnly = true,
                                    isError = state.addChoreForm.timeWindowError != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { showStartTimePicker = true }) {
                                            Icon(Icons.Default.Schedule, contentDescription = null)
                                        }
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showStartTimePicker = true }
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = state.addChoreForm.activeWindowEnd,
                                    onValueChange = {},
                                    label = { Text(stringResource(Res.string.window_end_label)) },
                                    placeholder = { Text(stringResource(Res.string.time_hint)) },
                                    singleLine = true,
                                    readOnly = true,
                                    isError = state.addChoreForm.timeWindowError != null,
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { showEndTimePicker = true }) {
                                            Icon(Icons.Default.Schedule, contentDescription = null)
                                        }
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { showEndTimePicker = true }
                                )
                            }
                        }
                        if (state.addChoreForm.timeWindowError != null) {
                            Text(
                                text = state.addChoreForm.timeWindowError.asString(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.size(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.requires_photo_proof_label), modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.addChoreForm.requiresPhotoProof,
                            onCheckedChange = { onAction(DashboardAction.OnChoreRequiresPhotoProofToggle(it)) }
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = {
                            onAction(DashboardAction.CreateChore(
                                name = choreName,
                                points = points.toIntOrNull() ?: 0,
                                description = null,
                                assignedTo = state.selectedAssigneeId ?: "",
                                recurrence = state.addChoreForm.recurrence,
                                activeDays = state.addChoreForm.activeDays.toList(),
                                requiresPhotoProof = state.addChoreForm.requiresPhotoProof
                            ))
                            // Simple logic to close if no error immediately (not ideal MVI but for now)
                            if (choreName.isNotBlank() && state.addChoreForm.timeWindowError == null && state.addChoreForm.dueDateError == null) {
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

    if (showDatePicker) {
        val todayStartUtc = remember { startOfTodayEpochMillis(Clock.System.now().toEpochMilliseconds(), TimeZone.UTC) }
        val datePickerState = rememberDatePickerState(
            selectableDates = remember {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= todayStartUtc
                    }
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateStr = formatEpochMillisToDate(millis)
                        onAction(DashboardAction.OnChoreDueDateChange(dateStr))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        val (initHour, initMinute) = parseTimeString(state.addChoreForm.activeWindowStart)
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    onAction(DashboardAction.OnChoreWindowStartChange(formatted))
                    showStartTimePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndTimePicker) {
        val (initHour, initMinute) = parseTimeString(state.addChoreForm.activeWindowEnd)
        val timePickerState = rememberTimePickerState(
            initialHour = initHour,
            initialMinute = initMinute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formatted = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                    onAction(DashboardAction.OnChoreWindowEndChange(formatted))
                    showEndTimePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyManagementContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    val addMemberForm = state.addMemberForm
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
                            selected = addMemberForm.role == role,
                            onClick = { onAction(DashboardAction.ChangeNewMemberRole(role)) },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = addMemberForm.nickname,
                    onValueChange = { onAction(DashboardAction.OnMemberNicknameChange(it)) },
                    label = { Text(stringResource(Res.string.family_management_nickname_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = addMemberForm.nicknameError != null,
                    supportingText = { addMemberForm.nicknameError?.let { Text(it.asString()) } }
                )
                
                var pinVisible by remember { mutableStateOf(false) }
                val isParent = addMemberForm.role == UserRole.PARENT
                OutlinedTextField(
                    value = addMemberForm.pin,
                    onValueChange = { onAction(DashboardAction.OnNewMemberPinChange(it)) },
                    label = { 
                        val hint = if (isParent) stringResource(Res.string.pin_mandatory) 
                                   else stringResource(Res.string.pin_optional)
                        Text("${stringResource(Res.string.pin_label)} ($hint)") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = addMemberForm.pinError != null,
                    supportingText = { addMemberForm.pinError?.let { Text(it.asString()) } },
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
                            addMemberForm.nickname, 
                            addMemberForm.role,
                            addMemberForm.pin.ifBlank { null }
                        )) 
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !addMemberForm.isAdding && 
                        addMemberForm.nickname.isNotBlank() && 
                        (!isParent || addMemberForm.pin.isNotBlank()) &&
                        (state.isServerReachable || state.isOfflineMode)
                ) {
                    if (addMemberForm.isAdding) {
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
                FamilyMemberCard(
                    user = member,
                    picturePath = state.memberPictures[member.id],
                    onAction = onAction,
                    isOnline = state.isServerReachable,
                )
            }
        }
    }

    state.pictureTarget?.let { target ->
        if (state.showPresetPicker) {
            PresetPickerDialog(target = target, onAction = onAction)
        } else if (!state.showCameraCapture) {
            PictureSourceDialog(
                target = target,
                isCameraAvailable = state.isCameraAvailable,
                onAction = onAction,
            )
        }
    }
    
    val permissionManager = rememberCameraPermissionManager()

    if (state.cameraPermissionRequestCount > 0) {
        permissionManager.requestPermission { granted ->
            onAction(DashboardAction.OnCameraPermissionResult(granted))
        }
    }

    if (state.showCameraCapture && state.pictureTarget != null) {
        CameraCaptureView(
            onImageCaptured = { bytes -> onAction(DashboardAction.OnPickedImage(state.pictureTarget.id, bytes)) },
            onCancel = { onAction(DashboardAction.CancelCameraCapture) }
        )
    }
}

@Composable
fun FamilyMemberCard(
    user: User,
    picturePath: String?,
    onAction: (DashboardAction) -> Unit,
    isOnline: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(
                user = user,
                picturePath = picturePath,
                size = 48.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                }
                IconButton(
                    onClick = { onAction(DashboardAction.ChangeMemberPin(user.id)) },
                    enabled = isOnline
                ) {
                    Icon(
                        Icons.Default.Password,
                        contentDescription = stringResource(Res.string.change_pin)
                    )
                }
                IconButton(
                    onClick = { onAction(DashboardAction.ShowPictureSourceDialog(user.id)) },
                    enabled = isOnline
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(Res.string.change_picture)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content
    )
}
