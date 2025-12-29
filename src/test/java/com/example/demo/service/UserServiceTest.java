package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserMapper userMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userService = new UserService(userMapper);
    }

    @Test
    void register_success() {
        when(userMapper.findByUsername("alice")).thenReturn(null);
        when(userMapper.insertUser(any())).thenReturn(1);

        User req = new User();
        req.setUsername("alice");
        req.setPassword("password123");
        req.setEmail("a@example.com");
        req.setRealName("Alice");
        req.setPhone("1234567890");

        User created = userService.register(req);

        assertNotNull(created);
        assertEquals("alice", created.getUsername());
        assertNull(created.getPassword()); // password should be cleared in response

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insertUser(captor.capture());
        User persisted = captor.getValue();
        assertNotNull(persisted.getPassword());
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        assertTrue(enc.matches("password123", persisted.getPassword()));
    }

    @Test
    void register_duplicateUsername() {
        User exist = new User();
        exist.setId(1L);
        exist.setUsername("bob");
        when(userMapper.findByUsername("bob")).thenReturn(exist);

        User req = new User();
        req.setUsername("bob");
        req.setPassword("x");
        req.setEmail("b@example.com");
        req.setRealName("Bob");
        req.setPhone("0987654321");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.register(req));
        assertTrue(ex.getMessage().toLowerCase().contains("exists"));
        verify(userMapper, never()).insertUser(any());
    }

    @Test
    void login_success() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        User stored = new User();
        stored.setId(5L);
        stored.setUsername("carol");
        stored.setPassword(enc.encode("secret"));
        stored.setEmail("c@example.com");
        when(userMapper.findByUsername("carol")).thenReturn(stored);

        Map<String, Object> res = userService.login("carol", "secret");
        assertNotNull(res.get("token"));
        assertTrue(res.get("token") instanceof String);
        assertNotNull(res.get("user"));
        User u = (User) res.get("user");
        assertEquals(5L, u.getId());
        assertEquals("carol", u.getUsername());
        assertNull(u.getPassword());
    }

    @Test
    void login_wrongPassword() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        User stored = new User();
        stored.setId(6L);
        stored.setUsername("dan");
        stored.setPassword(enc.encode("rightpass"));
        when(userMapper.findByUsername("dan")).thenReturn(stored);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.login("dan", "wrong"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }
    @Test
    void deleteUserById_success() {
        when(userMapper.deleteUserById(5L)).thenReturn(1);
        assertDoesNotThrow(() -> userService.deleteUserById(5L));
        verify(userMapper, times(1)).deleteUserById(5L);
    }

    @Test
    void deleteUserById_nullId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.deleteUserById(null));
        assertTrue(ex.getMessage().toLowerCase().contains("id"));
        verify(userMapper, never()).deleteUserById(any());
    }
}