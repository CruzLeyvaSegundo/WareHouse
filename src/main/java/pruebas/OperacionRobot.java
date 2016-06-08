/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pruebas;

/**
 *
 * @author JUNIOR
 */
public class OperacionRobot {
    Robot robot;
    int estadoRobot;
    int estanteObjetivo; /*DEJAR_CAJA=0; TRAER_CAJA=1; RETORNAR=2;  A_LA_COLA=3;*/

    public OperacionRobot(Robot robot, int estadoRobot, int estanteObjetivo) {
        this.robot = robot;
        this.estadoRobot = estadoRobot;
        this.estanteObjetivo = estanteObjetivo;
    }
    
}
