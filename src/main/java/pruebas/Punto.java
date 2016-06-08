/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import static java.lang.Math.abs;

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
        return abs(p-x)<=0.01f&&abs(q-y)<=0.01f&&abs(r-z)<=0.01f;
    }  
    public void sumar(float t, Punto q)
    {
        x += (t * q.x);
        y += (t * q.y);
        z += (t * q.z);
    }
}
