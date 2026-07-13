package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.behavior_award_to
import familychore.core.generated.resources.behavior_title
import familychore.core.generated.resources.dashboard_points_label
import familychore.core.generated.resources.dashboard_tab_today
import familychore.core.generated.resources.pts_suffix
import org.aals.family.chore.core.domain.model.BehaviorItem
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.aals.family.chore.feature.dashboard.presentation.DashboardState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChildTodayContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        PointsHeader(state.user.points)
        
        Spacer(modifier = Modifier.size(24.dp))
        
        Text(
            stringResource(Res.string.dashboard_tab_today),
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.size(16.dp))
        
        if (state.chores.isEmpty()) {
            Text(
                "No chores for today!",
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.chores) { chore ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(chore.name, style = MaterialTheme.typography.titleMedium)
                            Text("${chore.points} pts", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PointsHeader(points: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(Res.string.dashboard_points_label),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                "$points",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                stringResource(Res.string.pts_suffix),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun BehaviorTabContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(Res.string.behavior_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(Res.string.behavior_award_to, state.familyMembers.find { it.id == state.selectedAssigneeId }?.nickname ?: ""),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.size(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(state.behaviorItems) { item ->
                BehaviorCard(item = item) {
                    state.selectedAssigneeId?.let { userId ->
                        onAction(DashboardAction.AwardPoints(userId, item))
                    }
                }
            }
        }
    }
}

@Composable
fun BehaviorCard(item: BehaviorItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.points >= 0) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
            )
            Text(
                "${if (item.points > 0) "+" else ""}${item.points}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
