package com.kongqw.network.monitor.interfaces

import com.kongqw.network.monitor.enums.NetworkState

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class NetworkMonitor(

    val monitorFilter: Array<NetworkState> = [NetworkState.CELLULAR, NetworkState.WIFI, NetworkState.NONE]
)