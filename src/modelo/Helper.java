package modelo;

import java.util.ArrayList;
import java.util.List;

public class Helper {

	public Helper() {
	}

	public Paquete encriptarPaquete(Paquete paquete) {

		List<String> encripts = new ArrayList<String>();
		for (String s : paquete.getArgs()) {
			s = encriptarString(s);
			encripts.add(s);
		}

		paquete.setCommand(encriptarString(paquete.getCommand()));

		paquete.setArgs(encripts);
		return paquete;
	}

	public Paquete desencriptarPaquete(Paquete paquete) {

		List<String> encripts = new ArrayList<String>();
		for (String s : paquete.getArgs()) {
			s = desencriptarString(s);
			encripts.add(s);
		}

		paquete.setCommand(desencriptarString(paquete.getCommand()));

		paquete.setArgs(encripts);
		return paquete;
	}

	public String encriptarString(String mensaje) {

		char array[] = mensaje.toCharArray();

		for (int i = 0; i < array.length; i++) {
			array[i] = (char) (array[i] + (char) 5);
		}
		String encriptado = String.valueOf(array);
		return encriptado;
	}

	public String desencriptarString(String mensaje) {
		char arrayD[] = mensaje.toCharArray();
		for (int i = 0; i < arrayD.length; i++) {
			arrayD[i] = (char) (arrayD[i] - (char) 5);
		}
		String desencriptado = String.valueOf(arrayD);
		return desencriptado;
	}

}
