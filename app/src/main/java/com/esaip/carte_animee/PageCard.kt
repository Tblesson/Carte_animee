package com.esaip.carte_animee

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageCard : AppCompatActivity() {

    val requeteApi = RequeteApi()
    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_card)


        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<ImageView>(R.id.btn_logout)
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo

        if (userInfo != null && roleInfo != null) {
            val nom = userInfo.nom
            val prenom = userInfo.prenom
            id = userInfo.idUser

            fullName.text = "$nom $prenom"

            val idSerie = intent.getStringExtra("id_serie").toString()
            //System.out.println(idSerie)
            getCards(idSerie)

        }



        btn_logout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                showExitConfirmationDialog()
            }
        })

        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            finish()
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

    private fun getCards(idSerie: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val cartes = withContext(Dispatchers.IO) {
                    requeteApi.listeCardsByIdSeries(idSerie)
                }
                println("Cartes récupérées:")
                if (cartes.isEmpty()) {
                    showCardsEmptyDialog()
                } else {
                    cartes.forEach { carte ->
                        println("ID: ${carte[0]}, Intitulé: ${carte[1]}, ID Image: ${carte[2]}, ID Son: ${carte[3]}, ID Série: ${carte[4]}, Description: ${carte[5]}")
                    }
                }
            } catch (e: Exception) {
                // Gérer les exceptions ici
                e.printStackTrace()
            }
        }
    }

    private fun showCardsEmptyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Cette liste n'a pas de cartes")
            .setCancelable(false)
            .setPositiveButton("retour") { dialog, id ->
                val intent = Intent(baseContext, PageAccueil::class.java)
                startActivity(intent)
                finish()
            }

        val alert = builder.create()
        alert.show()
    }


}