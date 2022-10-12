package com.example.proyectofinalciclo.Clases;

public class Grupo {
    private String nombre, descripcion,tipoGrupo,imagen,idUsuario,idGrupo;

    public Grupo() {
    }

    public Grupo(String nombre, String descripcion, String tipoGrupo, String imagen, String idUsuario,String idGrupo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoGrupo = tipoGrupo;
        this.imagen = imagen;
        this.idUsuario = idUsuario;
        this.idGrupo=idGrupo;
    }

    public String getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(String idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoGrupo() {
        return tipoGrupo;
    }

    public void setTipoGrupo(String tipoGrupo) {
        this.tipoGrupo = tipoGrupo;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
