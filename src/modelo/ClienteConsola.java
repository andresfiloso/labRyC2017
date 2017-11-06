package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClienteConsola {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		VentanaClienteConsola ventanaClienteConsola = new VentanaClienteConsola();
		ventanaClienteConsola.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

class VentanaClienteConsola extends JFrame {

	static Helper h = new Helper();
	static int puerto = 0;
	static String ip;
	int oro = 0;
	boolean llave = false;
	public static int key = 346;
	int cSize = 30;
	int posx = 70;
	int posy = 50;
	int xEntrada = 0;
	int yEntrada = 0;
	public static Coordenada entrada;
	public static Coordenada posicionActual;
	public static KeyListener listener;
	static JTextArea[][] laberinto = new JTextArea[20][20];
	static Coordenada[][] mapa = new Coordenada[20][20];
	ArrayList<Coordenada> oroYaTomado = new ArrayList<Coordenada>();
	JTextArea casillero;
	static JTextArea txtMensajes;

	JTextField bashInput = new JTextField();
	JTextField bashOutput = new JTextField();
	JLabel oroLabel;

	public VentanaClienteConsola() throws ClassNotFoundException, IOException {

		entrada = new Coordenada();
		posicionActual = entrada;
		xEntrada = entrada.getX();
		yEntrada = entrada.getY();

		setFocusable(true);

		JLabel entradaLabel = new JLabel();
		entradaLabel.setBounds(10, 10, 200, 20);
		add(entradaLabel);

		oroLabel = new JLabel("Oro: " + oro);
		oroLabel.setBounds(300, 10, 200, 20);
		oroLabel.setFont(new Font("Arial", Font.PLAIN, 25));

		bashInput.addActionListener(action);
		bashInput.setText("log admin admin localhost 9020");
		bashInput.setFont(new Font("Arial", Font.BOLD, 15));
		bashInput.setBounds(10, 700, 700, 40);
		add(bashInput);

		bashOutput.addActionListener(action);
		bashOutput.setFont(new Font("Arial", Font.BOLD, 15));
		bashOutput.setBounds(10, 650, 700, 40);
		bashOutput.setEditable(false);
		add(bashOutput);

		// "Coordenadas de entrada: " + entrada.getX() + "; " + entrada.getY()

		setLayout(null);
		setTitle("Laberinto");
		setSize(800, 800);
		setLocationRelativeTo(null);
		setVisible(true);

	}

	Action action=new AbstractAction(){String msg="";

	@Override public void actionPerformed(ActionEvent e){Calendar calendario=new GregorianCalendar();String time=(calendario.get(Calendar.HOUR)+":"+calendario.get(Calendar.MINUTE)+":"+calendario.get(Calendar.SECOND));

	msg=e.getActionCommand();

	try{bashInput.setText(null);procesar(msg);}catch(ClassNotFoundException|NumberFormatException|IOException e1){bashOutput.setText(time+" | "+msg+" || "+e1.getMessage());e1.printStackTrace();}};};

	public void procesar(String msg) throws IOException, ClassNotFoundException {

		Calendar calendario = new GregorianCalendar();
		String time = (calendario.get(Calendar.HOUR) + ":" + calendario.get(Calendar.MINUTE) + ":"
				+ calendario.get(Calendar.SECOND));

		String args[] = msg.split(" ");
		String comando = args[0];

		switch (comando) {
		case "log":
			String user = args[1];
			String pass = args[2];
			ip = args[3];
			puerto = Integer.parseInt(args[4]);

			// PETICION DE AUTORIZACION

			List<String> settings = new ArrayList<String>();
			settings.add(user);
			settings.add(pass);

			Paquete credenciales = new Paquete(comando, settings);
			
			credenciales = h.encriptarPaquete(credenciales);
			
			Socket cliente = new Socket(ip, puerto);
			ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
			out.writeObject(credenciales);

			// RECIBIR AUTENTICACION

			Paquete autorizacion = new Paquete();
			ObjectInputStream autorizacionStream = new ObjectInputStream(cliente.getInputStream());
			autorizacion = (Paquete) autorizacionStream.readObject();
			
			autorizacion = h.desencriptarPaquete(autorizacion);

			if (autorizacion.getCommand().equalsIgnoreCase("Auth")) {
				entrada.setX(Integer.parseInt(autorizacion.getArgs().get(0)));
				entrada.setY(Integer.parseInt(autorizacion.getArgs().get(1)));
				entrada.setKnown(true);
				entrada.setLetra("E");

				add(oroLabel);
				dibujarLaberinto(20, false);
				initMapa();
				mostrarVecinos();

				bashOutput.setText(time + " | " + msg + " || Auth: OK - Bienvenido Admin");

			} else {
				System.out.println("Error al recibir coordenadas de entrada. Verificar autenticacion");
				bashOutput.setText(time + " | " + msg + " || Auth: NO - Verificar Credenciales");
			}
			break;
		case "mov":
			String direccion = args[1];
			

			if(direccion.equalsIgnoreCase("izquierda") ||
					direccion.equalsIgnoreCase("arriba") ||
					direccion.equalsIgnoreCase("derecha") ||
					direccion.equalsIgnoreCase("abajo")){
				
				if (limites(direccion)) {

					boolean camino = true;
					modificarPos(args[1]);
					mostrarVecinos();
					if (checkOro()) {
						oroLabel.setText("Oro: " + Integer.toString(oro));
						bashOutput.setText(time + " | " + msg + " || Mov: Ok - Oro!");
						camino = false;
					}
					if (checkGuardia()) {
						bashOutput.setText(time + " | " + msg + " || Mov: Ok - Guardia!");
						camino = false;
					}
					if (checkSalida()) {
						bashOutput.setText(time + " | " + msg + " || Mov: Ok - Salida!");
						camino = false;
					}
					if (llave == false) {
						if (checkLlave()) {
							bashOutput.setText(time + " | " + msg + " || Mov: Ok - Llave!");
							camino = false;
						}
					}
					if (camino) {
						bashOutput.setText(time + " | " + msg + " || Mov: Ok - Camino");
					}
				} else {
					bashOutput.setText(time + " | " + msg + " || Mov: NO - Pared!");
				}
			}else {
				bashOutput.setText(
						time + " | " + msg + " || Mov: NO - Direccion invalida. (izquierda/arriba/derecha/abajo)");	
			}
			break;
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

	public static void consola(String mensaje) {
		Calendar calendario = new GregorianCalendar();
		txtMensajes.append(calendario.get(Calendar.HOUR) + ":" + calendario.get(Calendar.MINUTE) + ":"
				+ calendario.get(Calendar.SECOND));
		txtMensajes.append(": " + mensaje + "\n");
	}

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
		String letra = "";
		Socket cliente = new Socket(InetAddress.getLocalHost().getHostAddress(), puerto);

		List<String> settings = new ArrayList<String>();
		String command = "square";
		settings.add(Integer.toString(c.getX()));
		settings.add(Integer.toString(c.getY()));

		Paquete pedidoLetra = new Paquete(command, settings);
		
		pedidoLetra = h.encriptarPaquete(pedidoLetra);

		ObjectOutputStream pedidoLetraStream = new ObjectOutputStream(cliente.getOutputStream());
		pedidoLetraStream.writeObject(pedidoLetra); // square x y

		ObjectInputStream valorLetraStream = new ObjectInputStream(cliente.getInputStream());
		Paquete valorLetra = new Paquete();
		valorLetra = (Paquete) valorLetraStream.readObject();
		
		valorLetra = h.desencriptarPaquete(valorLetra);

		letra = valorLetra.getArgs().get(0);

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
				respuesta = true;
				if (oro == 0) {
					JOptionPane.showMessageDialog(null, "No tiene oro para pagar al guardia. Perdirse!");
					posicionActual.setX(xEntrada);
					posicionActual.setY(yEntrada);
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

}
