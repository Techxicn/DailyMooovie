package com.techxicn.dailymooovie.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DateUtils.kt
 * ════════════
 * Utilidades de fecha compartidas por los repositorios. Todas las fechas que
 * viajan a/desde Realtime Database usan el formato ISO "yyyy-MM-dd", de modo que
 * ordenan lexicográficamente igual que cronológicamente (útil para queries por
 * rango sobre releaseDate).
 */
object DateUtils {

    private const val ISO_DATE = "yyyy-MM-dd"

    private fun isoFormatter(): SimpleDateFormat =
        SimpleDateFormat(ISO_DATE, Locale.US)

    /** Fecha de hoy como "yyyy-MM-dd". */
    fun today(): String = isoFormatter().format(Date())

    /** Convierte un Date a "yyyy-MM-dd". */
    fun format(date: Date): String = isoFormatter().format(date)

    /** Parsea "yyyy-MM-dd" a Date, o null si el formato es inválido. */
    fun parse(iso: String): Date? = try {
        isoFormatter().parse(iso)
    } catch (_: Exception) {
        null
    }

    /**
     * Prefijo "yyyy-MM" para un año/mes dados (month es 1..12). Se usa para
     * filtrar el catálogo de un mes por releaseDate.
     */
    fun monthPrefix(year: Int, month: Int): String =
        "%04d-%02d".format(year, month)

    /** Prefijo "yyyy-MM" del mes actual. */
    fun currentMonthPrefix(): String {
        val cal = Calendar.getInstance()
        return monthPrefix(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    /** Año calendario de la fecha ISO, o null si es inválida. */
    fun yearOf(iso: String): Int? {
        val date = parse(iso) ?: return null
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.YEAR)
    }

    /**
     * Diferencia en días entre dos fechas ISO (b - a), o null si alguna es
     * inválida. Positivo si [b] es posterior a [a].
     */
    fun daysBetween(a: String, b: String): Int? {
        val da = parse(a) ?: return null
        val db = parse(b) ?: return null
        val millis = db.time - da.time
        return Math.round(millis / (1000.0 * 60 * 60 * 24)).toInt()
    }
}
