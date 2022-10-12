package com.example.proyectofinalciclo.Clases;

public class Usuario {
    private String nombreUsuario, descripcionUsuario, telefonoUsuario, imagenUsuario, idUsuario,sexoUsuario, fechaNacimiento;
    private boolean isSelected = false;


    public Usuario(String nombreUsuario, String descripcionUsuario, String telefonoUsuario, String imagenUsuario, String idUsuario,String sexoUsuario,String fechaNacimiento) {
        this.nombreUsuario = nombreUsuario;
        this.descripcionUsuario = descripcionUsuario;
        this.telefonoUsuario = telefonoUsuario;
        this.imagenUsuario = imagenUsuario;
        this.idUsuario = idUsuario;
        this.sexoUsuario=sexoUsuario;
        this.fechaNacimiento=fechaNacimiento;
    }
    public Usuario() {
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public String getSexoUsuario() {
        return sexoUsuario;
    }

    public void setSexoUsuario(String sexoUsuario) {
        this.sexoUsuario = sexoUsuario;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }



    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getDescripcionUsuario() {
        return descripcionUsuario;
    }

    public void setDescripcionUsuario(String descripcionUsuario) {
        this.descripcionUsuario = descripcionUsuario;
    }

    public String getTelefonoUsuario() {
        return telefonoUsuario;
    }

    public void setTelefonoUsuario(String telefonoUsuario) {
        this.telefonoUsuario = telefonoUsuario;
    }

    public String getImagenUsuario() {
        return imagenUsuario;
    }

    public void setImagenUsuario(String imagenUsuario) {
        this.imagenUsuario = imagenUsuario;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
