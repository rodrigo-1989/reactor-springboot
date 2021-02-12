package com.bolsadeideas.springboot.reactor.app.models;

public class UsuarioComentarios {
	
	private Usuario usuario;
	private Comentarios comentario;
	
	public UsuarioComentarios(Usuario usuario, Comentarios comentario) {
		this.usuario = usuario;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "UsuarioComentarios [usuario=" + usuario + ", comentario=" + comentario + "]";
	}
	
}
