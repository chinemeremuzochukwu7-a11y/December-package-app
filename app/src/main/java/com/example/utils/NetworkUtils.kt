package com.example.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility to verify active internet connectivity (Wi-Fi or Mobile Cellular Data)
 * before initiating network-dependent operations, particularly Google Play Billing payments.
 */
object NetworkUtils {

    /**
     * Checks if the device is connected to the internet via Wi-Fi or Mobile Cellular Data.
     * Returns true if connected and network has internet capability, false otherwise.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternetCapability =
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        return hasInternetCapability && (isWifi || isCellular || isEthernet)
    }

    /**
     * Checks specifically if the user has Wi-Fi or Cellular Data connected.
     */
    fun hasWifiOrCellularData(context: Context): Boolean {
        return isNetworkAvailable(context)
    }

    /**
     * Returns a user-friendly string describing the current active network connection:
     * "Wi-Fi", "Mobile Cellular Data", "Ethernet", or "Offline / No Connection".
     */
    fun getNetworkTypeName(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "No Connection"

        val activeNetwork = connectivityManager.activeNetwork ?: return "Offline / No Connection"
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "Offline / No Connection"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data (Cellular)"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Offline / No Connection"
        }
    }
}
