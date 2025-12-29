package argon2id

import (
	"crypto/rand"
	"encoding/base64"
	"fmt"

	"golang.org/x/crypto/argon2"
)

type Argon2Hasher struct {
	Time    uint32
	Memory  uint32
	Threads uint8
	KeyLen  uint32
}

func (h *Argon2Hasher) GenerateHash(input string) (string, error) {
	salt := make([]byte, 16)

	if _, err := rand.Read(salt); err != nil {
		return "", err
	}

	hash := argon2.IDKey(
		[]byte(input),
		salt,
		h.Time,
		h.Memory,
		h.Threads,
		h.KeyLen,
	)

	return fmt.Sprintf(
		"$argon2id$v=19$t=%d$m=%d$p=%d$%s$%s",
		h.Time,
		h.Memory,
		h.Threads,
		base64.RawStdEncoding.EncodeToString(salt),
		base64.RawStdEncoding.EncodeToString(hash),
	), nil
}

func (h *Argon2Hasher) ValidateHash(input, encoded string) (bool, error) {
	return true, nil
}
