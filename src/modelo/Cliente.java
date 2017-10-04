package modelo;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

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

		ip = new JTextField();
		ip.setBounds(70, 50, 100, 20);
		add(ip);

		JLabel puertoLabel = new JLabel("Puerto: ");
		puertoLabel.setBounds(10, 80, 50, 20);
		add(puertoLabel);

		puerto = new JTextField();
		puerto.setBounds(70, 80, 100, 20);
		add(puerto);

		JLabel userLabel = new JLabel("Usuario: ");
		userLabel.setBounds(10, 110, 50, 20);
		add(userLabel);

		user = new JTextField();
		user.setBounds(70, 110, 100, 20);
		add(user);

		JLabel passLabel = new JLabel("Clave: ");
		passLabel.setBounds(10, 140, 50, 20);
		add(passLabel);

		password = new JTextField();
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
		setSize(400, 400); // tamaño del layout
		setLocationRelativeTo(null);
		setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnLogin) {
			try {

				int matrizSize = 0;
				boolean esperando = true;

				Login login = new Login(ip.getText(), puerto.getText(), user.getText(), password.getText());

				Socket cliente = new Socket("127.0.0.1", Integer.parseInt(puerto.getText()));

				// DataOutputStream flujo = new DataOutputStream(cliente.getOutputStream());

				ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream());

				out.writeObject(login);

				while (esperando == true) {
					DataInputStream entrada = new DataInputStream(cliente.getInputStream());
					matrizSize = entrada.readInt();
					respuesta.setText(Integer.toString(matrizSize));

					if (matrizSize > 0) {
						esperando = false;
					}
				}

				setVisible(false);

				VentanaCliente ventanaCliente = new VentanaCliente(matrizSize);
				ventanaCliente.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				cliente.close();

			} catch (Exception ex) {
				System.out.println("Error en cliente " + ex.getMessage());
			}
		}

	}

}

class VentanaCliente extends JFrame implements Runnable {

	public VentanaCliente(int size) {

	}
	public void dibujarLaberinto(int size) {

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

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
