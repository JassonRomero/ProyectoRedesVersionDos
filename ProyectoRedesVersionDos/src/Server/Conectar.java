/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Romero
 */
public class Conectar {

    private Connection conectar;

    public Connection conexion() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            this.conectar = DriverManager.getConnection("jdbc:mysql://163.178.107.10:3306/if_5000_proyecto_b76828_b70070?" + "user=laboratorios&password=UCRSA.118");
        } catch (Exception e) {

        }
        return this.conectar;
    }
}
