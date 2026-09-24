package com.techxicn.dailymooovie.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.techxicn.dailymooovie.data.DailyQueueRepository
import com.techxicn.dailymooovie.data.DateUtils
import com.techxicn.dailymooovie.data.MovieRepository

/**
 * DailyReminderWorker
 * ═══════════════════
 * Trabajo diario (programado por [DailyReminderScheduler] ~8:00 AM local) que:
 *   1. Verifica que haya un usuario autenticado; si no, no hace nada.
 *   2. Resuelve la película del día con la MISMA lógica que Today:
 *        DailyQueueRepository.resolveMovieIdForDate(hoy) → MovieRepository.getMovieById.
 *   3. Muestra la notificación local con el título (deep-link a Today al tocar).
 *
 * Es un CoroutineWorker: doWork() es suspend, así que las lecturas suspend de los
 * repositorios (Task.await()) encajan sin bloquear hilos.
 *
 * Política de reintentos: ante error transitorio (p. ej. red al leer RTDB) se
 * devuelve Result.retry() para que WorkManager lo reintente con backoff. Sin
 * sesión o sin película del día se devuelve Result.success() (no hay nada que
 * notificar; no es un fallo).
 */
class DailyReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 1) Sin sesión activa → no notificar. (No es error: el usuario cerró sesión
        //    o la programación quedó de una sesión anterior; se sale limpio.)
        if (FirebaseAuth.getInstance().currentUser == null) {
            return Result.success()
        }

        return try {
            val queueRepo = DailyQueueRepository()
            val movieRepo = MovieRepository()

            // 2) Película del día: mismo cálculo que TodayFragment.
            val today = DateUtils.today()
            val movieId = queueRepo.resolveMovieIdForDate(today)
                ?: return Result.success() // aún sin cola generada → nada que notificar
            val movie = movieRepo.getMovieById(movieId)
                ?: return Result.success()

            val title = movie.title
            if (title.isBlank()) return Result.success()

            // 3) Notificar (si falta el permiso 13+, showDailyMovie devuelve false;
            //    igualmente es success: no hay nada más que hacer).
            NotificationHelper.showDailyMovie(applicationContext, title)
            Result.success()
        } catch (_: Exception) {
            // Error probablemente transitorio (red). Deja que WorkManager reintente.
            Result.retry()
        }
    }

    companion object {
        /** Nombre único del trabajo periódico (para enqueue/cancel). */
        const val UNIQUE_WORK_NAME = "daily_movie_reminder_work"
    }
}
