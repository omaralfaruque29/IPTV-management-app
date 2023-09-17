package com.ipvision.dao;

import java.util.List;

import com.ipvision.domain.Role;


public interface RoleDao {
	
	public Role returnRoleById(String roleId);
	
	public List<Role> returnAllRole(String pageNumber,int rolePerPage);
	
	public List<Role> returnAllRole();
	
	public int returnNumberOfRole();
	
	public void saveRole(Role role) throws Exception;
	
	public void updateRole(Role role,String roleId) throws Exception;
	
	public void deleteRole(Role role) throws Exception;

}
