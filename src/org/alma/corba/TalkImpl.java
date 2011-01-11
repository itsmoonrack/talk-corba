package org.alma.corba;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.alma.corba.modele.MessageComponent;
import org.alma.corba.vue.SwingClient;
import org.omg.CORBA.ORB;

import MTalk.TalkHelper;
import MTalk.TalkOperations;

public class TalkImpl implements TalkOperations {

	private ORB mORB;
	private String mUserName;
	private Map<Short, MessageComponent> mMessageComp;

	// ORB, My UserName
	public TalkImpl(ORB orb, String username) {
		this.mORB = orb;
		this.mUserName = username;
		this.mMessageComp = new HashMap<Short, MessageComponent>();
	}

	@Override
	public void accept(short numConvSideB, final short numConvSideA,
			final String talkIor) {
		JOptionPane.showMessageDialog(null, "Nouvelle conversation avec "
				+ mUserName);
		// Création d'une conversation de numéro B.
		final MessageComponent messageComp = new MessageComponent();
		mMessageComp.put(numConvSideB, messageComp);

		org.omg.CORBA.Object obj = mORB.string_to_object(talkIor);
		final MTalk.Talk talkDistant = TalkHelper.narrow(obj);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SwingClient(messageComp, talkDistant, numConvSideA)
						.setVisible(true);
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
		mMessageComp.get(numConvYourSide).addMessage(mUserName, message);
	}

	@Override
	public void stop(short numConvYourSide) {
		mMessageComp.get(numConvYourSide).addMessage(mUserName,
				"à fermé la fenêtre de conversation.");
	}

}
