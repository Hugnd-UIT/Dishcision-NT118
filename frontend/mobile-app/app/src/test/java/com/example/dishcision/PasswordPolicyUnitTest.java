package com.example.dishcision;

import com.example.dishcision.network.AuthHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PasswordPolicyUnitTest {

    @Test
    public void acceptsStrongPasswords() {
        assertNull(AuthHelper.validateNewPassword("Password@123"));
        assertNull(AuthHelper.validateNewPassword("Abcdef1!"));
        assertNull(AuthHelper.validateNewPassword("Admin@Dishcision2026"));
    }

    @Test
    public void rejectsWeakOrInvalidPasswords() {
        assertNotNull(AuthHelper.validateNewPassword(null));
        assertNotNull(AuthHelper.validateNewPassword(""));
        assertNotNull(AuthHelper.validateNewPassword("Ab1@"));          // < 8 chars
        assertNotNull(AuthHelper.validateNewPassword("abcdefgh1@"));    // no uppercase
        assertNotNull(AuthHelper.validateNewPassword("ABCDEFGH1@"));    // no lowercase
        assertNotNull(AuthHelper.validateNewPassword("Abcdefgh@"));     // no digit
        assertNotNull(AuthHelper.validateNewPassword("Abcdefgh1"));     // no special char
        assertNotNull(AuthHelper.validateNewPassword("Pass word@1"));   // contains space
    }

    @Test
    public void extractsCooldownSecondsFromMessages() {
        assertEquals(47, AuthHelper.extractCooldownSeconds("Vui lòng đợi 47 giây trước khi gửi lại mã", 60));
        assertEquals(60, AuthHelper.extractCooldownSeconds("Vui lòng đợi 60 giây trước khi gửi lại mã", 60));
        assertEquals(60, AuthHelper.extractCooldownSeconds("Mã xác thực 6 chữ số không đúng", 60));
        assertEquals(60, AuthHelper.extractCooldownSeconds("Vui lòng đợi 120 giây", 60));
        assertEquals(60, AuthHelper.extractCooldownSeconds(null, 60));
    }
}

