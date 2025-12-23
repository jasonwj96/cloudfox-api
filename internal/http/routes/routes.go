package routes

import (
	"cloudfox-api/internal/http/controllers"

	"github.com/gin-gonic/gin"
)

func RegisterRoutes(
	router *gin.Engine,
	loginController *controllers.LoginController,
) {
	api := router.Group("/cloudfox-api/v1")

	api.POST("/login", loginController.LoginUser)
}
