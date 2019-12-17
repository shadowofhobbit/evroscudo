package iuliiaponomareva.evroscudo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT: Long = 15
private const val READ_TIMEOUT: Long = 15
private lateinit var client: OkHttpClient

fun getClient(): OkHttpClient {
    if (!::client.isInitialized) {
        client = OkHttpClient.Builder()
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS))
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).build()
    }
    return client
}


@Throws(IOException::class)
fun getInputStream(url: String): InputStream? {
    val request = Request.Builder().url(url).build()
    val response = getClient().newCall(request).execute()
    return response.body?.byteStream()
}

fun isConnectedToNetwork(context: Context): Boolean {
    val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val activeNetwork = manager.activeNetwork
        return (activeNetwork != null) &&
                (manager.getNetworkCapabilities(activeNetwork)?.hasCapability(NET_CAPABILITY_INTERNET) ?: false)
    }
    val networkInfo = manager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

