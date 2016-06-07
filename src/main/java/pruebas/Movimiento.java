/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

/**
 *
 * @author JUNIOR
 */
public class Movimiento {
    public int desplazamiento;
    public Punto direccion;
    public boolean caja;
    public boolean fin;
    public Movimiento(int desplazamiento, Punto direccion, boolean caja, boolean fin) {
        this.desplazamiento = desplazamiento;
        this.direccion = direccion;
        this.caja = caja;
        this.fin = fin;
    }

}
