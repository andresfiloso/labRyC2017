package modelo;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Servidor extends JFrame implements Runnable {

	JTextArea txtMensajes;

	Helper h = new Helper();

	String authUser = "admin";
	String authPass = "admin";

	int puerto;
	Coordenada posicionActual = new Coordenada();

	int matrizSize = 20;

	JTextArea[][] laberinto = new JTextArea[matrizSize][matrizSize];

	public Servidor(int p) {

		puerto = p;

		txtMensajes = new JTextArea();
		txtMensajes.setBounds(450, 20, 300, 1000);
		txtMensajes.setEditable(false);
		add(txtMensajes);

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

			while (true) {
				cliente = servidorJuego.accept(); // esperando una llamada

				Paquete solicitud = new Paquete();
				ObjectInputStream credencialesStream = new ObjectInputStream(cliente.getInputStream());
				solicitud = (Paquete) credencialesStream.readObject(); // log user pass

				solicitud = h.desencriptarPaquete(solicitud);

				switch (solicitud.getCommand()) {
				case "log":
					String user = solicitud.getArgs().get(0);
					String pass = solicitud.getArgs().get(1);

					if (autorizar(user, pass)) {
						consola("Bienvenido: " + user);

						laberinto = dibujarLaberinto(matrizSize);
						posicionActual = coordenadasEntrada(laberinto);

						// ENVIANDO AUTORIZACION

						String command = "auth";
						List<String> args = new ArrayList<String>();
						args.add(Integer.toString(posicionActual.getX()));
						args.add(Integer.toString(posicionActual.getY()));
						Paquete autorizacion = new Paquete(command, args);

						autorizacion = h.encriptarPaquete(autorizacion);

						ObjectOutputStream autorizacionStream = new ObjectOutputStream(cliente.getOutputStream());
						autorizacionStream.writeObject(autorizacion); // auth x y
					} else {
						Paquete autorizacion = new Paquete("error");

						autorizacion = h.encriptarPaquete(autorizacion);
						ObjectOutputStream autorizacionStream = new ObjectOutputStream(cliente.getOutputStream());
						autorizacionStream.writeObject(autorizacion);
						consola("Acesso denegado.");
						// servidorJuego.close();
					}
					break;
				case "square":

					int x = Integer.parseInt(solicitud.getArgs().get(0));
					int y = Integer.parseInt(solicitud.getArgs().get(1));

					Coordenada c = new Coordenada(x, y);

					String letra = devolverValor(c, laberinto);

					String command = "Letra";
					List<String> args = new ArrayList<String>();
					args.add(letra);

					Paquete envioLetra = new Paquete(command, args);

					envioLetra = h.encriptarPaquete(envioLetra);
					ObjectOutputStream envioLetraStream = new ObjectOutputStream(cliente.getOutputStream());
					envioLetraStream.writeObject(envioLetra);

					break;
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}

	}

	public boolean limites(String direccion) throws NumberFormatException, UnknownHostException, IOException {
		boolean resultado = true;
		if (direccion.equals("derecha")) { // Derecha
			if (posicionActual.getX() == 19) {
				resultado = false;
			}
			if (devolverValor(new Coordenada(posicionActual.getX() + 1, posicionActual.getY()), laberinto)
					.equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("arriba")) { // Arriba
			if (posicionActual.getY() == 0) {
				resultado = false;
			}
			if (devolverValor(new Coordenada(posicionActual.getX(), posicionActual.getY() - 1), laberinto)
					.equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("abajo")) { // Abajo
			if (posicionActual.getY() == 19) {
				resultado = false;
			}
			if (devolverValor(new Coordenada(posicionActual.getX(), posicionActual.getY() + 1), laberinto)
					.equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("izquierda")) { // Izquierda
			if (posicionActual.getX() == 0) {
				resultado = false;
			}
			if (devolverValor(new Coordenada(posicionActual.getX() - 1, posicionActual.getY()), laberinto)
					.equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		return resultado;
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

		String fila1[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P",
				"S", "P" };
		String fila2[] = { "E", "C", "C", "C", "C", "P", "P", "P", "O", "P", "C", "G", "C", "C", "P", "O", "C", "C",
				"C", "P" };
		String fila3[] = { "P", "P", "P", "P", "C", "P", "P", "P", "C", "P", "C", "P", "P", "C", "P", "P", "P", "C",
				"O", "P" };
		String fila4[] = { "P", "P", "P", "P", "C", "P", "P", "P", "C", "P", "C", "P", "P", "G", "C", "O", "C", "G",
				"C", "P" };
		String fila5[] = { "P", "O", "C", "C", "C", "C", "C", "C", "C", "P", "C", "P", "P", "C", "P", "P", "P", "C",
				"P", "P" };
		String fila6[] = { "P", "P", "P", "P", "P", "P", "C", "P", "G", "P", "C", "P", "P", "C", "P", "P", "P", "C",
				"P", "P" };
		String fila7[] = { "P", "C", "C", "C", "P", "P", "G", "P", "C", "P", "O", "C", "C", "G", "C", "O", "C", "G",
				"C", "P" };
		String fila8[] = { "P", "C", "P", "C", "P", "P", "C", "P", "C", "P", "C", "P", "P", "P", "P", "P", "P", "P",
				"C", "P" };
		String fila9[] = { "P", "O", "P", "C", "C", "C", "C", "P", "O", "P", "C", "P", "P", "P", "P", "P", "P", "P",
				"C", "P" };
		String fila10[] = { "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "C", "C", "C", "O", "C", "C",
				"C", "P" };
		String fila11[] = { "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "C", "P", "P", "p", "P", "P",
				"C", "P" };
		String fila12[] = { "P", "C", "C", "C", "C", "O", "C", "C", "C", "C", "G", "P", "O", "C", "C", "G", "C", "O",
				"O", "P" };
		String fila13[] = { "P", "P", "P", "P", "P", "P", "P", "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "P",
				"P", "P" };
		String fila14[] = { "P", "O", "O", "P", "P", "P", "P", "P", "C", "P", "P", "P", "P", "P", "P", "P", "P", "P",
				"P", "P" };
		String fila15[] = { "P", "O", "O", "C", "C", "G", "C", "C", "C", "C", "C", "C", "O", "C", "C", "C", "C", "C",
				"P", "P" };
		String fila16[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "C",
				"P", "P" };
		String fila17[] = { "P", "O", "K", "O", "G", "C", "C", "C", "C", "C", "C", "C", "C", "O", "G", "P", "P", "C",
				"C", "P" };
		String fila18[] = { "P", "P", "P", "P", "P", "P", "P", "P", "G", "P", "P", "P", "P", "P", "P", "P", "P", "P",
				"C", "P" };
		String fila19[] = { "P", "O", "O", "G", "C", "C", "C", "C", "C", "C", "C", "C", "C", "O", "C", "C", "C", "C",
				"O", "P" };
		String fila20[] = { "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P", "P",
				"P", "P" };

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