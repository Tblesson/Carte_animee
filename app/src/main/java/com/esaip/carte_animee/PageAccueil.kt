package com.esaip.carte_animee

import android.R.layout
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text


class PageAccueil : AppCompatActivity() {


    val requeteApi = RequeteApi()
    var id = 0
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_accueil)

        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<ImageView>(R.id.btn_logout)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)


        // Accédez aux informations de l'utilisateur depuis l'objet Singleton
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (userInfo != null && roleInfo != null) {
            val nom = userInfo.nom
            val prenom = userInfo.prenom
            id = userInfo.idUser

            fullName.text = "$nom $prenom"
            onRefresh()

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
            onRefresh()
        }
    }


    // Class liste custom
    class MyAdapter(private val items: List<Array<Any?>>, private val context: Context) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        private var listener: OnItemClickListener? = null
        val requeteApi = RequeteApi()

        interface OnItemClickListener {
            fun onItemClick(item: String)
        }

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.listener = listener
        }

        inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewItem: TextView = itemView.findViewById(R.id.titre)
            val textViewLineNumber: TextView = itemView.findViewById(R.id.id)
            val icone_lock: ImageView = itemView.findViewById(R.id.icon_lock)
            val cardView: CardView = itemView.findViewById(R.id.cardViewId)
            val pourcentage: TextView = itemView.findViewById(R.id.pourcentage);
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


            val nbCartes = items[position][4].toString().toInt()
            val cardPosition = items[position][5].toString().toInt()


            if ( nbCartes!= 0 && cardPosition != -1) {
                val pourcentage = (cardPosition * 100) / nbCartes // Calculez le pourcentage
                holder.pourcentage.text = "$pourcentage%" // Définissez le texte avec le pourcentage calculé
            } else {
                holder.pourcentage.text = "0%" // Ou tout autre texte que vous souhaitez afficher
            }

            val item = items[position][2]

            holder.textViewItem.text = item.toString()
            holder.textViewLineNumber.text = (position + 1).toString()
            if (items[position][1] == 1) {
                holder.icone_lock.visibility = View.VISIBLE
                holder.icone_lock.setBackgroundResource(R.drawable.icone_series_lock)
                holder.itemView.isEnabled = false
                if ( nbCartes!= 0 && cardPosition != -1) {
                    val pourcentage = (cardPosition * 100) / nbCartes // Calculez le pourcentage
                    if(pourcentage==100){
                        holder.cardView.setCardBackgroundColor(Color.parseColor("#d0ffaf"))
                    }
                }
            } else if (items[position][1] == 3) {
                holder.icone_lock.visibility = View.INVISIBLE
                holder.cardView.setCardBackgroundColor(Color.parseColor("#d0ffaf"))
                holder.itemView.isEnabled = true
            } else if (items[position][1] == 2) {
                holder.icone_lock.visibility = View.INVISIBLE
                holder.itemView.isClickable = true
            }


            //System.out.println("la series "+items[position][0])
            holder.cardView.setOnClickListener {
                val intent = Intent(context, PageCard::class.java)
                intent.putExtra("id_serie", items[position][0].toString())
                intent.putExtra("lastPosition", cardPosition.toString())
                intent.putExtra("statut",items[position][1].toString())
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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

    private fun onRefresh() {

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val tab = withContext(Dispatchers.IO) {
                    requeteApi.listeSeriesByUser(id)
                }

                if (tab.isEmpty()) {
                    val bar = Snackbar.make(
                        swipeRefreshLayout,
                        "Erreur lors de la récupération",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("Réessayer") {
                            onRefresh()
                        }
                    bar.show()
                }
                recyclerView.adapter = MyAdapter(tab.toList(), baseContext)
            } catch (e: Exception) {
                // Gérer les erreurs ici
                e.printStackTrace()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }

        }
    }

    override fun onResume() {
        super.onResume()
        // Votre code à exécuter lorsque l'activité reprend
        System.out.println("refresh")
        onRefresh()
    }


}


