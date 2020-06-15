package name.jboning.pageme.config.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AnnoyerPolicy(
    val actions: ArrayList<NotificationStep>
) {
    @Serializable
    data class NotificationStep(
        var delay_ms: Long,
        var action: NotificationAction
    )

    enum class NotificationAction {
        VIBRATE, FLASH_TORCH, PLAY_SOUND
    }
}