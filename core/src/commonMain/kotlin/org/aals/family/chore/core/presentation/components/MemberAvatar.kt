package org.aals.family.chore.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import familychore.core.generated.resources.Res
import familychore.core.generated.resources.avatar_preset_1
import familychore.core.generated.resources.avatar_preset_2
import familychore.core.generated.resources.avatar_preset_3
import familychore.core.generated.resources.avatar_preset_4
import org.aals.family.chore.core.domain.model.User
import org.jetbrains.compose.resources.painterResource

/** Marker prefix for a bundled preset avatar reference (vs. a local file path). */
const val BUNDLED_PREFIX = "bundled:"

/**
 * The set of bundled preset avatar keys available for selection.
 * Each key maps to a corresponding `Res.drawable.avatar_preset_<key>` drawable.
 */
val MemberAvatarPresets: List<String> = listOf("1", "2", "3", "4")

/**
 * Renders a member's avatar with this priority:
 *  1. Bundled preset (path starts with [BUNDLED_PREFIX]) via painterResource.
 *  2. Local file path via Coil [AsyncImage].
 *  3. Initials derived from the member's nickname (default when no picture set).
 *
 * @param user      the member whose initials/identity are used for the fallback.
 * @param picturePath the stored picture reference (bundled marker or local path), or null.
 * @param size      the diameter of the circular avatar.
 */
@Composable
fun MemberAvatar(
    user: User,
    picturePath: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    val resolved = picturePath?.takeIf { it.isNotBlank() }
    val avatarModifier = modifier.size(size)

    when {
        resolved != null && resolved.startsWith(BUNDLED_PREFIX) -> {
            val key = resolved.removePrefix(BUNDLED_PREFIX)
            val painter: Painter = bundledPresetPainter(key)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = avatarModifier.clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }

        resolved != null -> {
            AsyncImage(
                model = resolved,
                contentDescription = null,
                modifier = avatarModifier.clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }

        else -> {
            val initials = derivedInitials(user.nickname)
            Box(
                modifier = avatarModifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Renders a single bundled preset avatar (independent of any user). */
@Composable
fun PresetAvatar(
    presetKey: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    val painter: Painter = bundledPresetPainter(presetKey)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.size(size).clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun bundledPresetPainter(key: String): Painter = when (key) {
    "2" -> painterResource(Res.drawable.avatar_preset_2)
    "3" -> painterResource(Res.drawable.avatar_preset_3)
    "4" -> painterResource(Res.drawable.avatar_preset_4)
    else -> painterResource(Res.drawable.avatar_preset_1)
}

/** Derives up to two initials from a nickname (e.g. "John Doe" -> "JD", "sam" -> "SA"). */
internal fun derivedInitials(nickname: String): String {
    val trimmed = nickname.trim()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> (parts[0].first().toString() + parts[1].first()).uppercase()
        else -> trimmed.take(2).uppercase()
    }
}
