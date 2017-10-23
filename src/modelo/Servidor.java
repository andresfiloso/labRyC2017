package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class Servidor extends JFrame implements Runnable {

	JTextArea txtMensajes;

	String authUser = "admin";
	String authPass = "admin";

	int puerto;

	int matrizSize = 20;

	JTextArea[][] laberinto = new JTextArea[matrizSize][matrizSize];

	public Servidor(int p) {

		puerto = p;

		txtMensajes = new JTextArea();
		txtMensajes.setBounds(450, 20, 300, 1000);
		txtMensajes.setEditable(false);
		// add(txtMensajes);

		setLayout(null);
		setTitle("Servidor");
		setSize(800, 800);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

		Thread hilo = new Thread(this);
		hilo.start(); // llama al metodo run que creamos

	}

	public static void main(String[] args) {
		new Servidor(9020);

	}

	@Override
	public void run() {
		try {
			ServerSocket servidorJuego = new ServerSocket(puerto);
			Socket cliente; // socket para guardar las llamadas
			System.out.println("Servidor iniciado en puerto: " + puerto);
			consola("Servidor iniciado en puerto: " + puerto);

			boolean juego = false;

			
			while (juego == false) {
			cliente = servidorJuego.accept(); // esperando una llamada

			ObjectInputStream autorizacion = new ObjectInputStream(cliente.getInputStream());
			Login login = (Login) autorizacion.readObject();

			System.out.println("Usuario a validar: " + login.getUser());
			System.out.println("Password a validar: " + login.getPass());

			if (autorizar(login.getUser(), login.getPass())) {
				consola("Bienvenido: " + login.getUser());
				consola("Cerrando login y creando mapa de juego...");
				consola("Tamaño del laberinto: " + matrizSize);

				laberinto = dibujarLaberinto(matrizSize);
				Coordenada entradaXY = coordenadasEntrada(laberinto);

				ObjectOutputStream devolucionCoordenada = new ObjectOutputStream(cliente.getOutputStream());
				devolucionCoordenada.writeObject(entradaXY);
				consola("Enviando coordenadas de entrada");
				juego = true; // arranca el juego

				/*
				 * clienteLogin.close(); log("\nSe cierra el servidor de Logueo\n");
				 * servidorLogin.close(); System.out.println("Servidor de Logueo cerrado");
				 */

			} else {
				Coordenada denegado = new Coordenada (100,100);
				ObjectOutputStream devolucionCoordenada = new ObjectOutputStream(cliente.getOutputStream());
				devolucionCoordenada.writeObject(denegado);
				
				consola("Acesso denegado.. Servidor cerrado");
				//servidorJuego.close();
			}

			}
			
			while (juego == true) {
	
				try {	
					Coordenada aux;

					// socket para guardar las llamadas

					consola("Esperando peticion de casillero...");
					// System.out.println("Esperando peticion de casillero...");
					cliente = servidorJuego.accept(); // esperando una llamada
					// System.out.println("Peticion recibida");
					consola("Petición recibida");

					ObjectInputStream solicitudMovimiento = new ObjectInputStream(cliente.getInputStream());
					aux = (Coordenada) solicitudMovimiento.readObject();

					aux = desencriptarCoordenada(aux);
					// consola("Se recibio coordenada: " + aux.getX() + "; " + aux.getY());
					// System.out.println("Se recibio coordenada: " + aux.getX() + " ;" +
					// aux.getY());

					if (aux.getX() == -1 && aux.getY() == -1) {
						consola("Cerrando Servidor de juego por coordenadas -1;-1");
						juego = false;
						servidorJuego.close();
						setVisible(false);
						System.out.println("Servidor de juego cerrado por coordenadas -1;-1");
						break;
					}

					String valor = devolverValor(aux, laberinto);
					consola("Se devuelve valor: " + valor);
					// System.out.println("Se devuelve letra: " + valor);

					DataOutputStream devolver = new DataOutputStream(cliente.getOutputStream());

					int encript = encriptarValorLetra(valor);

					// System.out.println("El valor de la letra es: " + encript);

					devolver.writeInt(encript);
					// System.out.println("Se devuelve letra: " + valor);
				

			} catch (Exception e) {
				// JOptionPane.showMessageDialog(null, e.getMessage());
				System.out.println(e.getMessage());
				// e.printStackTrace();
			}
				
			} // while juego == true

		} catch (IOException e) {
			// JOptionPane.showMessageDialog(null, e.getMessage());
			System.out.println(e.getMessage());
			// e.printStackTrace();

		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			// e.printStackTrace();

		}

	}

	public void consola(String mensaje) {
		Calendar calendario = new GregorianCalendar();
		txtMensajes.append(calendario.get(Calendar.HOUR) + ":" + calendario.get(Calendar.MINUTE) + ":"
				+ calendario.get(Calendar.SECOND));
		txtMensajes.append(": " + mensaje + "\n");
	}

	public boolean autorizar(String user, String pass) {
		boolean resultado = false;

		if (user.equals(authUser) && pass.equals(authPass)) {
			resultado = true;
		} else {
			System.out.println("Credenciales incorrectas");
		}

		return resultado;
	}

	public String devolverValor(Coordenada c, JTextArea[][] array) {
		// boolean encontrado = false;
		String valor = "";

		valor = array[c.getX()][c.getY()].getText();

		return valor;
	}

	public Coordenada desencriptarCoordenada(Coordenada c) {
		Coordenada decript = new Coordenada();

		decript.setX(c.getX() / VentanaCliente.key);
		decript.setY(c.getY() / VentanaCliente.key);

		return decript;
	}

	public int encriptarValorLetra(String letra) {

		int encript = 0;

		switch (letra) {
		case "P":
			encript = (1 * VentanaCliente.key);
			break;
		case "C":
			encript = (2 * VentanaCliente.key);
			break;
		case "O":
			encript = (3 * VentanaCliente.key);
			break;
		case "G":
			encript = (4 * VentanaCliente.key);
			break;
		case "E":
			encript = (5 * VentanaCliente.key);
			break;
		case "S":
			encript = (6 * VentanaCliente.key);
			break;
		case "K":
			encript = (7 * VentanaCliente.key);
			break;
		}

		return encript;
	}

	public Coordenada coordenadasEntrada(JTextArea[][] array) {

		int x = 0;
		int y = 0;

		for (int k = 0; k < matrizSize; k++) {
			for (int l = 0; l < matrizSize; l++) {
				if (array[k][l].getText().equalsIgnoreCase("E")) {
					x = k;
					y = l;
				}
			}
		}
		return new Coordenada(x, y);
	}

	public JTextArea[][] dibujarLaberinto(int size) {

		String fila1[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "S", "P" };
		String fila2[] = { "E", "C", "C", "C", "C", "P", "P", "P", "O", "P", "C", "G", "C", "C", "P", "O", "C", "C", "C", "P" };
		String fila3[] = { "P", "P", "P", "P", "C", "P", "P", "P", "C", "P", "C", "P", "P", "C", "P", "P", "P", "C", "O", "P" };
		String fila4[] = { "P", "P", "P", "P", "C", "P", "P", "P", "C", "P", "C", "P", "P", "G", "C", "O", "C", "G", "C", "P" };
		String fila5[] = { "P", "O", "C", "C", "C", "C", "C", "C", "C", "P", "C", "P", "P", "C", "P", "P", "P", "C", "P", "P" };
		String fila6[] = { "P", "P", "P", "P", "P", "P", "C", "P", "G", "P", "C", "P", "P", "C", "P", "P", "P", "C", "P", "P" };
		String fila7[] = { "P", "C", "C", "C", "P", "P", "G", "P", "C", "P", "O", "C", "C", "G", "C", "O", "C", "G", "C", "P" };
		String fila8[] = { "P", "C", "P", "C", "P", "P", "C", "P", "C", "P", "C", "P", "P", "P", "P", "P", "P", "P", "C", "P" };
		String fila9[] = { "P", "O", "P", "C", "C", "C", "C", "P", "O", "P", "C", "P", "P", "P", "P", "P", "P", "P", "C", "P" };
		String fila10[] = { "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "C", "C", "C", "O", "C", "C", "C", "P" };
		String fila11[] = { "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "C", "P", "P", "p", "P", "P", "C", "P" };
		String fila12[] = { "P", "C", "C", "C", "C", "O", "C", "C", "C", "C", "G", "P", "O", "C", "C", "G", "C", "O", "O", "P" };
		String fila13[] = { "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" };
		String fila14[] = { "P", "O", "O", "P", "P", "P", "P", "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" };
		String fila15[] = { "P", "O", "O", "C", "C", "G", "C", "C", "C", "C", "C", "C", "O", "C", "C", "C", "C", "C", "P", "P" };
		String fila16[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "P" };
		String fila17[] = { "P", "O", "K", "O", "G", "C", "C", "C", "C", "C", "C", "C", "C", "O", "G", "P", "P", "C", "C", "P" };
		String fila18[] = { "P", "P", "P", "P", "P", "P", "P", "P", "G", "P", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P" };
		String fila19[] = { "P", "O", "O", "G", "C", "C", "C", "C", "C", "C", "C", "C", "C", "O", "C", "C", "C", "C", "O", "P" };
		String fila20[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P" };

		String[][] filas = { fila1, fila2, fila3, fila4, fila5, fila6, fila7, fila8, fila9, fila10, fila11, fila12,
				fila13, fila14, fila15, fila16, fila17, fila18, fila19, fila20 };

		int cSize = 30;

		JLabel sizeLabel;

		sizeLabel = new JLabel("Tamaño del laberinto: " + size);
		sizeLabel.setBounds(10, 10, 200, 20);
		add(sizeLabel);

		JTextArea txtmensajes;
		JTextArea[][] array = new JTextArea[size][size];

		int a = 70, b = 50;
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
				b += cSize + 5;
				j++;
				txtmensajes = new JTextArea();
				txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));

				txtmensajes.setBounds(a, b, cSize, cSize);
				txtmensajes.setEditable(false);
				array[i][j] = txtmensajes;
				txtmensajes.setText(filas[j][i]);
				add(array[i][j]);
			}
			b = 50;
			j = 0;
			a += cSize + 5;
			i++;
		}

		for (int k = 0; k < size; k++) {
			for (int l = 0; l < size; l++) {
				array[k][l].setBackground(Color.LIGHT_GRAY);

			}
		}

		return array;
	}

}

class Coordenada implements Serializable {
	private int x;
	private int y;
	private boolean known;
	private String letra;

	public Coordenada(int x, int y) {
		this.x = x;
		this.y = y;
		this.known = false;
		this.letra = "";

	}

	public Coordenada(int x, int y, boolean known) {
		this.x = x;
		this.y = y;
		this.known = known;
		this.letra = "";
	}

	public Coordenada() {
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isKnown() {
		return known;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public String getLetra() {
		return letra;
	}

	public void setLetra(String letra) {
		this.letra = letra;
	}

	@Override
	public String toString() {
		return x + " ;" + y + ", known=" + known;
	}

	public boolean equals(Coordenada c) {
		boolean respuesta = false;
		if (this.getX() == c.getX() && this.getY() == c.getY()) {
			respuesta = true;
		} else {
			respuesta = false;
		}
		return respuesta;
	}

}
