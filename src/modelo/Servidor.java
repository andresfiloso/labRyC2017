package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class Servidor extends JFrame implements Runnable{

	JTextArea txtMensajes;


	String authUser = "admin";
	String authPass = "admin";

	int puerto = 9018;
	
	int matrizSize = 10;

	public Servidor() {
		
		
		txtMensajes = new JTextArea();
		txtMensajes.setBounds(10, 10, 450, 20);
		txtMensajes.setEditable(false);
		add(txtMensajes);

		setLayout(null);
		setTitle("Servidor");
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
			JTextArea[][] laberinto = new JTextArea[matrizSize][matrizSize];
			
			ServerSocket servidor = new ServerSocket(puerto);
			Socket cliente; // socket para guardar las llamadas
			
			

			System.out.println("Servidor iniciado en puerto: " + puerto);
			boolean proceso = false;
			boolean juego = false;
			escribirlog("Servidor iniciado en puerto: " + puerto);
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
					escribirlog("\nBienvenido: " + login.getUser());
					escribirlog("\nCerrando login y creando mapa de juego...");
					escribirlog("\nTamaño del laberinto: " + matrizSize);
					
					laberinto = dibujarLaberinto(matrizSize);
					Coordenada entradaXY = coordenadasEntrada(laberinto);
					
					ObjectOutputStream inicio = new ObjectOutputStream(cliente.getOutputStream());
					inicio.writeObject(entradaXY);
					escribirlog("\nEnviando coordenadas de entrada");
					juego = true; // arranca el juego
					
					String valor = "";
					
					DataOutputStream devolver = new DataOutputStream(cliente.getOutputStream());
					
					Coordenada aux;
					
					while (juego == true) {
						escribirlog("\nEsperando peticion de casillero...");
						aux = (Coordenada) in.readObject();
						escribirlog("\nSe recibio coordenada: " + aux.getX() + "; " + aux.getY());
		
								
						valor = devolverValor(aux, laberinto);
						escribirlog("\nSe devuelve valor: " + valor);
						
						
						if(!valor.equals("")) {
							devolver.writeUTF(valor);
							System.out.println("Se devuelve letra: " +  valor);
							valor = "";
						}
							
					}
					
					
					
				} else {
					servidor.close();
					escribirlog("Acesso denegado.. Servidor cerrado");
					proceso = true;
				}
			}

			cliente.close();
			servidor.close();
			System.out.println("Servidor cerrado");


		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
	
	public void escribirlog(String mensaje) {
		txtMensajes.setBounds(10, 10, txtMensajes.getWidth(), txtMensajes.getHeight()+15);
		txtMensajes.append(mensaje);
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
	
	public String devolverValor(Coordenada c, JTextArea[][] array){
		
		String valor = "";
		
		for (int k = 0; k < matrizSize; k++) {
			for (int l = 0; l < matrizSize; l++) {
				if(c.getX() == k && c.getY() == l) {
					valor = array[k][l].getText();
				}
			}
		}
		return valor;
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

		int a = 70, b = txtMensajes.getHeight()+50;
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
			b = txtMensajes.getHeight()+50;
			j = 0;
			a += cSize+5;
			i++;
		}

		for (int k = 0; k < size; k++) {
			for (int l = 0; l < size; l++) {
				array[k][l].setBackground(Color.LIGHT_GRAY);
				
			}
		}

		setSize(500, 550); // tamaño del layout
		
		return array;
	}


}


class Coordenada implements Serializable{
    private int x;
    private int y;

    public Coordenada(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Coordenada() {}
    
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
