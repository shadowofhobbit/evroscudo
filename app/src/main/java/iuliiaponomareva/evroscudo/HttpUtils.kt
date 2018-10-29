package iuliiaponomareva.evroscudo

import android.content.Context
import android.net.ConnectivityManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT: Long = 15
private const val READ_TIMEOUT: Long = 15
private lateinit var client: OkHttpClient

fun getClient(): OkHttpClient {
    if (!::client.isInitialized) {
        client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).build()
    }
    return client
}


@Throws(IOException::class)
fun getInputStream(url: String): InputStream? {
    val request = Request.Builder().url(url).build()
    val response: Response
    response = getClient().newCall(request).execute()
    val body = response.body()
    return body?.byteStream()
}

fun isConnectedToNetwork(context: Context): Boolean {
    val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = manager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

