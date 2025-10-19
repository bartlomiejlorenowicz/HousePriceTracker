package com.scrapper.authservice.mapper;

import com.scrapper.authservice.dto.registerDto.UserDto;
import com.scrapper.authservice.entity.Role;
import com.scrapper.authservice.entity.User;
import com.scrapper.authservice.service.RoleService;
import com.scrapper.authservice.user.RoleType;
import com.scrapper.authservice.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private RoleService roleService;

    @Mock
    private Role roleUser;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    void shouldMapUserDtoToUserEntity() {
        UserDto userDto = new UserDto();
        userDto.setLastName("kowal");
        userDto.setFirstName("maciek");
        userDto.setEmail("maciek@wp.pl");
        userDto.setPassword("kowal123!");

        when(roleService.getRoleReference(RoleType.USER)).thenReturn(roleUser);

        User user = userMapper.toEntity(userDto);

        assertTrue(user.getLastName().contains("kowal"));
        assertTrue(user.getFirstName().contains("maciek"));
        assertTrue(user.getEmail().contains("maciek@wp.pl"));
        assertTrue(user.getPasswordHash().contains("kowal123!"));

        assertEquals(0, user.getFailedLoginAttempts());
        assertEquals(0, user.getFailedLoginAttempts());
        assertFalse(user.isEmailVerified());
        assertNull(user.getLastLoginAt());
        assertNull(user.getLockedUntil());
        assertEquals(UserStatus.PENDING_EMAIL_VERIFICATION, user.getStatus());
    }

    @Test
    void whenUserDtoNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> userMapper.toEntity(null));
    }

    @Test
    void whenUserRoleNullThrowsNPE() {
        UserDto userDto = new UserDto();
        userDto.setLastName("kowal");
        userDto.setFirstName("maciek");

        when(roleService.getRoleReference(RoleType.USER)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> userMapper.toEntity(userDto));
        verify(roleService).getRoleReference(RoleType.USER);
    }
}