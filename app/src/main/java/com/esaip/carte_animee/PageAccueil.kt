package com.esaip.carte_animee

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class PageAccueil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_accueil)

        val fullName = findViewById<TextView>(R.id.FullName)

        // Accédez aux informations de l'utilisateur depuis l'objet Singleton
        val userInfo = RequeteApi.UserSingleton.userInfo
        val roleInfo = RequeteApi.UserSingleton.roleInfo

        if (userInfo != null && roleInfo != null) {
            // Les informations de l'utilisateur sont disponibles, vous pouvez les afficher
            val nom = userInfo.nom
            val prenom = userInfo.prenom
            val idRole = userInfo.idRole
            val nomRole = roleInfo.nomRole

            // Affichez les informations dans votre interface utilisateur
            fullName.text = "$nom $prenom ($nomRole)"
        } else {
            // Les informations de l'utilisateur ne sont pas disponibles
            // Gérez ce cas en conséquence
        }
    }
}
