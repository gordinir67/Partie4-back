package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void getters_returnValues_fromBuilder() {
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("user@test.com")
                .firstName("U")
                .lastName("SER")
                .admin(true)
                .password("pwd")
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user@test.com");
        assertThat(user.getFirstName()).isEqualTo("U");
        assertThat(user.getLastName()).isEqualTo("SER");
        assertThat(user.getAdmin()).isTrue();
        assertThat(user.getPassword()).isEqualTo("pwd");
    }

    @Test
    void getAuthorities_returnsEmptySet() {
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(1L)
                .username("user@test.com")
                .password("pwd")
                .admin(false)
                .build();

        assertThat(user.getAuthorities()).isNotNull();
        assertThat(user.getAuthorities()).isEmpty();
    }

    @Test
    void springSecurityFlags_areAlwaysTrue() {
        UserDetailsImpl user = UserDetailsImpl.builder()
                .id(99L)
                .username("flags@test.com")
                .password("pwd")
                .admin(false)
                .build();

        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void equals_comparesById_andHandlesNullAndOtherType() {
        UserDetailsImpl a = UserDetailsImpl.builder().id(10L).username("a").password("p").build();
        UserDetailsImpl b = UserDetailsImpl.builder().id(10L).username("b").password("p").build();
        UserDetailsImpl c = UserDetailsImpl.builder().id(11L).username("c").password("p").build();

        // same id => equal
        assertThat(a).isEqualTo(b);

        // different id => not equal
        assertThat(a).isNotEqualTo(c);

        // branches: null + other type
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");

        // branch: same instance
        assertThat(a).isEqualTo(a);
    }

    @Test
    void equals_allowsNullId() {
        UserDetailsImpl a = UserDetailsImpl.builder().id(null).username("a").password("p").build();
        UserDetailsImpl b = UserDetailsImpl.builder().id(null).username("b").password("p").build();
        UserDetailsImpl c = UserDetailsImpl.builder().id(1L).username("c").password("p").build();

        // Objects.equals(null, null) == true
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
    }
}
