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
import java.util.LinkedList;
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
    private static LinkedList<Robot> colaRobots;
    private static LinkedList<Robot> robotsActivos;
    private static int X=1;

    //ESTADOS 
    private final int RETORNAR=0;
    private final int TRAER_CAJA=1;
    private final int DEJAR_CAJA=2;
    private final int  A_LA_COLA=3;
    
    private final LinkedList<Movimiento> movimientos;
    private Movimiento movidaActual=null;   
    private final Motor motor;
    private final Horquilla horq ;
    private final float avance  = 0.2f;
    private final float avanceCola  = 0.001f;
    private final int ordenRobot;
    private final Punto posHorquilla;
    private float miRotacion;
    private float miDireccion;
    private final float desplazo;   
    private final boolean enEspera; 
    private final boolean enRetroceso;
    
    private Punto posicionActual;    
    private Punto posicionInicial;
    private Punto direccion;
    private Punto destinoSiguiente;
    
    // vectores unitarios arriba (-z) abajo(+z) derecha(+x) izquierda(-x)
    private final Punto arriba=new Punto(0,0,-1);
    private final Punto abajo=new Punto(0,0,1);
    private final Punto derecha=new Punto(1,0,0);
    private final Punto izquierda=new Punto(-1,0,0);
    
    private boolean tengoCaja;
    private boolean dejoCaja;
    private boolean enDestino;



    public Robot(int n,float x , float z) {
        ordenRobot=n;
        posicionActual=new Punto(x,0.0f,z);
        posicionInicial=new Punto(x,0.0f,z);
        posHorquilla= new Punto(x,0.0f,z);
        direccion= new Punto();
        movimientos=new LinkedList<>();
        motor=new Motor();
        horq=new Horquilla();
        enEspera = true;
        enRetroceso=false;
        marcar(posicionActual, true);    
        movimientos.addFirst(new Movimiento(10,arriba,false,false));
        movimientos.addFirst(new Movimiento(1,derecha,false,true));
        /*
        movimientos.add(new Movimiento(4,arriba,false,false));
        movimientos.add(new Movimiento(5,izquierda,false,false));
        movimientos.add(new Movimiento(6,arriba,false,true));
        */
        desplazo = 0;
        miRotacion = 0.0f;
        miDireccion=0.0f;
        colaRobots.addFirst((Robot)this);
        if (x == 14)
        {
            if (z != 44)
            {
                direccion.inicio(0.0f, 0.0f, 1.0f);
                miDireccion=-180.0f;
            }
            else {
                direccion.inicio(1.0f, 0.0f, 0.0f);
                miDireccion=-90.0f;
                miRotacion = -90.0f;
            }
        }
        else {
            direccion.inicio(0.0f, 0.0f, -1.0f);
            miRotacion = -180.0f;
        }
        System.out.println("Configurado robot: "+n+", en la posicicion ("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");  
    }
    static void initRobot(GL3 opengl,Shader sh,Matrix4 model,String[][] lc) throws IOException
    {
        colaRobots=new LinkedList<>();
        robotsActivos=new LinkedList<>();
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
        //System.out.println(" robot cargado");
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
    public Punto mult (float m, Punto p)
    {
        p.x = m * p.x;
        p.y = m * p.y;
        p.z = m * p.z;
        return p;
    }
    static boolean isColaRobotOrdenada()//Verifica si la cola de robots esta ordenada
    {
        Punto pivote=new Punto(16,0,30);
        int factor=2;
        for (int i = colaRobots.size()-1; i >=0; i--) 
        {
            Robot bot=colaRobots.get(i);            
            Punto posActualBot = bot.getPosicionActual();
            if(!posActualBot.esIgual(pivote.x,pivote.y,pivote.z))
                return false;
            if(pivote.x==16&&pivote.z==44)
            {
                pivote.x=14;
                factor=-2;
            }
            else
                pivote.z=pivote.z+factor;
                
        }
        return true;
    }
    static void controlarRobots(boolean goRobot)//Controla todos los roboys
    {
        if(goRobot)
        {
            robotsActivos.addFirst(colaRobots.removeLast());
        }
        if(!robotsActivos.isEmpty())//Dibuja los robots activos
        {
            int aux=X;
            for (int i=robotsActivos.size()-1;i>=0;i--) 
            {
                Robot bot=robotsActivos.get(i);     
                bot.actuar(0,true);  
                //bot.actuar(new Punto(16.0f,0.0f,30.0f-aux),bot.getAvance(), false, 0);   
                aux=aux+X;
            }
        }
        if(!colaRobots.isEmpty())  //Dibuja los robots inactivas e ordena la cola de Robots
        {
            for (int i=colaRobots.size()-1;i>=0;i--) 
            {
                Robot bot=colaRobots.get(i);    
                Punto posActualBot = bot.getPosicionActual();
                //Punto posInicialBot = bot.getPosicionInicial();
                int factor;
                if(!isColaRobotOrdenada())
                {
                    if (posActualBot.x == 14)
                    {                        
                        if (posActualBot.z != 44)
                        {
                            factor=2;
                            if(posActualBot.z == 44)
                            {
                                bot.setMiDireccion(-90.0f);
                                bot.setDireccion(new Punto(0.0f, 0.0f, 1.0f));
                            }
                            else
                            {
                                bot.setMiDireccion(-180.0f);
                                bot.setDireccion(new Punto(0.0f, 0.0f, 1.0f));
                            }
                        }
                        else //if(posActualBot.z>42)
                        {
                            factor=2;
                            bot.setDireccion(new Punto(1.0f, 0.0f, 0.0f));
                            bot.setMiDireccion(0.0f);
                            bot.setMiRotacion(-90.0f);                       
                        }
                    }
                    else 
                    {
                        factor=2;
                        bot.setDireccion(new Punto(0.0f, 0.0f, -1.0f));
                        bot.setMiDireccion(0.0f);
                        bot.setMiRotacion(-180.0f);
                    } 
                    bot.actuar(factor,false);  
                    //bot.actuar(posInicialBot,bot.getAvanceCola(), false, 0); 
                }   
                else
                {
                    bot.setPosicionInicial(bot.getPosicionActual());
                    bot.actuar(0,false); 
                    //bot.actuar(bot.getPosicionInicial(),bot.getAvanceCola(), false, 0); 
                } 
            }
        }
    }
    //public void actuar(Punto ir,float velAvance,boolean estHorquilla,int girar)
    public void actuar(int ir,boolean activo)
    {
        float pos=0;
        modelMatrix.push();    
        modelMatrix.translate(posicionActual.x,posicionActual.y,posicionActual.z);
        modelMatrix.scale(0.6f, 0.6f, 0.6f);
        motor.avanzar(miRotacion, enRetroceso);
        modelMatrix.pop();
        
        posHorquilla.inicio(posicionActual.x, posicionActual.y,posicionActual.z);
        modelMatrix.push();
        modelMatrix.translate(posHorquilla.x,posHorquilla.y,posHorquilla.z);
        /*if(estHorquilla)
            pos=0;
        else
            pos=0.8f;
        if(girar==1)
            miDireccion=0;
        else if(girar==2)
            miDireccion=90.0f;
        else if(girar==3)
            miDireccion=-90.0f;*/
        horq.avanzar(-360.0f+miDireccion, tengoCaja,pos);
        modelMatrix.pop();
        if(!activo)
        {
            destinoSiguiente=new Punto(posicionActual.x, posicionActual.y,posicionActual.z);    
            movidaActual=new Movimiento(ir,direccion,false,true);
            sumar(destinoSiguiente,movidaActual.desplazamiento,movidaActual.direccion);
        }
        else
        {
            if(movimientos.isEmpty())
                destinoSiguiente=new Punto(posicionActual.x, posicionActual.y,posicionActual.z);
            else if(movidaActual==null)
            {
                movidaActual=movimientos.removeLast();
                destinoSiguiente=new Punto(posicionActual.x, posicionActual.y,posicionActual.z);
                sumar(destinoSiguiente,movidaActual.desplazamiento,movidaActual.direccion);
            }
        }
        if(posicionActual.esIgual(destinoSiguiente.x,destinoSiguiente.y,destinoSiguiente.z))
        {
             posicionActual = destinoSiguiente;
             if(!movimientos.isEmpty()&&activo)
             {
                 movidaActual=movimientos.removeLast();
                 sumar(destinoSiguiente,movidaActual.desplazamiento,movidaActual.direccion);
             }
             //System.out.println("Posicicion2 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");  
        }
        else
        {
            sumar(posicionActual, avance, movidaActual.direccion);    
            //sumar(posicionActual, avance, direccion);    
            System.out.println("Posicicion1 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");             
        }
    }
    public Punto getPosicionActual() {
        return posicionActual;
    }

    public Punto getPosicionInicial() {
        return posicionInicial;
    }

    public float getAvance() {
        return avance;
    }

    public float getAvanceCola() {
        return avanceCola;
    }
//Direccion unitaria
    public Punto getArriba() {
        return arriba;
    }

    public Punto getAbajo() {
        return abajo;
    }

    public Punto getDerecha() {
        return derecha;
    }

    public Punto getIzquierda() {
        return izquierda;
    }

    public void setPosicionInicial(Punto posicionInicial) {
        this.posicionInicial = posicionInicial;
    }

    public void setMiRotacion(float miRotacion) {
        this.miRotacion = miRotacion;
    }

    public void setMiDireccion(float miDireccion) {
        this.miDireccion = miDireccion;
    }

    public void setDireccion(Punto direccion) {
        this.direccion = direccion;
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
            modelMatrix.translate(0,0.85f,0);
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

        void avanzar(float alfa, boolean conCaja,float pos)
        {
            modelMatrix.push();
            modelMatrix.rotate(alfa, 0, 1, 0);
            modelMatrix.translate(0,1.0f+pos, -0.7f);
            modelMatrix.bind();
            horquilla.draw();
            modelMatrix.pop();
        }
    }
    
}