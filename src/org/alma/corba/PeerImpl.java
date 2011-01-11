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
import MTalk.TalkOperations;
import MTalk.TalkPOATie;

public class PeerImpl implements PeerOperations {

	private String mUserName = "";
	private ORB mORB;

	private Map<String, TalkImpl> mTalkMap;
	private short mConversationNumber = 0;

	// ORB, My UserName
	public PeerImpl(ORB orb, String username) {
		this.mUserName = username;
		this.mTalkMap = new HashMap<String, TalkImpl>();
		this.mORB = orb;
	}

	// Tue toutes les conversations
	public void killAll() {
		for (Iterator<String> i = mTalkMap.keySet().iterator(); i.hasNext();) {
			mTalkMap.get(i.next()).stop((short)0);
		}
	}
	
	public boolean hasCorrespondant(String username) {
		return mTalkMap.containsKey(username);
	}
	public TalkImpl getCorrespondant(String username) {
		return mTalkMap.get(username);
	}
	public TalkImpl createCorrespondant(String username) {
		final TalkImpl talk = new TalkImpl(mORB, mUserName);
		mTalkMap.put(username, talk);
		return talk;
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
				org.omg.CORBA.Object obj = mORB.string_to_object(sonTalkIOR);
				Talk talkDistant = TalkHelper.narrow(obj);

				// SI l'user accepte la conversation
				if (answer == JOptionPane.YES_OPTION) {
					// on test si on a déjà une conversation avec cet
					// utilisateur
					final MessageComponent messageComp = new MessageComponent();
					TalkOperations talk = null;
					
					if (mTalkMap.containsKey(sonPseudo)) {
						talk = mTalkMap.get(sonPseudo);
					} else {
						// on créer un nouveau talk
						talk = new TalkImpl(mORB, mUserName);
					}
					
					final TalkPOATie convTie = new TalkPOATie(talk);
					final Talk monTalk = convTie._this(mORB);
					final String monTalkIOR = mORB.object_to_string(monTalk);

					// On valide aupres du talk distant
					talkDistant.accept(mConversationNumber, numConvSideA, monTalkIOR);

					// On enregistre les donnees liees a la conversation
					mTalkMap.put(sonPseudo, new TalkImpl(mORB, mUserName)); // TODO: check

					// System.out.println(sonPseudo +
					// " debute avec vous la conversation " + convIncr);
					mConversationNumber++;

					// On ouvre une une new fenetre
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new SwingClient(messageComp, monTalk, mConversationNumber)
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