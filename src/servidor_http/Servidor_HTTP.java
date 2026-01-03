/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor_http;

/**
 *
 * @author Usuario
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor_HTTP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            final ServerSocket server=new ServerSocket(8080);
        } catch (IOException ex) {
            System.out.println("Error al escuchar el puerto 8080");
            Logger.getLogger(Servidor_HTTP.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Escuchando el puerto 8080");
        while(true){
            
        }
    }
    
}
