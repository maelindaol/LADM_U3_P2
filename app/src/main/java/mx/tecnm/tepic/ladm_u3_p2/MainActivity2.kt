package mx.tecnm.tepic.ladm_u3_p2

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseDatos = BaseDatos(this,"basedatos1",null,1)
    var id = "al"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var extra = intent.extras
        var id = extra!!.getString("idactualizar")!!
        idactualizar.setText(idactualizar.text.toString()+"${id}")
        try {
            var base = baseDatos.readableDatabase
            var respuesta = base.query("EVENTO", arrayOf("LUGAR","FECHA","HORA","DESCRIPCION"),"ID=?",
                arrayOf(id), null, null, null)
            if(respuesta.moveToFirst()){
              Actlugar.setText(respuesta.getString(0))
              Actfecha.setText(respuesta.getString(1))
              Acthora.setText(respuesta.getString(2))
              Actdescrip.setText(respuesta.getString(3))
            }else{
                mensaje("ERROR! no se encontro ID")
            }
            base.close()
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }
        buttonActualizar2.setOnClickListener {
            actualizar(id)
        }
        buttonRegresar2.setOnClickListener {
            finish()
        }
    }


    private fun actualizar(id:String){
        try {
            var trans = baseDatos.writableDatabase
            var valores = ContentValues()
            valores.put("LUGAR",Actlugar.text.toString())
            valores.put("FECHA",Actfecha.text.toString())
            valores.put("HORA",Acthora.text.toString())
            valores.put("DESCRIPCION",Actdescrip.text.toString())
            var res = trans.update("EVENTO",valores,"ID=?", arrayOf(id))
            if (res>0){
                mensaje("Se actualizo correctamente ID ${id}")
                finish()
            }else{
                mensaje("No se pudo actualizar ID")
            }
            trans.close()
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }

    }
    private fun mensaje(s:String){
        AlertDialog.Builder(this)
            .setMessage(s)
            .setTitle("ATENCION")
            .setPositiveButton("OK"){d,i->}
            .show()
    }
}