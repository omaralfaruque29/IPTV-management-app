package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.UserRole;

public interface UserRoleDao {

	public List<UserRole> returnUserRolesListByUserId(int userId);
	
	public void saveUserRole(UserRole userRole) throws Exception;
	
	public void deleteUserRoleByUserRoleId(int userRoleId);
}
