package org.alma.corba.vue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import lib.EasyNaming;
import lib.InvalidRootContextException;

import org.alma.corba.Conversation;
import org.alma.corba.PeerImpl;
import org.alma.corba.TalkImpl;
import org.alma.corba.modele.MessageComponent;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import MTalk.Peer;
import MTalk.PeerHelper;
import MTalk.PeerPOATie;
import MTalk.Talk;
import MTalk.TalkPOATie;

public class ListUsers extends JFrame {
	private static final long serialVersionUID = 1L;

	private DefaultListModel listModel;
	private JList list;
	private String userName;
	private PeerImpl peerImpl;

	private Short nbConv = 0;
	
	private EasyNaming easyNaming;
	
	public ListUsers(String[] args) {
//		Ajout d'un listener qui permet deconnecter l'user du service de nomage.
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					easyNaming.unbind_from_string("/talk/"+userName);
				} catch (InvalidName e1) {
					e1.printStackTrace();
				} catch (NotFound e1) {
					e1.printStackTrace();
				} catch (CannotProceed e1) {
					e1.printStackTrace();
				} catch (SystemException e1) {
					e1.printStackTrace();
				} catch (InvalidRootContextException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}

			@Override
			public void windowDeiconified(WindowEvent e) {}

			@Override
			public void windowActivated(WindowEvent e) {}

			@Override
			public void windowDeactivated(WindowEvent e) {}
		});

		try{
			final ORB orb = ORB.init(args,null);
			userName = JOptionPane.showInputDialog(null, "Nom d'utilisateur?", "User");

			// On enregistre l'utilisateur	
			// Deploiement du RootPOA
			org.omg.CORBA.Object obj = null;

			obj = orb.resolve_initial_references("RootPOA");

			POA rootPOA = POAHelper.narrow(obj);
			final POAManager manager = rootPOA.the_POAManager();
			
			//Fichier contenant l'IOR du naming service
			String fichier = "iorNammingService.ref";
			BufferedReader inFromUserFile = null;

			//on lit l'IOR du naming service dans le fichier
			inFromUserFile = new BufferedReader(new InputStreamReader(new FileInputStream(fichier)));
			String ior = null;
			ior = inFromUserFile.readLine();
			//on instancie le service de nommmage avec le bon IOR et mon ORB  
			easyNaming = new EasyNaming(orb, ior);

			// On met le orb.run() dans un thread, car il est bloquant 
			Runnable r = new Runnable() {
				public void run(){
					try {
						manager.activate();
					} catch (AdapterInactive e) {
						e.printStackTrace();
					}
					orb.run();		    	
				}
			};
			Thread runOrb=new Thread(r);
			runOrb.start();

			// Creation du mon peer
			peerImpl = new PeerImpl(userName, orb);
			PeerPOATie convTie = new PeerPOATie(peerImpl);
			Peer monPeer = convTie._this(orb);

			// On enregistre le client sur le naming service
			easyNaming.rebind_from_string("/talk/" + userName, monPeer);

			//Creation de la fenetre 
			setTitle("List Users");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(0, 0));

			JButton cmdRefresh = new JButton("Refresh");
			getContentPane().add(cmdRefresh, BorderLayout.NORTH);

			list = new JList();
			list.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(MouseEvent e) {
					//On effectue une requete sur l'utilisateur double clické de la liste
					JList list = (JList)e.getSource();
					if (e.getClickCount() == 2) { // Double click
						list.getSelectedIndex();
						String correspondant = (String)list.getModel().getElementAt(list.getSelectedIndex());
						
						org.omg.CORBA.Object objDistant = null;
						try {
							objDistant = easyNaming.resolve_from_string("/talk/" + correspondant);
						} catch (InvalidName e1) {
							e1.printStackTrace();
						} catch (NotFound e1) {
							e1.printStackTrace();
						} catch (CannotProceed e1) {
							e1.printStackTrace();
						} catch (SystemException e1) {
							e1.printStackTrace();
						} catch (InvalidRootContextException e1) {
							e1.printStackTrace();
						}
						Peer peerDistant = PeerHelper.narrow(objDistant);
						String mytalkIor = "";

						if (peerImpl.getMapConv().containsKey(correspondant)){
							mytalkIor = peerImpl.getMapConv().get(correspondant).getMonIOR();
						}else{
							nbConv ++; // on incremente le nombre de conversations

							MessageComponent mesConv = new MessageComponent();
							TalkImpl talkImpl = new TalkImpl(correspondant, peerImpl,mesConv);
							TalkPOATie talkTie = new TalkPOATie(talkImpl);
							Talk talkLocal = talkTie._this(orb);
							mytalkIor = orb.object_to_string(talkLocal);

							Conversation cor = new Conversation(null, mytalkIor,nbConv);

							peerImpl.getMapConv().put(correspondant, cor);
						}
						peerDistant.requestTalk(nbConv, correspondant, userName, mytalkIor);

					}


				}
			});
			getContentPane().add(list, BorderLayout.CENTER);
			this.setSize(new Dimension(400, 600));
			ActionListener refreshAction = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						DefaultListModel nLlistModel = new DefaultListModel();
						//On recupere les 100 users connectés sur /talk du service de nommage
						for (String nom : easyNaming.list_from_string("/talk", 100)) {
							nLlistModel.addElement(nom);
						}
						list.setModel(nLlistModel);
					} catch (InvalidName e1) {
						e1.printStackTrace();
					} catch (NotFound e1) {
						e1.printStackTrace();
					} catch (CannotProceed e1) {
						e1.printStackTrace();
					} catch (SystemException e1) {
						e1.printStackTrace();
					} catch (InvalidRootContextException e1) {
						e1.printStackTrace();
					}
				}
			};
			cmdRefresh.addActionListener(refreshAction);
			
			Timer timer = new Timer(500, refreshAction);
			timer.start();

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidName e) {
			e.printStackTrace();
		} catch (AlreadyBound e) {
			e.printStackTrace();
		} catch (CannotProceed e) {
			e.printStackTrace();
		} catch (NotFound e) {
			e.printStackTrace();
		} catch (SystemException e) {
			e.printStackTrace();
		} catch (InvalidRootContextException e) {
			e.printStackTrace();
		} catch (org.omg.CORBA.ORBPackage.InvalidName e) {
			e.printStackTrace();
		}
	}

}
