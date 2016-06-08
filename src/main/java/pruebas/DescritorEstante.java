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
public class DescritorEstante {
    boolean encontrado;
    int lado; //11 derecho   -   0 izquierdo
    Punto posicion;
    int piso;// 1 primero   -  2  segundo

    public DescritorEstante(boolean encontrado, int lado, Punto posicion, int piso) {
        this.encontrado = encontrado;
        this.lado = lado;
        this.posicion = posicion;
        this.piso = piso;
    }
       
}
