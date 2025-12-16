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
	pool *pgxpool.Pool
}

func NewAccountRepository(pool *pgxpool.Pool) *AccountRepository {
	if pool == nil {
		panic("AccountRepository: pool is nil")
	}
	return &AccountRepository{pool: pool}
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
			creation_date,
			phone_number,
			active
		FROM accounts
		WHERE id = $1
	`

	var acc model.Account

	err := r.pool.QueryRow(ctx, query, id).Scan(
		&acc.Id,
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
