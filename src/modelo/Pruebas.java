package modelo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pruebas {

	public static void main(String[] args) {
	
		List<String> paquete = new ArrayList<String>();
		
		String command = "log";
		
		paquete.add(command);
		paquete.add("admin");
		paquete.add("cisco");
		
		System.out.println("CONSTRUIR MENSAJE: ");
		
		String message = buildMessage(command, paquete);
		
		System.out.println(message);
		
		System.out.println("LECTURA DE PAQUETE: ");
		
		System.out.println(readMessage(message));
			

	}
	
	public static String buildMessage(String command, List<String> settings){
		String message = "";
		for(String m : settings) {
			message = message.concat(m + " ");
		}
		return message;
	}
	
	
	public static List<String> readMessage(String message){
		List<String> settings = new ArrayList<String>();
		
		String[] data = message.split(" ");
		for(String d: data) {
			settings.add(d);
		}
		
		return settings;
	}

}
