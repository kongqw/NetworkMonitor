package com.kongqw.network.monitor

import com.kongqw.network.monitor.enums.NetworkState
import java.lang.reflect.Method

internal class NetworkStateReceiverMethod(
    var any: Any? = null,
    var method: Method? = null,
    var monitorFilter: Array<NetworkState>? = null
)