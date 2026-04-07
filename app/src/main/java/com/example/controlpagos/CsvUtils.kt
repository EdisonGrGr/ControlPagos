package com.example.controlpagos

import com.example.controlpagos.model.Cuenta

fun cuentasToCsv(cuentas: List<Cuenta>): String {
    val header = "nombre,numeroCuenta,monto,fecha"
    val filas = cuentas.joinToString("\n") { cuenta ->
        listOf(
            escaparCsv(cuenta.nombre),
            escaparCsv(cuenta.numeroCuenta),
            cuenta.monto.toString(),
            escaparCsv(cuenta.fecha)
        ).joinToString(",")
    }
    return if (filas.isBlank()) "$header\n" else "$header\n$filas\n"
}

fun csvToCuentas(csv: String): List<Cuenta> {
    val lineas = csv.lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()

    if (lineas.isEmpty()) return emptyList()

    val dataLineas = if (lineas.first().startsWith("nombre,")) lineas.drop(1) else lineas

    return dataLineas.mapNotNull { linea ->
        val columnas = parsearLineaCsv(linea)
        if (columnas.size < 4) return@mapNotNull null

        val nombre = columnas[0].trim()
        val numeroCuenta = columnas[1].trim()
        val monto = columnas[2].trim().toDoubleOrNull() ?: return@mapNotNull null
        val fecha = columnas[3].trim()

        if (nombre.isBlank() || numeroCuenta.isBlank() || fecha.isBlank() || monto <= 0.0) {
            return@mapNotNull null
        }

        if (parseFechaApp(fecha) == null) return@mapNotNull null

        Cuenta(
            nombre = nombre,
            numeroCuenta = numeroCuenta,
            monto = monto,
            fecha = fecha
        )
    }
}

private fun escaparCsv(valor: String): String {
    val necesitaComillas = valor.contains(',') || valor.contains('"') || valor.contains('\n')
    val normalizado = valor.replace("\"", "\"\"")
    return if (necesitaComillas) "\"$normalizado\"" else normalizado
}

private fun parsearLineaCsv(linea: String): List<String> {
    val resultado = mutableListOf<String>()
    val actual = StringBuilder()
    var enComillas = false
    var i = 0

    while (i < linea.length) {
        val c = linea[i]
        when {
            c == '"' && enComillas && i + 1 < linea.length && linea[i + 1] == '"' -> {
                actual.append('"')
                i += 2
                continue
            }
            c == '"' -> enComillas = !enComillas
            c == ',' && !enComillas -> {
                resultado.add(actual.toString())
                actual.clear()
            }
            else -> actual.append(c)
        }
        i++
    }

    resultado.add(actual.toString())
    return resultado
}

