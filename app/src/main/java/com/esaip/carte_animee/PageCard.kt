package com.esaip.carte_animee

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageCard : AppCompatActivity() {

    val requeteApi = RequeteApi()
    var id = 0
    private lateinit var nbCard: TextView
    private var cartes: Array<Array<Any?>> = emptyArray()
    private var numCard: Int = 1
    private lateinit var webView: WebView
    private lateinit var description: TextView
    private var mediaPlayer: MediaPlayer? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_card)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())


        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<ImageView>(R.id.btn_logout)
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo
        val btn_suivant = findViewById<Button>(R.id.btn_suivant)
        val btn_precedent = findViewById<Button>(R.id.btn_precedent)
        webView = findViewById<WebView>(R.id.webView)
        description = findViewById<TextView>(R.id.descriptionCard)



        nbCard = findViewById(R.id.nbCards)


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

        btn_suivant.setOnClickListener(object :View.OnClickListener {
            override fun onClick(p0: View?) {
                if(numCard<cartes.size){
                    numCard = numCard+1
                    nbCard.text = "${numCard}/${cartes.size}"
                    mediaPlayer?.stop()
                    setImage(numCard)
                }
            }

        })

        btn_precedent.setOnClickListener(object :View.OnClickListener {
            override fun onClick(p0: View?) {
                if(numCard>1){
                    numCard = numCard-1
                    nbCard.text = "${numCard}/${cartes.size}"
                    mediaPlayer?.stop()
                    setImage(numCard)
                }

            }

        })

        webView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    setSon(numCard)
                    true
                }
                else -> false
            }
        }








        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            mediaPlayer?.stop()
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
        try {
            cartes = requeteApi.listeCardsByIdSeries(idSerie)
            runOnUiThread {
                // Mettez ici le code pour traiter les cartes récupérées
                if (cartes.isEmpty()) {
                    showCardsEmptyDialog()
                } else {
                    nbCard.text = "${numCard}/${cartes.size}"
                    //affichage Premiere carte
                    setImage(numCard)
                }
            }
        } catch (e: Exception) {
            // Gérer les exceptions ici
            e.printStackTrace()
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


    private fun setImage(numCard: Int){

        description.setText(cartes[numCard-1][1].toString())
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://www.demineur-ligne.com/PFETTCV/carteAnimees/image/"+cartes[numCard-1][2].toString()+".gif")

    }
    private fun setSon(numCard: Int) {
        // Couper le son en cours
        mediaPlayer?.stop()

        mediaPlayer = MediaPlayer().apply {
            setDataSource("https://www.demineur-ligne.com/PFETTCV/carteAnimees/son/${cartes[numCard - 1][3]}.mp3")
            prepareAsync()
            setOnPreparedListener {
                it.start()
            }
        }
    }


}