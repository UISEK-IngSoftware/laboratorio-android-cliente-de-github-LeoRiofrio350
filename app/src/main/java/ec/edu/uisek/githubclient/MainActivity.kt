package ec.edu.uisek.githubclient

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }

    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo -> handleEditClick(repo) },
            onDeleteClick = { repo -> handleDeleteClick(repo) }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        val apiService: GithubApiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>?>, response: Response<List<Repo>?>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                        reposAdapter.updateRepositories(emptyList()) // Limpiar la lista si no hay repositorios
                    }

                } else {
                    handleApiError("Error al cargar repositorios", response.code())
                }
            }


            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("Fallo en la conexión al cargar repositorios")
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm() {
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)
        }
    }

    // CORREGIDO: Pasa el nombre del dueño al formulario de edición
    private fun handleEditClick(repo: Repo) {
        val intent = Intent(this, RepoForm::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("REPO_OWNER", repo.owner.login) // Añadido
            putExtra("REPO_NAME", repo.name)
            putExtra("REPO_DESCRIPTION", repo.description)
        }
        startActivity(intent)
    }

    private fun handleDeleteClick(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // CORREGIDO: Usa el dueño del repositorio específico para eliminar
    private fun deleteRepository(repo: Repo) {
        RetrofitClient.gitHubApiService.deleteRepo(repo.owner.login, repo.name)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        showMessage("Repositorio eliminado con éxito")
                        fetchRepositories() // Actualizar la lista
                    } else {
                        handleApiError("Error al eliminar", response.code())
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showMessage("Fallo en la conexión al eliminar")
                }
            })
    }

    private fun handleApiError(contextMessage: String, code: Int) {
        val errorMessage = when (code) {
            401 -> "No autorizado. Revisa tu token de GitHub."
            403 -> "Prohibido. Revisa los permisos (scopes) de tu token de GitHub. Necesitas 'repo' y 'delete_repo'."
            404 -> "No encontrado. El repositorio o usuario no existe."
            else -> "Error código: $code"
        }
        showMessage("$contextMessage: $errorMessage")
    }
}