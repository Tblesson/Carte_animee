package com.esaip.carte_animee

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.StrictMode
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PageCard : AppCompatActivity() {

    val requeteApi = RequeteApi()
    var id = 0
    private lateinit var nbCard: TextView
    private var cartes: Array<Array<Any?>> = emptyArray()
    private var numCard: Int = 1
    private var typeCard: Int = 2
    private var statut: Int = -1
    private lateinit var webView: WebView
    private lateinit var description: TextView
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var btn_precedent: Button
    private lateinit var btn_suivant: Button
    private lateinit var btn_terminer: Button
    private lateinit var btn_swap: Button

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_card)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())


        val fullName = findViewById<TextView>(R.id.FullName)
        val btn_logout = findViewById<ImageView>(R.id.btn_logout)
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo
        btn_suivant = findViewById<Button>(R.id.btn_suivant)
        btn_precedent = findViewById<Button>(R.id.btn_precedent)
        webView = findViewById<WebView>(R.id.webView)
        description = findViewById<TextView>(R.id.descriptionCard)
        btn_terminer = findViewById<Button>(R.id.btn_terminer)
        btn_swap = findViewById<Button>(R.id.btn_swap)



        nbCard = findViewById(R.id.nbCards)


        if (userInfo != null && roleInfo != null) {
            val nom = userInfo.nom
            val prenom = userInfo.prenom
            id = userInfo.idUser

            fullName.text = "$nom $prenom"

            val idSerie = intent.getStringExtra("id_serie").toString()
            // Récupérer la valeur int extra avec une clé spécifiée et une valeur par défaut
            // Récupérer la valeur Int directement
            numCard = intent.getStringExtra("lastPosition").toString().toInt()
            statut = intent.getStringExtra("statut").toString().toInt()

            System.out.println("carte "+statut.toString())

            getCards(idSerie)
            gestionBtn()
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
                    setImage(numCard,2)
                    gestionBtn()
                    System.out.println("id de la carte "+cartes[numCard-1][4].toString()+" id lastcard "+cartes[numCard-1][0].toString())
                    btn_swap.setText("Image Réel")
                    typeCard = 2
                    requeteApi.updateLastCard(cartes[numCard-1][4].toString(),cartes[numCard-1][0].toString())
                }
            }

        })

        btn_precedent.setOnClickListener(object :View.OnClickListener {
            override fun onClick(p0: View?) {
                if(numCard>1){
                    numCard = numCard-1
                    nbCard.text = "${numCard}/${cartes.size}"
                    mediaPlayer?.stop()
                    setImage(numCard,2)
                    gestionBtn()
                    System.out.println("id de la carte "+cartes[numCard-1][4].toString()+" id lastcard "+cartes[numCard-1][0].toString())
                    btn_swap.setText("Image Réel")
                    typeCard = 2
                    requeteApi.updateLastCard(cartes[numCard-1][4].toString(),cartes[numCard-1][0].toString())
                }

            }

        })
        btn_terminer.setOnClickListener(object :View.OnClickListener {
            override fun onClick(p0: View?) {
                onBackPressedDispatcher
                if(statut!=3){
                    requeteApi.closeSeriesUser(cartes[numCard-1][4].toString(),id)
                }
                finish()
            }

        })


        btn_swap.setOnClickListener(object :View.OnClickListener {
            override fun onClick(p0: View?) {
                onBackPressedDispatcher

                if (typeCard==2){
                    btn_swap.setText("Image Fictive")
                    typeCard = 6
                    setImage(numCard,6)

                }else{
                    typeCard = 2
                    btn_swap.setText("Image Réel")
                    setImage(numCard,2)
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
               // System.out.println("id de la series:"+ cartes[numCard-1][0].toString()
                if (cartes.isEmpty()) {
                    showCardsEmptyDialog()
                } else {
                    if(numCard==-1){
                        // Premiere ouverture de la séries
                        numCard = 1;
                        requeteApi.updateLastCard(cartes[numCard-1][4].toString(),cartes[numCard-1][0].toString())
                    }
                    nbCard.text = "${numCard}/${cartes.size}"
                    //affichage Premiere carte
                    setImage(numCard,2)
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


    private fun setImage(numCard: Int, typeCard: Int) {
        description.text = cartes[numCard - 1][1].toString()

        val webView: WebView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false


        val imageUrl = "https://seriespoil.fr/PFE/carteAnimees/image/${cartes[numCard - 1][typeCard]}.gif"

        val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body, html {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    width: 100%;
                }
                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            </style>
        </head>
        <body>
            <img src="$imageUrl" alt="Image" />
        </body>
        </html>
    """


        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }





    private fun setSon(numCard: Int) {
        // Couper le son en cours
        mediaPlayer?.stop()

        mediaPlayer = MediaPlayer().apply {
            setDataSource("https://seriespoil.fr/PFE/carteAnimees/son/${cartes[numCard - 1][3]}.mp3")
            prepareAsync()
            setOnPreparedListener {
                it.start()
            }
        }
    }

    private fun gestionBtn(){

        // Gestion btn précedent
        if(numCard==1 &&  numCard!=cartes.size){
            btn_precedent.visibility = View.GONE
            btn_terminer.visibility = View.GONE
        }else if (numCard==cartes.size && cartes.size!=1){
            btn_suivant.visibility = View.GONE
            btn_terminer.visibility = View.VISIBLE
        }else if(numCard==cartes.size && cartes.size==1){
            btn_suivant.visibility = View.GONE
            btn_precedent.visibility = View.GONE
            btn_terminer.visibility = View.VISIBLE
        }else{
            btn_precedent.visibility = View.VISIBLE
            btn_suivant.visibility = View.VISIBLE
            btn_terminer.visibility = View.GONE
        }



    }
    // Dans la méthode onBackPressed
    override fun onBackPressed() {
        mediaPlayer?.stop()
        super.onBackPressed()
    }


}