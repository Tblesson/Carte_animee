package com.esaip.carte_animee

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PageAccueil : AppCompatActivity() {


    val items: List<Array<Any?>> = listOf(
        arrayOf(1, "Description animaux", "Animaux"),
        arrayOf(2, "Description véhicules", "Véhicules"),
        arrayOf(3, "Description bâtiments", "Bâtiments"),
        arrayOf(4, "Description nourriture", "Nourriture")
    )






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_accueil)

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<ImageView>(R.id.btn_logout)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val requeteApi = RequeteApi()
        var id = 0
        // Accédez aux informations de l'utilisateur depuis l'objet Singleton
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (userInfo != null && roleInfo != null) {
            val nom = userInfo.nom
            val prenom = userInfo.prenom
            id = userInfo.idUser

            fullName.text = "$nom $prenom"

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val tab = withContext(Dispatchers.IO) {
                        requeteApi.listeSeriesByUser(id)
                    }
                    recyclerView.adapter = MyAdapter(tab.toList())
                } catch (e: Exception) {
                    // Gérer les erreurs ici
                    e.printStackTrace()
                }
            }



        }




        //btn retour
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            showExitConfirmationDialog()
        }
        btn_logout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                showExitConfirmationDialog()
            }
        })

        // Gestion refresh
        swipeRefreshLayout.setOnRefreshListener {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val tab = withContext(Dispatchers.IO) {
                        requeteApi.listeSeriesByUser(id)
                    }
                    recyclerView.adapter = MyAdapter(tab.toList())
                } catch (e: Exception) {
                    // Gérer les erreurs ici
                    e.printStackTrace()
                } finally {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }




    }





    // Class liste custom
    class MyAdapter(private val items: List<Array<Any?>>) :
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
            val item = items[position][2]
            holder.textViewItem.text = item.toString()
            holder.textViewLineNumber.text = (position + 1).toString()
            holder.textViewItem.setTextColor(Color.WHITE);
            holder.textViewLineNumber.setTextColor(Color.WHITE)
            holder.itemView.setOnClickListener {
                //println(holder.textViewItem.text+" id : "+items[position][0])


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


