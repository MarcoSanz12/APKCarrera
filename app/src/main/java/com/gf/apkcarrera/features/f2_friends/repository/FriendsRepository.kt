package com.gf.apkcarrera.features.f2_friends.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.friend.FriendModel
import com.gf.common.entity.friend.FriendStatus
import com.gf.common.entity.toFriendModel
import com.gf.common.entity.user.UserModel
import com.gf.common.platform.NetworkHandler
import com.gf.common.response.FriendListResponse
import com.gf.common.response.FriendResponse
import com.gf.common.utils.Constants
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FriendsRepository {

    suspend fun searchAll(name:String) : FriendListResponse
    suspend fun getFriends() : FriendListResponse
    suspend fun getFriendRequests() : FriendListResponse
    suspend fun removeFriend(friendId: String) : FriendResponse
    suspend fun acceptFriendRequest(friendId: String) : FriendResponse
    suspend fun ignoreFriendRequest(friendId: String) : FriendResponse
    suspend fun sendFriendRequest(friendId: String) : FriendResponse
    suspend fun cancelFriendRequest(friendId: String) : FriendResponse
    @Singleton
    class FriendsRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val preferences: SharedPreferences,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics) : FriendsRepository{
        companion object{
            const val TAG = "FriendsRepository"
        }


        // 1. Lista de AMIGOS

        override suspend fun getFriends(): FriendListResponse {
            if (!networkHandler.isConnected)
                return FriendListResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendListResponse.Error

            val user =  database.userDao().getUserByUid(userId)
            return try {
                val users = firestore.collection("users")
                    .whereIn(FieldPath.documentId(),user.friendList)
                    .whereArrayContains("friendList",userId)
                    .get().await()

                // Sacamos la lista de amigos
                val friendList = users.getFriends(user).toMutableList()

                Log.d(TAG, "Devolvemos ${friendList.size} resultados")
                return FriendListResponse.Succesful(friendList)


            }catch (ex:Exception){
                ex.printStackTrace()
                FriendListResponse.Error
            }
        }
        override suspend fun removeFriend(friendId: String): FriendResponse {
            Log.d(TAG, "removeFriend: $friendId")
            if (friendId.isEmpty() || !networkHandler.isConnected)
                return FriendResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendResponse.Error
            val userCollection =  firestore.collection("users")

            return try {
                val batch = firestore.runBatch{
                    userCollection.document(userId)
                        .update("friendList", FieldValue.arrayRemove(friendId))

                    userCollection.document(friendId)
                        .update("friendList",FieldValue.arrayRemove(userId))
                }


                if (batch.isSuccessful){
                    database.userDao().removeFriend(userId,friendId)

                    FriendResponse.Succesful(friendId)
                }
                else
                    FriendResponse.Error



            }catch (ex:Exception){
                ex.printStackTrace()
                FriendResponse.Error
            }
        }

        // 2. Recibir PETICIONES
        override suspend fun getFriendRequests(): FriendListResponse {
            if (!networkHandler.isConnected)
                return FriendListResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendListResponse.Error

            val user =  database.userDao().getUserByUid(userId)

            runCatching {
                firestore.collection("users")
                    .whereArrayContains("friendList",userId)
                    .limit(50)
                    .get().await()
            }.fold(
                onSuccess = {
                    // Sacamos la lista de amigos
                    val friendList = it.getFriends(user).toMutableList()

                    /*
                        Eliminamos si:
                            a) Es el propio usuario
                            b) Son ya amigos
                     */

                    friendList.removeIf { (it.uid == userId) || it.friendStatus == FriendStatus.FRIEND }
                    Log.d(TAG, "Devolvemos ${friendList.size} resultados")
                    return FriendListResponse.Succesful(friendList)
                },
                onFailure = {
                    Log.e(TAG,it.stackTraceToString())
                    return FriendListResponse.Error
                }
            )
        }

        override suspend fun acceptFriendRequest(friendId: String) : FriendResponse{
            Log.d(TAG, "acceptFriendRequest: $friendId")
            if (friendId.isEmpty() || !networkHandler.isConnected)
                return FriendResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendResponse.Error

            return try {
                val userDoc = firestore.collection("users").document(userId)

                userDoc.update("friendList", FieldValue.arrayUnion(friendId)).await()
                database.userDao().addFriend(userId,friendId)


                FriendResponse.Succesful(friendId)
            }catch (ex:Exception){
                ex.printStackTrace()
                FriendResponse.Error
            }
        }
        override suspend fun ignoreFriendRequest(friendId: String): FriendResponse {
            Log.d(TAG, "ignoreFriendRequest: $friendId")
            if (friendId.isEmpty() || !networkHandler.isConnected)
                return FriendResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendResponse.Error

            return try {
                val ignoredDoc = firestore.collection("users").document(friendId)

                ignoredDoc.update("friendList", FieldValue.arrayRemove(userId)).await()

                FriendResponse.Succesful(friendId)
            }catch (ex:Exception){
                ex.printStackTrace()
                FriendResponse.Error
            }
        }

        // 3. Enviar PETICIONES
        override suspend fun searchAll(name: String): FriendListResponse {

            if (name.isEmpty())
                return FriendListResponse.Succesful(mutableListOf())

            if (!networkHandler.isConnected)
                return FriendListResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null)

            val user =  database.userDao().getUserByUid(userId ?: "")

            runCatching {
                firestore.collection("users")
                    .orderBy("searchname")
                    .startAt(name)
                    .endAt(name + "\uf8ff")
                    .limit(50)
                    .get().await()
            }.fold(
                onSuccess = {
                    // Sacamos la lista de amigos
                    val friendList = it.getFriends(user).toMutableList()

                    /*
                        Eliminamos si:
                            a) Es el propio usuario
                            b) Son ya amigos
                     */
                    friendList.removeIf { (it.uid == userId) || it.friendStatus == FriendStatus.FRIEND }
                    Log.d(TAG, "Devolvemos ${friendList.size} resultados")
                    return FriendListResponse.Succesful(friendList)
                },
                onFailure = {
                    Log.e(TAG,it.stackTraceToString())
                    return FriendListResponse.Error
                }
            )
        }
        override suspend fun sendFriendRequest(friendId : String) : FriendResponse{
            Log.d(TAG, "addFriendRequest: $friendId")
            if (friendId.isEmpty() || !networkHandler.isConnected)
                return FriendResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendResponse.Error

            return try {
                val userDoc = firestore.collection("users").document(userId)
                var updatedUser : UserModel? = null

                userDoc.update("friendList", FieldValue.arrayUnion(friendId)).await()
                updatedUser = UserModel().apply {
                    uid = userDoc.id
                    getModelFromDoc(userDoc.get().await())
                }

                database.userDao().addUser(updatedUser)
                FriendResponse.Succesful(friendId)
            }catch (ex:Exception){
                ex.printStackTrace()
                FriendResponse.Error
            }
        }

        override suspend fun cancelFriendRequest(friendId: String): FriendResponse {
            Log.d(TAG, "cancelFriendRequest: $friendId")
            if (friendId.isEmpty() || !networkHandler.isConnected)
                return FriendResponse.Error

            val userId =  preferences.getString(Constants.Login.LOG_UID,null) ?: return FriendResponse.Error

            return try {
                val userDoc = firestore.collection("users").document(userId)

                userDoc.update("friendList", FieldValue.arrayRemove(friendId)).await()
                database.userDao().removeFriend(userId,friendId)

                FriendResponse.Succesful(friendId)
            }catch (ex:Exception){
                ex.printStackTrace()

                FriendResponse.Error
            }
        }

        private suspend fun QuerySnapshot.getFriends(user : UserModel) : List<FriendModel>{
            val users = this.documents.map {
                UserModel().apply { getModelFromDoc(it) }
            }

            return users.map { it.toFriendModel(user) }
        }


    }
}


