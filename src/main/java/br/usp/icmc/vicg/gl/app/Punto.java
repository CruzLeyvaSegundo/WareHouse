/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.usp.icmc.vicg.gl.app;

/**
 *
 * @author JUNIOR
 */
public class Punto {
    float x;
    float y;
    float z;
    Punto()
    {
        
    }
    Punto(float xo,float yo,float zo)
    {
        x=xo;
        y=yo;
        z=zo;
    }
    void inicio(float xo,float yo,float zo)
    {
        x=xo;
        y=yo;
        z=zo;
    }
    boolean esIgual(float p,float q,float r)
    {
        return (x==p&&y==q&&z==r);
    }     
}
