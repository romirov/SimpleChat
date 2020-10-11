/*производим пакетизацию*/
package SimpleChat;
/*подключаем библиотеки*/
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
/**
 * простой чат клиент для приема и отправки сообщений
 * @param incoming - текстовая область для отображения входящих сообщений
 * @param outgoing - текстовая область для отображения исходящих сообщений
 * @param reader - буфер для чтения сообщений из входящего потока данных
 * @param write - буфер для записи сообщений в исходящий поток данных
 * @param sock - сокет для соединения с сервером
 */
public class SimpleChatClient{
  JTextArea incoming;
  JTextField outgoing;
  BufferedReader reader;
  PrintWriter writer;
  Socket sock;
  public static void main(String[] args){
    SimpleChatClient client = new SimpleChatClient();
    client.go();
  }
  public void go(){
    JFrame frame = new JFrame("Simple Chat Client");
    JPanel mainPanel = new JPanel();
    incoming = new JTextArea(15, 50);
    incoming.setLineWrap(true);
    incoming.setWrapStyleWord(true);
    incoming.setEditable(false);
    JScrollPane qScroller = new JScrollPane(incoming);
    qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    outgoing = new JTextField(20);
    JButton sendButton = new JButton("Send");
    sendButton.addActionListener(new SendButtonListener());
    mainPanel.add(qScroller);
    mainPanel.add(outgoing);
    mainPanel.add(sendButton);

    setUpNetworking();
    /*Запускаем новый поток, используя вложенный класс в качестве Runnable
     * Работа потока заключается в чтении данных с сервера через сокет и выводе любых
     * входящих сообщений в прокручиваемую текстовую область */
    Thread readerThread = new Thread(new IncomingReader());
    readerThread.start();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
    frame.setSize(400, 500);
    frame.setVisible(true);
  }
  /*Используем сокет для получения входящего и исходящего потоков*/
  private void setUpNetworking(){
    try{
      sock = new Socket("192.168.88.2", 5000);
      InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
      reader = new BufferedReader(streamReader);
      writer = new PrintWriter(sock.getOutputStream());
      System.out.println("Networking Established");
    }catch(IOException ex){
      ex.printStackTrace();
    }
  }
/*пользователь  нажимает кнопку Send и содержимое текстового поля отправляется на сервер*/
  class SendButtonListener implements ActionListener{
    public void actionPerformed(ActionEvent ev){
      try{
        writer.println(outgoing.getText());
        writer.flush();
      }catch(Exception ex){
        ex.printStackTrace();
      }
      outgoing.setText("");
      outgoing.requestFocus();
    }
  }
/*Работа, которую выполняет поток
 * В методе run() поток входит в цикл(пока ответ сервера null),
 * считывает за раз одну строку и добавляет ее в прокручиваемую текстовую область
 */
  class IncomingReader implements Runnable{
    public void run(){
      String message;
      try{
        while((message = reader.readLine()) != null){
          System.out.println("read " + message);
          incoming.append(message + "\n");
        }
      }catch(Exception ex){
        ex.printStackTrace();
      }
    }
  }
}
