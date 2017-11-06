package modelo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pruebas {

	public static void main(String[] args) throws UnknownHostException {
	
		Helper h = new Helper();
		
		List<String> settings = new ArrayList<String>();
		
		String command = "log";
	
		settings.add("admin");
		settings.add("cisco");
		settings.add("eugenia");
		settings.add("Andres");
		settings.add("fede");
		settings.add("fabian");
		
		
		Paquete p = new Paquete(command, settings);
		
		System.out.println("Paquete crudo: " + p.getCommand() + p.getArgs());
		
		p = h.encriptarPaquete(p);
		
		System.out.println("Paquete Encriptado: " + p.getCommand() + p.getArgs());
		
		p = h.desencriptarPaquete(p);
		
		System.out.println("Paquete Desencriptado: " + p.getCommand() + p.getArgs());
		
		
		
		
		
		
		
		
		/*
		
		System.out.println("CONSTRUIR MENSAJE: ");
		String message = buildMessage(command, paquete);
		System.out.println(message);
		System.out.println("LECTURA DE PAQUETE: ");
		System.out.println(readMessage(message));
		
		
		*/
			

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
