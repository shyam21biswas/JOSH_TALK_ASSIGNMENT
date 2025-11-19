package com.example.josh

import com.google.gson.annotations.SerializedName

import retrofit2.http.GET

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit






data class ProductResponse(
    val products: List<Product>
)

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("price")
    val price: Double
)



interface ApiService {
    @GET("products")
    suspend fun getProducts(): ProductResponse
}


object RetrofitInstance {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}


object Constants {
    const val BASE_URL = "https://dummyjson.com/"
    const val MIN_RECORDING_DURATION = 10
    const val MAX_RECORDING_DURATION = 20
    const val NOISE_THRESHOLD = 40
}