package com.yonier.apirest.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.yonier.apirest.models.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {
	
	public Usuario findByUsername(String username);
	
	/*
		@Query("select u from Usuario u Where u.username=?1")
		public Usuario findByUsername2(String username);
	*/
	
}
