package com.cloudfox.api.service;

import com.cloudfox.api.enums.HashAlgorithm;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    public record Argon2HashResult(
            String hash,
            String algorithm
    ) {}

    private final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id
    );

    public Argon2HashResult argon2Hash(String input) {
        int iterations = 3;
        int memoryKb = 65536;
        int parallelism = 1;

        char[] password = input.toCharArray();

        try {
            String hash = argon2.hash(
                    iterations,
                    memoryKb,
                    parallelism,
                    password
            );

            return new Argon2HashResult(hash, HashAlgorithm.ARGON2.getValue());
        } finally {
            argon2.wipeArray(password);
        }
    }

    public boolean verifyArgon2Hash(String hash, String input) {
        char[] password = input.toCharArray();
        try {
            return argon2.verify(hash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    public boolean verifyBcryptHash(String passwordHash, String password) {
        return false;
    }
}
