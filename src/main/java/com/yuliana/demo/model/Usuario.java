package com.yuliana.demo.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuario")
public class Usuario implements UserDetails {

	@Id
	private Long id;
	private String nombre;
	private String email;
	private String password;
	private String role;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(() -> "ROLE_" + role.toUpperCase());
	}

	@Override
	public String getUsername() {

		return this.email;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public Usuario() {

	}

	public Usuario(Long id, String nombre, String email, String password, String role) {
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String nombre;
		private String email;
		private String password;
		private String role;

		public Builder id(Long id) {
			this.id = id;
			return this;
		}

		public Builder nombre(String nombre) {
			this.nombre = nombre;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder role(String role) {
			this.role = role;
			return this;
		}

		public Usuario build() {
			return new Usuario(id, nombre, email, password, role);
		}
	}

}
