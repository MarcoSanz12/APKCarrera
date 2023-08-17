package com.gf.apkcarrera.features.f0_register

import android.content.Context
import com.gf.common.db.APKCarreraDatabase
import com.gf.common.entity.user.LoginRequest
import com.gf.common.entity.user.UserModel
import com.gf.common.exception.Failure
import com.gf.common.functional.Either
import com.gf.common.platform.NetworkHandler
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface RegisterRepository {

    suspend fun register(user: LoginRequest): Either<Failure, UserModel>

    suspend fun login(user: LoginRequest): Either<Failure, UserModel>

    suspend fun checkUserExists(user:LoginRequest): Boolean

    @Singleton
    class RegisterRepositoryImpl
    @Inject constructor(
        @ApplicationContext val context: Context,
        val networkHandler: NetworkHandler,
        val database: APKCarreraDatabase,
        val auth: FirebaseAuth,
        val firestore: FirebaseFirestore,
        val analytics: FirebaseAnalytics
    ) : RegisterRepository {

        override suspend fun register(user: LoginRequest): Either<Failure, UserModel> {

            if (!networkHandler.isConnected){
                return Either.Left(Failure.ServerError)
            }

            return try {
                //database.userDao().clearUser()

                // Checkeamos si el usuario ya está registrado
                if (checkUserExists(user))
                    Either.Left(Failure.LoginError)

                // Creamos el usuario
                val createdUser = createUser(user)

                // Logeamos en el usuario
                if (createdUser != null)
                    login(createdUser)
                else
                    Either.Left(Failure.LoginError)


            }catch (ex: Throwable){
                ex.printStackTrace()
                Either.Left(Failure.ServerError)
            }
        }

        override suspend fun login(user: LoginRequest): Either<Failure, UserModel> {

            if (!networkHandler.isConnected){
                return Either.Left(Failure.ServerError)
            }
            return try {
                //database.userDao().clearUser()
                val tskLogin = auth.signInWithEmailAndPassword(user.email,user.password).await()

                if (tskLogin.user != null){
                    user.apply {
                        uid = tskLogin.user!!.uid
                        getModelFromDoc(firestore.collection("users").document(user.uid).get().await())
                    }
                    val loggedUser = UserModel(user)
                    database.userDao().addUser(loggedUser)
                    Either.Right(loggedUser)
                }
                else
                    Either.Left(Failure.LoginError)

            }catch (ex: Throwable){
                ex.printStackTrace()
                Either.Left(Failure.ServerError.apply {
                    message = ex.message.toString()
                })
            }
        }

        override suspend fun checkUserExists(user: LoginRequest) : Boolean {

            if (!networkHandler.isConnected){
                return true
            }

            return try{
                val tskCheckUser = firestore.collection("users")
                    .whereEqualTo("email", user.email)
                    .whereEqualTo("username", user.username)
                    .limit(1)
                    .count()
                    .get(AggregateSource.SERVER).await()

                tskCheckUser.count > 0

            }catch (ex: Throwable){
                ex.printStackTrace()
                true
            }
        }

        private suspend fun createUser(user: LoginRequest) : LoginRequest?{
            val tskRegister = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            user.uid = tskRegister.user!!.uid

            firestore.collection("users").document(user.uid).set(user.setModelToMap()).await()

            return if (user.uid.isNotEmpty())
                user
            else
                null
        }




    }
}