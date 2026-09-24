package com.techxicn.dailymooovie.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * DailyReminderScheduler
 * ══════════════════════
 * Programa (y cancela) el recordatorio diario local de la película del día usando
 * WorkManager.
 *
 * ─ Por qué PeriodicWorkRequest de 24 h con initialDelay ─
 * WorkManager NO permite programar "a una hora exacta"; el mínimo intervalo
 * periódico es 15 min y el disparo real puede desviarse algunos minutos según
 * las restricciones del sistema (Doze, batería). Para apuntar a ~8:00 AM local:
 *   • initialDelay = tiempo desde ahora hasta la próxima 8:00 AM.
 *   • repeatInterval = 24 h → cada día vuelve a caer cerca de las 8:00 AM.
 *   • flexInterval de 1 h → el sistema tiene margen para agrupar el trabajo y
 *     ahorrar batería; el disparo ocurre dentro de esa ventana (no al segundo).
 * Esto cumple el requisito: cerca de las 8:00 AM, sin exigir precisión exacta.
 *
 * Se usa enqueueUniquePeriodicWork con un nombre único, de modo que reprogramar
 * es idempotente y no crea trabajos duplicados.
 */
object DailyReminderScheduler {

    /** Hora local objetivo del recordatorio (24h). */
    private const val TARGET_HOUR = 8
    private const val TARGET_MINUTE = 0

    /**
     * Programa el recordatorio diario. Idempotente: con [ExistingPeriodicWorkPolicy.KEEP]
     * si ya había un trabajo programado con este nombre, NO se reemplaza (así no se
     * reinicia el initialDelay en cada login). Llamar en el login/registro exitoso.
     */
    fun schedule(context: Context) {
        val initialDelayMs = computeInitialDelayMillis()

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS // flexInterval: ventana de ~1 h para el disparo.
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Cancela el recordatorio diario. Llamar al cerrar sesión, para no notificar a
     * un usuario que ya no tiene sesión (el Worker igual verifica la sesión, pero
     * cancelar evita trabajo innecesario y reprograma limpio en el próximo login).
     */
    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyReminderWorker.UNIQUE_WORK_NAME)
    }

    /**
     * Milisegundos desde ahora hasta la próxima ocurrencia de TARGET_HOUR:TARGET_MINUTE
     * en hora local. Si esa hora ya pasó hoy, apunta a mañana.
     */
    private fun computeInitialDelayMillis(): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, TARGET_HOUR)
            set(Calendar.MINUTE, TARGET_MINUTE)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Si las 8:00 AM de hoy ya pasaron, la próxima es mañana.
        if (next.timeInMillis <= now.timeInMillis) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }
}
