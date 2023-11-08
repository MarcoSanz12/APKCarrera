package com.gf.common.response

sealed class UploadActivityResponse {
    object Succesful : UploadActivityResponse()
    object Error : UploadActivityResponse()
}