package modelo;

import java.awt.Color;
import java.awt.Font;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Servidor extends JFrame implements Runnable {

	JTextArea txtMensajes;

	String authUser = "admin";
	String authPass = "admin";

	int puerto = 9009;
	
	int matrizSize = 10;

	public Servidor() {
		txtMensajes = new JTextArea();
		txtMensajes.setBounds(10, 10, 400, 20);
		txtMensajes.setEditable(false);
		add(txtMensajes);

		setLayout(null);
		setSize(500, 500);
		setLocationRelativeTo(null);
		setVisible(true);

		Thread hilo = new Thread(this);
		hilo.start(); // llama al metodo run que creamos

	}

	public static void main(String[] args) {
		new Servidor();

	}

	@Override
	public void run() {
		try {
			ServerSocket servidor = new ServerSocket(puerto);
			Socket cliente; // socket para guardar las llamadas

			System.out.println("Servidor iniciado en puerto: " + puerto);
			boolean proceso = false;
			txtMensajes.append("Servidor iniciado en puerto: " + puerto);
			cliente = servidor.accept(); // esperando una llamada

			/*
			 * DataInputStream flujo = new DataInputStream(cliente.getInputStream()); 
			 * String msg = flujo.readUTF();
			 * txtMensajes.append("\n" + cliente.getInetAddress() + ": " + msg);
			 * 
			 */

			while (proceso == false) {
				ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
				Login login = (Login) in.readObject();

				System.out.println("Usuario a validar: " + login.getUser());
				System.out.println("Password a validar: " + login.getPass());

				if (autorizar(login.getUser(), login.getPass())) {
					agrandarTxtMensajes();
					txtMensajes.append("\nBienvenido: " + login.getUser());
					proceso = true;
					DataOutputStream respuesta = new DataOutputStream(cliente.getOutputStream());
					respuesta.writeInt(matrizSize);
					agrandarTxtMensajes();
					txtMensajes.append("\nCerrando login y creando mapa de juego...");
					agrandarTxtMensajes();
					txtMensajes.append("\nTamaño del laberinto: " + matrizSize);
					
					dibujarLaberinto(matrizSize);
					
				} else {
					servidor.close();
					txtMensajes.append("Acesso denegado.. Servidor cerrado");
					proceso = true;
				}
			}

			cliente.close();
			servidor.close();
			System.out.println("Servidor cerrado");

			/*
			 * if (msg.equalsIgnoreCase("FIN")) { // si el mensaje recibido es fin, cerrar
			 * el servidor y salir del while infinito servidor.close(); }
			 */

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void agrandarTxtMensajes() {
		txtMensajes.setBounds(10, 10, txtMensajes.getWidth(), txtMensajes.getHeight()+10);
	}

	public boolean autorizar(String user, String pass) {
		boolean resultado = false;

		if (user.equals(authUser) && pass.equals(pass)) {
			resultado = true;
		}else {
			System.out.println("Credenciales incorrectas");
		}

		return resultado;
	}
	
	public void dibujarLaberinto(int size) {
		
		String fila1[] = {"P", "P", "P", "P", "P", "P", "P", "P", "P", "P"};
		String fila2[] = {"E", "C", "C", "C", "C", "P", "P", "P", "O", "P"};
		String fila3[] = {"P", "P", "P", "P", "C", "P", "P", "P", "C", "P"};
		String fila4[] = {"P", "P", "P", "P", "C", "P", "P", "C", "C", "P"};
		String fila5[] = {"O", "C", "C", "C", "C", "C", "C", "C", "C", "P"};
		String fila6[] = {"P", "P", "P", "P", "P", "P", "C", "P", "G", "P"};
		String fila7[] = {"P", "C", "C", "C", "P", "P", "G", "P", "C", "P"};
		String fila8[] = {"P", "C", "P", "C", "P", "P", "C", "P", "C", "P"};
		String fila9[] = {"P", "C", "P", "C", "C", "C", "C", "P", "P", "P"};
		String fila10[] = {"P", "S", "P", "P", "P", "P", "P", "P", "P", "P"};
		 
		String [][] filas = {fila1, fila2, fila3, fila4, fila5, fila6, fila7, fila8, fila9, fila10};
		
		
		int cSize = 30;

		JLabel sizeLabel;

		sizeLabel = new JLabel("Tamaño del laberinto: " + size);
		sizeLabel.setBounds(10, 10, 200, 20);
		add(sizeLabel);

		JTextArea txtmensajes;
		JTextArea[][] array = new JTextArea[size][size];
		JTextArea txtmensajes2;
		JTextArea[][] array2 = new JTextArea[size][size];

		int a = 70, b = txtMensajes.getHeight()+20;
		int i = 0, j = 0;
		while (i < size) {
			txtmensajes = new JTextArea();
			txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));
			txtmensajes.setBounds(a, b, cSize, cSize);
			txtmensajes.setEditable(false);
			array[i][j] = txtmensajes;
			txtmensajes.setText(filas[j][i]);
			add(array[i][j]);
			
			while (j < size - 1) {
				b += cSize+5;
				j++;
				txtmensajes = new JTextArea();
				txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));
				
				txtmensajes.setBounds(a, b, cSize, cSize);
				txtmensajes.setEditable(false);
				array[i][j] = txtmensajes;
				txtmensajes.setText(filas[j][i]);
				add(array[i][j]);
			}
			b = txtMensajes.getHeight()+20;
			j = 0;
			a += cSize+5;
			i++;
		}

		for (int k = 0; k < size; k++) {
			for (int l = 0; l < size; l++) {
				array[k][l].setBackground(Color.LIGHT_GRAY);
				
			}
		}

		setLayout(null); // para que los controles no esten uno encima del otro
		setSize(500, 500); // tamaño del layout
		setLocationRelativeTo(null);
		setVisible(true);

	}

}
