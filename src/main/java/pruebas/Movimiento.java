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
        // Informacion sobre variable miDireccion (Controla hacia donde apunta la horquilla)
        // dirHorquilla=-90  (La horquilla apunta a la derecha)
        // dirHorquilla=90   (La horquilla apunta a la izquierda)
        // dirHorquilla=180   (La horquilla apunta hacia abajo)
        // dirHorquilla=0   (La horquilla apunta hacia arriba)
    public float desplazamiento;
    public Punto direccion;  
    public boolean caja;
    public boolean fin;
    public Movimiento(float desplazamiento, Punto direccion, boolean caja, boolean fin) {
        this.desplazamiento = desplazamiento;
        this.direccion = direccion;
        this.caja = caja;
        this.fin = fin; 
    }

}
