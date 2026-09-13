package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentWorldExplorationBinding
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.CountryBarAdapter

/** Pantalla WORLD EXPLORATION — vista de lista de países. */
class WorldExplorationFragment : Fragment() {

    private var _binding: FragmentWorldExplorationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorldExplorationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { navigateBack() }
        binding.rvCountries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCountries.adapter = CountryBarAdapter(SampleData.countries)
        // "VER TODOS" (sellos) abre el pasaporte.
        binding.tvSeeAllStamps.setOnClickListener { navigateTo(MoviePassportFragment()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
