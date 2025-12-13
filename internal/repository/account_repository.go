package repository

import (
	"cloudfox-api/internal/service/model"
	"context"

	"github.com/jackc/pgx/v5/pgxpool"
	_ "github.com/lib/pq"
)

type AccountRepository struct {
	db *pgxpool.Pool
}

func NewAccountRepo(db *pgxpool.Pool) *AccountRepository {
	return &AccountRepository{db: db}
}

func (repository *AccountRepository) GetByID(ctx context.Context, id string) (*model.Account, error) {
	const q = `
        SELECT 
            id,
			username,
			fullname,
			passwordHash,
			passwordHashAlgo,
			email,
			mfaEnabled,
			mfaType,
			creationDate,
			phoneNumber,
			active
        FROM sp_query_accounts($1, NULL)
    `

	var a model.Account

	err := repository.db.QueryRow(ctx, q, id).Scan(
		&a.Id,
		&a.Username,
		&a.Fullname,
		&a.PasswordHash,
		&a.PasswordHashAlgo,
		&a.Email,
		&a.MFAEnabled,
		&a.MFAType,
		&a.CreationDate,
		&a.PhoneNumber,
		&a.Active,
	)

	if err != nil {
		return nil, err
	}

	return &a, nil
}
