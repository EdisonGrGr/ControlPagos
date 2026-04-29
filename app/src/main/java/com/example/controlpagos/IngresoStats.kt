package com.example.controlpagos

import com.example.controlpagos.model.Ingreso
import java.util.Calendar

fun calcularTotalesMensualesIngresos(
	ingresos: List<Ingreso>,
	meses: Int,
	baseReferencia: Calendar = Calendar.getInstance()
): List<Pair<String, Double>> {
	val mesesCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
	val totalesMap = ingresos.groupBy { ingreso ->
		val fecha = parseFechaApp(ingreso.fecha)
		if (fecha == null) "" else {
			val cal = Calendar.getInstance().apply { time = fecha }
			"${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
		}
	}.mapValues { (_, lista) -> lista.sumOf { it.monto } }

	val base = (baseReferencia.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
	return ((meses - 1).coerceAtLeast(0) downTo 0).map { offset ->
		val cal = base.clone() as Calendar
		cal.add(Calendar.MONTH, -offset)
		val clave = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
		mesesCortos[cal.get(Calendar.MONTH)] to (totalesMap[clave] ?: 0.0)
	}
}

fun totalIngresosDelMesActual(
	ingresos: List<Ingreso>,
	baseReferencia: Calendar = Calendar.getInstance()
): Double {
	val anio = baseReferencia.get(Calendar.YEAR)
	val mes = baseReferencia.get(Calendar.MONTH)
	return ingresos.filter { ingreso ->
		val fecha = parseFechaApp(ingreso.fecha) ?: return@filter false
		val cal = Calendar.getInstance().apply { time = fecha }
		cal.get(Calendar.YEAR) == anio && cal.get(Calendar.MONTH) == mes
	}.sumOf { it.monto }
}

