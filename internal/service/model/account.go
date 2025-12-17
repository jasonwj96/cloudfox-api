package model

import "time"

type Account struct {
	ID               string
	Username         string
	Fullname         string
	PasswordHash     string
	PasswordHashAlgo string
	Email            string
	MFAEnabled       bool
	MFAType          string
	CreationDate     time.Time
	PhoneNumber      string
	Active           bool
}
