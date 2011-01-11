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
import java.io.InputStreamReader;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import lib.EasyNaming;

import org.alma.corba.Conversation;
import org.alma.corba.PeerImpl;
import org.alma.corba.TalkImpl;
import org.alma.corba.modele.MessageComponent;
import org.omg.CORBA.ORB;
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

	private JList list;
	private String userName;
	private PeerImpl peerImpl;

	private Short nbConv = 0;

	private EasyNaming easyNaming;
	private ORB orb;

	public ListUsers(String[] args) {
		// Ajout d'un listener qui permet deconnecter l'user du service de
		// nomage.
		this.addWindowListener(new ListUserWindowListener());

		try {
			orb = ORB.init(args, null);
			userName = JOptionPane.showInputDialog(null, "Nom d'utilisateur?",
					"User");

			// On enregistre l'utilisateur
			// Deploiement du RootPOA
			org.omg.CORBA.Object obj = null;

			obj = orb.resolve_initial_references("RootPOA");

			POA rootPOA = POAHelper.narrow(obj);
			final POAManager manager = rootPOA.the_POAManager();

			String fichier = "iorNammingService.ref";
			BufferedReader inFromUserFile = null;

			inFromUserFile = new BufferedReader(new InputStreamReader(
					new FileInputStream(fichier)));

			String ior = null;

			ior = inFromUserFile.readLine();

			easyNaming = new EasyNaming(orb, ior);

			// On met le orb.run() dans un thread, car il est bloquant
			Runnable r = new Runnable() {
				public void run() {
					try {
						manager.activate();
					} catch (AdapterInactive e) {
						e.printStackTrace();
					}
					orb.run();
				}
			};

			Thread runOrb = new Thread(r);
			runOrb.start();

			// Creation du peer local
			peerImpl = new PeerImpl(userName, orb);
			PeerPOATie convTie = new PeerPOATie(peerImpl);
			Peer peerLocal = convTie._this(orb);

			// On enregistre le client

			easyNaming.rebind_from_string("/talk/" + userName, peerLocal);

			setTitle("List Users");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.getContentPane().setLayout(new BorderLayout(0, 0));

			JButton cmdRefresh = new JButton("Refresh");
			getContentPane().add(cmdRefresh, BorderLayout.NORTH);

			list = new JList();
			list.addMouseListener(new UserMouseListener());
			getContentPane().add(list, BorderLayout.CENTER);
			this.setSize(new Dimension(400, 600));
			ActionListener refreshAction = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refresh();
				}
			};
			cmdRefresh.addActionListener(refreshAction);
			Timer timer = new Timer(500, refreshAction);
			timer.start();

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}
	
	public void refresh() {
		try {
			DefaultListModel nLlistModel = new DefaultListModel();

			for (String nom : easyNaming.list_from_string("/talk",
					50)) {
				nLlistModel.addElement(nom);
			}
			list.setModel(nLlistModel);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	private class ListUserWindowListener implements WindowListener {

		@Override
		public void windowOpened(WindowEvent e) {
			refresh();
		}

		@Override
		public void windowClosing(WindowEvent e) {
			try {
				easyNaming.unbind_from_string("/talk/" + userName);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
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
	
	private class UserMouseListener implements MouseListener {
		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			JList list = (JList) e.getSource();
			if (e.getClickCount() == 2) { // Double click
				list.getSelectedIndex();
				String correspondant = (String) list.getModel()
						.getElementAt(list.getSelectedIndex());

				org.omg.CORBA.Object objDistant = null;
				try {
					objDistant = easyNaming
							.resolve_from_string("/talk/"
									+ correspondant);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Peer peerDistant = PeerHelper.narrow(objDistant);
				String mytalkIor = "";

				if (peerImpl.getMapConv().containsKey(correspondant)) {
					mytalkIor = peerImpl.getMapConv()
							.get(correspondant).getMonIOR();
				} else {
					nbConv++; // on incremente le nombre de
								// conversations

					MessageComponent mesConv = new MessageComponent();
					TalkImpl talkImpl = new TalkImpl(correspondant,
							peerImpl, mesConv);
					TalkPOATie talkTie = new TalkPOATie(talkImpl);
					Talk talkLocal = talkTie._this(orb);
					mytalkIor = orb.object_to_string(talkLocal);

					Conversation cor = new Conversation(null,
							mytalkIor, nbConv);

					peerImpl.getMapConv().put(correspondant, cor);
				}
				peerDistant.requestTalk(nbConv, correspondant,
						userName, mytalkIor);

			}

		}
	}

}
