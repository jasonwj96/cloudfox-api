package controllers

import (
	"net/http"

	"cloudfox-api/internal/repository"

	"github.com/gin-gonic/gin"
)

type LoginController struct {
	accountRepo *repository.AccountRepository
}

func NewLoginController(
	accountRepo *repository.AccountRepository,
) *LoginController {
	return &LoginController{
		accountRepo: accountRepo,
	}
}

type LoginRequest struct {
	AccountID string `json:"accountId" binding:"required"`
	// Password string `json:"password" binding:"required"` // next step
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

	// TODO: verify password, MFA, issue JWT

	ctx.JSON(http.StatusOK, gin.H{
		"id":       account.Id,
		"username": account.Username,
		"email":    account.Email,
	})
}
