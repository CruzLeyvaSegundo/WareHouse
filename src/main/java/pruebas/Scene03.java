/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;
import br.usp.icmc.vicg.gl.core.Light;
import br.usp.icmc.vicg.gl.core.Material;
import br.usp.icmc.vicg.gl.jwavefront.JWavefrontObject;
import br.usp.icmc.vicg.gl.matrix.Matrix4;
import br.usp.icmc.vicg.gl.model.SimpleModel;
import br.usp.icmc.vicg.gl.model.Square;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import br.usp.icmc.vicg.gl.util.Shader;
import br.usp.icmc.vicg.gl.util.ShaderFactory;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author JUNIOR
 */

public class Scene03 extends KeyAdapter implements GLEventListener{
    
  private final Shader shader; // Gerenciador dos shaders
  private final Matrix4 modelMatrix;
  private final Matrix4 projectionMatrix;
  private final Matrix4 viewMatrix;
  
  //Piso
  private final SimpleModel square;
  private static float[] QUAD_VERTICES;
  int dist = 5;
  private final Material material;
  
  //Local
  private final JWavefrontObject caja;
  private final JWavefrontObject pared;
  private final JWavefrontObject base;
  private final JWavefrontObject estante;  
  private final SimpleModel floor;
  
  private final Light light;
  private final String[][] local;
  private float alpha;
  private float beta;
  private float delta;
  private int height;
  private int width;
  private Punto vista;
  private Punto dir;
  private final Punto posFin;
  
  private final int DEJAR_CAJA=0;
  private final int TRAER_CAJA=1;
    
  private final int LARGO = 47;
  private final int ANCHO = 31;//29
  private final int ALTURA = 3;
  private boolean band=false;
  private boolean goRobot=false;
  private int estadoRobot=0;
  private int estanteObjetivo=1;
  //Robot
  private final int nRobot=16;
  private final Robot[] robot;

   public Scene03() 
   {
        // Carrega os shaders
        shader = ShaderFactory.getInstance(ShaderFactory.ShaderType.COMPLETE_SHADER);
        modelMatrix = new Matrix4();
        projectionMatrix = new Matrix4();
        viewMatrix = new Matrix4();
        
        //Carga de OBJ
        caja = new JWavefrontObject(new File("./warehouse/caja.obj"));
        pared = new JWavefrontObject(new File("./warehouse/pared.obj"));
        base = new JWavefrontObject(new File("./warehouse/base.obj"));
        estante = new JWavefrontObject(new File("./warehouse/estante.obj"));
        floor = new Rectangle();
        
        robot= new Robot[nRobot];
        light = new Light();
        material=new Material();
        
        QUAD_VERTICES=new float[12];
        alpha = 0;
        beta = 0;
        vista=new Punto(14,12,48);
        dir = new Punto(14,2,33);
        posFin=new Punto(15,0,30);     
        square=new Square(QUAD_VERTICES);
        this.local = new String[][]{
                            {
                                "===============================",//0
                                "=|||||||||||||||||||||||||||||=",//1
                                "=|...........................|=",//2
                                "=|.||||.|.||||.|.||||.|.||||.|=",//3
                                "=|..  ..|..  ..|..  ..|..  ..|=",//4
                                "=|..  ..|..  ..|..  ..|..  ..|=",//5
                                "=|..  ..|..  ..|..  ..|..  ..|=",//6
                                "=|..  ..|..  ..|..  ..|..  ..|=",//7
                                "=|..  ..|..  ..|..  ..|..  ..|=",//8
                                "=|.||||.|.||||.|.||||.|.||||.|=",//9
                                "=|...........................|=",//10
                                "=|.||||.|.||||.|.||||.|.||||.|=",//11
                                "=|..  ..|..  ..|..  ..|..  ..|=",//12
                                "=|..  ..|..  ..|..  ..|..  ..|=",//13
                                "=|..  ..|..  ..|..  ..|..  ..|=",//14
                                "=|..  ..|..  ..|..  ..|..  ..|=",//15
                                "=|..  ..|..  ..|..  ..|..  ..|=",//16
                                "=|.||||.|.||||.|.||||.|.||||.|=",//17
                                "=|...........................|=",//18
                                "=|.||||.|.||||.|.||||.|.||||.|=",//19
                                "=|..  ..|..  ..|..  ..|..  ..|=",//20
                                "=|..  ..|..  ..|..  ..|..  ..|=",//21
                                "=|..  ..|..  ..|..  ..|..  ..|=",//22
                                "=|..  ..|..  ..|..  ..|..  ..|=",//23
                                "=|..  ..|..  ..|..  ..|..  ..|=",//24
                                "=|.||||.|.||||.|.||||.|.||||.|=",//25
                                "=|...........................|=",//26
                                "=|.|||||||||||.|.|||||||||||||=",//27
                                "=|.||========|.|.|=============",//28
                                "=|.|||||||||||.|.|||||||||||||=",//29
                                "=|.............|.|||||||||||||=",//30 ////START
                                "=|||||||||||||.|.|||||||||||||=",//31
                                "=|||||||||||||.|.|=|||111111||=",//32
                                "=|||||||||||=|.|.|=|||111111||=",//33
                                "=|||||||||||=|.|.|=|||111111||=",//34
                                "=|||||||||||=|.|.|=|||111111||=",//35
                                "=|||||||||||=|.|.|=|||111111||=",//36
                                "=|||||||||||=|.|.|=|||||||||||=",//37
                                "=|||||||||||=|.|.|=|||||||||||=",//38
                                "=|||||||||||=|.|.|=|||||||||||=",//39
                                "=|||||||||||=|.|.|=|||||||||||=",//40
                                "=|||||||||||=|.|.|=|||||||||||=",//41
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|...|=|||||||||||=",
                                "=|||||||||||=|||||=|||||||||||=",
                                "=|||||||||||=======|||||||||||="
                            },
                            {
                                "===============================",
                                "=|||||||||||||||||||||||||||||=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..00..|..0 ..|..0 ..|..0 ..|=",
                                "=|..00..|..0 ..|..0 ..|..0 ..|=",
                                "=|..00..|..0 ..|..0 ..|..0 ..|=",
                                "=|..00..|..0 ..|..0 ..|..0 ..|=",
                                "=|..00..|..0 ..|..0 ..|..0 ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.|||||||||||.|.|||||||||||||=",
                                "=|.||========|.|.|=============",
                                "=|.|||||||||||.|.|||||||||||||=",
                                "=|.............|.|||||||||||||=",
                                "=|||||||||||||.|.|||||||||||||=",
                                "=|||||||||||||.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|...|=|||||||||||=",
                                "=|||||||||||=|||||=|||||||||||=",
                                "=|||||||||||=======|||||||||||="
                            },
                            {
                                "===============================",
                                "=|||||||||||||||||||||||||||||=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..0 ..|..0 ..|..0 ..|..00..|=",
                                "=|..0 ..|..0 ..|..0 ..|..00..|=",
                                "=|..0 ..|..0 ..|..0 ..|..00..|=",
                                "=|..0 ..|..0 ..|..0 ..|..0 ..|=",
                                "=|..0 ..|..0 ..|..0 ..|..0 ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|..0 ..|..  ..|..  ..|..  ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|..  ..|..  ..|..  ..|..  ..|=",
                                "=|.||||.|.||||.|.||||.|.||||.|=",
                                "=|...........................|=",
                                "=|.|||||||||||.|.|||||||||||||=",
                                "=|.||========|.|.|=============",
                                "=|.|||||||||||.|.|||||||||||||=",
                                "=|.............|.|||||||||||||=",
                                "=|||||||||||||.|.|||||||||||||=",
                                "=|||||||||||||.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|.|.|=|||||||||||=",
                                "=|||||||||||=|...|=|||||||||||=",
                                "=|||||||||||=|||||=|||||||||||=",
                                "=|||||||||||=======|||||||||||="
                            }
                        };
  }
 @Override
 public void init(GLAutoDrawable drawable) {
    // Get pipeline
    GL3 gl = drawable.getGL().getGL3();

    // Print OpenGL version
    System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION) + "\n");

    gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    gl.glClearDepth(1.0f);

    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glEnable(GL.GL_CULL_FACE);

    //inicializa os shaders
    shader.init(gl);
    //ativa os shaders
    shader.bind();

    //inicializa a matrix Model and Projection
    modelMatrix.init(gl, shader.getUniformLocation("u_modelMatrix"));
    projectionMatrix.init(gl, shader.getUniformLocation("u_projectionMatrix"));
    viewMatrix.init(gl, shader.getUniformLocation("u_viewMatrix"));
    try {
      //init robot
      Robot.initRobot(gl, shader,modelMatrix,local);
      //init the model caja
      caja.init(gl, shader);
      caja.unitize();
      caja.dump();
      
      //init the model pared
      pared.init(gl, shader);
      pared.unitize();
      pared.dump();
      
      //init the model cuadro
      base.init(gl, shader);
      base.unitize();
      base.dump();
      
      //init the model estante
      estante.init(gl, shader);
      estante.unitize();
      estante.dump();
      
    } catch (IOException ex) {
      Logger.getLogger(Scene03.class.getName()).log(Level.SEVERE, null, ex);
    }

    //init the light
    light.setPosition(new float[]{14.0f, 15.0f, 20.0f, 0.0f});
    light.setAmbientColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f });
    light.setDiffuseColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f });
    light.setSpecularColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f });
    light.init(gl, shader);  
    light.bind();
    
    //Material
    material.setAmbientColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
    material.setDiffuseColor(new float[]{1.0f, 0.0f, 0.0f, 1.0f});
    material.setSpecularColor(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
    material.setSpecularExponent(64.0f);
    material.init(gl, shader);
    material.bind();  
    
    floor.init(gl, shader);
    square.init(gl, shader);
    crearRobots();
  }
 private void crearRobots()
 {
     robot[0]=new Robot(0,16.0f,30.0f);
     robot[1]=new Robot(1,16.0f,32.0f);
    robot[2]=new Robot(2,16.0f,34.0f);
    robot[3]=new Robot(3,16.0f,36.0f);
    robot[4]=new Robot(4,16.0f,38.0f);
    robot[5]=new Robot(5,16.0f,40.0f);
    robot[6]=new Robot(6,16.0f,42.0f);
    robot[7]=new Robot(7,16.0f,44.0f);
    robot[8]=new Robot(8,14.0f,44.0f);
    robot[9]=new Robot(9,14.0f,42.0f);
    robot[10]=new Robot(10,14.0f,40.0f);
    robot[11]=new Robot(11,14.0f,38.0f);
    robot[12]=new Robot(12,14.0f,36.0f);
    robot[13]=new Robot(13,14.0f,34.0f);
    robot[14]=new Robot(14,14.0f,32.0f);
    robot[15]=new Robot(15,14.0f,30.0f);
    System.out.println("letra POS ROBOT: "+(local[0][30].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][32].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][34].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][36].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][38].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][40].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][42].toCharArray())[16]);
    System.out.println("letra POS ROBOT: "+(local[0][44].toCharArray())[16]);
    
    System.out.println("letra POS ROBOT: "+(local[0][44].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][42].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][40].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][38].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][36].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][34].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][32].toCharArray())[14]);
    System.out.println("letra POS ROBOT: "+(local[0][30].toCharArray())[14]);
 }
 public void guardarMedidas(int x,int y)
 {
     this.width=x;
     this.height=y;
 }
  void mirarA (Punto p, Punto pos)
  {   
      viewMatrix.lookAt(p.x, p.y, p.z, pos.x, pos.y, pos.z, 0, 1, 0);
  }
    public void drawCaja (float  x, float y, float z)
    {
        modelMatrix.push();
            modelMatrix.translate(x, y, z);
            modelMatrix.bind();
            caja.draw();
        modelMatrix.pop();
    }   
    public void drawEstante (int x, int y, int z)
    {
        if (y == 0)
        {
            modelMatrix.push();
                modelMatrix.translate(x, y, z);
                modelMatrix.scale(0.7f,1, 0.7f);
                modelMatrix.bind();
                base.draw();
            modelMatrix.pop();
        }
        if(x == 5 || x == 12 || x == 19 || x == 26)// (x == 5 || x == 11 || x == 18 || x == 24)
        {
            modelMatrix.push();
                modelMatrix.translate(x, y, z);
                modelMatrix.rotate(180, 0, 1, 0);
                modelMatrix.bind();
                estante.draw();
            modelMatrix.pop();
        }
        else
        {
            modelMatrix.push();
                modelMatrix.translate(x, y, z);
                modelMatrix.bind();
                estante.draw();
            modelMatrix.pop();;
        }
    }
    public void drawCuadro (int x, int z, boolean qr)
    {
        QUAD_VERTICES=new float[]{x  + dist, 0, z  + dist,
                                  x  + dist, 0, z  - dist,
                                  x  - dist, 0, z  - dist,
                                  x  - dist, 0, z  + dist};
        material.setDiffuseColor(new float[]{0.5f, 0.5f, 0.5f, 1.0f});
        material.bind();
        modelMatrix.push();
        modelMatrix.loadIdentity();
        modelMatrix.scale(2, 0, 2);
        modelMatrix.bind();
        square.bind();
        square.draw(GL3.GL_TRIANGLE_FAN);
        modelMatrix.pop();
    }
    public void drawPared (int x, int y, int z)
    {
        modelMatrix.push();
            modelMatrix.translate(x, y, z);
            modelMatrix.bind();
            pared.draw();
        modelMatrix.pop();
    }
     public void drawFloor (int x)
    {
        modelMatrix.push();
         material.setDiffuseColor(new float[]{0.2f, 0.2f, 0.25f, 1.0f});
         material.setAmbientColor(new float[]{0.2f, 0.2f, 0.25f, 1.0f});
         material.bind();
            modelMatrix.loadIdentity();
            modelMatrix.translate(15, -1, x);
            modelMatrix.rotate(-90, 1, 0, 0);
            modelMatrix.scale(LARGO - 15, 2, ANCHO);
            
            modelMatrix.bind();
            floor.bind();
            floor.draw();
        modelMatrix.pop();
    }
  public void cargarLocal ()
  {
        //System.out.println(local[1][2].charAt(5));
        int i, j, k;
        for (k = 0 ; k < ALTURA ; k++)
            for (i = 0 ; i < LARGO ; i++)
                for (j = 0 ; j < ANCHO ; j++)
                    if (local[k][i].charAt(j) == '0' || local[k][i].charAt(j) == '1') {
                        drawCaja(j, k, i);
                        if (i < 30)
                        drawEstante(j, k, i);
                    }
                    else if (local[k][i].charAt(j)== ' ' || local[k][i].charAt(j) == '2')
                        drawEstante(j, k, i);
                    else if (local[k][i].charAt(j) == '=')
                            {
                                    drawPared(j, k, i);
                            }
        /*
        //Cuadro
        for (i = 0 ; i < LARGO ; i++)
        { 
            for (j = 0 ; j < ANCHO ; j++)
            {
                  if (local[0][i].charAt(j) == '.')
                       drawCuadro(j, i, true);
                  else
                       drawCuadro(j, i, false);
  
            }       
        }*/
	for ( i = 0 ; i < ANCHO+ANCHO - 15; i++)
            drawFloor(i);
   }

  @Override
  public void display(GLAutoDrawable drawable) {
    // Recupera o pipeline
    GL3 gl = drawable.getGL().getGL3();    
    // Limpa o frame buffer com a cor definida
    gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

    //Proyeccion Perspectiva
    projectionMatrix.loadIdentity();
    projectionMatrix.perspective(60.0f+delta,width/height, 2, 600);
    projectionMatrix.bind();
   
    viewMatrix.loadIdentity();
    mirarA(vista,dir);
    viewMatrix.rotate(beta, 0.0f, 1.0f, 0.0f);
    viewMatrix.rotate(alpha, 1.0f, 0.0f, 0.0f);
    viewMatrix.bind();
    
    //Dibujo de escena Almacen
    modelMatrix.loadIdentity();
    cargarLocal();
    //for (int i = 0 ; i < nRobot ; i++)
    //robot[0].actuar(posFin, band, girar);
    Robot.controlarRobots(goRobot,estadoRobot,estanteObjetivo);
    if(goRobot=true)
        goRobot=false;
    // Força execução das operações declaradas
    gl.glFlush();
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    caja.dispose();
    pared.dispose();
    base.dispose();
    estante.dispose();
    square.dispose();
    Robot.dispose();
  }
  public KeyListener escucha() {
     KeyListener listener= new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
	    }        
            @Override
            public void keyPressed(KeyEvent e) {
              switch (e.getKeyChar()) {
                case '-'://faz zoom-in
                    if(delta<50)
                      delta = delta + 4.0f;
                    break;
                case '+'://faz zoom-out
                    if(delta>-60)
                      delta = delta - 4.0f;
                    break;
                case 'w'://
                    vista.z-=1;
                    dir.z-=1;
                    break;
                case 's'://
                    vista.z+=1;
                    dir.z+=1;
                    break;
                case 'a'://
                    vista.x-=1;
                    dir.x-=1;
                    break;
                case 'd'://
                    vista.x+=1;
                    dir.x+=1;
                    break;
                case 'n'://
                    band=true;
                    break;
                case 'm'://
                    band=false;                
                    break;
                case 'z'://
                    vista.y+=1;
                    dir.y+=1;
                    break;
                case 'x'://
                    vista.y-=1;
                    dir.y-=1;
                    break;
                case 'c': // CAMBIAR ESTADO DE ACTIVACION DE ROBOT
                    if(estadoRobot==DEJAR_CAJA)
                        estadoRobot=TRAER_CAJA;
                    else
                        estadoRobot=DEJAR_CAJA;
                    break;     
           /// Estante objetico del 1 al 6 = de 'r' hasta 'o' segun la ubicacion en el teclado horizontalmente         
                case 'r': 
                    goRobot=true;
                    estanteObjetivo=1;
                    break;
                case 't': 
                    goRobot=true;
                    estanteObjetivo=2;
                    break; 
                case 'y': 
                    goRobot=true;
                    estanteObjetivo=3;
                    break;
                case 'u': 
                    goRobot=true;
                    estanteObjetivo=4;
                    break;  
                case 'i': 
                    goRobot=true;
                    estanteObjetivo=5;
                    break;
                case 'o': 
                    goRobot=true;
                    estanteObjetivo=6;
                    break;  
            /// Estante objetico del 7 al 12 = de 'f' hasta 'l' segun la ubicacion en el teclado horizontalmente         
                case 'f': 
                    goRobot=true;
                    estanteObjetivo=7;
                    break;
                case 'g': 
                    goRobot=true;
                    estanteObjetivo=8;
                    break; 
                case 'h': 
                    goRobot=true;
                    estanteObjetivo=9;
                    break;
                case 'j': 
                    goRobot=true;
                    estanteObjetivo=10;
                    break;  
                case 'k': 
                    goRobot=true;
                    estanteObjetivo=11;
                    break;
                case 'l': 
                    goRobot=true;
                    estanteObjetivo=12;
                    break;       
                //FIN ESTANTE OBJETIVO
                case '1'://Go robot
                    goRobot=true;
                    break;                   
                case '8'://
                    alpha+=2;
                    break;
                case '2'://
                    alpha-=2;
                    break;
                case '4'://
                    beta+=2;
                  break;
                case '6'://
                    beta-=2;
                    break;
                case '5'://
                    vista=new Punto(14,12,48);
                    dir = new Punto(14,2,33);
                    alpha=0;
                    beta=0;
                    delta=60.0f;
                    break;
                default:
                       break;
              }
            }
	    @Override
	    public void keyReleased(KeyEvent e) {
	    }            
     };
     return listener;
  }
}
