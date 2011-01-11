package org.alma.corba;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

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


	private String pseudo = "";
	public static ORB orb;

	private Map<String,Conversation> mapConvs;
	private PeerImpl getInstance;
	private short numConv = 0;

	public PeerImpl(String pseudo, ORB orb) {
		this.pseudo = pseudo;
		this.mapConvs = new HashMap<String, Conversation>();
		this.orb = orb;

		getInstance = this;
	}

	public Map<String, Conversation> getMapConv() {
		return mapConvs;
	}

	// Tue la conversation numConv
	public void killConv(String correspondant){

		Conversation conv = mapConvs.get(correspondant);

		org.omg.CORBA.Object obj = orb.string_to_object(conv.getSonIOR());
		Talk talk = TalkHelper.narrow(obj);

		talk.stop(conv.getConvNum());

		obj = orb.string_to_object(conv.getMonIOR());
		talk = TalkHelper.narrow(obj);

		talk.stop(conv.getConvNum());	
	}

	// Tue toutes les conversations
	public void killAll(){
		for (Iterator<String> i = mapConvs.keySet().iterator() ; i.hasNext() ; ){
			String key = i.next();
			killConv(key);  
		}
	}

	@Override
	public String getInformations() {
		return pseudo;
	}

	@Override
	public void requestTalk(final short numConvSideA, final String subject,	final String sonPseudo, final String sonTalkIOR) {

		Runnable r = new Runnable(){
			public void run(){
				int answer = JOptionPane.showConfirmDialog(null,"Ouvrir une conversation avec " + sonPseudo ,"Acceptation : " + pseudo,  JOptionPane.YES_NO_OPTION);
				org.omg.CORBA.Object obj = orb.string_to_object(sonTalkIOR);
				Talk talkDistant = TalkHelper.narrow(obj);

				//SI l'user accepte la conversation
				if(answer == JOptionPane.YES_OPTION){
					//on test si on a déjà une conversation avec cet utilisateur
					String monTalkIOR = null;
					if (mapConvs.containsKey(sonPseudo)){
						monTalkIOR = mapConvs.get(sonPseudo).getMonIOR();
					}
					
					Talk monTalk = null;
					final MessageComponent mesConv = new MessageComponent();
					
					if(monTalkIOR == null){ // Donc on ne le connait pas!
						//on cree un nouveau talk
						TalkImpl conv = new TalkImpl(sonPseudo, getInstance,mesConv);
						TalkPOATie convTie = new TalkPOATie(conv);
						monTalk = convTie._this(orb);
						monTalkIOR = orb.object_to_string(monTalk);	
					}

					// On valide aupres du talk distant
					talkDistant.accept(numConv, numConvSideA, monTalkIOR);

					// On enregistre les donnees liees a la conversation
					mapConvs.put(sonPseudo, new Conversation(sonTalkIOR, monTalkIOR,numConv));

//					System.out.println(sonPseudo + " debute avec vous la conversation " + convIncr);
					numConv++;
					
					//On ouvre une une new fenetre
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							new SwingClient(mesConv,sonTalkIOR,numConv).setVisible(true);
						}
					});
					

				}else{
					//L'user a refusé la conversation, on notifie l'utilisateur
					talkDistant.deny(numConvSideA);
				}	


			}
		};

		Thread runOrb=new Thread(r);
		runOrb.start();

	}

}