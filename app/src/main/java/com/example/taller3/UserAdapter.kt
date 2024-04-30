package com.example.taller3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private var usuarios: List<User>, private val onClickListener: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UsuarioViewHolder>() {

    fun setUsers(usuarios: List<User>) {
        this.usuarios = usuarios
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario)
        holder.itemView.setOnClickListener { onClickListener(usuario) }
    }

    override fun getItemCount(): Int {
        return usuarios.size
    }

    class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.text_nombre)
        private val imagenImageView: ImageView = itemView.findViewById(R.id.image_usuario)
        private val verUbicacionButton: Button = itemView.findViewById(R.id.btn_ver_ubicacion)

        fun bind(usuario: User) {
            nombreTextView.text = usuario.nombre
            imagenImageView.setImageResource(usuario.imagenId)
            verUbicacionButton.setOnClickListener {
                //Ver Ubicación
            }
        }
    }
}