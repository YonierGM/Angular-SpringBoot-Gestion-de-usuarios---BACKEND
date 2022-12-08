package com.yonier.apirest.controllers;

import java.io.IOException;
import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yonier.apirest.models.entity.Cliente;
import com.yonier.apirest.models.services.IClienteService;
import com.yonier.apirest.models.services.IUploadFileService;

@CrossOrigin(origins =  {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClienteRestController {
	
	@Autowired
	private IClienteService clienteService;
	
	@Autowired
	private IUploadFileService uploadService;
	
	@GetMapping("/clientes")
	public List<Cliente> index(){
		return clienteService.findAll();	
	}
	
	@GetMapping("/clientes/page/{page}")
	public Page<Cliente> index(@PathVariable Integer page){
		Pageable pageable = PageRequest.of(page, 5);
		return clienteService.findAll(pageable);
	}
	
	@SuppressWarnings("unused")
	@GetMapping("/clientes/{id}")
	public ResponseEntity<?>showCliente(@PathVariable Long id) {
		
		Cliente cliente = null;
		
		Map<String, Object> response = new HashMap<>();
		
		try {
			cliente = clienteService.findById(id);
			
		} catch (DataAccessException e) {
			
			response.put("mensaje", "Error al realizar la consulta en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		if (cliente == null) {
			
			response.put("mensaje", "El cliente con el ID: ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);		
	}
	
	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String nombreFoto){
		
		Resource recurso = null;
		
		try {
			recurso = uploadService.cargar(nombreFoto);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		HttpHeaders cabezera = new HttpHeaders();
		cabezera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");
		
		return new ResponseEntity<Resource>(recurso, cabezera, HttpStatus.OK);
	}
	
	@PostMapping("/clientes")
	//RequestBody ya que la respuesta es en formato json
	public ResponseEntity<?> createCliente(@Valid @RequestBody Cliente cliente, BindingResult result) {
		
		Cliente clienteNew = null;
		Map<String, Object> response = new HashMap<>();
		
		if (result.hasErrors()) {
			
			/*
			List<String> errors = new ArrayList<>();
			
			for(FieldError err: result.getFieldErrors()) {
				errors.add("El campo "+err.getField()+" "+err.getDefaultMessage());
			}
			
			*/
			
			//Errores, validaciones de los campos
			
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(err -> "El campo '"+ err.getField()+"' "+ err.getDefaultMessage())
					.collect(Collectors.toList());
					
			
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			
			clienteNew = clienteService.saveCliente(cliente);
			
		} catch (DataAccessException e) {
			
			response.put("mensaje", "Error al guardar los datos en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		response.put("mensaje", "El cliente se ha creado con exito");
		response.put("cliente", clienteNew);
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@PutMapping("/clientes/{id}")
	public ResponseEntity<?> updateCliente(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id) {
		
		Cliente clienteActual = clienteService.findById(id);
		Cliente clienteUpdate = null;
		
		Map<String, Object> response = new HashMap<>();
		
		if (result.hasErrors()) {
			
			/*
			List<String> errors = new ArrayList<>();
			
			for(FieldError err: result.getFieldErrors()) {
				errors.add("El campo "+err.getField()+" "+err.getDefaultMessage());
			}
			*/
			
			//Errores, validaciones de los campos
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(err -> "El campo '"+ err.getField()+"' "+ err.getDefaultMessage())
					.collect(Collectors.toList());
					
			
			response.put("errors", errors);
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		if (clienteActual == null) {
			
			response.put("mensaje", "No se pudo editar: El cliente con el ID: ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		
		try {
			
			clienteActual.setNombre(cliente.getNombre());
			clienteActual.setApellido(cliente.getApellido());
			clienteActual.setEmail(cliente.getEmail());
			clienteActual.setCreateAt(cliente.getCreateAt());
			
			clienteUpdate = clienteService.saveCliente(clienteActual);
			
		} catch (DataAccessException e) {
			
			response.put("mensaje", "Error al actualizar el cliente en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		

		response.put("mensaje", "El cliente se ha actualizado con exito");
		response.put("cliente", clienteUpdate);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@DeleteMapping("/clientes/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id) {
		 
		Map<String, Object> response = new HashMap<>();
		Cliente cliente = clienteService.findById(id);
		
		if (cliente == null) {
			
			response.put("mensaje", "No se pudo eliminar: El cliente con el ID: ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
			
		}else {
			try {
				
				String nombreFotoAnterior = cliente.getFoto();
				uploadService.eliminar(nombreFotoAnterior);
				
				clienteService.delete(id);
				
			} catch (DataAccessException e) {
				
				response.put("mensaje", "Error al emiminar el cliente de la base de datos");
				response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
				return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		response.put("mensaje", "Registro eliminado con exito");
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
	}
	
	@PostMapping("/clientes/upload")
	public ResponseEntity<?> upload(@RequestParam ("archivo") MultipartFile archivo, @RequestParam("id") Long id){
		Map<String, Object> response = new HashMap<>();
		
		Cliente cliente = clienteService.findById(id);
		
		if (cliente == null) {
			
			response.put("mensaje", "No se pudo subir la imagen: El cliente con el ID: ".concat(id.toString().concat(" no existe en la base de datos")));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
			
		}else {
			
			if (!archivo.isEmpty()) {
				String nombreArchivo = null;

				try {
					nombreArchivo = uploadService.guardar(archivo);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					response.put("mensaje", "Error al subir la imagen");
					response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
					return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				String nombreFotoAnterior = cliente.getFoto();
				
				uploadService.eliminar(nombreFotoAnterior);
				
				cliente.setFoto(nombreArchivo);
				clienteService.saveCliente(cliente);
				
				response.put("cliente", cliente);
				response.put("mensaje", "Se ha subido la imagen "+nombreArchivo);
			}
			
		}
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
		
	}

}