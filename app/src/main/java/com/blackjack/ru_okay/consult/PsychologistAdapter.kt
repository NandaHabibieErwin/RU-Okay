package com.blackjack.ru_okay.consult

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blackjack.ru_okay.databinding.ItemPsychologistBinding
import com.bumptech.glide.Glide
import com.blackjack.ru_okay.R

class PsychologistAdapter(
    private val psychologists: List<Psychologist>,
    private val onCallButtonClicked: (Psychologist) -> Unit
) : RecyclerView.Adapter<PsychologistAdapter.PsychologistViewHolder>() {

    inner class PsychologistViewHolder(private val binding: ItemPsychologistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(psychologist: Psychologist) {
            binding.psychologistName.text = psychologist.name
            binding.psychologistSpecialty.text = psychologist.specialty
            binding.psychologistRating.rating = psychologist.rating
            Glide.with(binding.psychologistImage.context)
                .load(psychologist.imageUrl)
                .placeholder(R.drawable.ic_user_temporary)
                .into(binding.psychologistImage)

            binding.chatButton.setOnClickListener {
                val context = it.context
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("psychologist_id", psychologist.id)
                context.startActivity(intent)
            }

            binding.callButton.setOnClickListener {
                onCallButtonClicked(psychologist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PsychologistViewHolder {
        val binding = ItemPsychologistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PsychologistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PsychologistViewHolder, position: Int) {
        holder.bind(psychologists[position])
    }

    override fun getItemCount(): Int {
        return psychologists.size
    }
}
