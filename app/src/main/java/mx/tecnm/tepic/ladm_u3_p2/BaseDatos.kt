package mx.tecnm.tepic.ladm_u3_p2

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDatos (
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE EVENTO(ID INT NOT NULL PRIMARY KEY,LUGAR VARCHAR(250),FECHA DATE,HORA VARCHAR(50),DESCRIPCION VARCHAR(500))")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}