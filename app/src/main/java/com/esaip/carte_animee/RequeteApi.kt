package com.esaip.carte_animee

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class RequeteApi {


    data class UserInfo(val nom: String, val prenom: String, val idRole: Int)
    data class RoleInfo(val nomRole: String)


    object UserSingleton {
        var userInfo: RequeteApi.UserInfo? = null
        var roleInfo: RoleInfo? = null
    }

    fun connexionUtilisateur(identifiant: String, password: String): Deferred<Boolean> =
        CoroutineScope(Dispatchers.IO).async {
            val urlString = "http://25.45.22.19/carteAnimees/api.php?fonction=getUser&identifiant=${
                URLEncoder.encode(
                    identifiant,
                    "UTF-8"
                )
            }&mdp=${URLEncoder.encode(password, "UTF-8")}"
            val url = URL(urlString)

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Lecture de la réponse
                val inputStream = conn.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                bufferedReader.close()
                inputStream.close()

                // Analyse de la réponse JSON
                val jsonResponse = JSONObject(response.toString())
                val success = jsonResponse.getBoolean("success")

                if (success) {
                    // Extraire les valeurs des champs user et role
                    val userObject = jsonResponse.getJSONObject("user")
                    val roleObject = jsonResponse.getJSONObject("role")
                    val userInfo = RequeteApi.UserInfo(
                        nom = userObject.getString("Nom"),
                        prenom = userObject.getString("Prenom"),
                        idRole = userObject.getInt("IdRole")
                    )
                    val roleInfo = RoleInfo(
                        nomRole = roleObject.getString("NomRole")
                    )

                    UserSingleton.userInfo = userInfo
                    UserSingleton.roleInfo = roleInfo

                    true // Succès de la connexion
                } else {
                    false // Échec de la connexion
                }
            } else {
                false // Échec de la connexion
            }
        }


}


