package mx.tecnm.tepic.ladm_u3_p2

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var baseDatos = BaseDatos(this,"basedatos1",null,1)
    var listaID = ArrayList<String>()
    var idSeleccionadoLista = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSincronizar.setOnClickListener {
            insertarCloud()
        }
        buttonInsertar.setOnClickListener {
            insertar();
        }
        cargarContactos()
    }
    private fun insertarCloud(){
        var adapter = lista.adapter
        for (i in 0..(adapter.count-1) ){
            var concatenacion = adapter.getItem(i).toString()
            var attributs_del_obj = concatenacion.lines()
            val datosInsertar = hashMapOf(
                "ID" to attributs_del_obj[0].split(":")[1],
                "LUGAR" to attributs_del_obj[1].split(":")[1],
                "FECHA" to attributs_del_obj[2].split(":")[1],
                "HORA" to attributs_del_obj[3].split(" ")[1],
                "DESCRIPCION" to attributs_del_obj[4].split(":")[1]
            )
            baseRemota.collection("evento")
                .document(attributs_del_obj[0].split(":")[1])
                .set(datosInsertar as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this,"SE SINCRONIZÓ CORRECTAMENTE ", Toast.LENGTH_LONG)
                        .show()
                    lugar.setText("")
                }
                .addOnFailureListener {
                    mensaje("NO SE PUDO INSERTAR:\n${it.message!!}")
                }
            /* baseRemota.collection("evento")
                .add(datosInsertar as Any)
                .addOnSuccessListener {
                    Toast.makeText(this,"SE SINCRONIZÓ CORRECTAMENTE ${it.id}", Toast.LENGTH_LONG)
                        .show()
                    lugar.setText("")
                }
                .addOnFailureListener {
                    mensaje("NO SE PUDO INSERTAR:\n${it.message!!}")
                } */
        }

    }
    private fun insertar(){
        try{
            var trans = baseDatos.writableDatabase
            var variables = ContentValues()
            variables.put("ID",idevento.text.toString().toInt())
            variables.put("LUGAR",lugar.text.toString())
            variables.put("FECHA",fecha.text.toString())
            variables.put("HORA",time.text.toString())
            variables.put("DESCRIPCION",descrip.text.toString())
            var respuesta = trans.insert("EVENTO",null,variables)
            if (respuesta==-1L){
                mensaje("ERROR NO SE PUDO INSERTAR")
            }else{
                mensaje("SE INSERTÓ CON EXITO")
                limpiarCampos()
            }
            trans.close()
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }
        cargarContactos()
    }
    private fun eliminar(){
        baseRemota.collection("EVENTO")
            .document()
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"SE ELIMINO",Toast.LENGTH_LONG)
                    .show()
            }
            .addOnFailureListener {
                mensaje("ERROR: NO SE ELIMINO\n"+it.message)
            }
    }
    private fun limpiarCampos(){
        idevento.setText("")
        lugar.setText("")
        fecha.setText("")
        time.setText("")
        descrip.setText("")
    }
    private fun mensaje(s:String){
        AlertDialog.Builder(this)
            .setMessage(s)
            .setTitle("ATENCION")
            .setPositiveButton("OK"){d,i->}
            .show()
    }
    private fun cargarContactos(){
        try {
            var trans = baseDatos.readableDatabase
            var eventos = ArrayList<String>()
            var respuesta = trans.query("EVENTO", arrayOf("*"), null, null, null, null, null)
            listaID.clear()
            if (respuesta.moveToFirst()){
                do {
                    var concatenacion = "ID:${respuesta.getString(0)}\nLUGAR:${respuesta.getString(1)}\nFECHA:" +
                            "${respuesta.getString(2)}\nHORA: ${respuesta.getString(3)}\nDESCRIPCION:${respuesta.getString(4)}"
                    eventos.add(concatenacion)
                    listaID.add(respuesta.getInt(0).toString())
                }while (respuesta.moveToNext())
            }else{
                eventos.add("NO HAY EVENTOS INSERTADOS")
            }
            lista.adapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,eventos)
            this.registerForContextMenu(lista)
            lista.setOnItemClickListener { adapterView, view, i, l ->
                idSeleccionadoLista = i
                Toast.makeText(this,"Se seleccionó elemento", Toast.LENGTH_LONG)
                    .show()
            }
            trans.close()
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }

    }
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        var inflaterDB = menuInflater
        inflaterDB.inflate(R.menu.menuprincipal,menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (idSeleccionadoLista==-1){
            mensaje("ERROR! debes dar clic primero en un item para ACTUALIZAR/BORRAR")
            return true
        }
        when(item.itemId){
            R.id.itemactualizar->{
                var idEliminar = listaID.get(idSeleccionadoLista)
                var intent = Intent(this,MainActivity2::class.java)
                intent.putExtra("idactualizar",idEliminar)
                startActivity(intent)

            }
            R.id.itemeliminar->{
                var idEliminar = listaID.get(idSeleccionadoLista)
                AlertDialog.Builder(this)
                    .setTitle("ATENCION")
                    .setMessage("ESTAS SEGURO QUE DESEAS ELIMINAR ID: "+idEliminar+"?")
                    .setPositiveButton("ELIMINAR"){d,i->
                        eliminar(idEliminar)
                    }
                    .setNeutralButton("NO"){d,i->}
                    .show()
            }
            R.id.itemsalir->{
            }
        }
        idSeleccionadoLista=-1
        return true
    }
    private fun eliminar(ideliminar:String){
        try {
            var trans = baseDatos.writableDatabase
            var resultado = trans.delete("EVENTO","ID=?",
                arrayOf(ideliminar))
            if (resultado==0){
                mensaje("ERROR! No se pudo eliminar")
            }else{
                mensaje("Se logro eliminar con éxito el ID ${ideliminar}")
            }
            trans.close()
            cargarContactos()
        }catch (e:SQLiteException) {
            mensaje(e.message!!)
        }
    }
    override fun onResume() {
        super.onResume()
        cargarContactos()
    }

}