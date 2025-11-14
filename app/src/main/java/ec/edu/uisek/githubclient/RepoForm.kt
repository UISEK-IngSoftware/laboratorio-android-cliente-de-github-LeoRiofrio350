package ec.edu.uisek.githubclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RenameRepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {

    private lateinit var binding: ActivityRepoFormBinding
    private var isEditMode = false
    private var originalName = ""
    private var owner = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkEditMode()
        setupButtons()
    }

    private fun checkEditMode() {
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        if (isEditMode) {
            owner = intent.getStringExtra("REPO_OWNER") ?: ""
            originalName = intent.getStringExtra("REPO_NAME") ?: ""
            binding.repoNameInput.setText(originalName)
            binding.repoNameInput.isEnabled = true // CORREGIDO: Habilitar edición del nombre
            binding.repoDescriptionInput.setText(intent.getStringExtra("REPO_DESCRIPTION"))
            binding.saveButton.text = "Actualizar"
        }
    }

    private fun setupButtons() {
        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener {
            if (isEditMode) updateRepo() else createRepo()
        }
    }

    private fun validateForm(): Boolean {
        val name = binding.repoNameInput.text.toString().trim()
        if (name.isBlank()) {
            binding.repoNameInput.error = "Requerido"
            return false
        }
        if (name.contains(" ")) {
            binding.repoNameInput.error = "Sin espacios"
            return false
        }
        return true
    }

    private fun createRepo() {
        if (!validateForm()) return
        val name = binding.repoNameInput.text.toString().trim()
        val desc = binding.repoDescriptionInput.text.toString().trim()
        val request = RepoRequest(name, desc)
        RetrofitClient.gitHubApiService.addRepo(request)
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Creado con éxito")
                        finish()
                    } else {
                        handleApiError("Error al crear", response.code())
                    }
                }
                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Fallo en la conexión al crear")
                }
            })
    }

    // Lógica de actualización completamente nueva
    private fun updateRepo() {
        if (!validateForm()) return

        val newName = binding.repoNameInput.text.toString().trim()
        val newDescription = binding.repoDescriptionInput.text.toString().trim()

        val nameHasChanged = newName != originalName

        if (nameHasChanged) {
            // Paso 1: Renombrar el repositorio
            val renameRequest = RenameRepoRequest(newName)
            RetrofitClient.gitHubApiService.renameRepo(owner, originalName, renameRequest)
                .enqueue(object : Callback<Repo> {
                    override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                        if (response.isSuccessful) {
                            // Paso 2: Actualizar la descripción del repositorio (ya renombrado)
                            updateRepoDescription(newName, newDescription)
                        } else {
                            handleApiError("Error al renombrar", response.code())
                        }
                    }

                    override fun onFailure(call: Call<Repo>, t: Throwable) {
                        showMessage("Fallo en la conexión al renombrar")
                    }
                })
        } else {
            // Si el nombre no ha cambiado, solo actualizar la descripción
            updateRepoDescription(originalName, newDescription)
        }
    }

    private fun updateRepoDescription(repoName: String, description: String) {
        val updates = mapOf("description" to description)
        RetrofitClient.gitHubApiService.updateRepoDetails(owner, repoName, updates)
            .enqueue(object : Callback<Repo> {
                override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                    if (response.isSuccessful) {
                        showMessage("Actualizado con éxito")
                        finish()
                    } else {
                        handleApiError("Error al actualizar descripción", response.code())
                    }
                }

                override fun onFailure(call: Call<Repo>, t: Throwable) {
                    showMessage("Fallo en la conexión al actualizar descripción")
                }
            })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun handleApiError(contextMessage: String, code: Int) {
        val errorMessage = when (code) {
            401 -> "No autorizado. Revisa tu token de GitHub."
            403 -> "Prohibido. Revisa los permisos (scopes) de tu token de GitHub. Necesitas el permiso 'repo'."
            404 -> "No encontrado. El repositorio o usuario no existe."
            422 -> "Error de validación. El nombre del repositorio ya existe o es inválido."
            else -> "Error código: $code"
        }
        showMessage("$contextMessage: $errorMessage")
    }
}