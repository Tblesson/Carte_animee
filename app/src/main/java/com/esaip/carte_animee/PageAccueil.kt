package com.esaip.carte_animee

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PageAccueil : AppCompatActivity() {

    val items = arrayOf(
        "Animaux",
        "Véhicules",
        "Bâtiments",
        "Nourriture",
        "Paysages",
        "Fruits",
        "Personnes",
        "Plantes",
        "Sports",
        "Technologie"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_accueil)

        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<Button>(R.id.btn_logout)


        // Accédez aux informations de l'utilisateur depuis l'objet Singleton
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo

        if (userInfo != null && roleInfo != null) {
            // Les informations de l'utilisateur sont disponibles, vous pouvez les afficher
            val nom = userInfo.nom
            val prenom = userInfo.prenom

            // Affichez les informations dans votre interface utilisateur
            fullName.text = "$nom $prenom"
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter(items.toList())


        //btn retour
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            showExitConfirmationDialog()
        }
        btn_logout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                showExitConfirmationDialog()
            }
        })

    }

    // Class liste custom
    class MyAdapter(private val items: List<String>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private var listener: OnItemClickListener? = null

        interface OnItemClickListener {
            fun onItemClick(item: String)
        }

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewItem: TextView = itemView.findViewById(R.id.titre)
            val textViewLineNumber: TextView = itemView.findViewById(R.id.id)


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            view.setBackgroundResource(R.drawable.background_btn_connexion)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = items[position]
            holder.textViewItem.text = item
            holder.textViewLineNumber.text = (position + 1).toString()
            holder.textViewItem.setTextColor(Color.WHITE);
            holder.textViewLineNumber.setTextColor(Color.WHITE)
            holder.itemView.setOnClickListener {
                println(holder.textViewItem.text)
            }

        }

        override fun getItemCount(): Int {
            return items.size
        }


    }


    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Voulez-vous vraiment vous déconnecter ?")
            .setCancelable(false)
            .setPositiveButton("Oui") { dialog, id ->
                val intent = Intent(baseContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Non") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

}


