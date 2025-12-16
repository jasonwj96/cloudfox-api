package service

import (
	"context"
	"errors"

	"cloudfox-api/internal/repository"
	"cloudfox-api/internal/service/model"

	"github.com/jackc/pgx/v5"
)

var (
	ErrAccountNotFound = errors.New("account not found")
)

type AccountService struct {
	accountRepo *repository.AccountRepository
}

func NewAccountService(
	accountRepo *repository.AccountRepository,
) *AccountService {
	return &AccountService{
		accountRepo: accountRepo,
	}
}

func (s *AccountService) GetByID(
	ctx context.Context,
	id string,
) (*model.Account, error) {

	account, err := s.accountRepo.GetByID(ctx, id)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, ErrAccountNotFound
		}
		return nil, err
	}

	return account, nil
}
