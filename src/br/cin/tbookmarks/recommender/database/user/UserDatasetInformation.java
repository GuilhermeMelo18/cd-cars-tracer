package br.cin.tbookmarks.recommender.database.user;

import java.util.ArrayList;
import java.util.List;

public class UserDatasetInformation {
	List<UserInformation> users;
	
	public UserDatasetInformation() {
		users = new ArrayList<UserInformation>();
	}

	public List<UserInformation> getUsers() {
		return users;
	}

	public void setUsers(List<UserInformation> users) {
		this.users = users;
	}
}
