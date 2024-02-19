package com.esaip.carte_animee

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class RequeteApi {


    data class UserInfo(val idUser:Int ,val nom: String, val prenom: String, val idRole: Int)
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
            conn.requestMethod = "POST"

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
                        idUser = userObject.getInt("Id"),
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


    fun listeSeriesByUser(idUser: Int): Array<Array<Any?>> {
        try {
            val urlString = "http://25.45.22.19/carteAnimees/api.php?fonction=getUserSeries&id=$idUser"
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

                // Extrait le tableau JSON "series" de l'objet JSON racine
                val seriesArray = jsonResponse.getJSONArray("series")

                // Création du tableau pour stocker les résultats
                val resultArray = Array(seriesArray.length()) { arrayOfNulls<Any?>(3) }

                // Remplissage du tableau avec les données JSON
                for (i in 0 until seriesArray.length()) {
                    val serie = seriesArray.getJSONObject(i)
                    resultArray[i][0] = serie.getInt("IdSerie")
                    resultArray[i][1] = serie.getInt("IdStatut")
                    resultArray[i][2] = serie.getString("Theme")
                }

                // Retour du tableau
                return resultArray
            } else {
                // En cas d'erreur HTTP, retourner un tableau vide
                return arrayOf<Array<Any?>>()
            }
        } catch (e: Exception) {
            // Gérer l'exception ici, par exemple, imprimer le message d'erreur
            e.printStackTrace()
            // Retourner null ou un tableau vide, selon le cas
            return arrayOf<Array<Any?>>()
        }
    }
}




