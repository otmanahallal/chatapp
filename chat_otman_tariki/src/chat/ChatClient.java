package chat;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ChatClient extends JFrame {

    private JPanel contentPane;
    private JTextField clientUserName;
    private int port = 8818;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ChatClient window = new ChatClient();
                    window.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ChatClient() {
        setTitle("Client Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 200);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 17));
        lblUsername.setBounds(34, 34, 94, 23);
        contentPane.add(lblUsername);

        clientUserName = new JTextField();
        clientUserName.setFont(new Font("Tahoma", Font.PLAIN, 17));
        clientUserName.setBounds(138, 31, 230, 30);
        contentPane.add(clientUserName);
        clientUserName.setColumns(10);

        JButton btnConnect = new JButton("Connect");
        btnConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String id = clientUserName.getText();
                    Socket socket = new Socket("localhost", port);
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF(id);

                    String msgFromServer = new DataInputStream(socket.getInputStream()).readUTF();
                    if (msgFromServer.equals("Username already taken")) {
                        JOptionPane.showMessageDialog(contentPane, "Username already taken", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        new ClientView(id, socket);
                        dispose();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 17));
        btnConnect.setBounds(138, 84, 120, 30);
        contentPane.add(btnConnect);
    }
}