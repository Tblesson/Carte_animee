package com.esaip.carte_animee

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONException
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
            try {
            val urlString = "https://www.demineur-ligne.com/PFETTCV/carteAnimees/api.php?fonction=getUser&identifiant=${
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
                try {
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
                } catch (e: JSONException) {
                    // Gérer l'erreur de parsing JSON
                    e.printStackTrace()
                    System.out.println("Problème connexion server")
                    false
                }

            } else {
                false // Échec de la connexion
            }
            } catch (e: IOException) {
                // Gérer l'exception IOException, retourner false en cas d'échec de connexion
                e.printStackTrace()
                return@async false
            }
        }


    fun listeSeriesByUser(idUser: Int): Array<Array<Any?>> {
        try {
            val urlString = "https://www.demineur-ligne.com/PFETTCV/carteAnimees/api.php?fonction=getUserSeries&id=$idUser"
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

                // Extrait le tableau JSON "series" de l'objet JSON racine
                val seriesArray = jsonResponse.getJSONArray("series")

                // Création du tableau pour stocker les résultats
                val resultArray = Array(seriesArray.length()) { arrayOfNulls<Any?>(3) }

                /** statut
                 * 1 -> bloquer
                 * 2 -> en cours
                 * 3 -> finie
                 * */



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


    fun listeCardsByIdSeries(idSerie: String): Array<Array<Any?>> {
        val urlString = "https://www.demineur-ligne.com/PFETTCV/carteAnimees/api.php?fonction=getCartes&idSerie=$idSerie"

        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"

            // Vérifier le code de réponse HTTP
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = conn.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?

                // Lire la réponse ligne par ligne
                while (bufferedReader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                // Fermer les flux
                bufferedReader.close()
                inputStream.close()

                // Analyser la réponse JSON
                val jsonResponse = JSONObject(response.toString())
                val success = jsonResponse.getBoolean("success")

                if (success) {
                    // Extraire le tableau JSON "cartes"
                    val cartesArray = jsonResponse.getJSONArray("cartes")

                    // Créer un tableau pour stocker les résultats
                    val resultArray = Array(cartesArray.length()) { arrayOfNulls<Any>(6) }

                    // Remplir le tableau avec les données JSON
                    for (i in 0 until cartesArray.length()) {
                        val carte = cartesArray.getJSONObject(i)
                        resultArray[i][0] = carte.getString("Id")
                        resultArray[i][1] = carte.getString("Intitule")
                        resultArray[i][2] = carte.getString("IdImage")
                        resultArray[i][3] = carte.getString("IdSon")
                        resultArray[i][4] = carte.getString("IdSerie")
                        resultArray[i][5] = carte.getString("description")
                    }

                    resultArray
                } else {
                    // En cas d'échec, retourner un tableau vide
                    arrayOf<Array<Any?>>()
                }
            } else {
                // En cas d'erreur HTTP, retourner un tableau vide
                arrayOf<Array<Any?>>()
            }
        } catch (e: Exception) {
            e.printStackTrace() // Gérer les exceptions en imprimant la trace
            arrayOf<Array<Any?>>() // Retourner un tableau vide en cas d'erreur
        }
    }
}








