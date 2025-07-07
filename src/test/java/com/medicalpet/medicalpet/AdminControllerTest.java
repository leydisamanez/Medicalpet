package com.medicalpet.medicalpet;
import com.medicalpet.medicalpet.controller.AdminController;
import com.medicalpet.medicalpet.model.Usuario;
import com.medicalpet.medicalpet.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class AdminControllerTest {
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private Model model;
    @InjectMocks
    private AdminController adminController;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);}
    @Test
    void testListarEmpleados() {
        // Arrange: Datos simulados
        Usuario empleado1 = new Usuario();
        Usuario empleado2 = new Usuario();
        List<Usuario> empleados = Arrays.asList(empleado1, empleado2);
        when(usuarioRepository.findAll()).thenReturn(empleados);
        // Act: Llamamos al método del controlador
        String vista = adminController.listarEmpleados(model);
        // Assert: Verificamos que se comporta como se espera
        verify(model).addAttribute("empleados", empleados);
        assertEquals("admin/empleados", vista);
    }
}
