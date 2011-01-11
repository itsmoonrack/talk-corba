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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.alma.corba.modele.MessageComponent;

import MTalk.TalkOperations;

public class SwingClient extends JFrame {
	private static final long serialVersionUID = 1L;
	private final JTextArea mChatArea;
	private final MessageComponent mConversation;
	private TalkOperations mTalkDistant;
	private Short mConversationNumberDistant;

	public SwingClient(MessageComponent messageComp, TalkOperations talkDistant,
			short numConvSideA) {
		setTitle("Talk : ");
		this.mConversation = messageComp;
		this.mTalkDistant = talkDistant;
		this.mConversationNumberDistant = numConvSideA;
		
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
		this.getContentPane().add(messageComp, BorderLayout.CENTER);

		// Bottom
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

		mChatArea = new JTextArea();
		mChatArea.setColumns(1);
		mChatArea.setRows(1);
		mChatArea.setAutoscrolls(true);
		bottomPanel.add(mChatArea);
		mChatArea.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == 10) {// EnterKey
					sendMessage(mChatArea.getText());
				}
			}
		});

		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sendMessage(mChatArea.getText());
			}
		});
		bottomPanel.add(sendButton);
		this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		addWindowListener(new SwingClientWindowListener());
	}

	private void sendMessage(String message) {
		if (!message.equals("")) {
			mConversation.addMessage("Moi", message);
			mChatArea.setText("");

			mTalkDistant.talk(mConversationNumberDistant, message);
		}
	}
	
	private class SwingClientWindowListener implements WindowListener {

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			mTalkDistant.stop(mConversationNumberDistant);
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
	}
}
