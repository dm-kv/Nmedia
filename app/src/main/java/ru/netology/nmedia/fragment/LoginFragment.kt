package ru.netology.nmedia.fragment

import androidx.fragment.app.Fragment
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.viewmodel.LoginViewModel
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.os.Bundle
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import ru.netology.nmedia.viewmodel.LoginUiState
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment() {


    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.signInBtn.setOnClickListener {
            val login = binding.loginEt.text.toString().trim()
            val pass = binding.passwordEt.text.toString()
            if (login.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните логин и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(login, pass)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Success -> {
                            findNavController().popBackStack()
                        }
                        is LoginUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}