package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.FragmentYearInFilmBinding

/** Pantalla AÑO EN PELÍCULAS — se abre desde Journey. */
class YearInFilmFragment : Fragment() {

    private var _binding: FragmentYearInFilmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYearInFilmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { navigateBack() }
        binding.btnShare.setOnClickListener { /* compartir: pendiente de lógica */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
