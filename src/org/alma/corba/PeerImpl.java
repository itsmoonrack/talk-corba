package org.alma.corba;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.alma.corba.modele.MessageComponent;
import org.alma.corba.vue.SwingClient;
import org.omg.CORBA.ORB;

import MTalk.PeerOperations;
import MTalk.Talk;
import MTalk.TalkHelper;
import MTalk.TalkPOATie;

public class PeerImpl implements PeerOperations {

	private String mUserName = "";
	public static ORB sORB;

	private Map<String, Conversation> mConversationMap;
	private short mConversationNumber = 0;

	public PeerImpl(String pseudo, ORB myORB) {
		this.mUserName = pseudo;
		this.mConversationMap = new HashMap<String, Conversation>();
		sORB = myORB;
	}

	public Map<String, Conversation> getMapConv() {
		return mConversationMap;
	}

	// Tue la conversation numConv
	public void killConv(String correspondant) {

		Conversation conv = mConversationMap.get(correspondant);

		org.omg.CORBA.Object obj = sORB.string_to_object(conv.getSonIOR());
		Talk talk = TalkHelper.narrow(obj);

		talk.stop(conv.getConvNum());

		obj = sORB.string_to_object(conv.getMonIOR());
		talk = TalkHelper.narrow(obj);

		talk.stop(conv.getConvNum());
	}

	// Tue toutes les conversations
	public void killAll() {
		for (Iterator<String> i = mConversationMap.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			killConv(key);
		}
	}

	@Override
	public String getInformations() {
		return mUserName;
	}

	@Override
	public void requestTalk(final short numConvSideA, final String subject,
			final String sonPseudo, final String sonTalkIOR) {

		Runnable r = new Runnable() {
			public void run() {
				int answer = JOptionPane.showConfirmDialog(null,
						"Ouvrir une conversation avec " + sonPseudo,
						"Acceptation : " + mUserName, JOptionPane.YES_NO_OPTION);
				org.omg.CORBA.Object obj = sORB.string_to_object(sonTalkIOR);
				Talk talkDistant = TalkHelper.narrow(obj);

				// SI l'user accepte la conversation
				if (answer == JOptionPane.YES_OPTION) {
					// on test si on a déjà une conversation avec cet
					// utilisateur
					String monTalkIOR = null;
					if (mConversationMap.containsKey(sonPseudo)) {
						monTalkIOR = mConversationMap.get(sonPseudo).getMonIOR();
					}

					Talk monTalk = null;
					final MessageComponent messageComp = new MessageComponent();

					if (monTalkIOR == null) { // Donc on ne le connait pas!
						// on cree un nouveau talk
						TalkImpl conv = new TalkImpl(sonPseudo,	messageComp);
						TalkPOATie convTie = new TalkPOATie(conv);
						monTalk = convTie._this(sORB);
						monTalkIOR = sORB.object_to_string(monTalk);
					}

					// On valide aupres du talk distant
					talkDistant.accept(mConversationNumber, numConvSideA, monTalkIOR);

					// On enregistre les donnees liees a la conversation
					mConversationMap.put(sonPseudo, new Conversation(sonTalkIOR,
							monTalkIOR, mConversationNumber));

					// System.out.println(sonPseudo +
					// " debute avec vous la conversation " + convIncr);
					mConversationNumber++;

					// On ouvre une une new fenetre
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new SwingClient(messageComp, sonTalkIOR, mConversationNumber)
									.setVisible(true);
						}
					});

				} else {
					// L'user a refusé la conversation, on notifie l'utilisateur
					talkDistant.deny(numConvSideA);
				}

			}
		};

		Thread runOrb = new Thread(r);
		runOrb.start();

	}

}