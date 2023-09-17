package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.UserRoleDao;
import com.ipvision.domain.UserRole;


@Service
public class UserRoleServiceImplementation implements UserRoleService {

	@Autowired
	UserRoleDao userRoleDao;

	@Override
	public List<UserRole> returnUserRolesListByUserId(int userId) {
		
		return userRoleDao.returnUserRolesListByUserId(userId);
	}

	@Override
	public void saveUserRole(UserRole userRole) throws Exception {
		
		userRoleDao.saveUserRole(userRole);
		
	}

	@Override
	public void deleteUserRoleByUserRoleId(int userRoleId) {
		
		userRoleDao.deleteUserRoleByUserRoleId(userRoleId);
		
	}
}
