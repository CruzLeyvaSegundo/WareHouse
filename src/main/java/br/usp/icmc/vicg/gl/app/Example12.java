package br.usp.icmc.vicg.gl.app;

import br.usp.icmc.vicg.gl.core.Light;
import br.usp.icmc.vicg.gl.core.Material;
import br.usp.icmc.vicg.gl.jwavefront.JWavefrontObject;
import br.usp.icmc.vicg.gl.matrix.Matrix4;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import br.usp.icmc.vicg.gl.util.Shader;
import br.usp.icmc.vicg.gl.util.ShaderFactory;
import br.usp.icmc.vicg.gl.util.ShaderFactory.ShaderType;

import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import static com.sun.java.accessibility.util.AWTEventMonitor.addKeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class Example12 extends KeyAdapter implements GLEventListener {

  private final Shader shader; // Gerenciador dos shaders
  private final Matrix4 modelMatrix;
  private final Matrix4 projectionMatrix;
  private final Matrix4 viewMatrix;
  private Material material;
  private final JWavefrontObject model;
  private final Light light;
  private float alpha;
  private float beta;
  private float delta;

  public Example12() {
    // Carrega os shaders
    shader = ShaderFactory.getInstance(ShaderType.COMPLETE_SHADER);
    modelMatrix = new Matrix4();
    projectionMatrix = new Matrix4();
    viewMatrix = new Matrix4();

    model = new JWavefrontObject(new File("./warehouse/caja.obj"));
    light = new Light();
    material=new Material();
    alpha = 0;
    beta = 0;
    delta = 5;
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
      //init the model
      model.init(gl, shader);
      model.unitize();
      model.dump();
    } catch (IOException ex) {
      Logger.getLogger(Example12.class.getName()).log(Level.SEVERE, null, ex);
    }

    //init the light
    light.setPosition(new float[]{10, 10, 50, 1.0f});
    light.setAmbientColor(new float[]{0.1f, 0.1f, 0.1f, 1.0f});
    light.setDiffuseColor(new float[]{0.75f, 0.75f, 0.75f, 1.0f});
    light.setSpecularColor(new float[]{0.7f, 0.7f, 0.7f, 1.0f});
    light.init(gl, shader);
    material.init(gl, shader);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    // Recupera o pipeline
    GL3 gl = drawable.getGL().getGL3();

    // Limpa o frame buffer com a cor definida
    gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

    projectionMatrix.loadIdentity();
    projectionMatrix.ortho(
            -delta, delta, 
            -delta, delta, 
            -2 * delta, 2 * delta);
    projectionMatrix.bind();
    material.setDiffuseColor(new float[]{0.0f, 0.0f, 1.0f, 0.25f});
    material.setSpecularColor(new float[]{0.9f, 0.9f, 0.9f, 0.25f});
    material.setSpecularExponent(32);
    material.bind();
    modelMatrix.loadIdentity();
    modelMatrix.rotate(beta, 0, 1.0f, 0);
    modelMatrix.rotate(alpha, 1.0f, 0, 0);
    modelMatrix.bind();
    model.draw();

    viewMatrix.loadIdentity();
    viewMatrix.lookAt(
            1, 1, 1, 
            0, 0, 0, 
            0, 1, 0);
    viewMatrix.bind();

    light.bind();



    // Força execução das operações declaradas
    gl.glFlush();
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    model.dispose();
  }
  public KeyListener escucha() {
     KeyListener listener= new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {
	    }        
            @Override
            public void keyPressed(KeyEvent e) {
              switch (e.getKeyChar()) {
                case '+'://faz zoom-in
                  delta = delta * 0.809f;
                  break;
                case '-'://faz zoom-out
                  delta = delta * 1.1f;
                  break;
                case 'w'://gira sobre o eixo-x
                  alpha = alpha - 5;
                  break;
                case 's'://gira sobre o eixo-x
                  alpha = alpha + 5;
                  break;
                case 'a'://gira sobre o eixo-y
                  beta = beta - 5;
                  break;
                case 'd'://gira sobre o eixo-y
                  beta = beta + 5;
                  break;
              }
            }
	    @Override
	    public void keyReleased(KeyEvent e) {
		System.out.println("keyReleased="+KeyEvent.getKeyText(e.getKeyCode()));
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

    // Add listener to panel
    Example12 listener = new Example12();
    glCanvas.addGLEventListener(listener);

    Frame frame = new Frame("Example 09");
    frame.setSize(600, 600);
    frame.add(glCanvas);
    frame.setFocusable(true);
    final AnimatorBase animator = new FPSAnimator(glCanvas, 60);
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
