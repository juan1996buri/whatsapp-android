package com.example.proyectofinalciclo.Clases;

public class Mensaje {
    private String idMensaje;
    private String mensajeTxt;
    private int opcionReaccion;
    private String idMensajero;
    private String fechaMensaje,tiempoMensaje,tipoMensaje,mensajeLeido;

    public Mensaje() {
    }

    public Mensaje(String mensajeTxt, String idMensajero, String fechaMensaje, String tiempoMensaje,String tipoMensaje,String idMensaje,String mensajeLeido) {
        this.mensajeTxt = mensajeTxt;
        this.idMensajero = idMensajero;
        this.fechaMensaje = fechaMensaje;
        this.tiempoMensaje = tiempoMensaje;
        this.tipoMensaje=tipoMensaje;
        this.idMensaje=idMensaje;
        this.mensajeLeido=mensajeLeido;
    }

    public String getMensajeLeido() {
        return mensajeLeido;
    }

    public void setMensajeLeido(String mensajeLeido) {
        this.mensajeLeido = mensajeLeido;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public void setTipoMensaje(String tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getMensajeTxt() {
        return mensajeTxt;
    }

    public void setMensajeTxt(String mensajeTxt) {
        this.mensajeTxt = mensajeTxt;
    }

    public int getOpcionReaccion() {
        return opcionReaccion;
    }

    public void setOpcionReaccion(int opcionReaccion) {
        this.opcionReaccion = opcionReaccion;
    }

    public String getIdMensajero() {
        return idMensajero;
    }

    public void setIdMensajero(String idMensajero) {
        this.idMensajero = idMensajero;
    }

    public String getFechaMensaje() {
        return fechaMensaje;
    }

    public void setFechaMensaje(String fechaMensaje) {
        this.fechaMensaje = fechaMensaje;
    }

    public String getTiempoMensaje() {
        return tiempoMensaje;
    }

    public void setTiempoMensaje(String tiempoMensaje) {
        this.tiempoMensaje = tiempoMensaje;
    }
}
