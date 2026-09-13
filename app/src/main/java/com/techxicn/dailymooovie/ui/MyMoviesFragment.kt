package com.techxicn.dailymooovie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.techxicn.dailymooovie.databinding.FragmentMyMoviesBinding
import com.techxicn.dailymooovie.model.MyMoviesUiState
import com.techxicn.dailymooovie.model.SampleData
import com.techxicn.dailymooovie.ui.adapter.MovieGridAdapter

class MyMoviesFragment : Fragment() {

    private var _binding: FragmentMyMoviesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        render(MyMoviesUiState())
        binding.rvMoviesGrid.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvMoviesGrid.adapter = MovieGridAdapter(SampleData.gridMovies)
    }

    fun render(state: MyMoviesUiState) {
        binding.bind(state)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
