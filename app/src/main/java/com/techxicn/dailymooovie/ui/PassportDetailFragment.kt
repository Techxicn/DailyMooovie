package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentPassportDetailBinding
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.SealedMovieAdapter

/** Pantalla MOVIE PASSPORT — detalle de un sello de país. */
class PassportDetailFragment : Fragment() {

    private var _binding: FragmentPassportDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { navigateBack() }
        binding.rvSealedMovies.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSealedMovies.adapter = SealedMovieAdapter(SampleData.sealedMovies)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
