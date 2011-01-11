package org.alma.corba;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.alma.corba.modele.MessageComponent;
import org.alma.corba.vue.SwingClient;

import MTalk.TalkOperations;

public class TalkImpl implements TalkOperations {

	private String mUserName;
	private MessageComponent mMessageComp;

	public TalkImpl(String userName, MessageComponent conv) {
		this.mUserName = userName;
		this.mMessageComp = conv;
	}

	@Override
	public void accept(short numConvSideB, final short numConvSideA,
			final String talkIor) {
		JOptionPane.showMessageDialog(null, "Nouvelle conversation avec "
				+ mUserName);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingClient(mMessageComp, talkIor, numConvSideA).setVisible(true);
			}
		});
	}

	@Override
	public void deny(short numConvSideA) {
		JOptionPane.showMessageDialog(null, "Conversation refusée avec "
				+ mUserName);
	}

	@Override
	public void talk(short numConvYourSide, String message) {
		// on ecrit sur le textarea
		mMessageComp.addMessage(mUserName, message);
	}

	@Override
	public void stop(short numConvYourSide) {
		mMessageComp.addMessage(mUserName, "à fermé la fenêtre de conversation.");
	}

}
