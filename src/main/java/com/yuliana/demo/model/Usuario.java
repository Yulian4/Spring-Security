package com.yuliana.demo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.yuliana.demo.repository.Token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuario")
public class Usuario {

	@Id
	Long id;
	private String nombre;
	private String email;
	private String password;

	public Usuario() {

	}

	public Usuario(Long id, String nombre, String email, String password) {
		this.id = id;
		this.nombre = nombre;
		this.email = email;
		this.password = password;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Long id;
		private String nombre;
		private String email;
		private String pasword;

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

		public Builder pasword(String pasword) {
			this.pasword = pasword;
			return this;
		}

		public Usuario build() {
			return new Usuario(id, nombre, email, pasword);
		}
	}
}
