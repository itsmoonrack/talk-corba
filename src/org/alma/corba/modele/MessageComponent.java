package org.alma.corba.modele;

import javax.swing.JTextPane;



public class MessageComponent extends JTextPane{

	private static final long serialVersionUID = 1L;
	private String messages = "";
	
	public MessageComponent(){
		super();
		this.setContentType("text/html");
		this.setEditable(false);
	}
	
	public void addMessage(String userName, String message){

		this.messages += "<b>"+userName+": </b>" + message + "<br/>";
		
		this.setText(this.messages);
	}
	
}
