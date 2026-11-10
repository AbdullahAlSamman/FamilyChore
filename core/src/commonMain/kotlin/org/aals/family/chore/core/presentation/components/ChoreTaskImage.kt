package org.aals.family.chore.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.task_preset_clean
import familychore.core.generated.resources.task_preset_dishes
import familychore.core.generated.resources.task_preset_homework
import familychore.core.generated.resources.task_preset_laundry
import org.jetbrains.compose.resources.painterResource

/**
 * The set of bundled preset task-image (infographic) keys available for selection.
 * Each key maps to a corresponding `Res.drawable.task_preset_<key>` drawable.
 */
val TaskPresets: List<String> = listOf("dishes", "laundry", "clean", "homework")

/**
 * Renders a chore's task image / infographic with this priority:
 *  1. Bundled preset (path starts with [BUNDLED_PREFIX]) via painterResource.
 *  2. Local file path via Coil [AsyncImage].
 *  3. A generic image placeholder (default when no image set).
 *
 * @param imagePath the stored image reference (bundled marker or local path), or null.
 * @param size      the width/height of the rendered tile.
 */
@Composable
fun ChoreTaskImage(
    imagePath: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    val resolved = imagePath?.takeIf { it.isNotBlank() }
    val tileModifier = modifier.size(size)

    when {
        resolved != null && resolved.startsWith(BUNDLED_PREFIX) -> {
            val key = resolved.removePrefix(BUNDLED_PREFIX)
            val painter: Painter = taskPresetPainter(key)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = tileModifier.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        resolved != null -> {
            val request = ImageRequest.Builder(LocalPlatformContext.current)
                .data(resolved)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build()
            AsyncImage(
                model = request,
                contentDescription = null,
                modifier = tileModifier.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        }

        else -> {
            Box(
                modifier = tileModifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun taskPresetPainter(key: String): Painter = when (key) {
    "laundry" -> painterResource(Res.drawable.task_preset_laundry)
    "clean" -> painterResource(Res.drawable.task_preset_clean)
    "homework" -> painterResource(Res.drawable.task_preset_homework)
    else -> painterResource(Res.drawable.task_preset_dishes)
}
