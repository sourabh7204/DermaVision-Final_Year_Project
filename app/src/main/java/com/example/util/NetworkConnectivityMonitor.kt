package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NetworkConnectionType(val label: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Cellular Data"),
    ETHERNET("Ethernet"),
    OFFLINE("Offline")
}

class NetworkConnectivityMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _connectionType = MutableStateFlow(determineConnectionType())
    val connectionType: StateFlow<NetworkConnectionType> = _connectionType.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch {
                updateNetworkStatus()
            }
        }

        override fun onLost(network: Network) {
            scope.launch {
                updateNetworkStatus()
            }
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            scope.launch {
                updateNetworkStatus()
            }
        }
    }

    init {
        registerCallback()
        updateNetworkStatus()
    }

    private fun registerCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            // Fallback gracefully if permission or registration issue
        }
    }

    private fun updateNetworkStatus() {
        val online = checkInitialConnectivity()
        val type = determineConnectionType()
        _isOnline.value = online
        _connectionType.value = type
    }

    private fun checkInitialConnectivity(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                 caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                 caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    private fun determineConnectionType(): NetworkConnectionType {
        val cm = connectivityManager ?: return NetworkConnectionType.OFFLINE
        val activeNetwork = cm.activeNetwork ?: return NetworkConnectionType.OFFLINE
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkConnectionType.OFFLINE

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkConnectionType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkConnectionType.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkConnectionType.ETHERNET
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkConnectionType.WIFI
            else -> NetworkConnectionType.OFFLINE
        }
    }
}
