package br.cin.tbookmarks.util;

import java.util.ArrayList;

public class StructUserItem {
	
	
	private ArrayList<UserItemResource> userItemResource;
	private ArrayList<UserResource> userList;
	
	
	public StructUserItem(ArrayList<UserItemResource> userItemResource, ArrayList<UserResource> userList) {
		
		this.userItemResource = userItemResource;
		this.userList = userList;
	}


	public ArrayList<UserItemResource> getUserItemResource() {
		return userItemResource;
	}


	public void setUserItemResource(ArrayList<UserItemResource> userItemResource) {
		this.userItemResource = userItemResource;
	}


	public ArrayList<UserResource> getUserList() {
		return userList;
	}


	public void setUserList(ArrayList<UserResource> userList) {
		this.userList = userList;
	}
	
	
	
	
	

}
