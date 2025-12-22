package repository

import (
	"context"
	"errors"
	"fmt"

	"cloudfox-api/internal/service/model"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type AccountRepository struct {
	postgresPool *pgxpool.Pool
}

func NewAccountRepository(pool *pgxpool.Pool) (*AccountRepository, error) {
	if pool == nil {
		return nil, errors.New("AccountRepository: postgresPool is nil")
	}

	return &AccountRepository{postgresPool: pool}, nil
}

func (r *AccountRepository) GetByID(
	ctx context.Context,
	id string,
) (*model.Account, error) {
	if id == "" {
		return nil, errors.New("id cannot be empty")
	}

	const query = `
		SELECT
			id,
			username,
			fullname,
			password_hash,
			password_hash_algo,
			email,
			mfa_enabled,
			mfa_type,
			creationdate,
			phonenumber,
			active
		FROM cfx_accounts
		WHERE id = $1
	`

	var acc model.Account

	err := r.postgresPool.QueryRow(ctx, query, id).Scan(
		&acc.ID,
		&acc.Username,
		&acc.Fullname,
		&acc.PasswordHash,
		&acc.PasswordHashAlgo,
		&acc.Email,
		&acc.MFAEnabled,
		&acc.MFAType,
		&acc.CreationDate,
		&acc.PhoneNumber,
		&acc.Active,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, nil
		}
		return nil, fmt.Errorf("get account by id %s: %w", id, err)
	}

	return &acc, nil
}
