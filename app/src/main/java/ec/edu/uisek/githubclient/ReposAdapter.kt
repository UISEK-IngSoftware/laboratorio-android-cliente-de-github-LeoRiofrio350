package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ec.edu.uisek.githubclient.databinding.FragmentRepoitemBinding

class ReposViewHolder(private val binding: FragmentRepoitemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(position: Int) {
        binding.repoName.text = "Repositorio No. ${position}"
        binding.repoDescription.text = "Esta es la descripción para el elmento No. ${position}"
    }
}

class ReposAdapter: RecyclerView.Adapter<ReposViewHolder>() {
    override fun getItemCount(): Int = 4
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReposViewHolder {
        var binding = FragmentRepoitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReposViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReposViewHolder, position: Int) {
        holder.bind(position)
    }
}