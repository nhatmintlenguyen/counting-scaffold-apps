package com.example.dadn_app.core.network

import com.example.dadn_app.data.models.ProcessingResultResponse
import com.example.dadn_app.data.models.UploadImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProcessingApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
    ): Response<UploadImageResponse>

    @GET("status/{jobId}")
    suspend fun getResult(
        @Path("jobId") jobId: String,
    ): Response<ProcessingResultResponse>
}
