package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Cliente {

	public static void main(String[] args) {
		VentanaLogin ventanaLogin = new VentanaLogin();
		ventanaLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

class VentanaLogin extends JFrame implements ActionListener {

	Helper h = new Helper();

	JButton btnLogin;
	JTextField ip;
	JTextField puerto;
	JTextField user;
	JTextField password;
	JTextArea respuesta;

	public VentanaLogin() {
		JLabel titulo = new JLabel("Login");
		titulo.setBounds(10, 10, 50, 20);
		add(titulo);

		JLabel ipLabel = new JLabel("Puerto: ");
		ipLabel.setBounds(10, 50, 50, 20);
		add(ipLabel);

		ip = new JTextField("localhost");
		ip.setBounds(70, 50, 100, 20);
		add(ip);

		JLabel puertoLabel = new JLabel("Puerto: ");
		puertoLabel.setBounds(10, 80, 50, 20);
		add(puertoLabel);

		puerto = new JTextField("9020");
		puerto.setBounds(70, 80, 100, 20);
		add(puerto);

		JLabel userLabel = new JLabel("Usuario: ");
		userLabel.setBounds(10, 110, 50, 20);
		add(userLabel);

		user = new JTextField("admin");
		user.setBounds(70, 110, 100, 20);
		add(user);

		JLabel passLabel = new JLabel("Clave: ");
		passLabel.setBounds(10, 140, 50, 20);
		add(passLabel);

		password = new JTextField("admin");
		password.setBounds(70, 140, 100, 20);
		add(password);

		btnLogin = new JButton();
		btnLogin.setText("Ingresar");
		btnLogin.setBounds(10, 180, 150, 20); // (x, y, ancho, alto)
		btnLogin.addActionListener(this); // para poder programar en el boton
		add(btnLogin);

		JLabel respuestaLabel = new JLabel("Respuesta del servidor: ");
		respuestaLabel.setBounds(10, 290, 150, 20);
		add(respuestaLabel);

		respuesta = new JTextArea(" Esperando...");
		respuesta.setBounds(10, 320, 150, 20);
		add(respuesta);

		setLayout(null); // para que los controles no esten uno encima del otro
		setTitle("Cliente Login");
		setSize(400, 400); // tama�o del layout
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnLogin) {
			try {
				Coordenada entradaXY = new Coordenada();

				String command = "log";
				List<String> settings = new ArrayList<String>();
				settings.add(user.getText());
				settings.add(password.getText());

				// PETICION DE AUTORIZACION

				Paquete credenciales = new Paquete(command, settings);
				Socket cliente = new Socket(ip.getText(), Integer.parseInt(puerto.getText()));
				ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());

				credenciales = h.encriptarPaquete(credenciales);

				System.out.println(credenciales.getCommand());
				System.out.println(credenciales.getArgs());

				out.writeObject(credenciales);

				// RECIBIR AUTENTICACION

				Paquete autorizacion = new Paquete();
				ObjectInputStream autorizacionStream = new ObjectInputStream(cliente.getInputStream());
				autorizacion = (Paquete) autorizacionStream.readObject();

				autorizacion = h.desencriptarPaquete(autorizacion);

				if (autorizacion.getCommand().equalsIgnoreCase("Auth")) {
					entradaXY.setX(Integer.parseInt(autorizacion.getArgs().get(0)));
					entradaXY.setY(Integer.parseInt(autorizacion.getArgs().get(1)));
					entradaXY.setKnown(true);
					entradaXY.setLetra("E");

					setVisible(false);
					VentanaCliente ventanaCliente = new VentanaCliente(entradaXY, puerto.getText());
					ventanaCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				} else {
					System.out.println("Error al recibir coordenadas de entrada. Verificar autenticacion");
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Error en cliente " + ex.getMessage());
			}
		}
	}
}

class VentanaCliente extends JFrame {

	static Helper h = new Helper();
	static int puerto = 0;
	int oro = 0;
	boolean llave = false;
	public static int key = 346;
	int cSize = 30;
	int posx = 70;
	int posy = 50;
	int xEntrada = 0;
	int yEntrada = 0;
	public static Coordenada posicionActual;
	public static KeyListener listener;
	static JTextArea[][] laberinto = new JTextArea[20][20];
	static Coordenada[][] mapa = new Coordenada[20][20];
	ArrayList<Coordenada> oroYaTomado = new ArrayList<Coordenada>();
	JTextArea casillero;
	// static JTextArea txtMensajes;

	JLabel oroLabel;

	public VentanaCliente(Coordenada entrada, String port) throws ClassNotFoundException, IOException {

		puerto = Integer.parseInt(port);
		posicionActual = entrada;
		xEntrada = entrada.getX();
		yEntrada = entrada.getY();
		listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);
		// config = new
		// Configuracion(InetAddress.getLocalHost().getHostAddress(),puerto);
		JLabel entradaLabel = new JLabel("Coordenadas de entrada: " + entrada.getX() + "; " + entrada.getY());
		entradaLabel.setBounds(10, 10, 200, 20);
		add(entradaLabel);

		oroLabel = new JLabel("Oro: " + oro);
		oroLabel.setBounds(300, 10, 200, 20);
		oroLabel.setFont(new Font("Arial", Font.PLAIN, 25));
		add(oroLabel);

		/*
		 * txtMensajes = new JTextArea(); txtMensajes.setBounds(450, 20, 300, 1000);
		 * add(txtMensajes); txtMensajes.setEditable(false);
		 * consola("Inicio de juego.");
		 * 
		 */

		dibujarLaberinto(20, false);
		initMapa();
		setLayout(null);
		setTitle("Laberinto");
		setSize(800, 800);
		setLocationRelativeTo(null);
		setVisible(true);

		mostrarVecinos();

	}

	public void procesar(String msg)
			throws ClassNotFoundException, NumberFormatException, UnknownHostException, IOException {

		String args[] = msg.split(" ");
		String comando = args[0];

		switch (comando) {
		case "mov":
			if (limites(args[1])) {
				modificarPos(args[1]);
				mostrarVecinos();
			}

		}
	}

	private void modificarPos(String direccion) {
		switch (direccion) {
		case "izquierda":
			posicionActual.setX(posicionActual.getX() - 1);
			break;
		case "arriba":
			posicionActual.setY(posicionActual.getY() - 1);
			break;
		case "derecha":
			posicionActual.setX(posicionActual.getX() + 1);
			break;
		case "abajo":
			posicionActual.setY(posicionActual.getY() + 1);
			break;
		}

	}

	public void initMapa() {
		System.out.println("Inicializando mapa");
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				mapa[i][j] = new Coordenada(i, j, false);
			}
		}
		mapa[xEntrada][yEntrada].setKnown(true);
	}

	/*
	 * public static void consola(String mensaje) { Calendar calendario = new
	 * GregorianCalendar(); txtMensajes.append(calendario.get(Calendar.HOUR) + ":" +
	 * calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND));
	 * txtMensajes.append(": " + mensaje + "\n"); }
	 * 
	 */

	public JTextArea format(JTextArea j, Coordenada c) {
		j.setText(c.getLetra());
		j.setBounds(laberinto[c.getX()][c.getY()].getX(), laberinto[c.getX()][c.getY()].getY(), 35, 35);
		j.setFont(new Font("Arial", Font.PLAIN, 25));
		j.setEditable(false);

		if (j.getText().equalsIgnoreCase("C"))
			j.setBackground(Color.GREEN);

		if (j.getText().equalsIgnoreCase("P"))
			j.setBackground(Color.DARK_GRAY);

		if (j.getText().equalsIgnoreCase("O"))
			j.setBackground(Color.YELLOW);

		if (j.getText().equalsIgnoreCase("G"))
			j.setBackground(Color.MAGENTA);

		if (j.getText().equalsIgnoreCase("K"))
			j.setBackground(Color.RED);

		if (j.getText().equalsIgnoreCase(" "))
			j.setBackground(Color.WHITE);

		if (posicionActual.equals(c))
			j.setBackground(Color.ORANGE);

		return j;
	}

	public void dibujarLaberinto(int size, boolean reiniciar) {
		if (!reiniciar) {
			int cSize = 30;
			JTextArea txtmensajes;
			int a = 70, b = 50;
			int i = 0, j = 0;
			while (i < size) {
				txtmensajes = new JTextArea();
				txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));
				txtmensajes.setBounds(a, b, cSize, cSize);
				txtmensajes.setEditable(false);
				laberinto[i][j] = txtmensajes;

				while (j < size - 1) {
					b += cSize + 5;
					j++;
					txtmensajes = new JTextArea();
					txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));

					txtmensajes.setBounds(a, b, cSize, cSize);
					txtmensajes.setEditable(false);
					laberinto[i][j] = txtmensajes;
				}
				b = 50;
				j = 0;
				a += cSize + 5;
				i++;
			}
		}
	}

	public void mostrarVecinos()
			throws NumberFormatException, UnknownHostException, IOException, ClassNotFoundException {

		ArrayList<Coordenada> coordenadas = new ArrayList<Coordenada>();
		if (mapa[posicionActual.getX()][posicionActual.getY()].isKnown() == false) {
			Coordenada izquierda = new Coordenada(posicionActual.getX() - 1, posicionActual.getY());
			Coordenada derecha1 = new Coordenada(posicionActual.getX() + 1, posicionActual.getY());
			Coordenada arriba = new Coordenada(posicionActual.getX(), posicionActual.getY() - 1);
			Coordenada arriba1 = new Coordenada(arriba.getX() + 1, arriba.getY());
			Coordenada abajo = new Coordenada(posicionActual.getX(), posicionActual.getY() + 1);
			Coordenada abajo1 = new Coordenada(abajo.getX() + 1, abajo.getY());

			coordenadas.add(posicionActual);
			coordenadas.add(izquierda);
			coordenadas.add(derecha1);
			coordenadas.add(arriba);
			coordenadas.add(arriba1);
			coordenadas.add(abajo);
			coordenadas.add(abajo1);
		} else {
			Coordenada izquierda = new Coordenada(posicionActual.getX() - 1, posicionActual.getY());
			Coordenada derecha1 = new Coordenada(posicionActual.getX() + 1, posicionActual.getY());
			Coordenada arriba = new Coordenada(posicionActual.getX(), posicionActual.getY() - 1);
			Coordenada abajo = new Coordenada(posicionActual.getX(), posicionActual.getY() + 1);

			coordenadas.add(izquierda);
			coordenadas.add(derecha1);
			coordenadas.add(arriba);
			coordenadas.add(abajo);
		}

		for (Coordenada c : coordenadas) {
			if (c.getX() >= 0 && c.getX() <= 19 && c.getY() >= 0 && c.getY() <= 19) {
				mapa[c.getX()][c.getY()].setLetra(devolverLetra(c));

				if (mapa[c.getX()][c.getY()].isKnown() == false) {
					JTextArea casilla = new JTextArea();
					casilla = format(casilla, mapa[c.getX()][c.getY()]);
					add(casilla);
				}
				SwingUtilities.updateComponentTreeUI(this);
				mapa[c.getX()][c.getY()].setKnown(true);
			}
			JTextArea casilla = new JTextArea();
			casilla = format(casilla, posicionActual);
			add(casilla);
		}
	}

	public static String devolverLetra(Coordenada c)
			throws NumberFormatException, UnknownHostException, IOException, ClassNotFoundException {
		int letraEncript = 0;
		String letra = "";
		Socket cliente = new Socket(InetAddress.getLocalHost().getHostAddress(), puerto);

		List<String> settings = new ArrayList<String>();
		String command = "square";
		settings.add(Integer.toString(c.getX()));
		settings.add(Integer.toString(c.getY()));

		Paquete pedidoLetra = new Paquete(command, settings);

		pedidoLetra = h.encriptarPaquete(pedidoLetra);

		ObjectOutputStream pedidoLetraStream = new ObjectOutputStream(cliente.getOutputStream());
		// c = encriptarCoordenada(c);
		pedidoLetraStream.writeObject(pedidoLetra); // square x y

		ObjectInputStream valorLetraStream = new ObjectInputStream(cliente.getInputStream());
		Paquete valorLetra = new Paquete();
		valorLetra = (Paquete) valorLetraStream.readObject();

		valorLetra = h.desencriptarPaquete(valorLetra);

		letra = valorLetra.getArgs().get(0);

		// letraEncript = recibir.readInt();
		// letra = desencriptarLetra(letraEncript);
		cliente.close();
		return letra;
	}

	public boolean limites(String direccion)
			throws NumberFormatException, UnknownHostException, IOException, ClassNotFoundException {
		boolean resultado = true;
		if (direccion.equals("derecha")) { // Derecha
			if (posicionActual.getX() == 19) {
				resultado = false;
			}
			if (devolverLetra(new Coordenada(posicionActual.getX() + 1, posicionActual.getY())).equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("arriba")) { // Arriba
			if (posicionActual.getY() == 0) {
				resultado = false;
			}
			if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY() - 1)).equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("abajo")) { // Abajo
			if (posicionActual.getY() == 19) {
				resultado = false;
			}
			if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY() + 1)).equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		if (direccion.equals("izquierda")) { // Izquierda
			if (posicionActual.getX() == 0) {
				resultado = false;
			}
			if (devolverLetra(new Coordenada(posicionActual.getX() - 1, posicionActual.getY())).equalsIgnoreCase("P")) {
				resultado = false;
			}
		}
		return resultado;
	}

	public boolean checkOro() throws NumberFormatException, UnknownHostException, IOException {
		boolean respuesta = false;
		boolean tomado = false;

		try {
			for (Coordenada c : oroYaTomado) {
				if (c.equals(posicionActual)) {
					tomado = true;
				}
			}
			if (tomado == false) {
				if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY())).equalsIgnoreCase("O")) {
					oro++;
					respuesta = true;
					laberinto[posicionActual.getX()][posicionActual.getY()].setText("X");
					oroYaTomado.add(new Coordenada(posicionActual.getX(), posicionActual.getY()));
				}
			}

		} catch (Exception e) {
			System.out.println("\n\n\n\nHA OCURRIDO UN ERROR EN ORO: " + e.getMessage());
			e.printStackTrace();
		}
		return respuesta;
	}

	public boolean checkGuardia() throws NumberFormatException, UnknownHostException, IOException {
		boolean respuesta = false;
		try {
			if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY())).equalsIgnoreCase("G")) {
				if (oro == 0) {
					JOptionPane.showMessageDialog(null, "No tiene oro para pagar al guardia. Perdirse!");
					posicionActual.setX(xEntrada);
					posicionActual.setY(yEntrada);
					respuesta = true;
					salir();
				} else {
					oro--;
					// JOptionPane.showMessageDialog(null, "Guardia! Perdiste 1 oro. Ahora tienes: "
					// + oro + " de oro");
				}
			}
		} catch (Exception e) {
			System.out.println("\n\n\n\nHA OCURRIDO UN ERROR EN GUARDIA" + e.getMessage());
			e.printStackTrace();
		}
		return respuesta;
	}

	public boolean checkSalida() throws NumberFormatException, UnknownHostException, IOException {
		boolean respuesta = false;
		try {
			if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY())).equalsIgnoreCase("S")) {
				if (llave == true) {
					JOptionPane.showMessageDialog(null, "Ganaste! Llegaste a la salida");
					posicionActual.setX(xEntrada);
					posicionActual.setY(yEntrada);
					respuesta = true;
					salir();
				} else {
					JOptionPane.showMessageDialog(null, "No podes salir, todavia no encontraste la llave!");
					respuesta = false;
				}
			}
		} catch (Exception e) {
			System.out.println("\n\n\n\nHA OCURRIDO UN ERROR EN GUARDIA" + e.getMessage());
			e.printStackTrace();
		}
		return respuesta;
	}

	public boolean checkLlave() throws NumberFormatException, UnknownHostException, IOException {
		boolean respuesta = false;
		try {
			if (devolverLetra(new Coordenada(posicionActual.getX(), posicionActual.getY())).equalsIgnoreCase("K")) {
				llave = true;
				JOptionPane.showMessageDialog(null, "Encontraste la llave. Ahora encontra la salida!");
				respuesta = true;
			}
		} catch (Exception e) {
			System.out.println("\n\n\n\nHA OCURRIDO UN ERROR EN ORO: " + e.getMessage());
			e.printStackTrace();
		}
		return respuesta;
	}

	public void salir() throws NumberFormatException, UnknownHostException, IOException {

	}

	class MyKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// System.out.println("keyPressed=" + e.getKeyCode());
			oroLabel.setText("Oro: " + Integer.toString(oro));
			String direccion = "";
			switch (e.getKeyCode()) {
			case 37: // Izquierda
				direccion = "izquierda";
				break;
			case 38: // Arriba
				direccion = "arriba";
				break;
			case 39: // Derecho
				direccion = "derecha";
				break;
			case 40: // Abajo
				direccion = "abajo";
				break;
			}
			try {
				if (limites(direccion)) {
					modificarPos(direccion);
					mostrarVecinos();
					checkOro();
					checkGuardia();
					checkSalida();
					if (llave == false)
						checkLlave();

				}

			} catch (ClassNotFoundException | NumberFormatException | IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
		}
	}

	public void keyReleased(KeyEvent e) {
	}

}
