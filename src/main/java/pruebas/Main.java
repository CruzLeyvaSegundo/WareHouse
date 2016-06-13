/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebas;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Hikari Kyuubi
 */
public class Main 
{
    
    public static void main(String args[]) 
    {
        // Get GL3 profile (to work with OpenGL 4.0)
        GLProfile profile = GLProfile.get(GLProfile.GL3);

        // Configurations
        GLCapabilities glcaps = new GLCapabilities(profile);
        glcaps.setDoubleBuffered(true);
        glcaps.setHardwareAccelerated(true);

        // Create canvas
        GLCanvas glCanvas = new GLCanvas(glcaps);

        // Add listener to panel
        Scene03 escena = new Scene03();
        glCanvas.addGLEventListener(escena);
                   
        Frame frame = new Frame("WareHose");
        panelConfiguracion panel = new panelConfiguracion(escena,frame);      
        frame.setSize(1315, 680);
        panel.setSize(135,frame.getHeight());
        frame.add(panel);
        frame.add(glCanvas);
        escena.guardarMedidas(frame.getWidth(),frame.getHeight());
        
        final AnimatorBase animator = new FPSAnimator(glCanvas, 60);
        frame.setFocusable(true);
        frame.addKeyListener(escena.escucha());
        frame.setResizable(false);
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
        //panel.setVisible(true);
        animator.start();
    }
}