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
    private static JWavefrontObject caja; 
    private static Shader shader; // Gerenciador dos shaders
    private static GL3 gl;
    private static LinkedList<Robot> colaRobots;
    private static LinkedList<OperacionRobot> robotsActivos;

    //ESTADOS 
    private final int DEJAR_CAJA=0;
    private final int TRAER_CAJA=1;
    private final int RETORNAR=2;
    private final int  A_LA_COLA=3;
    
    private final LinkedList<Movimiento> movimientos;  
    private final Motor motor;
    private final Horquilla horq ;
    private float avance  = 0.2f;
    private float avanceCola  = 0.2f;
    private final int ordenRobot;
    private final Punto posHorquilla;
    private float miRotacion;
    private float miDireccion;
    private final boolean enRetroceso;
       
    private Punto posicionActual;    
    private Punto posicionInicial;
    private Punto direccion;
    private Punto posObj;//Guarda la posicion en el estante donde se encontro una caja o espacio disponible
    
    // vectores unitarios arriba (-z) abajo(+z) derecha(+x) izquierda(-x)
    private final Punto arriba=new Punto(0,0,-1);
    private final Punto abajo=new Punto(0,0,1);
    private final Punto derecha=new Punto(1,0,0);
    private final Punto izquierda=new Punto(-1,0,0);
    
    private boolean tengoCaja;
    private int nivelEstante;



    public Robot(int n,float x , float z) {
        ordenRobot=n;
        posicionActual=new Punto(x,0.0f,z);
        posicionInicial=new Punto(x,0.0f,z);
        posHorquilla= new Punto(x,0.0f,z);
        direccion= new Punto();
        movimientos=new LinkedList<>();
        motor=new Motor();
        horq=new Horquilla();
        enRetroceso=false;
        marcar(posicionActual, true);    
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
        caja = new JWavefrontObject(new File("./warehouse/caja.obj")); 
        
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
        
        caja.init(gl, shader);
        caja.unitize();
        caja.dump();  
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
    //ESTADOS DE CAJAS EN LOS ESTANTES
        //Vacio =  "No hay caja en esa prosicion(Espacio libre para ocupara caja)"
        // 0 = "Hay una caja disponible para retirar en esa posicion"
        // 1 = "Caja ya reservada para ser retirada"
        // 2 = "Espacio reservado para una caja"
    private DescritorEstante buscarEspacioEstante(int estante)
    {
        int rangoH1=4,rangoH2=5,rangoV1=4,rangoV2=8;
        int interH=7,interV=8;
        int c=0;
        
        boolean encontrado=false;
        int lado=0;
        Punto posEstadoCaja=null;
        int piso=0;
        for(int i=1;i<=3;i++) 
        {
            rangoH1=4;rangoH2=5;
            for(int j=1;j<=4;j++) 
            {
                c++;
                if(estante==c)
                {
                    for(int p=1;p<=2;p++) 
                    {
                        for(int k=rangoH1;k<=rangoH2;k++) 
                        {
                            for(int l=rangoV1;l<=rangoV2;l++) 
                            {
                                if(local[p][l].charAt(k)==' ')
                                {
                                    if(k==rangoH2)
                                        lado=1;
                                    encontrado=true;
                                    posEstadoCaja=new Punto(k,0,l);
                                    piso=p;
                                    char[] fila=local[p][l].toCharArray();
                                    fila[k]='2';
                                    local[p][l]=new String(fila);
                                    System.out.println(local[p][l-1]);
                                    System.out.println(local[p][l]);
                                    System.out.println(local[p][l+1]);
                                    return (new DescritorEstante(encontrado,lado,posEstadoCaja,piso));
                                }
                            }
                        }                        
                    }
                } 
                rangoH1+=interH;
                rangoH2+=interH;
            }  
            rangoV1+=interV;
            rangoV2+=interV;
            
        }
        return (new DescritorEstante(encontrado,lado,posEstadoCaja,piso));
    }
    // Informacion sobre variable miDireccion (Controla hacia donde apunta la horquilla)
    // miDireccion=-90  (La horquilla apunta a la derecha)
    // miDireccion=90   (La horquilla apunta a la izquierda)
    // miDireccion=-180   (La horquilla apunta hacia abajo)
    // miDireccion=180   (La horquilla apunta hacia arriba)
    private void calcularRuta(int estanteObj)
    {
        DescritorEstante desEstante=buscarEspacioEstante(estanteObj);
        if(desEstante.encontrado)
        {            
            Punto penultimaDir;
            Punto ultimaDir=null;
            posObj=desEstante.posicion;
            nivelEstante=desEstante.piso;
            float espaciosRetorno;
            System.out.println("espacio : ("+posObj.x+","+posObj.y+","+posObj.z+")");
            movimientos.addFirst(new Movimiento(4,arriba,false,false));
            if(estanteObj==1||estanteObj==5||estanteObj==9)
            {
                movimientos.addFirst(new Movimiento(9,izquierda,false,false));
                if(desEstante.lado==1)
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=izquierda;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));    
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(5,izquierda,false,false));   
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
                else
                {
                    float espacios=posObj.z-2;
                    espaciosRetorno=31-posObj.z;////
                    System.out.println("nEspacios : " +espacios);
                    movimientos.addFirst(new Movimiento(24,arriba,false,false));
                    movimientos.addFirst(new Movimiento(5,izquierda,false,false));
                    movimientos.addFirst(new Movimiento(espacios,abajo,false,false));
                    penultimaDir=abajo;
                    ultimaDir=derecha;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));   
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(12,derecha,false,false)); 
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
            }
            else if(estanteObj==2||estanteObj==6||estanteObj==10)
            {
               if(desEstante.lado==1)
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    movimientos.addFirst(new Movimiento(2,izquierda,false,false));
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=izquierda;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));    
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(12,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
                else
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    System.out.println("nEspacios : " +espacios);
                    movimientos.addFirst(new Movimiento(7,izquierda,false,false));
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=derecha;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));    
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(7,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
            }
            else if(estanteObj==3||estanteObj==7||estanteObj==11)
            {
               if(desEstante.lado==1)
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    movimientos.addFirst(new Movimiento(5,derecha,false,false));
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=izquierda; 
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));   
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(19,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
                else
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    System.out.println("nEspacios : " +espacios);
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=derecha;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));   
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(14,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
            }
            else if(estanteObj==4||estanteObj==8||estanteObj==12)
            {
               if(desEstante.lado==1)
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=izquierda;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(26,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
                else
                {
                    float espacios=26-posObj.z;
                    espaciosRetorno=posObj.z-2;
                    System.out.println("nEspacios : " +espacios);
                    movimientos.addFirst(new Movimiento(7,derecha,false,false));
                    movimientos.addFirst(new Movimiento(espacios,arriba,false,false));
                    penultimaDir=arriba;
                    ultimaDir=derecha;
                    movimientos.addFirst(new Movimiento(0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(-0.8f,ultimaDir,false,false));
                    movimientos.addFirst(new Movimiento(espaciosRetorno,penultimaDir,false,false));
                    
                    movimientos.addFirst(new Movimiento(21,izquierda,false,false)); 
                    movimientos.addFirst(new Movimiento(29,abajo,false,false));
                    movimientos.addFirst(new Movimiento(12,derecha,false,false));
                    movimientos.addFirst(new Movimiento(0,abajo,false,true));
                }
            }       

            
        }
        else
        {
            System.out.println("El estante solicitado esta lleno");
            nivelEstante=0;
        }
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
    // Informacion sobre variable miDireccion (Controla hacia donde apunta la horquilla)
        // miDireccion=-90  (La horquilla apunta a la derecha)
        // miDireccion=90   (La horquilla apunta a la izquierda)
        // miDireccion=-180   (La horquilla apunta hacia abajo)
        // miDireccion=180   (La horquilla apunta hacia arriba)
    static void controlarRobots(boolean goRobot,int estado,int estante)//Controla todos los roboys
    {
        if(goRobot)
        {
            if(!colaRobots.isEmpty())
            {
                Robot botActivado=colaRobots.removeLast();
                //ACA SE CALCULARAN LOS MOVIMIENTOS NECESARIOS PARA TRIPULAR EL BOT
                if(estado==0)
                    botActivado.calcularRuta(estante);
                if(!botActivado.getMovimientos().isEmpty())
                {
                    if(estado==0)//0
                        botActivado.tengoCaja=true;
                    else if(estado==1)//1
                        botActivado.tengoCaja=false; 
                    robotsActivos.addFirst(new OperacionRobot(botActivado,estado,estante));
                    System.out.println("nMovimientos : "+botActivado.getMovimientos().size());
                }
                else
                {
                    colaRobots.addLast(botActivado);
                }
            }
            else
                System.out.println("Todos los robots estan ocupados!! Aguarde un momento porfavor :D");
        }
        if(!robotsActivos.isEmpty())//Dibuja los robots activos
        {
            for (int i=robotsActivos.size()-1;i>=0;i--) 
            {
                OperacionRobot opRobot=robotsActivos.get(i);
                Robot bot=opRobot.robot;    
                int estadoRobot=opRobot.estadoRobot;
                //ACA SE MANDAN A EJECUTAR LOS MOVIMIENTOS DEL BOT PREVIAMENTE CALCULADOS
                Movimiento movActual = bot.getMovimientos().getLast();
                if(movActual.direccion.esIgual(0, 0, -1))
                {
                    bot.setMiDireccion(0);
                    bot.setMiRotacion(-180);
                }
                else if(movActual.direccion.esIgual(0, 0, 1))
                {
                    bot.setMiDireccion(180);
                    bot.setMiRotacion(-180);
                }
                else if(movActual.direccion.esIgual(1, 0, 0))
                {
                    bot.setMiDireccion(-90);
                    bot.setMiRotacion(-90);
                }
                else if(movActual.direccion.esIgual(-1, 0, 0))
                {
                    bot.setMiDireccion(90);    
                    bot.setMiRotacion(-90);
                }
                if(abs(movActual.desplazamiento-0.8f)<=0.001||abs(movActual.desplazamiento+0.8f)<=0.001)
                    bot.setAvance(0.005f);
                else if(movActual.desplazamiento==0)
                {
                    if(movActual.fin)
                    {
                        if(isColaRobotOrdenada())
                        {
                            Robot ultimoCola = colaRobots.getLast();
                            float despV=ultimoCola.getPosicionActual().z-bot.getPosicionActual().z;
                            float despH=ultimoCola.getPosicionActual().x-bot.getPosicionActual().x;
                            bot.getMovimientos().removeLast();
                            bot.getMovimientos().addFirst( new Movimiento(despV,bot.getAbajo(),false,false));
                            if(despH>=0)
                                bot.getMovimientos().addFirst( new Movimiento(despH,bot.getDerecha(),false,false));
                            bot.getMovimientos().addFirst(new Movimiento(0,bot.getAbajo(),false,false));
                        }
                    }
                    else   
                    {
                        if(isColaRobotOrdenada())
                        {
                            Robot ultimoCola = colaRobots.getLast();
                            float despV=ultimoCola.getPosicionActual().z;
                            float despH=ultimoCola.getPosicionActual().x;
                            Punto nuevaPos=new Punto();
                            nuevaPos.y=0;
                            if(abs(despH-14)<=0.001)
                            {
                                nuevaPos.x=despH;
                                nuevaPos.z=despV-2;
                            }
                            else if(abs(despH-16)<=0.001)
                            {
                                if(abs(despV-44)<=0.001)
                                {
                                    nuevaPos.x=despH-2;
                                    nuevaPos.z=despV;
                                }
                                else
                                {
                                    nuevaPos.x=despH;
                                    nuevaPos.z=despV+2;
                                }
                            }
                            bot.setPosicionInicial(nuevaPos);
                            bot.setPosicionActual(nuevaPos);
                            bot.movimientos.removeLast();
                            colaRobots.addFirst(bot);
                            robotsActivos.remove(i);
                        }   
                    }
                }
                else
                    bot.setAvance(0.2f);
                if(!(movActual.desplazamiento==0&&movActual.fin==false))
                    bot.actuar(movActual.desplazamiento,movActual.direccion,estadoRobot,movActual.fin);   
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
                                bot.animarRobotCola(factor); 
                                bot.setDireccion(bot.getDerecha());
                            }
                            else
                            {
                                bot.setMiDireccion(-180.0f);
                                bot.setDireccion(bot.getAbajo());
                                bot.animarRobotCola(factor); 
                            }
                        }
                        else //if(posActualBot.z>42)//1
                        {
                            //posInicialBot.x=posInicialBot.x+2.0f;
                            bot.setMiDireccion(0.0f);
                            bot.setMiRotacion(-90.0f);    
                            bot.setDireccion(bot.getDerecha());
                            bot.animarRobotCola(factor); 
                            bot.setDireccion(bot.getArriba());
                        }
                    }
                    else //2
                    {
                        //posInicialBot.z=posInicialBot.z-2.0f;
                        bot.setMiDireccion(0.0f);
                        bot.setMiRotacion(-180.0f);
                        bot.setDireccion(bot.getArriba());
                        bot.animarRobotCola(factor);                        
                    }                                                       
                }   
                else
                {
                    bot.setPosicionInicial(bot.getPosicionActual());        
                    /*Punto posIni = bot.getPosicionInicial();
                    Punto dir = bot.getDireccion();
                    System.out.println("Pos actual Robot["+bot.getOrdenRobot()+"] :("+
                            posIni.x+","+posIni.y+","+posIni.z+") con direccion: "+"("+
                            dir.x+","+dir.y+","+dir.z+") "); */
                    bot.animarRobotCola(0); 
                } 
            }
        }
    }   
    public void actuar(float pasos,Punto dir,int operacion,boolean isFinal)   
    {
        float pos=0;  
        if(abs(pasos-0.8f)<=0.001||abs(pasos+0.8f)<=0.001)
            if(nivelEstante==1)
                pos=0;
            else if(nivelEstante==2)
                pos=0.8f;
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
        Punto ir=new Punto(posicionInicial.x, posicionInicial.y,posicionInicial.z);
        ir.sumar(pasos,dir);  
        System.out.println("Destino Robot["+ordenRobot+"] :("+ir.x+","+ir.y+","+ir.z+")");  
        if(posicionActual.esIgual(ir.x,ir.y,ir.z))
        {
             posicionActual = ir;    
             posicionInicial.inicio(ir.x, ir.y, ir.z);
             if(!isFinal)
             {
                if(abs(movimientos.getLast().desplazamiento)-0.8f<=0.001)
                {
                    tengoCaja=false;
                    char[] fila=local[nivelEstante][(int)posObj.z].toCharArray();
                    fila[(int)posObj.x]='0';
                    local[nivelEstante][(int)posObj.z]=new String(fila);  
                }
                if(abs(movimientos.getLast().desplazamiento)+0.8f<=0.001)
                {
                    nivelEstante=1;
                }
                movimientos.removeLast();       
                 System.out.println("siguiente paso: " + movimientos.getLast().desplazamiento);
             }
             System.out.println("Pos final actual Robot["+ordenRobot+"] :("+
                            posicionInicial.x+","+posicionInicial.y+","+posicionInicial.z+") con direccion: "+"("+
                            dir.x+","+dir.y+","+dir.z+") "); 
             //System.out.println("Posicicion2 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");  
        }
        else
        {
            if(pasos<0)
                avance=-avance;           
            posicionActual.sumar(avance, dir);  
             System.out.println("Pos actual Robot["+ordenRobot+"] :("+
                            posicionActual.x+","+posicionActual.y+","+posicionActual.z+") con direccion: "+"("+
                            dir.x+","+dir.y+","+dir.z+") ");            
            //System.out.println("Posicicion1 actual Robot["+ordenRobot+"] :("+posicionActual.x+","+posicionActual.y+","+posicionActual.z+")");             
        }
    }
    public void animarRobotCola(float pasos)  
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

    public LinkedList<Movimiento> getMovimientos() {
        return movimientos;
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

    public void setPosicionActual(Punto posicionActual) {
        this.posicionActual = posicionActual;
    }

    public void setPosObj(Punto posObj) {
        this.posObj = posObj;
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



    public void setAvance(float avance) {
        this.avance = avance;
    }

    public void setAvanceCola(float avanceCola) {
        this.avanceCola = avanceCola;
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
            if(conCaja)
            {
                modelMatrix.translate(0,0.1f,-0.23f);
                modelMatrix.bind();
                caja.draw();
            }
            modelMatrix.pop();
        }
    }
    
}