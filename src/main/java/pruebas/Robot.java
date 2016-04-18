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
    private Motor motor;
    private Horquilla horq ;
    private float avance  = 0.1f;
    private boolean libre;
    private Vector<Punto> movida;
    private Punto posicion;
    private Punto posHorquilla;
    private Punto destino;
    private final float angulo = 3.0f;
    private float miRotacion;
    private final float rote = 0.0f;
    private float desplazo;
    private float alto;
    private int horario;
    private Punto direccion;
    private Punto anterior;
    private boolean enEspera;
    private boolean enEstante;
    private boolean deboGirar;
    private boolean seLevanta;
    private boolean seBaja;
    private boolean tengoCaja;
    private boolean dejoCaja;
    private boolean enDestino;
    private boolean enRetroceso;
    private boolean deboAvanzar;
    private boolean retornando;
    private int miColocarX;
    private Punto vista;
    private int tiempo;

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
        deboGirar = false;
        tengoCaja=false;
        enEspera = true;
        enRetroceso=false;
        posicion.inicio(x,0.0f,z);
        marcar(posicion, true);
        libre=posicion.esIgual(14.0f,0.0f,30.0f);
        posHorquilla.inicio(posicion.x,posicion.y,posicion.z);
        //vista = {posicion.x - 2, posicion.y + 2, posicion.z + 3};
        vista.inicio(13.0f, 1.25f, 35.0f);
        desplazo = 0;
        miRotacion = 0.0f;
        if (x == 14)
        {
            if (z != 44)
                direccion.inicio(0.0f, 0.0f, -1.0f);
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
        System.out.println("texturas robot cargadas");
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
                modelMatrix.scale(0.8f, 0.8f, 0.8f);
        /*if (enRetroceso)
             motor.avanzar(-miRotacion, enRetroceso);
       else*/
        motor.avanzar(miRotacion, enRetroceso);
        modelMatrix.pop();
        
        posHorquilla.inicio(posicion.x, posicion.y,posicion.z);
        modelMatrix.push();
        modelMatrix.translate(posHorquilla.x,posHorquilla.y,posHorquilla.z);
        horq.avanzar(miRotacion, tengoCaja);
        modelMatrix.pop();


        if(posicion.esIgual(ir.x,ir.y,ir.z)||posicion.z<ir.z)
        {
             posicion = ir;
           /* if (enRetroceso)
            {
                direccion = mult(-1, direccion);
                enRetroceso = false;
                alto = 0;
                seBaja = true;
            }*/
            //posicion = ir;
        }
        else
            sumar(posicion, avance, direccion);
        
    }
    public boolean desplazar(Punto ini, Punto ir)
    {
        modelMatrix.push();    
        modelMatrix.translate(posicion.x,posicion.y,posicion.z);
        if (enRetroceso)
             motor.avanzar(-miRotacion, enRetroceso);
       else
             motor.avanzar(miRotacion, enRetroceso);
        modelMatrix.pop();
        
        posHorquilla.inicio(posicion.x, posicion.y,posicion.z);
        modelMatrix.push();
        modelMatrix.translate(posHorquilla.x,posHorquilla.y,posHorquilla.z);
        horq.avanzar(miRotacion, tengoCaja);
        modelMatrix.pop();

        sumar(posicion, avance, direccion);

        if (!(  ((ini.x <= posicion.x && posicion.x <= ir.x) || (ir.x <= posicion.x && posicion.x <= ini.x)) &&
                ((ini.y <= posicion.y && posicion.y <= ir.y) || (ir.y <= posicion.y && posicion.y <= ini.y)) &&
                ((ini.z <= posicion.z && posicion.z <= ir.z) || (ir.z <= posicion.z && posicion.z <= ini.z))
            ))
        {

            if (enRetroceso)
            {
                direccion = mult(-1, direccion);
                enRetroceso = false;
                alto = 0;
                seBaja = true;
            }

            posicion = ir;

        }


        if (posicion.z != 26)
            vista.inicio(posicion.x + 1.5f * miColocarX, posicion.y + 1.25f, posicion.z + 3);
        else
            if (posicion.z < 28)
                vista.inicio(posicion.x + 3 * miColocarX, posicion.y + 1.25f, posicion.z);
            else
                vista.inicio(12,1.25f,33);

        return true;
    }
    public void actuar()
    {
        
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


        void detener(float ang)
        {
            girar(ang);
        }

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
            modelMatrix.bind();
            eje.draw();
            modelMatrix.pop();
            
            modelMatrix.push();
            modelMatrix.bind();
            tubo.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(-0.8f,0.4f,-0.8f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(-0.8f, 0.4f, 0.8f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(0.8f,0.4f,0.8f);
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.rotate(-giro, 1, 0, 0);
            modelMatrix.bind();
            rueda.draw();
            modelMatrix.pop();

            modelMatrix.push();
            modelMatrix.translate(0.8f, 0.4f, -0.8f);
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
        void detener(float ang, boolean conCaja)
        {
            girar(ang, conCaja);
        }

        void levantar(float ang, boolean conCaja)
        {
            avanzar(ang, conCaja);
        }

        void girar(float giro, boolean conCaja)
        {
            modelMatrix.push();
            modelMatrix.rotate(giro, 0, 1, 0);
            modelMatrix.bind();
            horquilla.draw();
            /*if (conCaja)
                drawCaja(0, -0.8, -1);*/
            modelMatrix.pop();
        }

        void avanzar(float alfa, boolean conCaja)
        {
            modelMatrix.push();
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.bind();
            horquilla.draw();
            /*if (conCaja)
                drawCaja(0, 0, -1);*/
            modelMatrix.pop();
        }
    }
}
