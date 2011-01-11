package org.alma.corba.vue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.alma.corba.PeerImpl;
import org.alma.corba.modele.MessageComponent;
import org.omg.CORBA.ORB;

import MTalk.Talk;
import MTalk.TalkHelper;

public class SwingClient extends JFrame {
	private static final long serialVersionUID = 1L;
	private final JTextArea textArea;
	private final MessageComponent conv;
	private String correspondantName;
	private String sontTalkIor;
	private Short numConvSideA;

	public SwingClient(MessageComponent conv, String sontTalkIor,
			short numConvSideA) {
		setTitle("Talk : ");
		this.conv = conv;
		this.sontTalkIor = sontTalkIor;
		this.numConvSideA = numConvSideA;
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout(0, 0));
		this.setSize(new Dimension(800, 600));

		// Top
		JPanel topPanel = new JPanel();
		topPanel.setBackground(new Color(115, 166, 255));
		FlowLayout flowLayout = (FlowLayout) topPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		flowLayout.setVgap(7);
		JLabel labelNickname = new JLabel();
		labelNickname.setFont(new Font("Tahoma", Font.BOLD, 13));
		labelNickname.setForeground(Color.WHITE);
		labelNickname.setText("Talk");
		topPanel.add(labelNickname);

		this.getContentPane().add(topPanel, BorderLayout.NORTH);
		this.getContentPane().add(conv, BorderLayout.CENTER);

		// Bottom
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

		textArea = new JTextArea();
		textArea.setColumns(1);
		textArea.setRows(1);
		textArea.setAutoscrolls(true);
		bottomPanel.add(textArea);
		textArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == 10) {// EnterKey
					sendMessage(textArea.getText());
				}
			}
		});

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sendMessage(textArea.getText());
			}
		});
		bottomPanel.add(sendButton);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

	}

	private void sendMessage(String message) {
		if (!message.equals("")) {
			conv.addMessage("moi", message);
			textArea.setText("");

			final ORB orb = PeerImpl.orb;

			org.omg.CORBA.Object obj = orb.string_to_object(sontTalkIor);
			Talk talkDistant = TalkHelper.narrow(obj);

			talkDistant.talk(numConvSideA, message);

		}
	}
}
