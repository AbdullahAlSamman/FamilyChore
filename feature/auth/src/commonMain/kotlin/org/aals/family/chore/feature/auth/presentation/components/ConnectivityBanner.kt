package org.aals.family.chore.feature.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.dashboard_offline_banner
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConnectivityBanner(
    isReachable: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isReachable) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.Red.copy(alpha = 0.85f))
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.dashboard_offline_banner),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
