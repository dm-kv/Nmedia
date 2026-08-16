package ru.netology.nmedia.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.fragment.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewmodel.PostUiEvent
import ru.netology.nmedia.viewmodel.ViewModelFactory


class FeedFragment : Fragment() {
    private val dependencyContainer = DependencyContainer.getInstance()

    private val viewModel: PostViewModel by activityViewModels(
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.repository,
                dependencyContainer.appAuth
            )}
    )
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PostsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment2,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }


            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
            }

            override fun onVideo(post: Post) {
                val intentVideo = Intent(Intent.ACTION_VIEW, post.video?.toUri())
                startActivity(intentVideo)
            }


            override fun onContent(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment2,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
            }

            override fun onViewImage(imageFileName: String) {
                val bundle = Bundle().apply {
                    putString(ImgFullScreenFragment.IMG_NAME, imageFileName)
                }
                findNavController().navigate(R.id.imgFullScreenFragment, bundle)
            }

        }
        )
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }


        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.posts.isEmpty()

            binding.newPostsBanner.isVisible = viewModel.hasNewPosts.value == true

            if (viewModel.shouldConsumeAndResetJustAccepted() && adapter.itemCount > 0) {
                binding.list.post {
                    binding.list.smoothScrollToPosition(0)
                    binding.newPostsBanner.visibility = View.GONE
                }
            }
        }

        viewModel.hasNewPosts.observe(viewLifecycleOwner) { hasNew ->
            binding.newPostsBanner.isVisible = hasNew == true
        }

        binding.bannerAction.setOnClickListener {
            viewModel.onNewPostsClicked()
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { count ->
            println(count)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is PostUiEvent.RequestAuth -> showAuthDialog()
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        binding.add.setOnClickListener {
            if (!viewModel.authenticated) {
                showAuthDialog()
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment2)
            }
        }

        return binding.root
    }

    private fun showAuthDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Требуется авторизация")
            .setMessage("Чтобы добавить пост или поставить лайк, нужно войти в аккаунт.")
            .setPositiveButton("Войти") { _, _ ->
                findNavController().navigate(R.id.action_global_loginFragment)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var Bundle.idArg by LongArg
    }
}