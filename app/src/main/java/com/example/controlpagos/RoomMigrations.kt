package com.example.controlpagos

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations {
    val MIGRATION_1_2 = noOpMigration(1, 2)
    val MIGRATION_2_3 = noOpMigration(2, 3)
    val MIGRATION_3_4 = noOpMigration(3, 4)
    val MIGRATION_4_5 = noOpMigration(4, 5)

    private fun noOpMigration(from: Int, to: Int) = object : Migration(from, to) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // El esquema actual no cambia en estas transiciones; solo avanzamos la versión.
        }
    }
}
