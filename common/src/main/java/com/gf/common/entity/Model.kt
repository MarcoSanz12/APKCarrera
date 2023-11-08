package com.gf.common.entity

import com.google.firebase.firestore.DocumentSnapshot

abstract class Model() {

    abstract var uid : String

    abstract fun getModelFromDoc (doc : DocumentSnapshot)

    abstract fun setModelToMap() : HashMap<String,Any>

}