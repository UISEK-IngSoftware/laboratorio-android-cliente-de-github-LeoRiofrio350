package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo;
import ec.edu.uisek.githubclient.models.RepoRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Objeto para definir el cuerpo de la petición de renombrado
data class RenameRepoRequest(val name: String)

interface GithubApiService {
    @Headers("Cache-Control: no-cache")
    @GET("user/repos")
    fun getRepos(
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String = "desc",
    ) : Call<List<Repo>>

    @POST("user/repos")
    fun addRepo(
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    // Esta función ahora solo actualizará los detalles (descripción, etc.)
    @PATCH("repos/{owner}/{repo}")
    fun updateRepoDetails(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
        @Body updates: Map<String, String>
    ): Call<Repo>

    // Esta función renombrará el repositorio. Es la misma ruta pero se usa diferente.
    @PATCH("repos/{owner}/{repo}")
    fun renameRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
        @Body renameRequest: RenameRepoRequest
    ): Call<Repo>

    @DELETE("repos/{owner}/{repo}")
    fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String
    ): Call<Void>
}