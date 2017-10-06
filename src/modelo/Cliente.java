package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Cliente {

	public static void main(String[] args) {
		VentanaLogin ventanaLogin = new VentanaLogin();
		ventanaLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

class VentanaLogin extends JFrame implements ActionListener {

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

		JLabel ipLabel = new JLabel("IP:");
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
		setSize(400, 400); // tamaño del layout
		setLocationRelativeTo(null);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnLogin) {
			try {

				boolean esperando = true;
				Coordenada entradaXY = new Coordenada();

				Login login = new Login(ip.getText(), puerto.getText(), user.getText(), password.getText());

				Socket cliente = new Socket(ip.getText(), Integer.parseInt(puerto.getText()));
				

				ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());

				out.writeObject(login);
				

				while (esperando == true) {
					ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
					entradaXY = (Coordenada) entrada.readObject();
					
					if (entradaXY != null) {
						esperando = false;
					}
					
				}
				
				

				setVisible(false);

				VentanaCliente ventanaCliente = new VentanaCliente(entradaXY, ip.getText(), puerto.getText());
				ventanaCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	

			} catch (Exception ex) {
				System.out.println("Error en cliente " + ex.getMessage());
			}
		}

	}

}

class VentanaCliente extends JFrame{
	
	int cSize = 30;
	int posx = 70;
	int posy = 50;
	
	Coordenada posicionActual;
	
	Coordenada [] vecinos = new Coordenada[8];
	
	JTextArea casillero;
	JTextArea txtMensajes;

	public VentanaCliente(Coordenada entrada, String ip, String puerto) { // recibir posicion inicial y casillas aledañas en vez de size
			
		posicionActual = entrada;
		
		
		
		try {
			
			//Socket cliente = new Socket(ip,Integer.parseInt(puerto));
			
			JLabel entradaLabel = new JLabel("Coordenadas de entrada: " + entrada.getX() + "; " + entrada.getY());
			entradaLabel.setBounds(10, 10, 200, 20);
			add(entradaLabel);
			
			txtMensajes = new JTextArea();
			txtMensajes.setBounds(450, 20, 300, 300);
			add(txtMensajes);
			consola("Inicio de juego.");
			
			
			casillero = new JTextArea("E");
			casillero.setFont(new Font("Arial", Font.PLAIN, 25));
			casillero.setEditable(false);
			
			posicionar(casillero, entrada);
			add(casillero);
			consola("Jugador posicionado en coordenadas: " + entrada.getX() + "; " + entrada.getY());
			
			pintarCasillero(casillero);
		
			
			
			setLayout(null); 
			setTitle("Laberinto");
			setSize(800, 500); 
			setLocationRelativeTo(null);
			setVisible(true);
			

			mostrarVecinos("localhost", "9020");
			
			
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(-1,-1)));
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		
		
		
		
	}
		
			
			/*
			consola("Se pide letra de posicion actual");
			System.out.println(devolverLetra("localhost", "9020", posicionActual));
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(0,0)));
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(2,0)));
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(0,8)));
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(8,5)));
			System.out.println(devolverLetra("localhost", "9020", new Coordenada(-1,-1)));
			*/
			
			
	public void posicionar(JTextArea casillero, Coordenada c) {
		casillero.setBounds((c.getX()*(cSize+5))+posx, (c.getY()*(cSize+5))+posy, cSize, cSize);
		consola("Se muestra casillero: " + c.getX() + "; " + c.getY());
	}
	
	public void consola(String mensaje) {
		Calendar calendario = new GregorianCalendar();
		txtMensajes.append(calendario.get(Calendar.HOUR) + ":" + calendario.get(Calendar.MINUTE) + ":" + calendario.get(Calendar.SECOND));
		txtMensajes.append(": " + mensaje+"\n");
	}
	
	
	
	public void pintarCasillero(JTextArea casillero) {
		casillero.setBackground(Color.LIGHT_GRAY);
	}
	
	
	public void mostrarVecinos(String ip, String puerto) throws NumberFormatException, UnknownHostException, IOException  {
		JTextArea casillero;
		String letra = "";
		casillero = new JTextArea();
		casillero.setFont(new Font("Arial", Font.PLAIN, 25));
		casillero.setEditable(false);
		consola("Creando casillero derecho");
		letra = devolverLetra(ip, puerto, new Coordenada(posicionActual.getX()+1, posicionActual.getY()));
		consola("Recibimos la letra: " + letra);
		casillero.setText(letra);
		posicionar(casillero, new Coordenada(posicionActual.getX()+1, posicionActual.getY()));
		add(casillero);
		
	}
	
	public String devolverLetra(String ip, String puerto, Coordenada c) throws NumberFormatException, UnknownHostException, IOException {
		String letra = "";
		Socket cliente = new Socket(ip,Integer.parseInt(puerto));
		
		System.out.println("Cliente contacto en: " + ip + " sobre puerto: " + puerto);
		
		ObjectOutputStream pedido = new ObjectOutputStream(cliente.getOutputStream());
		
		consola("Se pide letra de coordenada: " + c.getX() + "; " + c.getY());
		pedido.writeObject(c);
		

		consola("Esperando letra");
		DataInputStream recibir = new DataInputStream(cliente.getInputStream());
		letra = recibir.readUTF();
			

		cliente.close();
		return letra;
	}

	


}

class Login implements Serializable {
	private String ip;
	private String puerto;
	private String user;
	private String pass;

	public Login(String ip, String puerto, String user, String pass) {
		this.ip = ip;
		this.puerto = puerto;
		this.user = user;
		this.pass = pass;
	}

	public String getIp() {
		return ip;
	}

	public String getPuerto() {
		return puerto;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}
}
