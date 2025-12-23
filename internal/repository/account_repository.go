package repository

import (
	"cloudfox-api/internal/repository/connectors"
)

type AccountRepository struct {
	pgxConnector *connectors.PGXConnector
}

func NewAccountRepository(pgxConnector *connectors.PGXConnector) *AccountRepository {
	return &AccountRepository{pgxConnector: pgxConnector}
}
