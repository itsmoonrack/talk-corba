package org.alma.corba.modele;

import javax.swing.JTextPane;



public class UsersListComponent extends JTextPane{

	private static final long serialVersionUID = 1L;
	private String users = "";
	
	public UsersListComponent(){
		super();
		this.setContentType("text/html");
		this.setEditable(false);
	}
	
	public void addUser(String userName){
		this.users += "<b>"+userName+"</b><br/>";
		this.setText(this.users);
	}
	
}
