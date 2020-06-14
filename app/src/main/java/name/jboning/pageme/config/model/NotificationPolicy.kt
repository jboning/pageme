package name.jboning.pageme.config.model

import java.util.*

data class NotificationPolicy(
    val actions: ArrayList<NotificationStep>
) {
    data class NotificationStep(
        var delay_ms: Long,
        var action: NotificationAction
    )

    enum class NotificationAction {
        VIBRATE, FLASH_TORCH, PLAY_SOUND
    }
}