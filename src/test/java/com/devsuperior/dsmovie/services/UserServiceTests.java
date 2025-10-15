package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CustomUserUtil util;

    private String existingUsername;
    private String notExistingUsername;
    private UserEntity user;
    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setUp() throws Exception {
        existingUsername = "maria@gmail.com";
        notExistingUsername = "user@gmail.com";
        user = UserFactory.createUserEntity();
        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);
        
        when(repository.searchUserAndRolesByUsername(existingUsername)).thenReturn(userDetails);
        when(repository.searchUserAndRolesByUsername(notExistingUsername)).thenReturn(new ArrayList<>());
        when(repository.findByUsername(existingUsername)).thenReturn(Optional.of(user));
        when(repository.findByUsername(notExistingUsername)).thenReturn(Optional.empty());
    }

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
        when(util.getLoggedUsername()).thenReturn(existingUsername);
        UserEntity result = service.authenticated();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        doThrow(ClassCastException.class).when(util).getLoggedUsername();
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            service.authenticated();
        });
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        UserDetails result = service.loadUserByUsername(existingUsername);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(existingUsername, result.getUsername());
	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(notExistingUsername);
        });
	}
}
