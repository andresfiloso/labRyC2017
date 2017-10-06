package modelo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AdmServidor {

	public static void main(String[] args) {
		VentanaAdmin ventanaAdmin = new VentanaAdmin();
		ventanaAdmin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
	
}

	class VentanaAdmin extends JFrame implements ActionListener {

		JButton btnCerrar;

		public VentanaAdmin() {
			JLabel titulo = new JLabel("Administrador del servidor");
			titulo.setBounds(10, 10, 200, 30);
			add(titulo);

			btnCerrar = new JButton();
			btnCerrar.setText("Cerrar Servidor");
			btnCerrar.setBounds(10, 50, 150, 20);
			btnCerrar.addActionListener(this); // para poder programar en el boton
			add(btnCerrar);
			
			setLayout(null); // para que los controles no esten uno encima del otro
			setTitle("Administrador del servidor");
			setSize(400, 400); // tamaño del layout
			setLocationRelativeTo(null);
			setVisible(true);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnCerrar) {
				try {

					Socket admin = new Socket("localhost", 9020);
					
					DataOutputStream out = new DataOutputStream(admin.getOutputStream());

					out.writeUTF("Cerrar");

					admin.close();

				} catch (Exception ex) {
					System.out.println("Error en cliente " + ex.getMessage());
				}
			}

		}

	}

