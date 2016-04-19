/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

import br.usp.icmc.vicg.gl.jwavefront.JWavefrontObject;
import br.usp.icmc.vicg.gl.matrix.Matrix4;
import br.usp.icmc.vicg.gl.util.Shader;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.PI;
import java.util.Vector;
import javax.media.opengl.GL3;

/**
 *
 * @author JUNIOR
 */
public class Robot {
    private static  Matrix4 modelMatrix;
    private static String[][] local;
    private static JWavefrontObject rueda;
    private static JWavefrontObject tubo;
    private static JWavefrontObject eje;
    private static JWavefrontObject carcasa;  
    private static JWavefrontObject horquilla;  
    private static Shader shader; // Gerenciador dos shaders
    private static GL3 gl;

    //ESTADOS 
    private final int RETORNAR=0;
    private final int TRAER_CAJA=1;
    private final int DEJAR_CAJA=2;
    private final int  A_LA_COLA=3;
    
    
    private int accion;
    private final Motor motor;
    private final Horquilla horq ;
    private final float avance  = 0.1f;
    private final boolean libre;
    private Vector<Punto> movida;
    private Punto posicion;
    private final Punto posHorquilla;
    private final Punto destino;
    private float miRotacion;
    private final float desplazo;
    private final Punto direccion;
    private final Punto anterior;
    private final boolean enEspera;
    private boolean tengoCaja;
    private boolean dejoCaja;
    private boolean enDestino;
    private final boolean enRetroceso;
    private final boolean retornando;
    private final Punto vista;

    public Robot(int n,float x , float z) {
        posicion=new Punto();
        posHorquilla= new Punto();
        vista=new Punto();
        direccion= new Punto();
        anterior=new Punto();
        destino = new Punto();
        motor=new Motor();
        horq=new Horquilla();
        retornando = true;
        enEspera = true;
        enRetroceso=false;
        posicion.inicio(x,0.0f,z);
        marcar(posicion, true);
        libre=posicion.esIgual(14.0f,0.0f,30.0f);
        posHorquilla.inicio(posicion.x,posicion.y,posicion.z);
        vista.inicio(13.0f, 1.25f, 35.0f);
        desplazo = 0;
        miRotacion = 0.0f;
        if (x == 14)
        {
            if (z != 44)
            {
                direccion.inicio(0.0f, 0.0f, -1.0f);
                miRotacion=90.0f;
            }
            else {
                direccion.inicio(1.0f, 0.0f, 0.0f);
                miRotacion = -90.0f;
            }
        }
        else {
            direccion.inicio(0.0f, 0.0f, 1.0f);
            miRotacion = -180.0f;
        }
        System.out.println("Configurado robot: "+n+", en la posicicion ("+posicion.x+","+posicion.y+","+posicion.z+")");  
    }
    static void initRobot(GL3 opengl,Shader sh,Matrix4 model,String[][] lc) throws IOException
    {
        gl=opengl;
        shader=sh;  
        modelMatrix=model;
        local=lc;
        rueda = new JWavefrontObject(new File("./warehouse/miLlanta.obj"));
        carcasa = new JWavefrontObject(new File("./warehouse/carcasa.obj"));
        eje = new JWavefrontObject(new File("./warehouse/eje.obj"));
        tubo = new JWavefrontObject(new File("./warehouse/tubo.obj"));
        horquilla = new JWavefrontObject(new File("./warehouse/horquilla.obj"));  
        
        rueda.init(gl, shader);
        rueda.unitize();
        rueda.dump();
        
        carcasa.init(gl, shader);
        carcasa.unitize();
        carcasa.dump();
        
        eje.init(gl, shader);
        eje.unitize();
        eje.dump();
        
        tubo.init(gl, shader);
        tubo.unitize();
        tubo.dump();    
        
        horquilla.init(gl, shader);
        horquilla.unitize();
        horquilla.dump();    
        System.out.println(" robot cargado");
    } 
    final void marcar (Punto p, boolean s)
    {
        char[] c=local[0][(int)p.z].toCharArray();
        if (s)
        {
            c[(int)p.x]='x';
        }
        else
        {
            c[(int)p.x]='x';
        }
            local[0][(int)p.z]=new String(c);
    }
    public void sumar (Punto p, float t, Punto q)
    {
        p.x += (t * q.x);
        p.y += (t * q.y);
        p.z += (t * q.z);
    }
    Punto mult (float m, Punto p)
    {
        p.x = m * p.x;
        p.y = m * p.y;
        p.z = m * p.z;
        return p;
    }
    public void actuar(Punto ir)
    {
        modelMatrix.push();    
        modelMatrix.translate(posicion.x,posicion.y,posicion.z);
        modelMatrix.scale(0.6f, 0.6f, 0.6f);
        motor.avanzar(miRotacion, enRetroceso);
        modelMatrix.pop();
        
        posHorquilla.inicio(posicion.x, posicion.y,posicion.z);
        modelMatrix.push();
        modelMatrix.translate(posHorquilla.x,posHorquilla.y,posHorquilla.z+0.1f);
        horq.avanzar(-360.0f, tengoCaja);
        modelMatrix.pop();


        if(posicion.esIgual(ir.x,ir.y,ir.z)||posicion.z<ir.z)
        {
             posicion = ir;
        }
        else
            sumar(posicion, avance, direccion);     
    }
    static void dispose(){
        rueda.dispose();
        carcasa.dispose();
        eje.dispose();
        tubo.dispose();
        horquilla.dispose();
    }   
    public class Motor
    {
        float distancia = 0.0f;
        float giro = 0.0f;
        float radio = 1.0f;
        float angulo;

        public Motor() {
            angulo = (float) (((avance / radio)*180)/PI + 1.5);
        }

/**
        void girar(float giro)
        {
            modelMatrix.push();
            modelMatrix.bind();
            carcasa.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            eje.draw();
            tubo.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(-2.5f,1,-2.5f);
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(-2.5f,1,2.5f);
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();;

            modelMatrix.push();
            modelMatrix.translate(2.5f,1,-2.5f);
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(2.5f,1,2.5f);
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();
        }
*/
        void avanzar(float alfa, boolean atras)
        {
            modelMatrix.push();
            modelMatrix.bind();
            carcasa.draw();
            modelMatrix.pop();
            
            modelMatrix.push();
            if (atras)
                modelMatrix.rotate(-alfa, 0, 1, 0);
            else
                modelMatrix.rotate(alfa, 0, 1, 0);    
            modelMatrix.translate(0,0.3f,0.0f);
            modelMatrix.bind();
            eje.draw();
            modelMatrix.pop();
            
            modelMatrix.push();
            modelMatrix.scale(2.0f,2.5f, 2.00f);
            modelMatrix.translate(0,1f,0);
            modelMatrix.bind();
            tubo.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.scale(0.4f, 0.4f, 0.4f);
            modelMatrix.translate(-1.3f,-1.3f,-1.3f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.scale(0.4f, 0.4f, 0.4f);
            modelMatrix.translate(-1.3f,-1.3f,1.3f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.scale(0.4f, 0.4f, 0.4f);
            modelMatrix.translate(1.3f,-1.3f,1.3f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.scale(0.4f, 0.4f, 0.4f);
            modelMatrix.translate(1.3f,-1.3f,-1.3f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();
            giro += angulo;
        }
    }
    public class Horquilla
    {
        /*
        void girar(float giro, boolean conCaja)
        {
            modelMatrix.push();
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            horquilla.draw();
            modelMatrix.pop();
        }
        */
        void avanzar(float alfa, boolean conCaja)
        {
            modelMatrix.push();
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.translate(0,1.0f, -0.7f);
            modelMatrix.bind();
            horquilla.draw();
            modelMatrix.pop();
        }
    }
}
