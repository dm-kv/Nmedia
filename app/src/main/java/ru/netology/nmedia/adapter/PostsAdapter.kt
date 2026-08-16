package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import java.math.RoundingMode
import android.view.View
import android.widget.PopupMenu
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop


interface OnInteractionListener {
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onVideo(post: Post)
    fun onContent(post: Post)
    fun onViewImage(imageFileName: String)
}

fun checkTheDigit(digit: Int,) = when(digit) {
    in 0..999 -> digit.toString()
    in 1000..9999 -> (digit.toDouble() / 1000).toBigDecimal().setScale(1, RoundingMode.DOWN).toString() + "K"
    in 10000..999999 -> (digit / 1000).toString() + "K"
    else -> (digit.toDouble() / 1000000).toBigDecimal().setScale(1, RoundingMode.DOWN).toString() + "M"
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published.toString()
            content.text = post.content
            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")
            like.isChecked = post.likedByMe
            like.text = checkTheDigit(post.likes)
            share.text = checkTheDigit(post.shares)


            if (post.video == null) {
                playVideoGroup.visibility = View.GONE
            } else {
                playVideoGroup.visibility = View.VISIBLE
            }

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE


            postImage.visibility = if (post.attachment?.type == AttachmentType.IMAGE) View.VISIBLE else View.GONE
            if (post.attachment?.type == AttachmentType.IMAGE && post.attachment.url != null) {
                val url = "http://10.0.2.2:9999/media/${post.attachment.url}"
                postImage.load(url)
                postImage.setOnClickListener {
                    onInteractionListener.onViewImage(post.attachment!!.url)
                }
            } else {
                postImage.setOnClickListener(null)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.option_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
