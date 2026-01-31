package com.example.app_buladigital

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigInteger


// Interface que define a estrutura de nossa API.
interface ApiService {
    // A anotação @GET indica que esta função fará uma requisição GET.
    // O valor "hello" é o endpoint relativo, por exemplo, http://localhost:3000/hello
    @POST("api/v1/data/ping")
    suspend fun pingApiWithData(@Body data: DeviceData): Response<PingData>

    @GET("api/v1/data/{uuid}/edit_user")
    suspend fun getUserData(@Path("uuid") uuid: String): Response<UserUpdateData>

    @POST("api/v1/data/{uuid}/update_user")
    suspend fun updateUser(
        @Path("uuid") uuid: String,
        @Body userData: UserUpdateData
    ): Response<UserUpdateData>

    @GET("api/v1/data/app_info")
    suspend fun getAppInfo(): Response<AppInfo>

    @GET("api/v1/data/medicamentos_search")
    suspend fun buscarMedicamentos(
        @Query("q") query: String,
        @Query("page") page: Int
    ): Response<List<Medicamento>>

    //Versões das bulas por laboratório e data
    @GET("api/v1/data/{medicamento_id}/bula_versoes")
    suspend fun getVersoes(
        @Path("medicamento_id") medId: Long
    ): Response<List<BulaVersao>>

    //Carrega uma versão específica da bula do medicamento
    @GET("api/v1/data/{bula_id}/detalhes_bula")
    suspend fun getDetalhesBula(@Path("bula_id") bulaId: Long): Response<BulaConteudoCompleto>

}

// Data class para o envio do formulário
data class UserUpdateData(
    val message: String? = null,
    val is_temporary: Boolean? = null,
    val nome: String? = null,
    val email: String? = null,
    val nickname: String? = null,
    //val nascimento: String? = null,
    val password: String? = null,
    val password_confirmation: String? = null,
    val errors: Map<String, List<String>>? = null
)

data class PingData(
    val user: String?,
    val welcome_message: String?
)

data class AppInfo(
    val about_text: String?,
    val terms_text: String?
)

data class Medicamento (
    val id: Long,
    val nome: String,
    val present: Boolean,
    val versions: Int
)

data class BulaVersao (
    val bula_id: Long,
    val date: String,
    val laboratorio: String
)

data class BulaConteudoCompleto (
    val present: Boolean,
    val resumo: String,
    val curiosidades: String,
    val url_busca: String,
    val pdf_url: String
)