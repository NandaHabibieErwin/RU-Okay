package com.blackjack.ru_okay.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.consult.ConsultPsychologListActivity
import com.blackjack.ru_okay.databinding.FragmentConsultBinding

class ConsultFragment : Fragment() {

    private var _binding: FragmentConsultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chatCallService.setOnClickListener {
            val intent = Intent(activity, ConsultPsychologListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
