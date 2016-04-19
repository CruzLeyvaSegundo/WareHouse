
package br.usp.icmc.vicg.gl.app;

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
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;


public class Example02 implements GLEventListener {

    private final Shader shader; // Gerenciador dos shaders
    private final Matrix4 modelMatrix;
    private final Matrix4 projectionMatrix;
    private final Matrix4 viewMatrix;
    private final SimpleModel square;
    private Punto vista=new Punto();
    private Punto dir=new Punto();
    private  float[] vertex=new float[12];
    private   float x=0;
    private   float z=0;
    private   float dist=0.5f;
    private  float delta;
    private float alpha;
    private float beta;
    private  int height;
    private  int width;
    public Example02() {
        // Carrega os shaders
        vertex=new float[]{x  + dist, 0, z  + dist,
                            x  + dist, 0, z  - dist,
                            x  - dist, 0, z  - dist,
                            x  - dist, 0, z  + dist};
           /* vertex= new float[]{
                        -0.5f, -0.5f, 0.0f,
                        0.5f, -0.5f, 0.0f,
                        0.5f, 0.5f, 0.0f,
                        -0.5f, 0.5f, 0.0f};*/
            delta=0;
            alpha=0;
            beta=0;
            vista=new Punto(0,10,5);
            dir = new Punto(1,1,1);
        projectionMatrix = new Matrix4();
        viewMatrix = new Matrix4();
        shader = ShaderFactory.getInstance(ShaderFactory.ShaderType.COMPLETE_SHADER);
        modelMatrix = new Matrix4();
        square = new Square(vertex);

    }
        public void guardarMedidas(int x,int y)
        {
            this.width=x;
            this.height=y;
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

        //inicializa a matrix Model
        modelMatrix.init(gl, shader.getUniformLocation("u_modelMatrix"));
        projectionMatrix.init(gl, shader.getUniformLocation("u_projectionMatrix"));
        viewMatrix.init(gl, shader.getUniformLocation("u_viewMatrix"));
        //cria o objeto a ser desenhado
        square.init(gl, shader);
    }

      void mirarA (Punto p,Punto pos)
      {   
            viewMatrix.lookAt(p.x, p.y, p.z, pos.x, pos.y, pos.z, 0, 1, 0);
      } 
    @Override
    public void display(GLAutoDrawable drawable) {
        // Recupera o pipeline
        GL3 gl = drawable.getGL().getGL3();

        // Limpa o frame buffer com a cor definida
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
        projectionMatrix.loadIdentity();
        projectionMatrix.perspective(60.0f+delta,width/height,2, 600);
        projectionMatrix.bind();

        viewMatrix.loadIdentity();
        mirarA(vista,dir);
        viewMatrix.rotate(beta, 0.0f, 1.0f, 0.0f);
        viewMatrix.rotate(alpha, 1.0f, 0.0f, 0.0f);
        viewMatrix.bind();   
        
        modelMatrix.loadIdentity();
        /*modelMatrix.translate(-0.75f, 0, 0);*/
        //modelMatrix.scale(0.25f, 0.25f, 1);
        modelMatrix.bind();
        
        square.bind();
        square.draw(GL3.GL_TRIANGLE_FAN);

        // Força execução das operações declaradas
        gl.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Apaga o buffer
        square.dispose();
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
                case 'z'://
                  vista.y+=1;
                  dir.y+=1;
                  break;
                case 'x'://
                  vista.y-=1;
                  dir.y-=1;
                  break;
                case 'i':
                  //posFin.z-=1;
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
                  /*  vista=new Punto(14,12,48);
                    dir = new Punto(14,2,33);
                    alpha=0;
                    beta=0;
                    delta=60.0f;*/
                    break;
                default:
                       System.out.println("naniiiiii!!!!");
                       break;
              }
            }
	    @Override
	    public void keyReleased(KeyEvent e) {
		//System.out.println("Alfa: "+alpha+" Beta: "+beta+" Omega: "+omega);
	    }            
     };
     return listener;
  }
    public static void main(String[] args) {
        // Get GL3 profile (to work with OpenGL 4.0)
        GLProfile profile = GLProfile.get(GLProfile.GL3);

        // Configurations
        GLCapabilities glcaps = new GLCapabilities(profile);
        glcaps.setDoubleBuffered(true);
        glcaps.setHardwareAccelerated(true);

        // Create canvas
        GLCanvas glCanvas = new GLCanvas(glcaps);

         Example02 listener = new Example02();
        // Add listener to panel
        glCanvas.addGLEventListener(listener);

        Frame frame = new Frame("Example 02");
        frame.setSize(1180, 680);
        frame.add(glCanvas);
        listener.guardarMedidas(frame.getWidth(),frame.getHeight());
        final AnimatorBase animator = new FPSAnimator(glCanvas, 60);
        frame.setFocusable(true);
        frame.addKeyListener(listener.escucha());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }

                }).start();
            }

        });
        frame.setVisible(true);
        animator.start();
    }

}
