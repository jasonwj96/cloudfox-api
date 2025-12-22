package controllers

import (
	"cloudfox-api/internal/repository/connectors"
	"context"
	"errors"
	"net/http"

	"cloudfox-api/internal/repository"

	"github.com/gin-gonic/gin"
)

type LoginController struct {
	accountRepo *repository.AccountRepository
}

func NewLoginController(ctx context.Context) (*LoginController, error) {

	postgresConnector, err := connectors.NewPostgresSQLConnector(ctx)

	if err != nil {
		return nil, errors.New("error while creating the postgresConnector")
	}

	accountRepo, err := repository.NewAccountRepository(postgresConnector.Pool)

	if err != nil {
		return nil, errors.New("error while creating the account repository")
	}

	return &LoginController{accountRepo}, nil
}

type LoginRequest struct {
	AccountID string `json:"accountId" binding:"required"`
}

func (c *LoginController) LoginUser(ctx *gin.Context) {
	var req LoginRequest

	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid request body",
		})
		return
	}

	account, err := c.accountRepo.GetByID(
		ctx.Request.Context(),
		req.AccountID,
	)

	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"error": "internal error",
		})
		return
	}

	if account == nil {
		ctx.JSON(http.StatusUnauthorized, gin.H{
			"error": "invalid credentials",
		})
		return
	}

	ctx.JSON(http.StatusOK, gin.H{
		"id":       account.ID,
		"username": account.Username,
		"email":    account.Email,
	})
}
