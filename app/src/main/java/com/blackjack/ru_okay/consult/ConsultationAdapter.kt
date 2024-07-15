package com.blackjack.ru_okay.consult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blackjack.ru_okay.R

class ConsultationAdapter(private val consultations: List<Consultation>) :
    RecyclerView.Adapter<ConsultationAdapter.ConsultationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_consultation, parent, false)
        return ConsultationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
        val consultation = consultations[position]
        holder.bind(consultation)
    }

    override fun getItemCount(): Int = consultations.size

    inner class ConsultationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(consultation: Consultation) {
            dateTextView.text = consultation.date
            timeTextView.text = consultation.time
            statusTextView.text = consultation.status
        }
    }
}
