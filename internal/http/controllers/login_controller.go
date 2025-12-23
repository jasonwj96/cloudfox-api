package controllers

import (
	"net/http"

	"cloudfox-api/internal/repository"

	"github.com/gin-gonic/gin"
)

type LoginController struct {
	accountRepository *repository.AccountRepository
}

func NewLoginController(accountRepository *repository.AccountRepository) *LoginController {
	return &LoginController{accountRepository: accountRepository}
}

func (c *LoginController) LoginUser(ctx *gin.Context) {
	accountID := "85f13ac1-9417-4b05-a577-fc5737bf330d"

	account, err := c.accountRepository.GetById(ctx.Request.Context(), accountID)

	if err != nil {
		ctx.JSON(http.StatusInternalServerError, gin.H{
			"error": "internal server error",
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
