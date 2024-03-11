package com.esaip.carte_animee

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.logging.Handler


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var isButtonClicked = true
        val editIdentifiant = findViewById<EditText>(R.id.editIdentifiant)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val btn_connexion = findViewById<Button>(R.id.btn_connexion)
        val RequeteApi = RequeteApi()


        //btn connexion
        btn_connexion.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (isButtonClicked){
                    isButtonClicked = false
                    val identifiant = editIdentifiant.text.toString()
                    val password = editPassword.text.toString()

                    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"


                    if (!identifiant.isEmpty()) {
                        if (identifiant.matches(emailPattern.toRegex())) {

                            if (!password.isEmpty()) {
                                val reponseDeferred =
                                    RequeteApi().connexionUtilisateur(identifiant, password)
                                GlobalScope.launch(Dispatchers.Main) {
                                    val reponse = reponseDeferred.await()
                                    if (reponse != null) {
                                        if (reponse) {
                                            val userInfo = com.esaip.carte_animee.RequeteApi.UserSingleton.userInfo
                                            var idrole =-1
                                            if (userInfo != null) {
                                                idrole = userInfo.idRole
                                            }
                                            if(idrole==2){
                                                val intent = Intent(baseContext, PageAccueil::class.java)
                                                startActivity(intent)
                                                finish() // Optionnel : pour fermer l'activité actuelle si nécessaire
                                            }else{

                                                val snackbar = Snackbar.make(
                                                    v,
                                                    "Ce compte n'a pas les droits",
                                                    Snackbar.LENGTH_LONG
                                                )
                                                val view = snackbar.view
                                                snackbar.setBackgroundTint((Color.parseColor("#e30000")))
                                                val params = view.layoutParams as FrameLayout.LayoutParams
                                                params.gravity = Gravity.TOP
                                                view.layoutParams = params
                                                val textView =
                                                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                                textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                                                snackbar.show()
                                                isButtonClicked = true


                                            }



                                        } else {
                                            val snackbar = Snackbar.make(
                                                v,
                                                "Mot de passe incorrect",
                                                Snackbar.LENGTH_LONG
                                            )
                                            val view = snackbar.view
                                            snackbar.setBackgroundTint((Color.parseColor("#e30000")))
                                            val params = view.layoutParams as FrameLayout.LayoutParams
                                            params.gravity = Gravity.TOP
                                            view.layoutParams = params
                                            val textView =
                                                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                                            snackbar.show()
                                            isButtonClicked = true
                                        }
                                    }
                                }

                            } else {
                                val snackbar = Snackbar.make(
                                    v,
                                    "Veuillez saisir un mot de passe",
                                    Snackbar.LENGTH_LONG
                                )
                                val view = snackbar.view
                                snackbar.setBackgroundTint((Color.parseColor("#e30000")))
                                val params = view.layoutParams as FrameLayout.LayoutParams
                                params.gravity = Gravity.TOP
                                view.layoutParams = params
                                val textView =
                                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                                textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                                snackbar.show()
                                isButtonClicked = true
                            }

                        } else {

                            val snackbar = Snackbar.make(v, "Email invalide", Snackbar.LENGTH_LONG)
                            val view = snackbar.view
                            snackbar.setBackgroundTint((Color.parseColor("#e30000")))
                            val params = view.layoutParams as FrameLayout.LayoutParams
                            params.gravity = Gravity.TOP
                            view.layoutParams = params
                            val textView =
                                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                            snackbar.show()
                            isButtonClicked = true

                        }
                    } else {

                        val snackbar = Snackbar.make(v, "Veuillez saisir un mail", Snackbar.LENGTH_LONG)
                        val view = snackbar.view
                        snackbar.setBackgroundTint((Color.parseColor("#e30000")))
                        val params = view.layoutParams as FrameLayout.LayoutParams
                        params.gravity = Gravity.TOP
                        view.layoutParams = params
                        val textView =
                            view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        snackbar.show()
                        isButtonClicked = true

                    }


                }



            }

        })

    }
}