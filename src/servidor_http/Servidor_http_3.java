/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor_http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 *
 * @author Usuario
 */
public class Servidor_http_3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        ServerSocket server=new ServerSocket(8080);
        System.out.println("Escuchando por el puerto 8080");
        System.out.println(System.getProperty("user.dir"));
        while(true){
            try(Socket socket=server.accept()){
                Date today=new Date();
                String httpResponse="HTTP/1.1 200 OK \r\n\r\n"+today;
                socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            }          
        }
    }
    
}
