package com.bolsadeideas.springboot.reactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentario {
    private List<String> comentarios;

    public Comentario() {
        this.comentarios = new ArrayList<>();
    }

    public void addComentarios(String comentario) {
        this.comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "Comentarios{" +
              comentarios +
                '}';
    }
}
