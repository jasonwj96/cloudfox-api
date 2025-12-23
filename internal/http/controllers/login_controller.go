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
	ctx.JSON(http.StatusOK, gin.H{
		"message": "login ok",
	})
}
