package modelo;

import modelo.Coordenada;

import java.awt.Color;
import java.awt.Font;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
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

class VentanaCliente extends JFrame {
	
	int cSize = 30;
	int posx = 70;
	int posy = 50;
	
	Coordenada posicionActual;
	
	JTextArea [][] laberinto = new JTextArea[10][10];
	
	JTextArea casillero;
	JTextArea txtMensajes;
	
	
	
	public VentanaCliente(Coordenada entrada, String ip, String puerto) { // recibir posicion inicial y casillas aledañas en vez de size
			
		posicionActual = entrada;
		
		KeyListener listener = new MyKeyListener();
		addKeyListener(listener);
		setFocusable(true);
	
		
		try {
			
			
			JLabel entradaLabel = new JLabel("Coordenadas de entrada: " + entrada.getX() + "; " + entrada.getY());
			entradaLabel.setBounds(10, 10, 200, 20);
			add(entradaLabel);
			
			txtMensajes = new JTextArea();
			txtMensajes.setBounds(450, 20, 300, 1000);
			add(txtMensajes);
			txtMensajes.setEditable(false);
			consola("Inicio de juego.");
			
			dibujarLaberinto(10);
			
			/*
			
			casillero = new JTextArea("E");
			casillero.setFont(new Font("Arial", Font.PLAIN, 25));
			casillero.setEditable(false);
			
			posicionar(casillero, entrada);
			add(casillero);
			pintarCasillero(casillero);
			consola("Jugador posicionado en coordenadas: " + entrada.getX() + "; " + entrada.getY());
			
			*/
			
			setLayout(null); 
			setTitle("Laberinto");
			setSize(800, 500); 
			setLocationRelativeTo(null);
			setVisible(true);
			
			mostrarVecinos("localhost", "9020");
				
				
				
				
			
			
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		
		
		
		
	}
		
	
			
			
	public JTextArea posicionar(JTextArea casillero, Coordenada c) {
		//casillero.setBounds((c.getX()*35)+posx, (c.getY()*35)+posy, cSize, cSize);
		casillero.setBounds(laberinto[c.getX()][c.getY()].getX(), laberinto[c.getX()][c.getY()].getY(), 35, 35);
		
		return casillero;
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

		//borrarLaberinto(laberinto);
		
		ArrayList<Coordenada> coordenadas = new ArrayList<Coordenada>();
		
		devolverPisadas();
		
		Coordenada izquierda = new Coordenada(posicionActual.getX()-1, posicionActual.getY()); 
		Coordenada izquierda1 = new Coordenada(izquierda.getX()-1, izquierda.getY()); 
		Coordenada derecha1 = new Coordenada(posicionActual.getX()+1, posicionActual.getY()); 
		Coordenada derecha2 = new Coordenada(derecha1.getX()+1, derecha1.getY());
		Coordenada arriba = new Coordenada(posicionActual.getX(), posicionActual.getY()-1); 
		Coordenada arriba1 = new Coordenada(arriba.getX()+1, arriba.getY());
		Coordenada arriba2 = new Coordenada(arriba1.getX()+1, arriba1.getY());
		Coordenada abajo = new Coordenada(posicionActual.getX(), posicionActual.getY()+1);
		Coordenada abajo1 = new Coordenada(abajo.getX()+1, abajo.getY());
		Coordenada abajo2 = new Coordenada(abajo1.getX()+1, abajo1.getY());
		
		//consola("Prueba de coordenada: " + abajo.getX() + "; " + abajo.getY());
		coordenadas.add(posicionActual);
		coordenadas.add(izquierda);
		coordenadas.add(izquierda1);
		coordenadas.add(derecha1);
		coordenadas.add(derecha2);
		coordenadas.add(arriba);
		coordenadas.add(arriba1);
		coordenadas.add(abajo);
		coordenadas.add(abajo1);
		coordenadas.add(arriba2);
		coordenadas.add(abajo2);
		
		posicionActual.setKnown(true);
		consola("Posicion Actual: " + posicionActual);
		
		//consola("Prueba de coordenada2: " + abajo.getX() + "; " + abajo.getY());
		
		/*
		JTextArea casillero;
		String letra = "";
		*/
		
		for(Coordenada c : coordenadas) {
			
			if(c.getX() >= 0 && c.getX() <= 9 && c.getY() >= 0 && c.getY() <= 9) {
					
				
						add(posicionar(format(new JTextArea(), devolverLetra("localhost", "9020", c), c), c));
					
						
				
					this.getContentPane().setSize(this.getWidth(), this.getHeight());	
					
			}
			
			
			
			
		}
		
		this.setVisible(true);

		
	}
	/*
	public void borrarLaberinto(JTextArea laberinto [][]) {
		for (int k = 0; k < 10; k++) {
			for (int l = 0; l < 10; l++) {
				consola("Borrando JTextArea: " + laberinto[k][l].getX() + "; " + laberinto[k][l].getY());
				
				
			}
		}
		consola("Se borra laberinto");
	}
	*/
	
	public void devolverPisadas() {

		for (int k = 0; k < 10; k++) {
			for (int l = 0; l < 10; l++) {
				Coordenada c = new Coordenada(k,l);
				if(c.isKnown()) {
					consola("Coordenadas KNOWN: " + c);
				}
			}
		}
	}
	
	public String devolverLetra(String ip, String puerto, Coordenada c) throws NumberFormatException, UnknownHostException, IOException {
		String letra = "";
		Socket cliente = new Socket(ip,Integer.parseInt(puerto));
		
		ObjectOutputStream pedido = new ObjectOutputStream(cliente.getOutputStream());
		
		//consola("Se pide letra de coordenada: " + c.getX() + "; " + c.getY());
		pedido.writeObject(c);
		

		//consola("Esperando letra");
		DataInputStream recibir = new DataInputStream(cliente.getInputStream());
		letra = recibir.readUTF();
			

		cliente.close();
		return letra;
	}
	
	public JTextArea format (JTextArea j, String nombre, Coordenada c) {
		
		j.setText(nombre);
		j.setFont(new Font("Arial", Font.PLAIN, 25));
		j.setEditable(false);
		
		
		if(j.getText().equalsIgnoreCase("C")) {
			j.setBackground(Color.GREEN);
		}
		
		
		if(j.getText().equalsIgnoreCase("P")) {
			j.setBackground(Color.DARK_GRAY);
		}
		
		if(j.getText().equalsIgnoreCase("O")) {
			j.setBackground(Color.YELLOW);
		}
		
		if(j.getText().equalsIgnoreCase("G")) {
			j.setBackground(Color.MAGENTA);
		}
		
		if(posicionActual.getX() == c.getX() && posicionActual.getY() == c.getY()) {
			j.setBackground(Color.RED);
		}
		
		
		
		
		
		
		return j;
	}

	
public void dibujarLaberinto(int size) {
		
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
			//add(laberinto[i][j]);
			
			while (j < size - 1) {
				b += cSize+5;
				j++;
				txtmensajes = new JTextArea();
				txtmensajes.setFont(new Font("Arial", Font.PLAIN, 25));
				
				txtmensajes.setBounds(a, b, cSize, cSize);
				txtmensajes.setEditable(false);
				laberinto[i][j] = txtmensajes;
				//add(laberinto[i][j]);
			}
			b = 50;
			j = 0;
			a += cSize+5;
			i++;
		}
	}

class MyKeyListener implements KeyListener {
	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("keyPressed="+e.getKeyCode());
		
		if(e.getKeyCode() == 39) { // Derecho
			posicionActual.setX(posicionActual.getX()+1);
			//consola("Posicion Actual: " + posicionActual.getX() + "; " + posicionActual.getY());
			
			try {
				mostrarVecinos("localhost", "9020" );
				
				
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		if(e.getKeyCode() == 38) { // Arriba
			posicionActual.setY(posicionActual.getY()-1);
			//consola("Posicion Actual: " + posicionActual.getX() + "; " + posicionActual.getY());
			
			try {
				mostrarVecinos("localhost", "9020");
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		if(e.getKeyCode() == 40) { // Abajo
			posicionActual.setY(posicionActual.getY()+1);
			//consola("Posicion Actual: " + posicionActual.getX() + "; " + posicionActual.getY());
			
			try {
				mostrarVecinos("localhost", "9020");
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		if(e.getKeyCode() == 37) { // Izquierda
			posicionActual.setX(posicionActual.getX()-1);
			//consola("Posicion Actual: " + posicionActual.getX() + "; " + posicionActual.getY());
			
			
			
			try {
				mostrarVecinos("localhost", "9020");
				System.out.println(devolverLetra("localhost", "9020", new Coordenada(-1,-1)));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		System.out.println("keyReleased="+KeyEvent.getKeyText(e.getKeyCode()));
	}
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



