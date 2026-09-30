package ar.edu.unnoba.appweb_concurso_materiales_educativos;

import ar.edu.unnoba.appweb_concurso_materiales_educativos.model.User;
import ar.edu.unnoba.appweb_concurso_materiales_educativos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class AppwebConcursoMaterialesEducativosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppwebConcursoMaterialesEducativosApplication.class, args);
	}

	/**
	 * Configura un usuario administrador por defecto si no existe en la base de datos al iniciar la aplicación.
	 *
	 * @param userRepository   Repositorio de usuarios.
	 * @param passwordEncoder  Codificador de contraseñas.
	 * @return                  Un CommandLineRunner para configurar el usuario por defecto.
	 */
	@Bean
	public CommandLineRunner setupDefaultUser(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.email:admin@example.com}") String adminEmail,
			@Value("${app.admin.password:Admin1234}") String adminPassword) {
		return args -> {
			User user = userRepository.findByEmail(adminEmail);
			if (user == null) {
				user = new User();
				user.setEmail(adminEmail);
				user.setPassword(passwordEncoder.encode(adminPassword));
				user.setNombre("Admin");
				user.setApellido("Admin");
				user.setRol(User.Rol.ADMINISTRADOR);
				userRepository.save(user);
			}
		};
	}
}
