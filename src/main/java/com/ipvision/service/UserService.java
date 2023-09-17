package com.ipvision.service;

import java.util.List;
import java.util.Set;

import com.ipvision.domain.User;
import com.ipvision.domain.UserRole;

public interface UserService {

    public boolean checkUserExist(User user);
	
	public Set<UserRole> returnUserRoles(User user);
	
	public User returnUser(User user);
	
    public User returnUserById(String userId);
	
	public List<User> returnAllUser(int startUser, int selectedUserPerPage);
	
	public int returnNumberOfUser();
	
	public void saveUser(User user) throws Exception;
	
	public void updateUser(User user,String userId) throws Exception;
	
	public void deleteUser(User user) throws Exception;
}
