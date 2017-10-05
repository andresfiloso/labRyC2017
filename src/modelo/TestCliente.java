package modelo;

import javax.swing.JFrame;

public class TestCliente {

	public static void main(String[] args) {
		
			Coordenada xy = new Coordenada(5,5);
			
			VentanaCliente ventanaCliente = new VentanaCliente(xy, "localhost", "9010");
			ventanaCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	

	}

}
