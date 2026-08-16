package ru.netology.nmedia.fragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import com.bumptech.glide.Glide
import androidx.core.graphics.drawable.toDrawable
import android.graphics.Color
import ru.netology.nmedia.databinding.FragmentImgFullBinding
import androidx.navigation.fragment.findNavController


class ImgFullScreenFragment : Fragment() {

    companion object {
        const val IMG_NAME = "image_name"

        fun newInstance(imageFileName: String): ImgFullScreenFragment {
            return ImgFullScreenFragment().apply {
                arguments = Bundle().apply { putString(IMG_NAME, imageFileName) }
            }
        }
    }

    private var _binding: FragmentImgFullBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImgFullBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageFileName = requireArguments().getString(IMG_NAME)
            ?: throw IllegalArgumentException("Image file name is required")

        val url = "http://10.0.2.2:9999/media/$imageFileName"

        val toolbar = binding.imageToolbar
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        Glide.with(this)
            .load(url)
            .timeout(15_000)
            .placeholder(Color.DKGRAY.toDrawable())
            .error(Color.DKGRAY.toDrawable())
            .into(binding.fullScreenImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}