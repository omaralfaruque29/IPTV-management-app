package com.ipvision.service;

import java.util.List;

import com.ipvision.domain.UserRole;

public interface UserRoleService {

	public List<UserRole> returnUserRolesListByUserId(int userId);
	
	public void saveUserRole(UserRole userRole) throws Exception;
	
	public void deleteUserRoleByUserRoleId(int userRoleId);
}
