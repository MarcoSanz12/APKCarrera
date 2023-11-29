package com.gf.common.response

sealed class GenericResponse {
    object Succesful : GenericResponse()
    object Error : GenericResponse()
}