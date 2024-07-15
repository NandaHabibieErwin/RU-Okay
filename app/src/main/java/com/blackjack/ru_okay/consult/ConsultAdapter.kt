package com.blackjack.ru_okay.consult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blackjack.ru_okay.R
import com.blackjack.ru_okay.databinding.ItemConsultBinding

class ConsultsAdapter(
    private val consultsList: List<Consult>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ConsultsAdapter.ConsultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultViewHolder {
        val binding = ItemConsultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConsultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultViewHolder, position: Int) {
        val consult = consultsList[position]
        holder.bind(consult, onItemClick)
    }

    override fun getItemCount(): Int = consultsList.size

    class ConsultViewHolder(private val binding: ItemConsultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(consult: Consult, onItemClick: (String) -> Unit) {
            binding.textViewService.text = consult.service
            binding.textViewLocation.text = consult.location
            binding.textViewDatetime.text = consult.datetime.toString() // Format datetime as needed

            binding.root.setOnClickListener {
                onItemClick(consult.id)
            }
        }
    }
}
