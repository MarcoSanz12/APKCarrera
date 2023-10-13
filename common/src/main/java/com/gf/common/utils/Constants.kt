package com.gf.common.utils

object Constants {

    object Camera {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }

    object Login{
        const val ALWAYS_LOGGED = "ALWAYS_LOGGED"
        const val LOG_EMAIL = "LOG_EMAIL"
        const val LOG_PASSWORD = "LOG_PASSWORD"
        const val LOG_UID = "LOG_UID"
    }

    const val ACTION_START_OR_RESUME_RUNNING = "ACTION_START_OR_RESUME_RUNNING"
    const val ACTION_PAUSE_RUNNING = "ACTION_PAUSE_RUNNING"
    const val ACTION_STOP_RUNNING = "ACTION_STOP_RUNNING"
    const val ACTION_SHOW_RUNNING_FRAGMENT = "ACTION_SHOW_RUNNING_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "running_channel"
    const val NOTIFICATION_CHANNEL_NAME = "running"
    const val NOTIFICATION_ID = 1


}