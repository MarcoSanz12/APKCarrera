package com.gf.common.platform

import android.content.Context
import com.gf.common.extensions.networkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable class which returns information about the network connection state.
 */
@Singleton
class NetworkHandler
@Inject constructor(@ApplicationContext val context: Context) {

    //FIXME: Implementar no deprecado
    val isConnected = context.networkInfo?.isConnectedOrConnecting ?: false
}