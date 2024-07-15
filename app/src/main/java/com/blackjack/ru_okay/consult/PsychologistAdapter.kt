package com.blackjack.ru_okay.consult

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ItemPsychologistBinding
import com.bumptech.glide.Glide

class PsychologistAdapter(
    private val psychologists: List<Psychologist>,
    private val onPsychologistClick: (Psychologist) -> Unit
) : RecyclerView.Adapter<PsychologistAdapter.PsychologistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PsychologistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPsychologistBinding.inflate(inflater, parent, false)
        return PsychologistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PsychologistViewHolder, position: Int) {
        val psychologist = psychologists[position]
        holder.bind(psychologist, onPsychologistClick)
    }

    override fun getItemCount(): Int = psychologists.size

    class PsychologistViewHolder(private val binding: ItemPsychologistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(psychologist: Psychologist, onPsychologistClick: (Psychologist) -> Unit) {
            binding.psychologistName.text = psychologist.name
            binding.psychologistSpecialty.text = psychologist.specialty
            binding.psychologistRating.rating = psychologist.rating
            Glide.with(binding.psychologistImage.context)
                .load(psychologist.imageUrl)
                .placeholder(R.drawable.ic_user_temporary)
                .into(binding.psychologistImage)

            binding.callButton.setOnClickListener { onPsychologistClick(psychologist) }
        }
    }
}
