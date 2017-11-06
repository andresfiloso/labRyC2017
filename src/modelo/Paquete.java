package modelo;

import java.io.Serializable;
import java.util.List;

class Paquete implements Serializable {
	private String command;
	private List<String> args;

	public Paquete(String command, List<String> args) {
		super();
		this.command = command;
		this.args = args;
	}
	
	public Paquete(){}
	
	public Paquete(String command) {
		super();
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}
}
