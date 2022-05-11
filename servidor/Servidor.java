package servidor;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import bean.FileMessage;

public class Servidor {
	private ServerSocket serverSocket;
	private Socket socket;
	private Map<String, ObjectOutputStream> streamMap = new HashMap<String, ObjectOutputStream>();

	public Servidor() {
		try {
			serverSocket = new ServerSocket(5555);
			System.out.println("Servidor on!");
			
			while (true) { //SERVIDOR FICAR SEMPRE FUNCIONADO QUANDO FOR CONECTADO
				socket = serverSocket.accept();
				
				new Thread(new ListenerSocket(socket)).start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}
	
	private class ListenerSocket implements Runnable {
		private ObjectOutputStream outputStream;
	    private ObjectInputStream inputStream;
		
	    public ListenerSocket(Socket socket) throws IOException {
	    	this.outputStream = new ObjectOutputStream(socket.getOutputStream());
	    	this.inputStream = new ObjectInputStream(socket.getInputStream());
			
		}

		public void run() {
			FileMessage message = null; //variavel local//
			try {
				while ((message = (FileMessage) inputStream.readObject()) !=null) { //enquanto for diferente de null receberemos uma mensagem
					streamMap.put(message.getCliente(), outputStream);
					if (message.getFile() != null) { //se for null apenas conexão foi feita, se for diferente é pq a mensagem esta sendo enviada 
						for (Map.Entry<String, ObjectOutputStream> kv : streamMap.entrySet()) {
							if (!message.getCliente().equals(kv.getKey())) { //teste para saber se o cliente que enviou tambem vai receber a mensagem. caso a chave for igual a de quem enviou, a mensagem nao será recebida. 
								kv.getValue().writeObject(message); //essa linha serve para enviar a mensagem, "representa o outputstream
							}
						}
					}
				}
			}catch (IOException e) {
				streamMap.remove(message.getCliente()); //remove o cliente que desconectou a partir do nome do cliente
			    System.out.println(message.getCliente() + "desconectou!");
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
				
			}
			}
				
		}
	
	public static void main(String[] args) {
		new Servidor();
	}
	}


