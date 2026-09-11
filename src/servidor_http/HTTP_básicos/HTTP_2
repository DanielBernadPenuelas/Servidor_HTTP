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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor_http_2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        final ServerSocket server= new ServerSocket(8080);
        System.out.println("Escuchando por el puerto 8080");
        while(true){
            Socket clientSocket=server.accept();
            InputStreamReader isr=new InputStreamReader(clientSocket.getInputStream());
            BufferedReader reader=new BufferedReader(isr);
            String line= reader.readLine();
            while(!line.isEmpty()){
                System.out.println(line);
                line=reader.readLine();
            }
            
        }
    }
    
}
