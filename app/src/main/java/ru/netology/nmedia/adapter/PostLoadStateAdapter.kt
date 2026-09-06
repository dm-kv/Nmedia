package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.LoadStateBinding
import androidx.core.view.isVisible

class PostLoadStateAdapter(
    private val retry: () -> Unit,
) : LoadStateAdapter<PostLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): PostLoadStateViewHolder {
        val binding = LoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PostLoadStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(
        holder: PostLoadStateViewHolder,
        loadState: LoadState,
    ) {
        holder.bind(loadState)
    }
}

class PostLoadStateViewHolder(
    private val binding: LoadStateBinding,
    private val retry: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        binding.apply {
            root.isVisible = loadState is LoadState.Loading || loadState is LoadState.Error
            progressBar.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = loadState is LoadState.Error
            if (loadState is LoadState.Error) {
                errorMsg.text = loadState.error.localizedMessage
            }
        }
    }
}