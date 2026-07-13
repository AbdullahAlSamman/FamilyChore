package org.aals.family.chore.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_tab_history
import org.aals.family.chore.core.domain.model.Transaction
import org.aals.family.chore.feature.dashboard.presentation.DashboardAction
import org.aals.family.chore.feature.dashboard.presentation.DashboardState
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryTabContent(
    state: DashboardState.Success,
    onAction: (DashboardAction) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            stringResource(Res.string.dashboard_tab_history),
            style = MaterialTheme.typography.titleLarge
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.transactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (transaction.amount >= 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                contentDescription = null,
                tint = if (transaction.amount >= 0) Color.Green else Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(transaction.note ?: "No note", style = MaterialTheme.typography.titleMedium)
                Text(
                    transaction.type.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "${if (transaction.amount > 0) "+" else ""}${transaction.amount}",
                style = MaterialTheme.typography.titleLarge,
                color = if (transaction.amount >= 0) Color.Green else Color.Red
            )
        }
    }
}
