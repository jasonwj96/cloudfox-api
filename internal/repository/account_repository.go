package repository

import (
	"context"
	"errors"

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
			return nil, nil // not found
		}
		return nil, err
	}

	return &account, nil
}
