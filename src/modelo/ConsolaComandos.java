package modelo;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.sound.midi.Synthesizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ConsolaComandos {

	public static void main(String[] args) {
		VentanaConsola ventanaConsola = new VentanaConsola();
		ventanaConsola.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

class VentanaConsola extends JFrame {
	boolean juego = false;
	JTextArea bash;
	JTextField bashInput = new JTextField();
	JButton btnEnviar;
	JScrollPane jp;
	String init = "Consola Cliente para Laberinto v.1\nIngrese \"help\" para mas información\n\n";
	Login loginBash = new Login();

	public VentanaConsola() {

		bash = new JTextArea(init);
		bash.setFont(new Font("Arial", Font.PLAIN, 15));
		bash.setEditable(false);

		jp = new JScrollPane(bash);
		add(jp, BorderLayout.CENTER);

		bashInput.addActionListener(action);
		bashInput.setFont(new Font("Arial", Font.BOLD, 15));
		bashInput.setPreferredSize(new Dimension(0, 30));
		add(bashInput, BorderLayout.SOUTH);

		setTitle("Consola de Comandos");
		setSize(800, 500);
		setLocationByPlatform(true);
		setVisible(true);
	}

	Action action = new AbstractAction() {
		String msg = "";
		@Override
		public void actionPerformed(ActionEvent e) {
			Calendar calendario = new GregorianCalendar();
			String time = (calendario.get(Calendar.HOUR) + ":" + calendario.get(Calendar.MINUTE) + ":"
					+ calendario.get(Calendar.SECOND));
			msg = e.getActionCommand();

			if (loginBash.getUser().equalsIgnoreCase("unlogged")) {
				bash.append(time + " | " + msg + "\n");
				bashInput.setText(null);
				procesar(msg);
			} else {
				bash.append(time + " | " + loginBash.getUser() + " | " + msg + "\n");
				bashInput.setText(null);
				procesar(msg);
			}
			jp.getViewport().setViewPosition((new Point(0, bash.getHeight()))); // Scroll siempre abajo
		};

	};

	public void procesar(String msg) {

		try {
			String parametros[] = msg.split(" ");
			String comando = parametros[0];
			String noLogin = "Error: Antes de ejecutar un comando de movimiento tiene que conectarse al servidor\n"
					+ "Utilice el comando login user pass ip port para autenticar\n\n";

			for (int i = 0; i < parametros.length; i++) {
				System.out.println(parametros[i]);
			}

			switch (comando) {
			case "help":
				VentanaFAQS ventanaFAQS = new VentanaFAQS();
				break;
			case "init":
				if (parametros[1].equalsIgnoreCase("server")) {
					int puerto = Integer.parseInt(parametros[2]);
					new Servidor(puerto);
				} else if (parametros[1].equalsIgnoreCase("login") && parametros[2].equals("gui")) {
					System.out.println("login gui");
					VentanaLogin ventanaLogin = new VentanaLogin();
				}
				break;
			case "login":
				if (!msg.equalsIgnoreCase("login")) {
					bash.append("Autorizando usuario: " + parametros[1] + "\n");
					Login login = new Login(parametros[3], parametros[4], parametros[1], parametros[2]);
					loguear(login);
				} else {
					bash.append(
							"Error: Para utilizar el comando login tiene que ingresar credenciales, ip y puerto\n\n");
				}
				break;
			case "exit":
				if (!loginBash.getUser().equalsIgnoreCase("Unlogged")) {
					cerrar();
				} else {
					bash.append("Error: Antes de ejecutar el comando \"exit\" tiene que conectarse al servidor.\n\n");
				}
				break;
			case "status":
				if (juego == true) {
					bash.append("Posicion Actual: " + VentanaCliente.posicionActual.getX() + " ;"
							+ VentanaCliente.posicionActual.getY() + " \n\n");
				}
				break;
			case "clear":
				bash.setText(init);
				break;
			case "izquierda":
			case "arriba":
			case "derecha":
			case "abajo":
				if (juego == false) {
					bash.append(noLogin);
				} else {
					// No esta desarrollado
				}
				break;
			default:
				bash.append("Error: \"" + msg
						+ "\" no se reconoce como un comando válido. Ingrese \"help\" para mas información\n\n");
				throw new IllegalArgumentException("Error: \"" + msg
						+ "\" no se reconoce como un comando válido. Ingrese \"help\" " + "para mas información\n\n");
			}
		} catch (Exception e) {
			// loginBash.setUser("Unlogged");
			System.out.println(e.getMessage());
		}
		
		

	}
	
	public boolean loguear(Login login) {
		boolean resultado = true;
		Coordenada entradaXY = new Coordenada();

		Socket cliente;
		try {
			cliente = new Socket(login.getIp(), Integer.parseInt(login.getPuerto()));
			ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());
			out.writeObject(login);
			ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
			entradaXY = (Coordenada) entrada.readObject();
			if(entradaXY.getX() == 100 && entradaXY.getY() == 100) {
				juego = false;
				bash.append("Servidor | " + login.getPuerto() + " | Credenciales incorrectas\n");
			}else {
				
				bash.append("Servidor | " + login.getPuerto() + " | Autorizado\n");
				
				loginBash = login;
				juego = true;

				VentanaCliente ventanaCliente = new VentanaCliente(entradaXY, login.getIp(), login.getPuerto());
				ventanaCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
			}
		} catch (NumberFormatException | IOException | ClassNotFoundException e) {
			bash.append("Error en cliente: " + e.getMessage() + "\n\n");
			System.out.println(e.getMessage());
		}
		return resultado;
	}

	public void cerrar() throws NumberFormatException, UnknownHostException, IOException {
		bash.append("Cerrando Servidor por instruccion de usuario\n\n");
		VentanaCliente.devolverLetra(loginBash.getIp(), loginBash.getPuerto(), new Coordenada(-1, -1));
	}
}

class VentanaFAQS extends JFrame {

	JLabel titulo, help, clear, initServer, loginGui, login, exit, izquierda, arriba, derecha, abajo, status;
	int y = 50;
	int x = 10;
	int w = 300;
	int h = 20;

	public VentanaFAQS() {

		titulo = new JLabel("Guia de comandos");
		titulo.setBounds(x, 10, w, h);
		titulo.setFont(new Font("Arial", Font.PLAIN, 20));
		add(titulo);

		help = new JLabel("help");
		help.setBounds(x, y, w, h);
		help.setFont(new Font("Arial", Font.BOLD, 15));
		add(help);
		y += 30;

		clear = new JLabel("clear");
		clear.setBounds(x, y, w, h);
		clear.setFont(new Font("Arial", Font.BOLD, 15));
		add(clear);
		y += 30;

		initServer = new JLabel("init server port");
		initServer.setBounds(x, y, w, h);
		initServer.setFont(new Font("Arial", Font.BOLD, 15));
		add(initServer);
		y += 30;

		loginGui = new JLabel("init login gui");
		loginGui.setBounds(x, y, w, h);
		loginGui.setFont(new Font("Arial", Font.BOLD, 15));
		add(loginGui);
		y += 30;

		login = new JLabel("login user pass ip port");
		login.setBounds(x, y, w, h);
		login.setFont(new Font("Arial", Font.BOLD, 15));
		add(login);
		y += 30;

		status = new JLabel("status");
		status.setBounds(x, y, w, h);
		status.setFont(new Font("Arial", Font.BOLD, 15));
		add(status);
		y += 30;

		exit = new JLabel("exit");
		exit.setBounds(x, y, w, h);
		exit.setFont(new Font("Arial", Font.BOLD, 15));
		add(exit);
		y += 30;

		/*
		izquierda = new JLabel("izquierda");
		izquierda.setBounds(x, y, w, h);
		izquierda.setFont(new Font("Arial", Font.BOLD, 15));
		add(izquierda);
		y += 30;

		arriba = new JLabel("arriba");
		arriba.setBounds(x, y, w, h);
		arriba.setFont(new Font("Arial", Font.BOLD, 15));
		add(arriba);
		y += 30;

		derecha = new JLabel("derecha");
		derecha.setBounds(x, y, 300, 20);
		derecha.setFont(new Font("Arial", Font.BOLD, 15));
		add(derecha);
		y += 30;

		abajo = new JLabel("abajo");
		abajo.setBounds(x, y, 300, 20);
		abajo.setFont(new Font("Arial", Font.BOLD, 15));
		add(abajo);
		y += 30;
		*/

		setLayout(null); // para que los controles no esten uno encima del otro
		setTitle("Comandos");
		setSize(400, 500); // tamaño del layout
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);

	}
}
