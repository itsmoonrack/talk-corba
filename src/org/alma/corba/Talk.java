package org.alma.corba;

import javax.swing.SwingUtilities;

import org.alma.corba.vue.ListUsers;

public class Talk {
		
	public static void main(final String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ListUsers lu = new ListUsers(args);
				lu.setVisible(true);
			}
		});
	}
}
