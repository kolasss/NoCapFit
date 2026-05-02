package dev.kolas.nocapfit.service

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kolas.nocapfit.util.SOUND_URI_SILENT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingtonePlayer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun play(savedUri: String?) {
        try {
            if (savedUri == SOUND_URI_SILENT) return
            val uri = savedUri?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = uri?.let { RingtoneManager.getRingtone(context, it) } ?: return
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
        } catch (_: Exception) { }
    }
}
