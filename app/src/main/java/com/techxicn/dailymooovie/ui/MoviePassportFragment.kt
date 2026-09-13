package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentMoviePassportBinding
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.PassportStampAdapter

/**
 * Pantalla MOVIE PASSPORT — vista de sellos.
 * Se abre desde Profile. Enlaza al detalle de un sello (PassportDetailFragment).
 */
class MoviePassportFragment : Fragment() {

    private var _binding: FragmentMoviePassportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoviePassportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { navigateBack() }
        binding.rvStamps.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvStamps.adapter = PassportStampAdapter(SampleData.stamps)
        // Fila del primer sello y "Ver los 5 sellos →" abren el detalle.
        binding.rowSealedByFirst.setOnClickListener { navigateTo(PassportDetailFragment()) }
        binding.tvSeeAllSealed.setOnClickListener { navigateTo(PassportDetailFragment()) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
