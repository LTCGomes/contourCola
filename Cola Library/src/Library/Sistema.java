/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author tjsantos
 */
class Sistema {
    //bios
    /* 
        id da bios CHECK
        id da placa de rede
        uuid - A universally unique identifier (UUID) is a 128-bit number used to identify information in computer systems. 
        
    
        JHARDWARE pesquisar esta biblioteca
    */
    private String numeroSerie;
    private String enderecoMac;
    private String uuid;
    

    public Sistema() {
        numeroSerie = setNumeroSerie();
        enderecoMac = setEnderecoMac();
        uuid = setUuid();
    }
    
    private String setNumeroSerie() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "wmic", "bios", "get", "SerialNumber" });

            process.getOutputStream().close();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        Scanner sc = new Scanner(process.getInputStream());
        String property = sc.next();
        String numeroSerie = sc.next();
        return numeroSerie;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public String getEnderecoMac() {
        return enderecoMac;
    }

    public String getUuid() {
        return uuid;
    }

    public String setEnderecoMac() {
        
        InetAddress ip;
    try {
  
        ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
  
        byte[] mac = network.getHardwareAddress();
  
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
  
    } catch (UnknownHostException e) {
  
        e.printStackTrace();
  
    } catch (SocketException e){
  
        e.printStackTrace();
  
    }
        return "";
    }

    public String setUuid() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "wmic", "csproduct", "get", "UUID" });

            process.getOutputStream().close();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        Scanner sc = new Scanner(process.getInputStream());
        String property = sc.next();
        String uuid = sc.next();
        return uuid;
    }
    
}
