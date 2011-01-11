package org.alma.corba;

import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.alma.corba.modele.MessageComponent;
import org.alma.corba.vue.SwingClient;

import MTalk.TalkOperations;

public class TalkImpl implements TalkOperations {

	private String userName;
	private MessageComponent conv;

	public TalkImpl(String userName, PeerImpl peer, MessageComponent conv ) {
		this.userName = userName;
		this.conv = conv;
	}

	@Override
	public void accept(short numConvSideB, final short numConvSideA, final String talkIor) {
		JOptionPane.showMessageDialog(null,"Nouvelle conversation avec " + userName );

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingClient(conv,talkIor,numConvSideA).setVisible(true);
			}
		});
	}

	@Override
	public void deny(short numConvSideA) {
		JOptionPane.showMessageDialog(null,"Conversation refusée avec " + userName );
	}

	@Override
	public void talk(short numConvYourSide, String message) {
		//on ecrit sur le textarea
		conv.addMessage(userName, message);
	}

	@Override
	public void stop(short numConvYourSide) {
		JOptionPane.showInputDialog("Conversation refusée avec " + userName );

	}

}
