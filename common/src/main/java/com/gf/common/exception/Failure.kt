package com.gf.common.exception

/**
 * Base Class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure {
    var message: String = ""
    object NetworkConnection : Failure()
    object SaveDB : Failure()
    object ServerError : Failure()
    object DBEmptyError: Failure()
    object LoginError: Failure()

    /** * Extend this class for feature specific failures.*/
    abstract class FeatureFailure: Failure()
}
