package modelo;

import java.io.Serializable;

class Coordenada implements Serializable {
	private int x;
	private int y;
	private boolean known;
	private String letra;

	public Coordenada(int x, int y) {
		this.x = x;
		this.y = y;
		this.known = false;
		this.letra = "";

	}

	public Coordenada(int x, int y, boolean known) {
		this.x = x;
		this.y = y;
		this.known = known;
		this.letra = "";
	}

	public Coordenada() {
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isKnown() {
		return known;
	}

	public void setKnown(boolean known) {
		this.known = known;
	}

	public String getLetra() {
		return letra;
	}

	public void setLetra(String letra) {
		this.letra = letra;
	}

	@Override
	public String toString() {
		return x + " ;" + y + ", known=" + known;
	}

	public boolean equals(Coordenada c) {
		boolean respuesta = false;
		if (this.getX() == c.getX() && this.getY() == c.getY()) {
			respuesta = true;
		} else {
			respuesta = false;
		}
		return respuesta;
	}

}