package ru.netology.nmedia.activity


import androidx.fragment.app.Fragment
import ru.netology.nmedia.viewmodel.RegisterViewModel
import android.view.LayoutInflater
import androidx.fragment.app.viewModels
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import ru.netology.nmedia.viewmodel.RegisterUiState
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import androidx.lifecycle.repeatOnLifecycle
import ru.netology.nmedia.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {


    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels ()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerBtn.setOnClickListener {
            val name = binding.nameEt.text.toString().trim()
            val login = binding.loginEt.text.toString().trim()
            val pass = binding.passwordEt.text.toString()
            val confirm = binding.confirmPasswordEt.text.toString()

            if (name.isEmpty() || login.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(login, pass, name)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is RegisterUiState.Success -> {
                            findNavController().popBackStack()
                        }
                        is RegisterUiState.Error -> {
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