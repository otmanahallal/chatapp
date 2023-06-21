package chat;

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class ChatServer {

	private static final long serialVersionUID = 1L;
	private static Map<String, Socket> allUsersList = new ConcurrentHashMap<>();
	private static Set<String> activeUserSet = new HashSet<>();
	private static int port = 8818;
	private JFrame frame;
	private ServerSocket serverSocket;
	private JTextArea serverMessageBoard;
	private JList allUserNameList;
	private JList activeClientList;
	private DefaultListModel<String> activeDlm = new DefaultListModel<String>();
	private DefaultListModel<String> allDlm = new DefaultListModel<String>();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatServer window = new ChatServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ChatServer() {
		initialize();
		try {
			serverSocket = new ServerSocket(port);
			serverMessageBoard.append("Server started on port: " + port + "\n");
			serverMessageBoard.append("Waiting for the clients...\n");
			new ClientAccept().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ClientAccept extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					String uName = new DataInputStream(clientSocket.getInputStream()).readUTF();
					DataOutputStream cOutStream = new DataOutputStream(clientSocket.getOutputStream());
					if (activeUserSet != null && activeUserSet.contains(uName)) {
						cOutStream.writeUTF("Username already taken");
					} else {
						allUsersList.put(uName, clientSocket);
						activeUserSet.add(uName);
						cOutStream.writeUTF("");
						activeDlm.addElement(uName);
						if (!allDlm.contains(uName))
							allDlm.addElement(uName);
						activeClientList.setModel(activeDlm);
						allUserNameList.setModel(allDlm);
						serverMessageBoard.append("Client " + uName + " Connected...\n");
						new MsgRead(clientSocket, uName).start();
						new PrepareCLientList().start();
					}
				} catch (IOException ioex) {
					ioex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MsgRead extends Thread {
		Socket s;
		String Id;

		private MsgRead(Socket s, String uname) {
			this.s = s;
			this.Id = uname;
		}

		@Override
		public void run() {
			while (allUserNameList != null && !allUsersList.isEmpty()) {
				try {
					String message = new DataInputStream(s.getInputStream()).readUTF();
					System.out.println("message read ==> " + message);
					String[] msgList = message.split(":");
					if (msgList[0].equalsIgnoreCase("multicast")) {
						String[] sendToList = msgList[1].split(",");
						for (String usr : sendToList) {
							try {
								if (activeUserSet.contains(usr)) {
									new DataOutputStream(((Socket) allUsersList.get(usr)).getOutputStream())
											.writeUTF("< " + Id + " >" + msgList[2]);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else if (msgList[0].equalsIgnoreCase("broadcast")) {
						Iterator<String> itr1 = allUsersList.keySet().iterator();
						while (itr1.hasNext()) {
							String usrName = (String) itr1.next();
							if (!usrName.equalsIgnoreCase(Id)) {
								try {
									if (activeUserSet.contains(usrName)) {
										new DataOutputStream(((Socket) allUsersList.get(usrName)).getOutputStream())
												.writeUTF("< " + Id + " >" + msgList[1]);
									} else {
										new DataOutputStream(s.getOutputStream())
												.writeUTF("Message couldn't be delivered to user " + usrName + " because it is disconnected.\n");
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					} else if (msgList[0].equalsIgnoreCase("exit")) {
						activeUserSet.remove(Id);
						serverMessageBoard.append(Id + " disconnected....\n");

						new PrepareCLientList().start();

						Iterator<String> itr = activeUserSet.iterator();
						while (itr.hasNext()) {
							String usrName2 = (String) itr.next();
							if (!usrName2.equalsIgnoreCase(Id)) {
								try {
									new DataOutputStream(((Socket) allUsersList.get(usrName2)).getOutputStream())
											.writeUTF(Id + " disconnected...");
								} catch (Exception e) {
									e.printStackTrace();
								}
								new PrepareCLientList().start();
							}
						}
						activeDlm.removeElement(Id);
						activeClientList.setModel(activeDlm);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class PrepareCLientList extends Thread {
		@Override
		public void run() {
			try {
				String ids = "";
				Iterator itr = activeUserSet.iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					ids += key + ",";
				}
				if (ids.length() != 0) {
					ids = ids.substring(0, ids.length() - 1);
				}
				itr = activeUserSet.iterator();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					try {
						new DataOutputStream(((Socket) allUsersList.get(key)).getOutputStream())
								.writeUTF(":;.,/=" + ids);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 796, 530);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Server View");

		serverMessageBoard = new JTextArea();
		serverMessageBoard.setEditable(false);
		serverMessageBoard.setBounds(12, 29, 489, 435);
		frame.getContentPane().add(serverMessageBoard);
		serverMessageBoard.setText("Starting the Server...\n");

		allUserNameList = new JList();
		allUserNameList.setBounds(526, 324, 218, 140);
		frame.getContentPane().add(allUserNameList);

		activeClientList = new JList();
		activeClientList.setBounds(526, 78, 218, 156);
		frame.getContentPane().add(activeClientList);

		JLabel lblNewLabel = new JLabel("All Usernames");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(530, 295, 127, 16);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Active Users");
		lblNewLabel_1.setBounds(526, 53, 98, 23);
		frame.getContentPane().add(lblNewLabel_1);
	}
}
