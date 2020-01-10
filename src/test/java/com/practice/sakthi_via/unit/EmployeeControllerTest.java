package com.practice.sakthi_via.unit;

import com.practice.sakthi_via.controller.EmployeeController;
import com.practice.sakthi_via.exception.ResourceNotFoundException;
import com.practice.sakthi_via.facade.EmployeeFacade;
import com.practice.sakthi_via.model.Employee;
import com.practice.sakthi_via.model.dto.EmployeeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmployeeControllerTest {

    private static final String EMPLOYEE_ID_NOT_FOUND = "Employee Id not found";
    private static final String EMAIL_VALIDATION_MSG = "Not a well-formed email address";
    private static final String EMPLOYEE_EMAIL_NOT_FOUND = "Employee email not found";
    private static final String EMPLOYEE_USERNAME_OR_EMAIL_NOT_FOUND = "Employee Username or email id not found";
    private static final String EMPLOYEE_USERNAME_SIZE = "Username must have minimum 4 and maximum 10 characters";
    private static final String EMPLOYEE_MINIMUM_AGE = "Employee must be 20 years old";

    @TestConfiguration
    static class EmployeeFacadeTestConfiguraion {

        @Autowired
        EmployeeFacade employeeFacade;

        @Bean
        public EmployeeController employeeController() {
            return new EmployeeController(employeeFacade);
        }

    }

    @Autowired
    EmployeeController employeeController;

    @MockBean
    EmployeeFacade employeeFacade;

    Employee employee;

    public EmployeeControllerTest() {
        employee = new Employee();
        employee.setId((long) 40000);
        employee.setName("Sakthi");
        employee.setEmail("sgsakthi1992@gmail.com");
        employee.setUsername("sgsakthi");
        employee.setAge(27);
    }

    @Test
    @DisplayName("Fetch Employee")
    void testGetEmployee() {
        //GIVEN
        when(employeeFacade.getEmployees()).thenReturn(
                Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResponseEntity<List> responseEntity = employeeController.getEmployees();
        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals(employee.toString(), responseEntity.getBody().get(0).toString());
    }

    @Test
    @DisplayName("Create Employee with valid details")
    void testPostEmployee() throws Exception {
        //GIVEN
        EmployeeDto employeeDto = new EmployeeDto("Employee 1",
                "employee1", "emp1@gmail.com", 25);

        when(employeeFacade.createEmployee(Mockito.any(EmployeeDto.class))).thenReturn(employee);
        when(employeeFacade.checkUsername(employeeDto.getUsername())).thenReturn(true);

        //WHEN
        ResponseEntity<Employee> responseEntity = employeeController.createEmployee(employeeDto);

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals(employee.toString(), responseEntity.getBody().toString());
    }

    @Test
    @DisplayName("Fetch Employee with valid Id")
    void testGetEmployeeById() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeById(employee.getId())).thenReturn(employee);

        //WHEN
        ResponseEntity<Employee> responseEntity = employeeController.getEmployeeById(employee.getId());

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals(employee.toString(), responseEntity.getBody().toString());
    }

    @Test
    @DisplayName("Fetch Employee with invalid Id")
    void testGetEmployeeWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.getEmployeeById(id)).thenThrow(
                new ResourceNotFoundException(EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        //THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeController.getEmployeeById(id));
        assertTrue(exception.getMessage().contains(EMPLOYEE_ID_NOT_FOUND));

    }

    @Test
    @DisplayName("Delete Employee with Id")
    void testDeleteEmployeeById() throws Exception {
        //GIVEN
        when(employeeFacade.deleteEmployeeById(employee.getId())).thenReturn("Success");

        //WHEN
        ResponseEntity<String> responseEntity = employeeController.deleteEmployeeById(employee.getId());

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals("Success", responseEntity.getBody().toString());
    }

    @Test
    @DisplayName("Delete Employee with Invalid Id")
    void testDeleteEmployeeWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.deleteEmployeeById(id)).thenThrow(
                new ResourceNotFoundException(EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        //THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeController.deleteEmployeeById(id));
        assertTrue(exception.getMessage().contains(EMPLOYEE_ID_NOT_FOUND));
    }

    @Test
    @DisplayName("Update Employee Email with Id")
    void testUpdateEmployeeEmailById() throws Exception {
        //GIVEN
        when(employeeFacade.updateEmployeeEmail(employee.getId(), "newemail@gmail.com")).thenReturn("Success");

        //WHEN
        ResponseEntity<String> responseEntity = employeeController.updateEmployeeEmail(employee.getId(), "newemail@gmail.com");

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals("Success", responseEntity.getBody().toString());
    }

    @Test
    @DisplayName("Update Employee Email with invalid Id")
    void testUpdateEmployeeEmailWithInvalidId() throws Exception {
        //GIVEN
        Long id = 1L;
        when(employeeFacade.updateEmployeeEmail(id, "newemail@gmail.com"))
                .thenThrow(new ResourceNotFoundException(EMPLOYEE_ID_NOT_FOUND));

        //WHEN
        //THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeController.updateEmployeeEmail(id, "newemail@gmail.com"));
        assertTrue(exception.getMessage().contains(EMPLOYEE_ID_NOT_FOUND));
    }

    @Test
    @DisplayName("Fetch Employee with Email")
    void testGetEmployeeByEmail() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeByEmail(employee.getEmail()))
                .thenReturn(Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResponseEntity<List> responseEntity = employeeController.getEmployeeByEmail(employee.getEmail());

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals(employee.toString(), responseEntity.getBody().get(0).toString());

    }

    @Test
    @DisplayName("Fetch Employee with not available Email")
    void testGetEmployeeByEmailWithNewEmail() throws Exception {
        //GIVEN
        String email = "newemail@gmail.com";
        when(employeeFacade.getEmployeeByEmail(email)).thenThrow(
                new ResourceNotFoundException(EMPLOYEE_EMAIL_NOT_FOUND));

        //WHEN
        //THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeController.getEmployeeByEmail(email));
        assertTrue(exception.getMessage().contains(EMPLOYEE_EMAIL_NOT_FOUND));
    }


    @Test
    @DisplayName("Fetch Employee with Username or Email")
    void testGetEmployeeByUsernameOrEmail() throws Exception {
        //GIVEN
        when(employeeFacade.getEmployeeByUsernameOrEmail(employee.getUsername(), employee.getEmail()))
                .thenReturn(Stream.of(employee).collect(Collectors.toList()));

        //WHEN
        ResponseEntity<List> responseEntity = employeeController.getEmployeeByUsernameOrEmail(employee.getUsername(), employee.getEmail());

        //THEN
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertTrue(responseEntity.hasBody());
        assertEquals(employee.toString(), responseEntity.getBody().get(0).toString());
    }

    @Test
    @DisplayName("Fetch Employee with Username or Email with new Email & Username")
    void testGetEmployeeByUsernameOrEmailWithNewEmailAndUserName() throws ResourceNotFoundException {
        //GIVEN
        String email = "newemail@gmail.com";
        String username = "username";
        when(employeeFacade.getEmployeeByUsernameOrEmail(username, email))
                .thenThrow(new ResourceNotFoundException(EMPLOYEE_USERNAME_OR_EMAIL_NOT_FOUND));
        //WHEN
        //THEN
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> employeeController.getEmployeeByUsernameOrEmail(username, email));
        assertTrue(exception.getMessage().contains(EMPLOYEE_USERNAME_OR_EMAIL_NOT_FOUND));
    }

}
