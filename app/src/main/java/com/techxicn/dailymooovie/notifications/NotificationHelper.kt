package com.techxicn.dailymooovie.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.techxicn.dailymooovie.MainActivity
import com.techxicn.dailymooovie.R

/**
 * NotificationHelper
 * ══════════════════
 * Centraliza la creación del NotificationChannel y el envío de la notificación
 * del recordatorio diario (la película del día). Es 100% local: no depende de
 * Firebase Cloud Messaging ni de un servidor.
 *
 * Uso típico (desde el Worker):
 *   NotificationHelper.ensureChannel(context)
 *   NotificationHelper.showDailyMovie(context, movieTitle)
 */
object NotificationHelper {

    /** Id estable del canal (visible en Ajustes del sistema como "Recomendación diaria"). */
    const val CHANNEL_ID = "daily_movie_reminder"

    /** Id de la notificación. Fijo → una nueva del día reemplaza a la anterior. */
    private const val NOTIFICATION_ID = 1001

    /** Request code del PendingIntent (debe ser estable para reusar/actualizar). */
    private const val CONTENT_REQUEST_CODE = 2001

    /**
     * Crea el NotificationChannel si aún no existe (no-op en < Android 8, donde no
     * hay canales). Es idempotente: recrear un canal con el mismo id no lo duplica.
     * Se le da nombre y descripción visibles en la configuración del sistema.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        // Ya existe: no hace falta recrearlo.
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_daily_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notif_channel_daily_desc)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Muestra la notificación de la película del día. Al tocarla abre la app en la
     * pantalla Today (vía [MainActivity.EXTRA_OPEN_TODAY]).
     *
     * En Android 13+ requiere el permiso POST_NOTIFICATIONS: si no está concedido,
     * el envío se omite silenciosamente (no lanza) para no romper el Worker.
     *
     * @return true si se envió, false si faltaba el permiso.
     */
    fun showDailyMovie(context: Context, movieTitle: String): Boolean {
        ensureChannel(context)

        // Android 13+: sin el permiso, no se puede publicar. Se respeta la decisión
        // del usuario y se sale sin efecto.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val contentIntent = buildContentIntent(context)

        val title = context.getString(R.string.notif_daily_title_fmt, movieTitle)
        val text = context.getString(R.string.notif_daily_body)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_movie)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        } catch (_: SecurityException) {
            // Salvaguarda extra: revocación de permiso entre el check y el notify().
            false
        }
    }

    /**
     * PendingIntent que abre MainActivity marcada para ir a Today. Se usa
     * launchMode="singleTop" (manifest) + FLAG_SINGLE_TOP para reusar la instancia
     * viva y entregar el intent a onNewIntent en vez de recrear la Activity.
     */
    private fun buildContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_OPEN_TODAY, true)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, CONTENT_REQUEST_CODE, intent, flags)
    }
}
