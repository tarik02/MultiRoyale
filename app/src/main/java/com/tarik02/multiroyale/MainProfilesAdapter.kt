package com.tarik02.multiroyale

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.main_profile.view.*

class MainProfilesAdapter(val listener: Listener) : RecyclerView.Adapter<MainProfilesAdapter.ViewHolder>() {
    val items = mutableListOf<Profile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(parent.inflate(R.layout.main_profile))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position], listener)

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(profile: Profile, listener: Listener) = with(itemView) {
            name.text = profile.name
            setOnClickListener { listener.onClick(profile) }
            setOnLongClickListener { listener.onLongClick(profile); true }
        }
    }

    interface Listener {
        fun onClick(profile: Profile)

        fun onLongClick(profile: Profile)
    }
}