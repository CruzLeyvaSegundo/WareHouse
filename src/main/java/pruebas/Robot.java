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
import static java.lang.Math.abs;
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
    private static int X=2;

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
    private final float avanceCola  = 0.1f;
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
        movimientos.addFirst(new Movimiento(5,arriba,false,false));
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
                direccion=arriba;
                miDireccion=-180.0f;
            }
            else {
                direccion=derecha;
                miDireccion=-90.0f;
                miRotacion = -90.0f;
            }
        }
        else {
            direccion=(arriba);
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
                /*Punto ir=new Punto(16,0,30);
                ir.sumar(6,bot.getDireccion()); */                  
                bot.actuar(aux,true);   
                aux=aux+X;
            }
        }
        if(!colaRobots.isEmpty())  //Dibuja los robots inactivas e ordena la cola de Robots
        {
            for (int i=colaRobots.size()-1;i>=0;i--) 
            {
                Robot bot=colaRobots.get(i);    
                Punto posInicialBot = bot.getPosicionInicial();
                float factor=2;
                if(!isColaRobotOrdenada())
                {
                    if (abs(posInicialBot.x-14)<=0.001)
                    {                        
                        if (!(abs(posInicialBot.z-44)<=0.001))//0
                        {
                            //posInicialBot.z=posInicialBot.z+2.0f;                          
                            if(abs(posInicialBot.z-42)<=0.001)
                            {
                                bot.setMiDireccion(-90.0f);
                                bot.setDireccion(bot.getAbajo());
                                bot.animarRobotCola(factor,false); 
                                bot.setDireccion(bot.getDerecha());
                            }
                            else
                            {
                                bot.setMiDireccion(-180.0f);
                                bot.setDireccion(bot.getAbajo());
                                bot.animarRobotCola(factor,false); 
                            }
                        }
                        else //if(posActualBot.z>42)//1
                        {
                            //posInicialBot.x=posInicialBot.x+2.0f;
                            bot.setMiDireccion(0.0f);
                            bot.setMiRotacion(-90.0f);    
                            bot.setDireccion(bot.getDerecha());
                            bot.animarRobotCola(factor,false); 
                            bot.setDireccion(bot.getArriba());
                        }
                    }
                    else //2
                    {
                        //posInicialBot.z=posInicialBot.z-2.0f;
                        bot.setMiDireccion(0.0f);
                        bot.setMiRotacion(-180.0f);
                        bot.setDireccion(bot.getArriba());
                        bot.animarRobotCola(factor,false);                        
                    }                                                       
                }   
                else
                {
                    bot.setPosicionInicial(bot.getPosicionActual());        
                    Punto posIni = bot.getPosicionInicial();
                    Punto dir = bot.getDireccion();
                    System.out.println("Pos actual Robot["+bot.getOrdenRobot()+"] :("+
                            posIni.x+","+posIni.y+","+posIni.z+") con direccion: "+"("+
                            dir.x+","+dir.y+","+dir.z+") "); 
                    bot.animarRobotCola(0,false); 
                } 
            }
        }
    }   
    public void actuar(float pasos,boolean activo)  
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
        horq.avanzar(-360.0f+miDireccion, tengoCaja,pos);
        modelMatrix.pop();

        //Punto ir= this.getPosicionInicial();
        Punto ir=new Punto(posicionInicial.x, posicionInicial.y,posicionInicial.z);
        //Punto ir=posicionInicial;
        ir.sumar(pasos,this.getDireccion());  
        //System.out.println("Destino Robot["+ordenRobot+"] :("+ir.x+","+ir.y+","+ir.z+")");  
        if(posicionActual.esIgual(ir.x,ir.y,ir.z))
        {
             posicionActual = ir;          
             //System.out.println("Posicicion2 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");  
        }
        else
        {
            posicionActual.sumar(avanceCola, direccion);    
            //System.out.println("Posicicion1 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");             
        }
    }
    public void animarRobotCola(float pasos,boolean activo)  
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
        horq.avanzar(-360.0f+miDireccion, tengoCaja,pos);
        modelMatrix.pop();

        //Punto ir= this.getPosicionInicial();
        Punto ir=new Punto(posicionInicial.x, posicionInicial.y,posicionInicial.z);
        //Punto ir=posicionInicial;
        ir.sumar(pasos,this.getDireccion());  
        //System.out.println("Destino Robot["+ordenRobot+"] :("+ir.x+","+ir.y+","+ir.z+")");  
        if(posicionActual.esIgual(ir.x,ir.y,ir.z))
        {
             posicionActual = ir;          
             //System.out.println("Posicicion2 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");  
        }
        else
        {
            posicionActual.sumar(avanceCola, direccion);    
            //System.out.println("Posicicion1 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");             
        }
    }
    public Punto getPosicionActual() {
        return posicionActual;
    }

    public int getOrdenRobot() {
        return ordenRobot;
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

    public Punto getDireccion() {
        return direccion;
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

    public void setMovidaActual(Movimiento movidaActual) {
        this.movidaActual = movidaActual;
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