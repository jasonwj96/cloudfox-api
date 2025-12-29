package repository

import (
	"context"
	"errors"

	"cloudfox-api/internal/crypto/argon2id"
	"cloudfox-api/internal/repository/connectors"
	"cloudfox-api/internal/service/model"

	"github.com/jackc/pgx/v5"
)

type AccountRepository struct {
	pgxConnector *connectors.PGXConnector
}

func NewAccountRepository(pgxConnector *connectors.PGXConnector) *AccountRepository {
	return &AccountRepository{pgxConnector: pgxConnector}
}

func (r *AccountRepository) GetById(ctx context.Context, id string) (*model.Account,
	error) {

	row := r.pgxConnector.QueryRow(
		ctx,
		`SELECT * FROM sp_query_accounts($1, $2)`,
		id,
		nil,
	)

	var account model.Account

	err := row.Scan(
		&account.ID,
		&account.Username,
		&account.Fullname,
		&account.Email,
		&account.CreationDate,
		&account.PhoneNumber,
		&account.Active,
	)

	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}

	return &account, nil
}

func (r *AccountRepository) Create(ctx context.Context, account model.Account) error {

	argon2Hasher := argon2id.Argon2Hasher{}
	newPasswordHash, err := argon2Hasher.GenerateHash(account.PasswordHash)

	if err != nil {
		return err
	}

	err = r.pgxConnector.Exec(ctx, ``,
		account.ID,
		account.Username,
		account.Fullname,
		newPasswordHash,
		account.PasswordSalt,
		"argon2")

	if err != nil {
		return err
	}

	return nil
}
