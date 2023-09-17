package com.ipvision.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipvision.dao.RoleDao;
import com.ipvision.domain.Role;

@Service
public class RoleServiceImplementation implements RoleService {

	@Autowired
	RoleDao roleDao;
	
	@Override
	public Role returnRoleById(String roleId) {
		
		return roleDao.returnRoleById(roleId);
	}

	@Override
	public List<Role> returnAllRole(String pageNumber, int rolePerPage) {
		
		return roleDao.returnAllRole(pageNumber, rolePerPage);
	}
	
	@Override
	public List<Role> returnAllRole() {
		
		return roleDao.returnAllRole();
	}

	@Override
	public int returnNumberOfRole() {
		
		return roleDao.returnNumberOfRole();
	}

	@Override
	public void saveRole(Role role) throws Exception {
		
		roleDao.saveRole(role);
		
	}

	@Override
	public void updateRole(Role role, String roleId) throws Exception {
		
		roleDao.updateRole(role, roleId);
		
	}
	
	@Override
	public void deleteRole(Role role) throws Exception {
		roleDao.deleteRole(role);
		
	}


}
