package cliente;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JFileChooser;

import bean.FileMessage;

public class Cliente {

	
	private Socket socket; //variavel
	private ObjectOutputStream outputStream; //variavel
	
	public Cliente() throws IOException {
		this.socket = new Socket("localhost", 5555); //ip da maquina e a porta do servidor
		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
		
		new Thread(new ListenerSocket(socket)).start();
		
		menu(); //metodo. é chamado apos a thread ser iniciada. 
	}
		
	private void menu() throws IOException { //usado pra enviar mensagens; essa excessão é pq apenas a mensagem sera enviada e não o arquivo
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Digite seu nome: "); //aqui sera colocado o nome do cliente.
		
		String nome = scanner.nextLine();
		
		this.outputStream.writeObject(new FileMessage(nome)); //objeto do tipo FileMessage passando o nome do cliente e assim garantindo que ele esta se conectando
		
		int option = 0;
		
		while (option != 1) { //enquanto o option for difente de - 1, se mantem o processo dentro de while.
			System.out.print("1 - sair | 2 - enviar : ");
			option = scanner.nextInt();
			if (option == 2) { //se option for igual a 2, a mensagem esta sendo enviada
				send(nome); // "metodo" send e o nome do cliente como parametro
				
			}else if (option == 1) { //caso seja igual a 1, é forçado o encerramento da aplicação
				System.exit(0); // usando o system exit é forçado a saida
			}
		}
	}

	private void send(String nome) throws IOException { //metodo send
			//FileMessage fileMessage = new FileMessage(); // fileMessage É UMA VARIAVEL
			
			JFileChooser fileChooser = new JFileChooser(); //usaremos o "metodo" JfileChooser para selecionar o arquivo que iremos enviar.
			
			int opt = fileChooser.showOpenDialog(null); // com o metodo showOpenDialog abriremos a janela pro FileChooser e teremos o retorno se o cliente clicou em confirmar ou cancelar o envio do arquivo
		
			if (opt == JFileChooser.APPROVE_OPTION)	{ // se FileChosser for igual a approve option é pq ele selecionou o arquivo.
				File file = fileChooser.getSelectedFile(); //objeto do tipo File sera a varialvel que vai armazenar o arquivo selecionado. e recuperamos o arquivo com o metodo getselected do filechooser.
				
				this.outputStream.writeObject(new FileMessage(nome, file)); //	COMO O OBJETO FOI DECLARADO AQUI DENTRO, A VARIAVEL fileMessage na "linha 52" não sera necessario	
				
			}
	
	}

	private class ListenerSocket implements Runnable {
		
		private ObjectInputStream inputStream; //classe do ouvinte 
		//o objeto da linha 70 sera iniciada no construtor da linha 74
		
		public ListenerSocket(Socket socket) throws IOException {
		     this.inputStream = new	ObjectInputStream(socket.getInputStream()); // iniciado com parametro socket e com o metodo getInputStream.
		}
		
		public void run() { //usado pra receber mensagens
			FileMessage message = null; //variavel local
		
				try {
					while ((message = (FileMessage) inputStream.readObject()) !=null) { //enquanto for diferente de null receberemos uma mensagem
						System.out.println("\nVocê recebeu um arquivo de " + message.getCliente());
						System.out.println("O arquivo é " + message.getFile().getName());
						
						//imprime(message); //o metodo "imprime" mostra no console o conteudo do arquivo
					
						salvar(message); // metodo salvar com message como parametro
						
						System.out.print("1 - sair | 2 - enviar : ");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
			}
		
	      }

		private void salvar(FileMessage message) {
			try {
				
				Thread.sleep(new Random().nextInt(1000)); //para garantir que haja o tempo diferente para cada transferencia mesmo que  a diferença seja de milisegundos. 
				
				long time = System.currentTimeMillis(); //vai me dar o horario que o arquivo foi enviada, podendo enviar arquivos repetidos caso o horario seja diferente.
				
				FileInputStream fileInputStream = new FileInputStream(message.getFile()); //variavel de entrada do tipo fileinputstram e recebe como parametro construtor o arquivo da message
				FileOutputStream fileOutputStream = new FileOutputStream("c:\\ivoarquivos\\" + time + message.getFile().getName()); // o diretorio "z" sera onde os arquivos recebidos serão salvos!
			
				FileChannel fin = fileInputStream.getChannel(); //essas classes são do pacote java.io serve como complemento
				FileChannel fout = fileOutputStream.getChannel();
				
				long size = fin.size(); // size para pegar o tamanho do arquivo 
				
				fin.transferTo(0, size, fout); //0 é o parametro, size é o tamanho e fout é o arquivo que vc quer salvar.
			} catch (FileNotFoundException e) {
			
				e.printStackTrace();
			} catch (IOException e) {
			
				e.printStackTrace();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
		}

		private void imprime(FileMessage message) {   //metodo adicionado passando nossa mensagem como parametro.
			try {
				FileReader fileReader = new FileReader(message.getFile()); // criado Objeto filereader para começar a leitura do arquivo	
				BufferedReader bufferedReader = new BufferedReader(fileReader); //
				String linha; //varaiavel tipo string chamada linha
				while ((linha = bufferedReader.readLine()) !=null); { //criar um while para ler o nosso o bufferedReader e adiciona na variavel linha o conteudo de cada linha lida no arquivo 
					System.out.println(linha);
				}
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			} 
		}
       }
	
	public static void main(String[] args) { //metodo main para testar a aplicação
		try {
			new Cliente();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	}		
