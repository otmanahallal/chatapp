package chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ClientView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField clientTypingBoard;
	private JList clientActiveUsersList;
	private JTextArea clientMessageBoard;
	private JButton clientKillProcessBtn;
	private JRadioButton oneToNRadioBtn;
	private JRadioButton broadcastBtn;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, clientIds = "";

	public ClientView() {
		initialize();
	}

	public ClientView(String id, Socket s) {
		initialize();
		this.id = id;
		try {
			frame.setTitle("Client View - " + id);
			dm = new DefaultListModel<String>();
			clientActiveUsersList.setModel(dm);
			inputStream = new DataInputStream(s.getInputStream());
			outStream = new DataOutputStream(s.getOutputStream());
			new Read().start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	class Read extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					String m = inputStream.readUTF();
					//m = AES.decrypt(m);
					System.out.println("inside read thread : " + m);
					if (m.contains(":;.,/=")) {
						m = m.substring(6);
						dm.clear();
						StringTokenizer st = new StringTokenizer(m, ",");
						while (st.hasMoreTokens()) {
							String u = st.nextToken();
							if (!id.equals(u))
								dm.addElement(u);
						}
					} else {
						clientMessageBoard.append("" + m + "\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 926, 705);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client View");

		clientMessageBoard = new JTextArea();
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(12, 25, 530, 495);
		frame.getContentPane().add(clientMessageBoard);

		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(12, 533, 530, 84);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);

		JButton clientSendMsgBtn = new JButton("Send");
		clientSendMsgBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String textAreaMessage = clientTypingBoard.getText();
				if (textAreaMessage != null && !textAreaMessage.isEmpty()) {
					try {
						String messageToBeSentToServer = "";
						String cast = "broadcast";
						int flag = 0;
						if (oneToNRadioBtn.isSelected()) {
							cast = "multicast";
							List<String> clientList = clientActiveUsersList.getSelectedValuesList();
							if (clientList.size() == 0)
								flag = 1;
							for (String selectedUsr : clientList) {
								if (clientIds.isEmpty())
									clientIds += selectedUsr;
								else
									clientIds += "," + selectedUsr;
							}
							messageToBeSentToServer = cast + ":" + clientIds + ":" + textAreaMessage;
						} else {
							messageToBeSentToServer = cast + ":" + textAreaMessage;
						}
						//messageToBeSentToServer = AES.encrypt(messageToBeSentToServer);
						if (cast.equalsIgnoreCase("multicast")) {
							if (flag == 1) {
								JOptionPane.showMessageDialog(frame, "No user selected");
							} else {
								outStream.writeUTF(messageToBeSentToServer);
								clientTypingBoard.setText("");
								clientMessageBoard.append("< You sent msg to " + clientIds + ">" + textAreaMessage + "\n");
							}
						} else {
							outStream.writeUTF(messageToBeSentToServer);
							clientTypingBoard.setText("");
							clientMessageBoard.append("< You sent msg to All >" + textAreaMessage + "\n");
						}
						clientIds = "";
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "User does not exist anymore.");
					}
				}
			}
		});
		clientSendMsgBtn.setBounds(554, 533, 137, 84);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(554, 63, 327, 457);
		frame.getContentPane().add(clientActiveUsersList);

		clientKillProcessBtn = new JButton("Kill Process");
		clientKillProcessBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					outStream.writeUTF("exit");
					clientMessageBoard.append("You are disconnected now.\n");
					frame.dispose();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		clientKillProcessBtn.setBounds(703, 533, 193, 84);
		frame.getContentPane().add(clientKillProcessBtn);

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(559, 43, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		oneToNRadioBtn = new JRadioButton("1 to N");
		oneToNRadioBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(true);
			}
		});
		oneToNRadioBtn.setSelected(true);
		oneToNRadioBtn.setBounds(682, 24, 72, 25);
		frame.getContentPane().add(oneToNRadioBtn);

		broadcastBtn = new JRadioButton("Broadcast");
		broadcastBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(false);
			}
		});
		broadcastBtn.setBounds(774, 24, 107, 25);
		frame.getContentPane().add(broadcastBtn);

		ButtonGroup btngrp = new ButtonGroup();
		btngrp.add(oneToNRadioBtn);
		btngrp.add(broadcastBtn);

		frame.setVisible(true);
	}
}
