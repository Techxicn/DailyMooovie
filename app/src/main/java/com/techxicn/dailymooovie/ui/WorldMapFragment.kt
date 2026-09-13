package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.techxicn.dailymooovie.databinding.FragmentWorldMapBinding

/**
 * Pantalla WORLD EXPLORATION — vista de mapa.
 * Se abre desde Profile. Enlaza a la vista de lista (WorldExplorationFragment).
 */
class WorldMapFragment : Fragment() {

    private var _binding: FragmentWorldMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorldMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { navigateBack() }
        // "VER LISTA" y "Ver los 18 países →" abren la vista de lista.
        binding.tvSeeList.setOnClickListener { navigateTo(WorldExplorationFragment()) }
        binding.tvSeeAllCountries.setOnClickListener { navigateTo(WorldExplorationFragment()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
