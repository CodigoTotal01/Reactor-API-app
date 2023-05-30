package com.bolsadeideas.springboot.reactor.app.models;

public class UsuariosComentarios {
    private Usuario usuario;
    private Comentarios comentarios;

    public UsuariosComentarios(Usuario usuario, Comentarios comentarios) {
        this.usuario = usuario;
        this.comentarios = comentarios;
    }


    @Override
    public String toString() {
        return "UsuariosComentarios{" +
                "usuario=" + usuario +
                ", comentarios=" + comentarios +
                '}';
    }
}
