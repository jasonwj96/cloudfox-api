package controllers

import (
	"cloudfox-api/internal/service/model"
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

	var request model.LoginRequest

	if err := ctx.ShouldBindJSON(&request); err != nil {

		ctx.JSON(http.StatusBadRequest, gin.H{
			"error": "invalid request body",
		})

		return
	}

	account, err := c.accountRepository.GetById(
		ctx.Request.Context(),
		request.AccountID,
	)

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
		"id":           account.ID,
		"username":     account.Username,
		"fullname":     account.Fullname,
		"email":        account.Email,
		"creationDate": account.CreationDate,
		"phoneNumber":  account.PhoneNumber,
		"active":       account.Active,
	})
}
