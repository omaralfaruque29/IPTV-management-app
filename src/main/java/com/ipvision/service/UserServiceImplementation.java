package com.ipvision.service;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.UserDao;
import com.ipvision.domain.User;
import com.ipvision.domain.UserRole;

@Service
public class UserServiceImplementation implements UserService {

	@Autowired
	UserDao userDao;
	
	@Override
	public boolean checkUserExist(User user) {
		return userDao.checkUserExist(user);
	}

	@Override
	public Set<UserRole> returnUserRoles(User user) {
		return userDao.returnUserRoles(user);
	}

	@Override
	public User returnUser(User user) {
		return userDao.returnUser(user);
	}

	@Override
	public User returnUserById(String userId) {
		
		return userDao.returnUserById(userId);
	}

	@Override
	public List<User> returnAllUser(int startUser, int selectedUserPerPage) {
		
		return userDao.returnAllUser(startUser, selectedUserPerPage);
	}

	@Override
	public int returnNumberOfUser() {
		
		return userDao.returnNumberOfUser();
	}

	@Override
	public void saveUser(User user) throws Exception {
		
		userDao.saveUser(user);
		
	}

	@Override
	public void updateUser(User user, String userId) throws Exception {
		
		userDao.updateUser(user, userId);
		
	}
	
	@Override
	public void deleteUser(User user) throws Exception {
		userDao.deleteUser(user);
		
	}

}
