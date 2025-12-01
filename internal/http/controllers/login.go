package controllers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func LoginUser(context *gin.Context) {
	context.JSON(http.StatusOK, gin.H{"message": "user logged in"})
}
