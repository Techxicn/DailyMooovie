package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentProfileBinding
import com.techxicn.dailymooovie.model.ProfileUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.FavoritesAdapter

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(ProfileUiState())
        binding.rvFavorites.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavorites.adapter = FavoritesAdapter(SampleData.favorites)
        binding.rowMoviePassport.setOnClickListener { navigateTo(MoviePassportFragment()) }
        binding.rowWorldExploration.setOnClickListener { navigateTo(WorldMapFragment()) }
    }

    fun render(state: ProfileUiState) {
        binding.bind(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
