package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ec.edu.uisek.githubclient.databinding.FragmentRepoitemBinding
import ec.edu.uisek.githubclient.models.Repo

// ViewHolder con botones de acción
class ReposViewHolder(
    private val binding: FragmentRepoitemBinding,
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(repo: Repo) {
        // Datos del repositorio
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: "Sin descripción"
        binding.repoLang.text = repo.language ?: "Desconocido"

        // Avatar del owner (circular)
        Glide.with(binding.root.context)
            .load(repo.owner.avatarUrl)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.repoOwnerImagen)

        // Botón Editar
        binding.btnEdit.setOnClickListener {
            onEditClick(repo)
        }

        // Botón Eliminar
        binding.btnDelete.setOnClickListener {
            onDeleteClick(repo)
        }
    }
}

// Adapter con callbacks para editar y eliminar
class ReposAdapter(
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit
) : RecyclerView.Adapter<ReposViewHolder>() {

    private var repositories: List<Repo> = emptyList()

    override fun getItemCount(): Int = repositories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReposViewHolder {
        val binding = FragmentRepoitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReposViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ReposViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    fun updateRepositories(newRepositories: List<Repo>) {
        repositories = newRepositories
        notifyDataSetChanged()
    }
}