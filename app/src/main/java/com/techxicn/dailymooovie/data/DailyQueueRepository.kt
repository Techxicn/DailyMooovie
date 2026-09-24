package com.techxicn.dailymooovie.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.Collections

/**
 * DailyQueueRepository
 * ════════════════════
 * Maneja la "cola diaria" por usuario, que define QUÉ película corresponde a QUÉ
 * día — reemplazando el antiguo releaseDate fijo compartido por todos.
 *
 * Estructura en Realtime Database:
 *   • /users/{uid}/dailyQueue       → lista ordenada de movieIds (todo el catálogo
 *                                      en un orden aleatorio único por usuario).
 *   • /users/{uid}/queueStartDate   → fecha ("yyyy-MM-dd") que corresponde a la
 *                                      posición 0 de dailyQueue.
 *
 * La "película del día" para una fecha D se resuelve así:
 *   índice = daysBetween(queueStartDate, D) mód dailyQueue.size
 *   movieId = dailyQueue[índice]
 * Para fechas anteriores a queueStartDate (el usuario aún no tenía cuenta) no hay
 * película: [resolveMovieIdForDate] devuelve null.
 *
 * Todas las operaciones son suspend y usan Task.await(), coherente con el resto
 * del proyecto (AuthManager, MovieRepository, UserMovieRepository).
 */
class DailyQueueRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val movieRepo: MovieRepository = MovieRepository()
) {

    /** uid del usuario actual, o null si no hay sesión. */
    private val uid: String? get() = auth.currentUser?.uid

    private fun requireUid(): String =
        uid ?: throw IllegalStateException("No hay usuario autenticado.")

    /** /users/{uid} */
    private fun userRef(): DatabaseReference =
        db.getReference("users").child(requireUid())

    private fun dailyQueueRef(): DatabaseReference = userRef().child("dailyQueue")
    private fun queueStartDateRef(): DatabaseReference = userRef().child("queueStartDate")

    // ─────────────────────────────────────────────────────────────────────
    //  LECTURAS DE LA COLA
    // ─────────────────────────────────────────────────────────────────────

    /** dailyQueue del usuario (lista ordenada de movieIds), o vacía si no existe. */
    suspend fun getQueue(): List<String> {
        if (uid == null) return emptyList()
        val snapshot = dailyQueueRef().get().await()
        return snapshot.children.mapNotNull { it.getValue(String::class.java) }
    }

    /** queueStartDate del usuario ("yyyy-MM-dd"), o null si no existe. */
    suspend fun getQueueStartDate(): String? {
        if (uid == null) return null
        val snapshot = queueStartDateRef().get().await()
        return snapshot.getValue(String::class.java)?.ifBlank { null }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  GENERACIÓN INICIAL
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Genera la cola en el PRIMER acceso del usuario si aún no existe:
     *   • Toma todos los ids de /movies.
     *   • Los mezcla aleatoriamente (Collections.shuffle).
     *   • Guarda dailyQueue + queueStartDate = hoy.
     *
     * Idempotente: si el usuario ya tiene dailyQueue, no hace nada. Devuelve true
     * si generó una cola nueva, false si ya existía (o no hay catálogo/sesión).
     */
    suspend fun ensureQueueGenerated(): Boolean {
        if (uid == null) return false
        // Ya existe una cola: no regenerar (respeta el orden del usuario).
        if (getQueue().isNotEmpty()) return false

        val allIds = movieRepo.getAllMovieIds()
        if (allIds.isEmpty()) return false

        val shuffled = allIds.toMutableList().also { Collections.shuffle(it) }
        writeQueue(shuffled, DateUtils.today())
        return true
    }

    /**
     * Sincroniza el catálogo con la cola del usuario:
     *   • Compara los ids de /movies contra los ya presentes en dailyQueue.
     *   • Si hay ids nuevos que faltan, los mezcla ENTRE SÍ y los agrega al FINAL
     *     de la cola existente (sin tocar el orden de lo que ya había).
     *
     * No hace nada si el usuario aún no tiene cola (usar [ensureQueueGenerated]
     * primero) o si no hay ids nuevos. Devuelve el número de ids agregados.
     */
    suspend fun syncCatalog(): Int {
        if (uid == null) return 0
        val currentQueue = getQueue()
        // Sin cola previa no hay nada que sincronizar (se genera aparte).
        if (currentQueue.isEmpty()) return 0

        val allIds = movieRepo.getAllMovieIds()
        val existing = currentQueue.toHashSet()
        val newIds = allIds.filterNot { existing.contains(it) }
        if (newIds.isEmpty()) return 0

        val shuffledNew = newIds.toMutableList().also { Collections.shuffle(it) }
        val updatedQueue = currentQueue + shuffledNew
        // Solo se reescribe la lista; queueStartDate no cambia.
        dailyQueueRef().setValue(updatedQueue).await()
        return shuffledNew.size
    }

    // ─────────────────────────────────────────────────────────────────────
    //  RESOLUCIÓN DÍA → PELÍCULA
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Resuelve el movieId que corresponde a [date] ("yyyy-MM-dd") para el usuario
     * actual, usando su dailyQueue y queueStartDate:
     *
     *   índice = daysBetween(queueStartDate, date) mód dailyQueue.size
     *
     * Devuelve null si:
     *   • no hay sesión, cola o queueStartDate,
     *   • la fecha es anterior a queueStartDate (el usuario no tenía cuenta aún),
     *   • la fecha es inválida.
     */
    suspend fun resolveMovieIdForDate(date: String): String? {
        val queue = getQueue()
        if (queue.isEmpty()) return null
        val startDate = getQueueStartDate() ?: return null
        return resolveMovieIdForDate(date, queue, startDate)
    }

    /**
     * Variante pura que resuelve el movieId sin volver a leer de la red. Útil para
     * el catálogo mensual: se leen [queue] y [startDate] UNA vez y se resuelven
     * todos los días del mes en memoria.
     */
    fun resolveMovieIdForDate(
        date: String,
        queue: List<String>,
        startDate: String
    ): String? {
        if (queue.isEmpty()) return null
        val offset = DateUtils.daysBetween(startDate, date) ?: return null
        // Fechas anteriores al inicio de la cola: el usuario no tenía cuenta aún.
        if (offset < 0) return null
        val index = offset % queue.size
        return queue[index]
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────────────

    /** Escribe dailyQueue + queueStartDate en un solo update atómico. */
    private suspend fun writeQueue(queue: List<String>, startDate: String) {
        val value = mapOf(
            "dailyQueue" to queue,
            "queueStartDate" to startDate
        )
        userRef().updateChildren(value).await()
    }
}
